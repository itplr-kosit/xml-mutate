package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.runner.MutationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static de.kosit.xmlmutate.assertions.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testet den RandomizeMutator
 *
 * @author Victor del Campo
 */
public class RandomizeMutatorTest {

    private final RandomizeMutator mutator = new RandomizeMutator();

    @Test
    @DisplayName("Test if the randomize mutator throws and exception if there is no target node")
    public void testUnexisting() {
        final MutationContext context = createContext();
        context.getParentElement().removeChild(context.getTarget());
        assertThrows(MutationException.class, () -> this.mutator.mutate(context, createConfig()));
    }

    @Test
    @DisplayName("Test the rearrangement of the node for the target node given a new node list order")
    public void testNewOrder() {
        final MutationContext context = createContext(target -> {
            final Document doc = target.getOwnerDocument();
            final Element sub1 = doc.createElement("sub1");
            final Element sub2 = doc.createElement("sub2");
            final Element sub3 = doc.createElement("sub3");
            final Element subsub1 = doc.createElement("subsub2");
            sub1.appendChild(subsub1);
            target.appendChild(sub1);
            target.appendChild(sub2);
            target.appendChild(sub3);
        });

        final Node targetNode = context.getTarget();
        final List<Node> nodeListe = rearrangeRandomChilds(targetNode);
        final MutationConfig config = new MutationConfig().add(RandomizeMutator.INTERNAL_PROP_VALUE, nodeListe);
        this.mutator.mutate(context, config);

        assertThat(context.getTarget()).hasChildren();
        assertThat(context.getTarget().getChildNodes()).hasSize(3);
        nodeListe.forEach(n -> assertThat(context.getTarget()).hasChildInPosition(n.getNodeName(), nodeListe.indexOf(n)));
    }

    private List<Node> rearrangeRandomChilds(final Node targetNode) {
        final NodeList nodeList = targetNode.getChildNodes();
        final List<Node> liste = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            liste.add(nodeList.item(i));
        }
        Collections.shuffle(liste);
        return liste;
    }


}
