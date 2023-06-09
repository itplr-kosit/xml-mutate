package de.init.kosit.commons.validate;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

import de.kosit.xmlmutate.mutation.Schematron;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchematronValidationService {

  private static final Logger log = LoggerFactory.getLogger(SchematronValidationService.class);
  private static final String FAILURE_NODE_NAME = "failed-assert";
  private static final String SCHEMATRON_RULE_ID_ATTR = "id";
  private static final String RULE_EVALUATION_XPATH_ATTR = "location";
  private static final String UNIDENTIFIED_SCHEMATRON_RULE = "UNIDENTIFIED";
  private static final String SHADOWED_BY_PRECEDING_RULE = "shadowed by preceding rule";

  public XdmDestination validate(final URI xmlDocumentUri, final Schematron schematron) {
    try {
      XsltTransformer transformer = createXsltTransformer(schematron);
      applyDocumentToBeTested(xmlDocumentUri, transformer);
      XdmDestination validationResultDestination = applySvrlResultDestination(transformer);
      runSchematronValidation(transformer);
      logIfAnyShadowedRulesAreSuspected(xmlDocumentUri, validationResultDestination);
      return validationResultDestination;
    } catch (SaxonApiException e) {
      log.error("Could not validate XML {} without any mutators using schematron {}",
          xmlDocumentUri, schematron.getUri(), e);
      return new XdmDestination();
    }
  }

  private void logIfAnyShadowedRulesAreSuspected(URI xmlDocumentUri, XdmDestination validationResultDestination) {
    if (validationResultDestination.getXdmNode() != null &&
        validationResultDestination.getXdmNode().toString().contains(SHADOWED_BY_PRECEDING_RULE)) {
      log.error("Schematron {} contains shadowed rules! Mutation evaluations may be incorrect.", xmlDocumentUri.getPath());
    }
  }

  /**
   * Finds schematron failures.
   *
   * @param svrlReport the instance of XdmDestination from Saxon after XML validation.
   * @return map of failures where key - schematron rule id, value - list of xpath values indicating
   * where the failing schematron assertion occurred.
   */
  public Map<String, Set<String>> findFailuresWithXPaths(XdmDestination svrlReport) {
    try {
      return findFailures(svrlReport.getXdmNode());
    } catch (IllegalStateException e) {
      log.error("Could not find any failures from SVRL report having invalid state.");
      return new HashMap<>();
    }
  }

  private Map<String, Set<String>> findFailures(XdmNode rootNode) {
    return StreamSupport.stream(findXdmChildNodes(rootNode).spliterator(), false)
        .filter(xdmNode -> xdmNode.getNodeName() != null)
        .filter(xdmNode -> StringUtils.equals(FAILURE_NODE_NAME, xdmNode.getNodeName().getLocalName()))
        .collect(groupingBy(this::findSchematronRuleCode, mapping(
            xdmNode -> xdmNode.attribute(RULE_EVALUATION_XPATH_ATTR), toSet()
            ))
        );
  }

  private String findSchematronRuleCode(XdmNode xdmNode) {
    return xdmNode.attribute(SCHEMATRON_RULE_ID_ATTR) == null
        ? UNIDENTIFIED_SCHEMATRON_RULE
        : xdmNode.attribute(SCHEMATRON_RULE_ID_ATTR);
  }

  private Iterable<XdmNode> findXdmChildNodes(XdmNode rootNode) {
    return rootNode.children().iterator().next().children();
  }

  private void runSchematronValidation(XsltTransformer transformer) throws SaxonApiException {
    transformer.transform();
  }

  private XdmDestination applySvrlResultDestination(XsltTransformer transformer) {
    XdmDestination chainResult = new XdmDestination();
    transformer.setDestination(chainResult);
    return chainResult;
  }

  private void applyDocumentToBeTested(final URI xmlDocumentUri, XsltTransformer transformer) {
    transformer.setSource(new StreamSource(new File(xmlDocumentUri)));
  }

  private XsltTransformer createXsltTransformer(Schematron schematron) throws SaxonApiException {
    Processor processor = new Processor(false);
    XsltCompiler compiler = processor.newXsltCompiler();
    XsltExecutable xslt = compiler.compile(new StreamSource(new File(schematron.getUri())));
    return xslt.load();
  }

}
