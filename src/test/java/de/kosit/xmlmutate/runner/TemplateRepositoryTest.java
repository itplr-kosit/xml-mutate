package de.kosit.xmlmutate.runner;

import static de.kosit.xmlmutate.TestHelper.TEST_ROOT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import de.kosit.xmlmutate.TestResource;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link TemplateRepository}.
 * 
 * @author Andreas Penski
 */
public class TemplateRepositoryTest {

    private static final URI SIMPLE_TRANSFORMATION = TestResource.TransformResource.SIMPLE_TRANSFORM;

    private static final URI INVALID_TRANSFORMATION = TestResource.TransformResource.INVALD_TRANSFORM;

    private static final String NAME = "someName";

    @Test
    public void simpleRegistration() {
        final TemplateRepository repo = new TemplateRepository();
        repo.registerTemplate(NAME, SIMPLE_TRANSFORMATION);
        assertThat(repo.exists(NAME)).isTrue();
        assertThat(repo.getTemplate(NAME)).isNotNull();
    }

    @Test
    public void findUnregistered() {
        final TemplateRepository repo = new TemplateRepository();
        assertThat(repo.exists("someName")).isFalse();
        assertThat(repo.getTemplate("someName")).isNull();
    }

    @Test
    public void registerUnexisting() {
        final TemplateRepository repo = new TemplateRepository();
        assertThrows(IllegalArgumentException.class, () -> {
            repo.registerTemplate("asdf", URI.create("someUnexisting"));
        });
    }

    @Test
    public void registerWithoutName() {
        final TemplateRepository repo = new TemplateRepository();
        assertThrows(IllegalArgumentException.class, () -> {
            repo.registerTemplate(null, SIMPLE_TRANSFORMATION);
        });
    }

    @Test
    public void registerTwice() {
        final TemplateRepository repo = new TemplateRepository();
        repo.registerTemplate("someName", SIMPLE_TRANSFORMATION);
        assertThrows(IllegalArgumentException.class, () -> {
            repo.registerTemplate("someName", SIMPLE_TRANSFORMATION);
        });
    }

    @Test
    public void registerInvalid() {
        final TemplateRepository repo = new TemplateRepository();
        assertThrows(IllegalArgumentException.class, () -> {
            repo.registerTemplate(NAME, INVALID_TRANSFORMATION);
        });
    }

}
