package de.kosit.xmlmutate.cli;

import de.kosit.xmlmutate.runner.DocumentParser;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(description = "XMl-Scan: Scan for schematron rules.", name = "XML Scan", mixinStandardHelpOptions = true, separator = " ")
public class XmlScan implements Callable<Integer> {

  @Option(names = "--scan", description = "Enables scan mode", defaultValue = "false")
  private boolean scan;

  @Option(names = "--snippets", description = "Shows xml snippets", defaultValue = "false")
  private boolean snippets;

  @Parameters(arity = "1..*", description = "Documents to scan for schematron rules")
  private List<Path> documents;

  @Override
  public Integer call() throws Exception {
    final ExecutorService executor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors());
    process(executor);
    executor.shutdown();
    return 0;
  }

  private void process(ExecutorService executorService) {
    XmlCommonProcessing xmlCommonProcessing = new XmlCommonProcessing();
    List<Path> processedDocs = xmlCommonProcessing.prepareDocuments(this.documents);

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
        report.append(buildReport(path, rule, status, mutator, values, pi));
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

  private StringBuilder buildReport(Path path, String rule, String status, String mutator,
      String values, ProcessingInstruction pi) {
    StringBuilder report = new StringBuilder();
    report.append("\n");
    report.append("File: ").append(path.toString()).append("\n");
    report.append("Rule: ").append(rule).append("\n");
    report.append("Expectations: ").append(status).append("\n");
    report.append("Mutation: ").append(mutator).append("\n");

    if (values != null) {
      report.append("   Mutation Parameters:\n");
      report.append("     Values: ").append(values).append("\n");
    }

    if (snippets) {
      appendXmlSnippet(report, pi);
    }

    return report;
  }

  private void appendXmlSnippet(StringBuilder report, ProcessingInstruction pi) {
    Node nextNode = pi.getNextSibling();
    if (nextNode != null && nextNode.getNextSibling() != null) {
      Node sibling = nextNode.getNextSibling();

      if (sibling.getNodeType() == Node.ELEMENT_NODE) {
        String tagName = sibling.getNodeName();
        String textValue = sibling.getTextContent();

        if (textValue != null && !textValue.trim().isEmpty()) {
          String tag = "<" + tagName + ">" + textValue + "</" + tagName + ">";
          report.append(tag).append("\n");
        }
      }
    }
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
