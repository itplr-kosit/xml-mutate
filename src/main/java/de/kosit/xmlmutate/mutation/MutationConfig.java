package de.kosit.xmlmutate.mutation;

import de.kosit.xmlmutate.expectation.ExpectedResult;
import de.kosit.xmlmutate.expectation.SchematronRuleExpectation;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Konfigurationsoptionen für eine bestimmte Mutation. Diese werden durch die im
 * XML-Dokument hinterlegte {@link org.w3c.dom.ProcessingInstruction}
 * Information bestimmt.
 *
 * @author Andreas Penski
 */
@Getter
@Setter
public class MutationConfig {

    private Map<String, Object> properties = new HashMap<>();

    private ExpectedResult schemaValidationExpectation;

    private List<SchematronRuleExpectation> schematronExpectations = new ArrayList<>();

    private String mutatorName;

    /**
     * Fügt einen weiteren Parameter hinzu.
     *
     * @param keyword
     *                    der Parameter
     * @param value
     *                    der Wert des Parameters
     * @return die Konfiguration zum verketten von Aktionen
     */
    public MutationConfig add(final String keyword, final Object value) {
        final Object existing = this.properties.get(keyword);
        if (existing != null) {
            final Collection<Object> list;
            if (Collection.class.isAssignableFrom(existing.getClass())) {
                // noinspection unchecked
                list = (Collection<Object>) existing;
            } else {
                list = new ArrayList<>();
                list.add(existing);
                this.properties.put(keyword, list);
            }
            list.add(value);
        } else {
            this.properties.put(keyword, value);
        }
        return this;
    }

    public List<Object> resolveList(final String propertyName) {
        List<Object> result = Collections.emptyList();
        final Object o = this.properties.get(propertyName);
        if (o != null) {
            if (List.class.isAssignableFrom(o.getClass())) {
                // noinspection unchecked
                result = (List<Object>) o;
            } else {
                result = Collections.singletonList(o);
            }
        }
        return result;
    }

    public MutationConfig cloneConfig() {
        final MutationConfig c = new MutationConfig();
        c.setProperties(new HashMap<>());
        c.getProperties().putAll(this.properties);
        c.setMutatorName(this.mutatorName);
        c.getSchematronExpectations().addAll(this.getSchematronExpectations());
        c.setSchemaValidationExpectation(this.schemaValidationExpectation);
        return c;
    }

    public void addExpectation(final SchematronRuleExpectation expectation) {

        this.schematronExpectations.add(expectation);
    }

    /**
     * Returns a string-representation of a defined property.
     * 
     * @param propKey the key of the property
     * @return String representation or null
     */
    public String getStringProperty(final String propKey) {
        final Object objekt = this.properties.get(propKey);
        return objekt != null ? objekt.toString() : null;
    }

    /**
     * Returns a defined property and casting it to the desired target type.
     *
     * @param propKey the key of the property
     * @return the property value or null
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(final String propKey) {
        return (T) this.properties.get(propKey);
    }
}
