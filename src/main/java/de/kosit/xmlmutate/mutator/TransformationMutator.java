package de.kosit.xmlmutate.mutator;

import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import lombok.extern.slf4j.Slf4j;

import de.init.kosit.commons.ObjectFactory;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.runner.MutationException;

/**
 * Mutator which executes an XSLT-Script to mutate the origin document
 * 
 * @author Andreas Penski
 */
@Slf4j
public class TransformationMutator extends BaseMutator {

    static final String TEMPLATE_PARAM = TransformationMutator.class.getSimpleName() + ".Template";

    static final String PARAMETER_PARAM = TransformationMutator.class.getSimpleName() + ".Parameters";

    static final String TEMPLATE_NAME_PARAM = "template";

    @Override
    public String getName() {
        return "xslt";
    }

    @Override
    public void mutate(final MutationContext context, final MutationConfig config) {
        try {
            final Document d = createTransformDocument(context);
            final DOMResult result = new DOMResult();

            final Transformer t = createTransformer(config);
            final Map<String, String> parameters = config.getProperty(PARAMETER_PARAM);
            if (parameters != null) {
                parameters.forEach((key, value) -> {
                    log.debug("Setting parameter {}", key);
                    t.setParameter(key, value);
                });
            }

            t.transform(new DOMSource(d), result);
            final Node newNode = context.getDocument().adoptNode(result.getNode().getFirstChild());
            context.getTarget().getParentNode().replaceChild(newNode, context.getTarget());
            context.setSpecificTarget(newNode);
        } catch (final TransformerException e) {
            log.error(e.getMessage());
            throw new MutationException(ErrorCode.TRANSFORM_ERROR, e.getMessage(), e);
        }
    }

    private Document createTransformDocument(final MutationContext context) {
        final Document d = createTempDocument();
        final Node clone = d.adoptNode(context.getTarget().cloneNode(true));
        d.appendChild(clone);
        return d;
    }

    private Document createTempDocument() {
        return ObjectFactory.createDocumentBuilder(true).newDocument();
    }

    private Transformer createTransformer(final MutationConfig config) {
        final Templates templates = config.getProperty(TEMPLATE_PARAM);
        if (templates == null) {
            throw new IllegalStateException("No template for transformation specified");
        }
        try {
            final Transformer transformer = templates.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            return transformer;
        } catch (final TransformerConfigurationException e) {
            log.error("Error creating transformer from template {}", config.getStringProperty(TEMPLATE_NAME_PARAM));
            throw new MutationException(ErrorCode.TRANSFORM_ERROR, e.getMessage(), e);
        }

    }

}