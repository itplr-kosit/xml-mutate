package de.kosit.xmlmutate.mutator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;

import org.junit.jupiter.api.Test;

/**
 * @author Andreas Penski
 */
public class WithoutResetTest {

    @Test
    public void testSimple() throws IOException {

        // final MutationContext context = createContext(target -> {
        // IntStream.range(0, 1000).forEach(i -> {
        // final Element element = target.getOwnerDocument().createElement("some-element");
        // element.setTextContent("someContent");
        // target.appendChild(element);
        // });
        // target.setAttribute("attr", "value");
        //
        // });
        // memoryHistogram();
        // final List<DocumentFragment> fragmentList = new ArrayList<>();
        // IntStream.range(0, 1000).forEach(i -> {
        // final DocumentFragment fragment = context.getDocument().createDocumentFragment();
        // final Node clone = context.getDocument().getDocumentElement().cloneNode(true);
        // fragment.appendChild(clone);
        // fragmentList.add(fragment);
        // });
        //
        // memoryHistogram();
    }

    public void memoryHistogram() throws IOException {
        final String name = ManagementFactory.getRuntimeMXBean().getName();
        final String PID = name.substring(0, name.indexOf("@"));
        final Process p = Runtime.getRuntime().exec("jcmd " + PID + " GC.class_histogram");
        try ( final BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream())) ) {
            input.lines().forEach(System.out::println);
        }
    }
}
