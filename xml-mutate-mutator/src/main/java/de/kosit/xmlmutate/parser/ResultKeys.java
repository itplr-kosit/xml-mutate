package de.kosit.xmlmutate.parser;

public enum ResultKeys {
  RULE("Rule"),
  EXPECTATIONS("Expectations"),
  MUTATION("Mutation"),
  MUTATION_PARAMETERS("Mutation Parameters"),
  VALUES("Values"),
  COLON(": ");

  private final String key;

  ResultKeys(String key) {
    this.key = key;
  }

  public String key() {
    return this.key;
  }

  @Override
  public String toString() {
    return this.key;
  }
}
