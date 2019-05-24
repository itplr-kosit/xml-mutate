package de.kosit.xmlmutate.runner;

/**
 * MutationRunner takes as input a list of files/paths and parses each input for
 * mutation instructions and executes them
 *
 * @author Renzo Kottmann
 */
public class MutationRunner {
    // private final static Logger log = LogManager.getLogger(MutationRunner.class);
    //
    // private List<Path> inputPathList = new ArrayList<Path>();
    // private Path outputDir = null;
    // private DocumentBuilder docBuilder;
    // private Map<String, Templates> xsltCache = null;
    // private XMLMutateConfiguration config = null;
    // private MutateReport report = null;
    //
    // private MutationRunner() {
    // };
    //
    // public MutationRunner(final List<Path> inputPathList, final XMLMutateConfiguration config, final Map<String,
    // Templates> xsltCache) {
    // this.inputPathList = inputPathList;
    // this.setOutputDir(config.getOutputDir());
    // this.xsltCache = xsltCache;
    // this.config = config;
    // this.report = new MutateReportText();
    // this.report.addConfig(config);
    //
    // }
    //
    // private void setOutputDir(final Path outputDir) {
    // if (outputDir == null) {
    // log.error("outputdir is null");
    // throw new IllegalArgumentException("Need a valid output dir instead of a null value");
    //
    // }
    // if (!Files.isDirectory(outputDir)) {
    // throw new IllegalArgumentException("Output path must be a valid directory");
    // }
    // if (!Files.isWritable(outputDir)) {
    // throw new IllegalArgumentException("Output directory must be writable by user");
    // }
    // this.outputDir = outputDir;
    // }
    //
    // private void write(final Document doc, final NamingStrategy name) {
    // log.debug("Writing to dir=" + this.outputDir);
    // log.debug("Writing to file name=" + name.getFileName());
    // final Path out = Paths.get(this.outputDir.toString(), name.getFileName());
    //
    // try {
    // XMLMutateApp.printDocument(doc, new FileOutputStream(out.toFile()));
    // } catch (final IOException | TransformerException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
    //
    // public int execute(final boolean testMutation) throws XMLMutateException {
    // log.debug("Executing Mutation runner");
    //
    // Document docOrigin = null;
    // int numDoc = 0;
    // for (final Path file : this.inputPathList) {
    // Files.isReadable(file);
    // docOrigin = XMLMutateManufactory.domDocumentFromPath(file);
    // docOrigin.normalize();
    // docOrigin.normalizeDocument();
    //
    // String name = file.getFileName().toString();
    // name = name.replaceFirst("\\.xml", "");
    // log.debug("Doc name=" + name);
    // log.debug("Doc URI=" + docOrigin.getDocumentURI());
    // this.mutate(docOrigin, name, testMutation);
    // numDoc++;
    // }
    // this.report.setNumDoc(numDoc);
    // try {
    // this.report.write(this.outputDir.toString(), "MutaTe-Report.txt");
    // } catch (final IOException e) {
    // throw new XMLMutateException("Could not write report to" + this.outputDir,
    // XMLMutateException.Status.FILE_ERROR);
    // }
    //
    // return 0;
    // }
    //
    // private void mutate(final Document origin, final String documentName, final boolean testMutation) {
    // final TreeWalker piWalker = ((DocumentTraversal) origin).createTreeWalker(origin,
    // NodeFilter.SHOW_PROCESSING_INSTRUCTION, null,
    // true);
    // final TreeWalker elemWalker = ((DocumentTraversal) origin).createTreeWalker(origin, NodeFilter.SHOW_ELEMENT, null,
    // true);
    // // finde pi
    // // finde next elem
    // // deep copy of oroginal elem as docfrag or any other way that decopuls it from
    // // doc
    // // gib elem an mutator (with return elem and/or anyway side effect through
    // // object referece)
    // // write out doc
    // // replace mutated elem mit original
    // Element mutationTargetElem = null;
    // DocumentFragment origFragment = null;
    // Mutator mutator = null;
    // ProcessingInstruction pi = null;
    // int docNum = 0;
    // // String schematron =
    // // XMLMutateManufactory.fileFromClasspath("XRechnung-UBL-validation-Invoice.xsl");
    // // SchematronTester st = new SchematronTester("xr-ubl-in", schematron);
    //
    // while (piWalker.nextNode() != null) {
    // pi = (ProcessingInstruction) piWalker.getCurrentNode();
    // log.debug("PI=" + pi);
    //
    // elemWalker.setCurrentNode(piWalker.getCurrentNode());
    //
    // mutationTargetElem = (Element) elemWalker.nextNode();
    // // because walker also moves to parent, call after walker.nextNode() above
    // final Element parent = (Element) elemWalker.parentNode();
    //
    // // now we know we have a valid pi and an elem
    // mutator = MutatorParser.parse(pi, this.xsltCache);
    // // copy elem to docfrag
    // origFragment = origin.createDocumentFragment();
    // origFragment.appendChild(mutationTargetElem.cloneNode(true));
    // // mutating
    // final Node changed = mutator.execute(mutationTargetElem);
    // final NamingStrategy namingStrategy = new NamingStrategyImpl().byId(documentName, String.valueOf(++docNum));
    // this.write(origin, namingStrategy);
    // boolean schemaValid = false;
    //
    // if (testMutation) {
    // log.debug("Check mutated against schema");
    // schemaValid = this.testSchema(origin);
    // final Map<String, SchematronTester> schematronTester = this.config.getAllSchematronTester();
    //
    // for (final SchematronTester st : schematronTester.values()) {
    // final List<TestItem> testReport = st.test(origin, mutator.getConfig().getSchematronExpectations());
    // this.report.addDocumentReport(namingStrategy.getName(), testReport);
    //
    // }
    //
    // }
    // String schemaTestStatement = "";
    // if (schemaValid == mutator.getConfig().expectSchemaValid()) {
    // schemaTestStatement = String.format("Mutation has expected outcome :) result=%s and expected=%s",
    // schemaValid, mutator.getConfig().expectSchemaValid());
    //
    // } else {
    // schemaTestStatement = String.format(
    // "Mutation resulted in UN-expected outcome :( , result=%s and expected=%s", schemaValid,
    // mutator.getConfig().expectSchemaValid());
    //
    // }
    // this.report.addSchemaTestSatement(namingStrategy.getName(), schemaTestStatement);
    // log.debug(schemaTestStatement);
    //
    // log.debug("Replacing mutated=" + mutationTargetElem + " with original=" + origFragment.getFirstChild()
    // + " again. Parent of original=" + parent);
    //
    // parent.replaceChild(origFragment, changed);
    //
    // } // end mutations
    // this.report.addToMutationCount(docNum);
    // }
    //
    // private static boolean testSchematronSaxon(final Document doc) {
    // final DOMSource xmlSource = new DOMSource(doc);
    // final Processor processor = new Processor(false);
    // final net.sf.saxon.s9api.DocumentBuilder documentBuilder = processor.newDocumentBuilder();
    // try {
    // final XdmNode node = documentBuilder.build(xmlSource);
    // final XsltCompiler xsltCompiler = processor.newXsltCompiler();
    // final XsltExecutable schematronValidator = xsltCompiler.compile(new StreamSource(new File(
    // "D:/git-repos/validator-configuration-xrechnung/build/resources/xrechnung/1.1/xsl/XRechnung-UBL-validation-Invoice.xsl")));
    // final XsltTransformer transformer = schematronValidator.load();
    // transformer.setInitialContextNode(node);
    // final XdmDestination output = new XdmDestination();
    // transformer.setDestination(output);
    // // processor.
    //
    // transformer.transform();
    // } catch (final SaxonApiException e) {
    // throw new MutatorException("saxon issue");
    // }
    // return false;
    // }
    //
    // private static boolean testSchematron(final Document doc) {
    // log.debug("Validate doc=" + doc.getNodeName());
    // Templates schematron = null;
    // Transformer xsltTransformer = null;
    // final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    // // transformerFactory.
    // final StreamSource xsltSource = new StreamSource(new File(
    // "D:/git-repos/validator-configuration-xrechnung/build/resources/xrechnung/1.1/xsl/XRechnung-UBL-validation-Invoice.xsl"));
    //
    // try {
    // schematron = transformerFactory.newTemplates(xsltSource);
    // } catch (final TransformerConfigurationException e) {
    // // TODO Auto-generated catch block
    // log.error("Error loadding xslt", e);
    // }
    // try {
    // xsltTransformer = schematron.newTransformer();
    // } catch (final TransformerConfigurationException e) {
    // // TODO Auto-generated catch block
    // log.error("Could not configure schematron cheker =" + schematron, e);
    // }
    //
    // final DOMResult result = new DOMResult();
    // final DOMSource xmlSource = new DOMSource(doc);
    // try {
    // xsltTransformer.transform(xmlSource, result);
    // } catch (final TransformerException e) {
    // // TODO Auto-generated catch block
    // log.error("Could not schematron check" + doc, e);
    // }
    // final Node resultNode = result.getNode();
    // final Node resultChild = resultNode.getFirstChild();
    // log.debug("Got result node=" + resultNode.getNodeName() + " of type=" + resultNode.getNodeType());
    // log.debug("Got resultChild node=" + resultChild.getNodeName() + " of type=" + resultChild.getNodeType());
    // if (resultChild.getNodeType() == Node.ELEMENT_NODE) {
    // log.debug("Is element node!");
    // }
    // try {
    // log.debug("Print schmematron result");
    // final PrintStream out = new PrintStream("svrl.xml");
    //
    // XMLMutateApp.printDocument((Document) resultNode, out);
    // } catch (final IOException | TransformerException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // return false;
    // }
    //
    // private boolean testSchema(final Document doc) {
    // log.debug("Validate doc=" + doc.getNodeName());
    // final Validator val = this.config.getSchema("ubl").newValidator();
    // final List<String> valErrors = new ArrayList<String>();
    // val.setErrorHandler(new ErrorHandler() {
    // @Override
    // public void error(final SAXParseException err) throws SAXException {
    // valErrors.add(err.getMessage());
    // }
    //
    // @Override
    // public void fatalError(final SAXParseException err) throws SAXException {
    // valErrors.add(err.getMessage());
    // }
    //
    // @Override
    // public void warning(final SAXParseException err) throws SAXException {
    // valErrors.add(err.getMessage());
    // }
    // });
    // try {
    // val.validate(new DOMSource(doc));
    // } catch (final IOException e) {
    // throw new RuntimeException(e);
    // } catch (final SAXException e) {
    // throw new RuntimeException(e);
    // }
    // if (valErrors.isEmpty()) {
    // log.debug("No validation erros");
    // return true;
    // }
    // for (final String var : valErrors) {
    // log.debug("valErrors=" + var);
    // }
    // return false;
    // }
    //
    // /**
    // * Copy an XML document, adding it as a child of the target document root
    // *
    // * @param source Document to copy
    // * @param target Document to contain copy
    // */
    // private Document copyDocument(final Document source) {
    // final Document target = this.docBuilder.newDocument();
    // final Node node = target.importNode(source.getDocumentElement(), true);
    //
    // target.getDocumentElement().appendChild(node);
    // return target;
    // }
}