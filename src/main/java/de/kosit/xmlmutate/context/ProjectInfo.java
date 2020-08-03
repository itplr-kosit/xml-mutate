package de.kosit.xmlmutate.context;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ProjectInfo {

    @Getter
    private String version;

    @Getter
    private String buildTimestamp;

    @Getter
    private String name;

    @Getter
    private String description;

    public ProjectInfo() {
        final Properties p = new Properties();
        try (final InputStream in = ProjectInfo.class.getClassLoader().getResourceAsStream("version.properties")) {
            p.load(in);
            this.buildTimestamp = p.getProperty("maven_build_timestamp");
            this.version = p.getProperty("project_version");
            this.name = p.getProperty("project_name");
            this.description = p.getProperty("project_description");
        } catch (final IOException e) {
            throw new IllegalStateException("Can not initialize version information");
        }
    }
}
