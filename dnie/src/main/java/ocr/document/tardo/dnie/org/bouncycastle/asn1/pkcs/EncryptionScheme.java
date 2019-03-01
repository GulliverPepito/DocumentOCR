package org.bouncycastle.asn1.pkcs;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class EncryptionScheme extends ASN1Object {
    private AlgorithmIdentifier algId;

    public EncryptionScheme(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Encodable aSN1Encodable) {
        this.algId = new AlgorithmIdentifier(aSN1ObjectIdentifier, aSN1Encodable);
    }

    private EncryptionScheme(ASN1Sequence aSN1Sequence) {
        this.algId = AlgorithmIdentifier.getInstance(aSN1Sequence);
    }

    public static final EncryptionScheme getInstance(Object obj) {
        return obj instanceof EncryptionScheme ? (EncryptionScheme) obj : obj != null ? new EncryptionScheme(ASN1Sequence.getInstance(obj)) : null;
    }

    public ASN1ObjectIdentifier getAlgorithm() {
        return this.algId.getAlgorithm();
    }

    public ASN1Encodable getParameters() {
        return this.algId.getParameters();
    }

    public ASN1Primitive toASN1Primitive() {
        return this.algId.toASN1Primitive();
    }
}
