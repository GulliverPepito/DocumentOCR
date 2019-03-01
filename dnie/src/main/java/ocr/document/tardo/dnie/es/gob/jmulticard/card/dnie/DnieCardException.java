package es.gob.jmulticard.card.dnie;

import es.gob.jmulticard.apdu.StatusWord;
import es.gob.jmulticard.card.CryptoCardException;

public final class DnieCardException extends CryptoCardException {
    private static final long serialVersionUID = 5935577997660561619L;
    private final StatusWord returnCode;

    DnieCardException(String desc, StatusWord retCode) {
        super(desc);
        this.returnCode = retCode;
    }

    DnieCardException(String desc, Throwable t) {
        super(desc, t);
        this.returnCode = null;
    }

    DnieCardException(StatusWord retCode) {
        this.returnCode = retCode;
    }

    public StatusWord getStatusWord() {
        return this.returnCode;
    }
}
