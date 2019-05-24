package de.kosit.xmlmutate.mutation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Konfigurationsoptionen für eine bestimmte Mutation.
 * 
 * @author Andreas Penski
 */
@Getter
@Setter
public class MutationConfig {

    private Map<String, Object> properties = new HashMap<>();

    private boolean expectSchemaValid;

    private List<Expectation> schematronExpectations = new ArrayList<>();

    private String mutatorName;

    public void add(final String keyword, final Object value) {
        final Object existing = this.properties.get(keyword);
        if (existing != null) {
            final Collection list;
            if (Collection.class.isAssignableFrom(existing.getClass())) {
                list = (Collection) existing;
            } else {
                list = new ArrayList();
                this.properties.put(keyword, list);
            }
            // noinspection unchecked
            list.add(value);
        } else {
            this.properties.put(keyword, value);
        }

    }

    public MutationConfig cloneConfig() {
        final MutationConfig c = new MutationConfig();
        c.setProperties(new HashMap<>());
        c.getProperties().putAll(this.properties);
        c.setMutatorName(this.mutatorName);
        return c;
    }

    public void addExpectation(final Expectation valid) {
        schematronExpectations.add(valid);
    }
}
