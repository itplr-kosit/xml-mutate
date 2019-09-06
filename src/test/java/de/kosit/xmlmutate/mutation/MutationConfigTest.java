package de.kosit.xmlmutate.mutation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.junit.jupiter.api.Test;

import de.kosit.xmlmutate.expectation.ExpectedResult;

/**
 * Testet das {@link MutationConfig}-Objekt.
 *
 * @author Andreas Penski
 */
public class MutationConfigTest {

    @Test
    public void testAddParameters() {
        final MutationConfig c = new MutationConfig();
        c.add("test", "test");
        assertThat(c.getProperties()).hasSize(1);
        c.add("test2", "test");
        assertThat(c.getProperties()).hasSize(2);
        c.add("test", "test2");
        assertThat(c.getProperties()).hasSize(2);
        assertThat(c.getProperties().get("test")).isInstanceOf(Collection.class);
        assertThat(((Collection) c.getProperties().get("test"))).hasSize(2);
    }

    @Test
    public void testResolveList() {
        final MutationConfig c = new MutationConfig();
        assertThat(c.resolveList("test")).hasSize(0);
        c.add("test", "test");
        assertThat(c.resolveList("test")).hasSize(1);
        c.add("test", "test2");
        assertThat(c.resolveList("test")).hasSize(2);
    }

    @Test
    public void testClone() {
        final MutationConfig c = new MutationConfig();
        c.add("prop", "property");
        c.addExpectation(new SchematronRuleExpectation("test", "test", ExpectedResult.FAIL));
        final MutationConfig clone = c.cloneConfig();
        assertThat(clone == c).isFalse();
        assertThat(clone.getProperties()).hasSize(1);
        assertThat(clone.getSchematronExpectations()).hasSize(1);
    }

}
