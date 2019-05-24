package de.kosit.xmlmutate;

/**
 *
 * XMLMutateCnfigurator
 */
public class XMLMutateConfigurator {
    // private final static Logger log = LogManager.getLogger(XMLMutateConfigurator.class);
    // private static XMLMutateConfigurationImpl config = new XMLMutateConfigurationImpl();
    // private static List<Path> inputPathList = new ArrayList<Path>();
    //
    // private XMLMutateConfigurator() {
    // }
    //
    // public static XMLMutateConfiguration byDefault() {
    // return config;
    // }
    //
    // /**
    // * assumes files as arguments at the end only
    // */
    // public static XMLMutateConfiguration fromCommandLine(String[] line) {
    // if (line == null || line.length < 1) {
    // log.debug("No command line arguments given. Return default config");
    // return config;
    // }
    // parseCommandLine(line);
    // return config;
    // }
    //
    // public static List<Path> getInputPaths() {
    // return inputPathList;
    // }
    //
    // private static void printHelp() {
    // System.out.println("This is not helpful yet!");
    //
    // }
    //
    // private static void parseCommandLine(String[] line) {
    // String arg = "";
    // for (int i = 0; i < line.length; i++) {
    // arg = line[i].toLowerCase();
    // switch (arg) {
    // case "--output-dir":
    // config.setOutputDir(line[++i]);
    // break;
    // case "-m":
    // config.setRunMode(line[++i]);
    // break;
    // case "--run-mode":
    // config.setRunMode(line[++i]);
    // break;
    // case "--schema":
    // String schemaName = line[++i];
    // String schemaFile = line[++i];
    //
    // config.addSchema(schemaName, schemaFile);
    // break;
    // case "--schematron":
    // String schematronName = line[++i];
    // String schematronFile = line[++i];
    //
    // config.addSchematron(schematronName, schematronFile);
    // break;
    // case "--help":
    // printHelp();
    // System.exit(0);
    // break;
    // default:
    // inputPathList.add(Paths.get(line[i]));
    // break;
    // }
    //
    // }
    // }
}