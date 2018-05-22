package de.kosit.xmlmutate.tester;

import java.util.Objects;

public class SchematronTestItem implements TestItem {
    private String what = "";
    // chosing different boolean values such that isAsExcpectd does not result in
    // true by default
    private boolean expected = false;
    private boolean actual = true;
    private SchematronTestItemDetail detail = null;

    private SchematronTestItem() {
    }

    public SchematronTestItem(String what, boolean expected, boolean actual, SchematronTestItemDetail detail) {
        if (Objects.nonNull(what)) {
            this.what = what;
        }
        if (Objects.nonNull(expected)) {
            this.expected = expected;
        }
        if (Objects.nonNull(actual)) {
            this.actual = actual;
        }
        if (Objects.nonNull(detail)) {
            this.detail = detail;
        }
    }

    public String of() {
        return what;
    }

    public boolean expected() {
        return expected;
    }

    public boolean actual() {
        return actual;
    }

    public boolean asExpected() {
        return expected == actual;
    }

    public SchematronTestItemDetail getDetail() {
        return this.detail;
    }

}