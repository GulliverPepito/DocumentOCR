package org.bouncycastle.cms.bc;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.PasswordRecipient;
import org.bouncycastle.crypto.Wrapper;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public abstract class BcPasswordRecipient implements PasswordRecipient {
    private char[] password;
    private int schemeID = 1;

    BcPasswordRecipient(char[] cArr) {
        this.password = cArr;
    }

    protected KeyParameter extractSecretKey(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, byte[] bArr, byte[] bArr2) throws CMSException {
        Wrapper createRFC3211Wrapper = EnvelopedDataHelper.createRFC3211Wrapper(algorithmIdentifier.getAlgorithm());
        createRFC3211Wrapper.init(false, new ParametersWithIV(new KeyParameter(bArr), ASN1OctetString.getInstance(algorithmIdentifier.getParameters()).getOctets()));
        try {
            return new KeyParameter(createRFC3211Wrapper.unwrap(bArr2, 0, bArr2.length));
        } catch (Exception e) {
            throw new CMSException("unable to unwrap key: " + e.getMessage(), e);
        }
    }

    public char[] getPassword() {
        return this.password;
    }

    public int getPasswordConversionScheme() {
        return this.schemeID;
    }

    public BcPasswordRecipient setPasswordConversionScheme(int i) {
        this.schemeID = i;
        return this;
    }
}
