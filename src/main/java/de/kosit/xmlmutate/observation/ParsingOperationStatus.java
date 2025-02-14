package de.kosit.xmlmutate.observation;

public class ParsingOperationStatus extends XMuteOperationStatus {

    private String location = "na";
    private String fixHint = "na";

    public ParsingOperationStatus(final int severity, final String message) {
        super(severity, message);
    }

    public ParsingOperationStatus(final int severity, final String message, final Throwable exception) {
        super(severity, -1, message, exception);
    }

    public ParsingOperationStatus(final int severity, final int code, final String message, final Throwable exception,
            final String location, final String fixHint) {
        super(severity, code, message, exception);
        setLocation(location);
        setFixHint(fixHint);
    }

    public ParsingOperationStatus(final int severity, final String message, final String location,
            final String fixHint) {
        super(severity, message);
        setLocation(location);
        setFixHint(fixHint);
    }

    public ParsingOperationStatus() {
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " At location: " + this.getLocation();
    }

    private void setLocation(final String location) {
        if (location != null && !location.isBlank()) {
            this.location = location;
        }
    }

    private void setFixHint(final String fixHint) {
        if (fixHint != null && !fixHint.isBlank()) {
            this.fixHint = fixHint;
        }
    }

}
