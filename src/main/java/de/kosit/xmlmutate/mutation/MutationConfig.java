// Generated by delombok at Fri Aug 13 16:07:39 CEST 2021
package de.kosit.xmlmutate.mutation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import de.kosit.xmlmutate.expectation.ExpectedResult;
import de.kosit.xmlmutate.expectation.SchematronEnterity;
import de.kosit.xmlmutate.expectation.SchematronRuleExpectation;

/**
 * Configuration options for an specific mutation. These are delivered through the information defined
 * in the XML document {@link org.w3c.dom.ProcessingInstruction}
 *
 * @author Andreas Penski
 */
public class MutationConfig {
    private Map<String, Object> properties = new HashMap<>();
    private ExpectedResult schemaValidationExpectation;
    private List<SchematronRuleExpectation> schematronExpectations = new ArrayList<>();
    private Pair<SchematronEnterity, ExpectedResult> schematronEnterityExpectation;
    private String mutatorName;
    private String mutationId;
    private List<String> tagNames = new ArrayList<>();

    /**
     * Adds a new parameter
     *
     * @param keyword the parameter
     * @param value   the parameter value
     * @return  the configuration for chaining actions
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

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public ExpectedResult getSchemaValidationExpectation() {
        return this.schemaValidationExpectation;
    }

    public List<SchematronRuleExpectation> getSchematronExpectations() {
        return this.schematronExpectations;
    }

    public Pair<SchematronEnterity, ExpectedResult> getSchematronEnterityExpectation() {
        return this.schematronEnterityExpectation;
    }

    public String getMutatorName() {
        return this.mutatorName;
    }

    public String getMutationId() {
        return this.mutationId;
    }

    public List<String> getTagNames() {
        return this.tagNames;
    }

    public void setProperties(final Map<String, Object> properties) {
        this.properties = properties;
    }

    public void setSchemaValidationExpectation(final ExpectedResult schemaValidationExpectation) {
        this.schemaValidationExpectation = schemaValidationExpectation;
    }

    public void setSchematronExpectations(final List<SchematronRuleExpectation> schematronExpectations) {
        this.schematronExpectations = schematronExpectations;
    }

    public void setSchematronEnterityExpectation(final Pair<SchematronEnterity, ExpectedResult> schematronEnterityExpectation) {
        this.schematronEnterityExpectation = schematronEnterityExpectation;
    }

    public void setMutatorName(final String mutatorName) {
        this.mutatorName = mutatorName;
    }

    public void setMutationId(final String mutationId) {
        this.mutationId = mutationId;
    }

    public void setTagNames(final List<String> tagNames) {
        this.tagNames = tagNames;
    }
}
