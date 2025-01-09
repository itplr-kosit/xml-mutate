package de.kosit.xmlmutate.cli;

import de.kosit.xmlmutate.cli.XmlMutate.LogLevelConverter;
import de.kosit.xmlmutate.runner.LogLevel;
import java.util.Arrays;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

public class XmlMain {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XmlMain.class);

  public static void main(final String[] args) {

    AnsiConsole.systemInstall();
    int i = -1;
    try {
      boolean scanOnly;
      if (Arrays.stream(args).anyMatch(arg -> arg.equals("--scan"))) {
        scanOnly = true;
      } else {
        scanOnly = false;
      }
      i = executeCommand(scanOnly, args);
    } catch (final Exception e) {
      System.err.print(e.getMessage());
      System.err.print(";");
      System.err.println("Exit with code=" + i);
    }
    // add a new line at the end of processing
    System.out.println("\n");
    System.out.println("with exit code=" + i + "\n");
    System.exit(i);
  }

  private static int executeCommand(boolean scanOnly, String[] args) {
    CommandLine commandLine;
    if (scanOnly) {
      commandLine = new CommandLine(new XmlScan());
    } else {
      commandLine = new CommandLine(new XmlMutate());
    }
    commandLine.setExecutionExceptionHandler(XmlMain::logExecutionException);
    commandLine.registerConverter(LogLevel.class, new LogLevelConverter());

    return commandLine.execute(args);
  }

  private static int logExecutionException(final Exception ex, final CommandLine cli, final ParseResult parseResult) {
    System.err.println(ex.getMessage());
    log.error(ex.getMessage(), ex);
    return 1;
  }
}
