package de.kosit.xmlmutate.runner;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

/**
 * @author Andreas Penski
 */
public class DocumentParserTest {

    @Test
    public void testParse() {
        final Document document = DocumentParser.readDocument(Paths.get("src/test/resources/ubl-invoice-add-mutation-tests.xml"));

    }
}
