package es.gob.jmulticard.asn1;

public final class TlvException extends Exception {
    private static final long serialVersionUID = -295492295001355798L;

    public TlvException(String message) {
        super(message);
    }

    public TlvException(String message, Throwable cause) {
        super(message + ": " + cause.getMessage());
    }
}
