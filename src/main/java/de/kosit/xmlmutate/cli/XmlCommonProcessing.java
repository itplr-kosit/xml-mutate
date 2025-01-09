package de.kosit.xmlmutate.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Stream;

public class XmlCommonProcessing {

  public List<Path> prepareDocuments(List<Path> documents) {
    final List<Path> available = documents.stream().filter(Files::exists).filter(Files::isReadable).toList();
    if (available.size() < documents.size()) {
      documents.removeAll(available);
      throw new IllegalArgumentException(
          MessageFormat.format("Document {0} does not exist or is not readable", documents.get(0)));
    }
    return available.stream().flatMap(this::expandDirectories)
        .filter(e -> e.getFileName().toString().endsWith(".xml")).toList();
  }

  private Stream<Path> expandDirectories(final Path path) {
    try {
      if (!Files.exists(path)) {
        throw new IllegalArgumentException("Document or directory does not exist: " + path.toAbsolutePath());
      }
      if (Files.isDirectory(path)) {
        return Files.walk(path);
      }
      return Stream.of(path);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Error looking for input documents", e);
    }
  }
}
