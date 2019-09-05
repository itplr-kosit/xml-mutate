package de.kosit.xmlmutate.mutator;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationGenerator;
import de.kosit.xmlmutate.runner.ErrorCode;
import de.kosit.xmlmutate.runner.MutationException;
import de.kosit.xmlmutate.runner.Services;

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

        public static List<Code> resolveCodes(final URI uri, final String key) {
            if (uri != null) {
                final URI source = uri.getScheme() == null ? Paths.get(uri.toString()).toUri() : uri;

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

    private static final String SEPERATOR = ",";

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
        final URI uri = URI.create(config.getStringProperty(PROP_GENERICODE));

        final String codeKey = config.getStringProperty(PROP_CODE_KEY);
        return CodeFactory.resolveCodes(uri, defaultIfBlank(codeKey, "code")).stream()
                .map(c -> createMutation(config, context, c.getCode())).collect(Collectors.toList());

    }

    private Collection<Mutation> generateSimpleCodes(final MutationConfig config, final MutationContext context) {
        return config.resolveList(PROP_VALUES).stream().flatMap(e -> Arrays.stream(e.toString().split(SEPERATOR))
                .filter(StringUtils::isNotEmpty).map(s -> createMutation(config, context, s))).collect(Collectors.toList());
    }

    private Mutation createMutation(final MutationConfig config, final MutationContext context, final String s) {
        final Mutator mutator = MutatorRegistry.getInstance().getMutator(getPreferredName());
        final MutationConfig cloned = config.cloneConfig();
        cloned.add(CodeMutator.INTERNAL_PROP_VALUE, s);
        return new Mutation(context.cloneContext(),
                Services.getNameGenerator().generateName(context.getDocumentName(), s.trim()), cloned, mutator);
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
