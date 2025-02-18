package de.kosit.xmlmutate.cli;

import de.kosit.xmlmutate.mutation.XMuteInstruction;
import de.kosit.xmlmutate.parser.MutatorInstructionParser;
import de.kosit.xmlmutate.parser.PropertyKeys;
import de.kosit.xmlmutate.parser.ResultKeys;
import de.kosit.xmlmutate.runner.DocumentParser;
import de.kosit.xmlmutate.runner.DomFragment;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
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
  private static final String XMUTE_TARGET = "xmute";
  private static final String UNKNOWN_RULE = "Unknown Rule";
  private static final String VALID = "valid";
  private static final String INVALID = "invalid";

  @Option(names = "--snippets", description = "Shows xml snippets", defaultValue = "false")
  boolean snippets;

  @ParentCommand
  XmlMutate xmlMutate;

  private final MutatorInstructionParser parser = new MutatorInstructionParser();
  private String SPACER = " "; //default indent cahracter is simple whitespace

  @Override
  public Integer call() throws Exception {
    final ExecutorService executor = xmlMutate.executor;
    process(executor);
    executor.shutdown();
    return 0;
  }

  void process(ExecutorService executorService) {
    List<Path> processedDocs = xmlMutate.prepareDocuments();

    List<Future<StringBuilder>> futureList = processedDocs.stream()
        .map(path -> executorService.submit(() -> processDocument(path))).toList();

    futureList.forEach(future -> {
      StringBuilder result = awaitTermination(future);
      System.out.println(result);
    });
  }

  private List<XMuteInstruction> parseInstructions(Path path) {
    final Document xmlDocument = DocumentParser.readDocument(path, false);

    Node root = xmlDocument.getFirstChild();
    log.trace("parsing instructions starting at root={}", root);
    log.trace(XmlMutateUtil.printToString(xmlDocument, 2));

    List<XMuteInstruction> instructions = new ArrayList<>();
    final TreeWalker piWalker = ((DocumentTraversal) xmlDocument).createTreeWalker(xmlDocument,
        NodeFilter.SHOW_PROCESSING_INSTRUCTION, null, true);

    XMuteInstruction xin;
    while (piWalker.nextNode() != null) {
      ProcessingInstruction pi = (ProcessingInstruction) piWalker.getCurrentNode();
      if (pi.getTarget().equals(XMUTE_TARGET)) {
        log.debug("TreeWalking found pi={}", pi);
        xin = this.parser.parseInstruction(pi);
        instructions.add(xin);
      }
    }

    log.trace("Returning {} instructions.", instructions.size());
    return instructions;
  }

  StringBuilder processDocument(Path path) {
    StringBuilder report = new StringBuilder();
    report.append("\n-------------------------------------------------\n");
    report.append("File: ").append(path.toString()).append("\n");

    List<XMuteInstruction> xMuteInstructions = parseInstructions(path);
    xMuteInstructions.forEach(i -> {
      log.trace("instruction={}", i);
      log.trace("get mutable frag");

      String mutator = i.getProperty(PropertyKeys.MUTATOR.key());
      String values = i.getProperty(PropertyKeys.VALUES.key());
      String schematronValid = i.getProperty(PropertyKeys.SCHEMATRON_VALID.key());
      String schematronInvalid = i.getProperty(PropertyKeys.SCHEMATRON_INVALID.key());

      String rule = determineRule(schematronValid, schematronInvalid);
      String status = !schematronInvalid.isEmpty() ? INVALID : VALID;

      report.append(buildReport(rule, status, mutator, values));

      DomFragment fragment = i.getTargetFragment();
      if (snippets) {
        report.append(SPACER).append(XmlMutateUtil.printToString(fragment.getContentNode())).append("\n");
      }

    });
    return report;
  }

  private String determineRule(String schematronValid, String schematronInvalid) {
    if (schematronValid != null && !schematronValid.isEmpty()) {
      return schematronValid;
    } else if (schematronInvalid != null && !schematronInvalid.isEmpty()) {
      return schematronInvalid;
    } else {
      return UNKNOWN_RULE;
    }
  }

  private StringBuilder buildReport(String rule, String status, String mutator, String values) {
    StringBuilder report = new StringBuilder()
        .append(SPACER).append(ResultKeys.RULE).append(ResultKeys.COLON).append(rule).append("\n")
        .append(SPACER + SPACER).append(ResultKeys.EXPECTATIONS).append(ResultKeys.COLON).append(status)
        .append("\n")
        .append(SPACER + SPACER).append(ResultKeys.MUTATION).append(ResultKeys.COLON).append(mutator)
        .append("\n");

    if (values != null && !values.isEmpty()) {
      report.append(SPACER + SPACER = SPACER).append(ResultKeys.MUTATION_PARAMETERS).append(ResultKeys.COLON)
          .append("\n")
          .append(SPACER + SPACER + SPACER + SPACER).append(ResultKeys.VALUES).append(ResultKeys.COLON).append(values)
          .append("\n");
    }
    return report;
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
