package de.kosit.xmlmutate.parser;

public enum PropertyKeys {
    MUTATOR("mutator"),
    VALUES("values"),
    DESCRIPTION("description"),
    SCHEMA_VALID("schema-valid"),
    SCHEMA_INVALID("schema-invalid"),
    SCHEMATRON_VALID("schematron-valid"),
    SCHEMATRON_INVALID("schematron-invalid"),
    TEST_CASE("tc"),
    TEST_GROUP("tg");

    private final String key;

    PropertyKeys(String key) {
        this.key = key.trim().toLowerCase();
    }

    public String key() {
        return this.key;
    }

    @Override
    public String toString() {
        return this.key;
    }
}
