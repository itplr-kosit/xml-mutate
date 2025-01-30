package de.kosit.xmlmutate.cli;

import de.kosit.xmlmutate.runner.DocumentParser;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(description = "XMl-Scan: Scan for schematron rules.", name = "scan", mixinStandardHelpOptions = true, separator = " ")
public class XmlScan implements Callable<Integer> {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XmlScan.class);

  @Option(names = "--snippets", description = "Shows xml snippets", defaultValue = "false")
  private boolean snippets;

  @ParentCommand
  private XmlMutate xmlMutate;

  @Override
  public Integer call() throws Exception {
    final ExecutorService executor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors());
    process(executor);
    executor.shutdown();
    return 0;
  }

  private void process(ExecutorService executorService) {
    List<Path> processedDocs = xmlMutate.prepareDocuments();

    List<Future<StringBuilder>> futureList = processedDocs.stream()
        .map(path -> executorService.submit(() -> processDocument(path))).toList();

    futureList.forEach(future -> {
      StringBuilder result = awaitTermination(future);
      System.out.println(result);
    });
  }

  private StringBuilder processDocument(Path path) {
    final Document xmlDocument = DocumentParser.readDocument(path, false);

    TreeWalker piWalker = ((DocumentTraversal) xmlDocument).createTreeWalker(
        xmlDocument, NodeFilter.SHOW_PROCESSING_INSTRUCTION, null, true);

    StringBuilder report = new StringBuilder();
    report.append("\n-------------------------------------------------\n");
    report.append("File: ").append(path.toString()).append("\n");
    while (piWalker.nextNode() != null) {
      ProcessingInstruction pi = (ProcessingInstruction) piWalker.getCurrentNode();

      if (pi.getTarget().equals("xmute")) {
        Map<String, String> attributes = extractAttributes(pi);

        String mutator = attributes.get("mutator");
        String values = attributes.get("values");
        String schematronValid = attributes.get("schematron-valid");
        String schematronInvalid = attributes.get("schematron-invalid");

        String rule = determineRule(schematronValid, schematronInvalid);
        String status = (schematronInvalid != null) ? "invalid" : "valid";
        report.append(buildReport(rule, status, mutator, values, pi));
      }
    }
    return report;
  }

  private static Map<String, String> extractAttributes(ProcessingInstruction pi) {
    Map<String, String> attributes = new HashMap<>();
    String data = pi.getData();

    String regex = "([a-zA-Z0-9\\\\-]+)=\"([^\"]*)\"";

    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(data);

    while (matcher.find()) {
      String key = matcher.group(1);
      String value = matcher.group(2);
      attributes.put(key, value);
    }

    return attributes;
  }

  private String determineRule(String schematronValid, String schematronInvalid) {
    if (schematronValid != null) {
      return schematronValid;
    } else if (schematronInvalid != null) {
      return schematronInvalid;
    } else {
      return "Unknown Rule";
    }
  }

  private StringBuilder buildReport(String rule, String status, String mutator,
      String values, ProcessingInstruction pi) {
    StringBuilder report = new StringBuilder();
    report.append("\t").append("Rule: ").append(rule).append("\n");
    report.append("\t\t").append("Expectations: ").append(status).append("\n");
    report.append("\t\t").append("Mutation: ").append(mutator).append("\n");

    if (values != null) {
      report.append("\t\t\t").append("Mutation Parameters:\n");
      report.append("\t\t\t\t").append(" Values: ").append(values).append("\n");
    }

    if (snippets) {
      appendXmlSnippet(report, pi);
    }

    return report;
  }

  private void appendXmlSnippet(StringBuilder report, ProcessingInstruction pi) {
    Node sibling = pi.getNextSibling();
    while (sibling != null && sibling.getNodeType() != Node.ELEMENT_NODE) {
      sibling = sibling.getNextSibling();

      if (sibling.getNodeType() == Node.ELEMENT_NODE) {
        appendSibling(report, sibling);
        break;
      }
    }
  }
  private void appendSibling(StringBuilder report, Node sibling) {
    StringWriter sw = new StringWriter();
    try {
      Transformer t = TransformerFactory.newInstance().newTransformer();
      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      t.setOutputProperty(OutputKeys.INDENT, "yes");
      t.transform(new DOMSource(sibling), new StreamResult(sw));
    } catch (TransformerException te) {
      log.error(te.getMessage(), te);
    }
    report.append("\t").append(sw).append("\n");
  }

  private StringBuilder awaitTermination(final Future<StringBuilder> pairFuture) {
    try {
      return pairFuture.get();
    } catch (final InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e.getCause().getMessage(), e);
    }
  }

}
