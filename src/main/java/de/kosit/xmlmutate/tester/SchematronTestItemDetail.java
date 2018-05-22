package de.kosit.xmlmutate.tester;

import java.util.Objects;

public class SchematronTestItemDetail {
    private String id = "";
    private String test = "";
    private String location = "";
    private String role = "";
    private String flag = "";

    public SchematronTestItemDetail(String id, String location, String test) {
        if (Objects.isNull(id) || Objects.isNull(location) || Objects.isNull(test)) {
            throw new IllegalArgumentException("None of the constructor arguments should be null");
        }
        this.id = id;
        this.test = test;
        this.location = location;
    }

    public SchematronTestItemDetail(String id, String location, String test, String role, String flag) {
        this(id, location, test);
        if (Objects.isNull(role) || Objects.isNull(flag)) {
            throw new IllegalArgumentException("None of the constructor arguments should be null");
        }
        this.role = role;
        this.flag = flag;

    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the test
     */
    public String getTest() {
        return test;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @return the flag
     */
    public String getFlag() {
        return flag;
    }
}
