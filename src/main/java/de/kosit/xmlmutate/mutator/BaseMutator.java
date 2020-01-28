package de.kosit.xmlmutate.mutator;

import java.io.StringWriter;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.init.kosit.commons.ObjectFactory;

/**
 * Basisklasse für Mutatoren.
 * 
 * @author Andreas Penski
 */
public abstract class BaseMutator implements Mutator {

    protected static Comment wrap(final Comment comment, final NodeList nodesToWrap) {
        streamElements(nodesToWrap).forEach(n -> comment.appendData(nodeToString(n)));
        return comment;
    }

    @Override
    public String getPreferredName() {
        return getNames().stream().findFirst().orElseThrow(IllegalStateException::new);
    }

    protected static Comment wrap(final Comment comment, final List<Node> nodesToWrap) {
        nodesToWrap.forEach(n -> comment.appendData(nodeToString(n)));
        return comment;
    }

    protected static Comment wrap(final Comment comment, final Node node) {
        comment.appendData(nodeToString(node));
        return comment;
    }

    protected static Stream<Node> streamElements(final NodeList list) {
        return stream(list, new short[] { Node.ELEMENT_NODE });
    }

    protected static Stream<Node> stream(final NodeList list, final short... types) {

        return IntStream.range(0, list.getLength()).mapToObj(list::item)
                .filter(n -> types.length == 0 || ArrayUtils.contains(types, n.getNodeType()));
    }

    protected static String nodeToString(final Node node) {
        final StringWriter sw = new StringWriter();
        try {
            final Transformer t = ObjectFactory.createTransformer(true);
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (final TransformerException te) {
            System.out.println("nodeToString Transformer Exception");
        }
        return sw.toString().replace("<!--", "##").replace("-->", "##");
    }

}
