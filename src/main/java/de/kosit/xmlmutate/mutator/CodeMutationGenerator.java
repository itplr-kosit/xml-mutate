package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationGenerator;
import de.kosit.xmlmutate.runner.MutationException;
import de.kosit.xmlmutate.runner.Services;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Generator for Mutation having a defined vlaue from a list of values.
 *
 * @author Andreas Penski
 */
@Slf4j
public class CodeMutationGenerator implements MutationGenerator {

    @RequiredArgsConstructor
    static class Code {

        @Getter
        private final String code;
    }

    static class CodeFactory {

        private CodeFactory() {
            // hide
        }

        public static List<Code> resolveCodes(final URI uri) {
            return resolveCodes(uri, "code");
        }

        public static List<Code> resolveCodes(final URI source, final String key) {
            if (source != null) {

                final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                try {
                    final DocumentBuilder builder = builderFactory.newDocumentBuilder();
                    final Document xmlDocument = builder.parse(source.toString());
                    final XPath xPath = XPathFactory.newInstance().newXPath();
                    final String expression = String.format("/CodeList/SimpleCodeList/Row/Value[@ColumnRef='%s']/SimpleValue", key);
                    final NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
                    final List<Code> result = IntStream.range(0, nodeList.getLength()).mapToObj(nodeList::item).map(Node::getTextContent)
                            .map(Code::new).collect(Collectors.toList());
                    if (result.isEmpty()) {
                        throw new MutationException(ErrorCode.CONFIGURATION_ERRROR, String.format("No codes found for %s", source));
                    }
                    return result;
                } catch (final ParserConfigurationException | IOException | XPathExpressionException | SAXException e) {
                    throw new MutationException(ErrorCode.CONFIGURATION_ERRROR,
                            String.format("Error resolving codes from %s: %s", source, e.getMessage()), e);
                }
            }
            throw new MutationException(ErrorCode.CONFIGURATION_ERRROR, "No genericode input given");
        }
    }

    private static final String PROP_VALUES = "values";

    private static final String PROP_CODE_KEY = "codeKey";

    private static final String PROP_GENERICODE = "genericode";

    private static final String DEFAULT_SEPARATOR = ",";

    private static final String SEPARATOR = "separator";

    @Override
    public List<Mutation> generateMutations(final MutationConfig config, final MutationContext context) {
        final List<Mutation> list = new ArrayList<>();
        if (config.getProperties().get(PROP_VALUES) != null) {
            list.addAll(generateSimpleCodes(config, context));
        }
        if (config.getProperties().get(PROP_GENERICODE) != null) {
            list.addAll(generateGenericodeCodes(config, context));
        }
        if (list.isEmpty()) {
            throw new MutationException(ErrorCode.CONFIGURATION_ERRROR, "No code source found");
        }
        return list;
    }

    private Collection<Mutation> generateGenericodeCodes(final MutationConfig config, final MutationContext context) {
        final URI uri = resolveCodelistURI(context, config.getStringProperty(PROP_GENERICODE));

        final String codeKey = config.getStringProperty(PROP_CODE_KEY);
        return CodeFactory.resolveCodes(uri, defaultIfBlank(codeKey, "code")).stream()
                .map(c -> createMutation(config, context, c.getCode())).collect(Collectors.toList());

    }

    private URI resolveCodelistURI(final MutationContext context, final String stringProperty) {
        URI result = URI.create(stringProperty);
        if (result.getScheme() == null) {
            final Path relative2Document = context.getDocumentPath().resolve("../" + stringProperty).toAbsolutePath().normalize();
            final Path relative2Cwd = Paths.get(stringProperty).toAbsolutePath();
            if (Files.exists(relative2Document)) {
                result = relative2Document.toUri();
            } else if (Files.exists(relative2Cwd)) {
                result = relative2Cwd.toUri();
            } else {
                throw new MutationException(ErrorCode.CONFIGURATION_ERRROR, String.format("Codeliste %s not found", stringProperty));
            }
        }
        return result;
    }

    private Collection<Mutation> generateSimpleCodes(final MutationConfig config, final MutationContext context) {
        final String separator = config.getProperties().get(SEPARATOR) != null ? config.getProperties().get(SEPARATOR).toString() : DEFAULT_SEPARATOR;
        return config.resolveList(PROP_VALUES).stream().flatMap(e -> Arrays.stream(e.toString().split(separator))
                .filter(StringUtils::isNotEmpty).map(s -> createMutation(config, context, s))).collect(Collectors.toList());
    }

    private Mutation createMutation(final MutationConfig config, final MutationContext context, final String code) {
        final Mutator mutator = MutatorRegistry.getInstance().getMutator(getPreferredName());
        final MutationConfig cloned = config.cloneConfig();
        cloned.add(CodeMutator.INTERNAL_PROP_VALUE, trim(code));
        return new Mutation(context.cloneContext(), Services.getNameGenerator().generateName(context.getDocumentName(), trim(code)), cloned,
                mutator);
    }

    @Override
    public List<String> getNames() {
        return Collections.singletonList(CodeMutator.NAME);
    }

    @Override
    public String getPreferredName() {
        return getNames().stream().findFirst().orElseThrow(IllegalStateException::new);
    }
}
