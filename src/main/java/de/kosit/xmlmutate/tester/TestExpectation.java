package de.kosit.xmlmutate.tester;

public class TestExpectation implements Expectation {

    private String on = "";
    private String what = "";
    private boolean valid = false;

    private TestExpectation() {
    }

    public TestExpectation(String on, String what, boolean valid) {
        this.on = on;
        this.what = what;
        this.valid = valid;
    }

    @Override
    public boolean is() {
        return valid;
    }

    @Override
    public String on() {
        return this.on;
    }

    @Override
    public String what() {
        return this.what;
    }

}