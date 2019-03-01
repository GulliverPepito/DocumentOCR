package org.bouncycastle.pqc.crypto.mceliece;

import java.security.SecureRandom;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.pqc.crypto.MessageEncryptor;
import org.bouncycastle.pqc.math.linearalgebra.GF2Matrix;
import org.bouncycastle.pqc.math.linearalgebra.GF2Vector;
import org.bouncycastle.pqc.math.linearalgebra.GF2mField;
import org.bouncycastle.pqc.math.linearalgebra.GoppaCode;
import org.bouncycastle.pqc.math.linearalgebra.Permutation;
import org.bouncycastle.pqc.math.linearalgebra.PolynomialGF2mSmallM;
import org.bouncycastle.pqc.math.linearalgebra.Vector;

public class McEliecePKCSCipher implements MessageEncryptor {
    public static final String OID = "1.3.6.1.4.1.8301.3.1.3.4.1";
    public int cipherTextSize;
    /* renamed from: k */
    private int f322k;
    McElieceKeyParameters key;
    public int maxPlainTextSize;
    /* renamed from: n */
    private int f323n;
    private SecureRandom sr;
    /* renamed from: t */
    private int f324t;

    private byte[] computeMessage(GF2Vector gF2Vector) throws Exception {
        Object encoded = gF2Vector.getEncoded();
        int length = encoded.length - 1;
        while (length >= 0 && encoded[length] == (byte) 0) {
            length--;
        }
        if (encoded[length] != (byte) 1) {
            throw new Exception("Bad Padding: invalid ciphertext");
        }
        Object obj = new byte[length];
        System.arraycopy(encoded, 0, obj, 0, length);
        return obj;
    }

    private GF2Vector computeMessageRepresentative(byte[] bArr) {
        Object obj = new byte[(((this.f322k & 7) != 0 ? 1 : 0) + this.maxPlainTextSize)];
        System.arraycopy(bArr, 0, obj, 0, bArr.length);
        obj[bArr.length] = 1;
        return GF2Vector.OS2VP(this.f322k, obj);
    }

    public int getKeySize(McElieceKeyParameters mcElieceKeyParameters) {
        if (mcElieceKeyParameters instanceof McEliecePublicKeyParameters) {
            return ((McEliecePublicKeyParameters) mcElieceKeyParameters).getN();
        }
        if (mcElieceKeyParameters instanceof McEliecePrivateKeyParameters) {
            return ((McEliecePrivateKeyParameters) mcElieceKeyParameters).getN();
        }
        throw new IllegalArgumentException("unsupported type");
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        if (!z) {
            this.key = (McEliecePrivateKeyParameters) cipherParameters;
            initCipherDecrypt((McEliecePrivateKeyParameters) this.key);
        } else if (cipherParameters instanceof ParametersWithRandom) {
            ParametersWithRandom parametersWithRandom = (ParametersWithRandom) cipherParameters;
            this.sr = parametersWithRandom.getRandom();
            this.key = (McEliecePublicKeyParameters) parametersWithRandom.getParameters();
            initCipherEncrypt((McEliecePublicKeyParameters) this.key);
        } else {
            this.sr = new SecureRandom();
            this.key = (McEliecePublicKeyParameters) cipherParameters;
            initCipherEncrypt((McEliecePublicKeyParameters) this.key);
        }
    }

    public void initCipherDecrypt(McEliecePrivateKeyParameters mcEliecePrivateKeyParameters) {
        this.f323n = mcEliecePrivateKeyParameters.getN();
        this.f322k = mcEliecePrivateKeyParameters.getK();
        this.maxPlainTextSize = this.f322k >> 3;
        this.cipherTextSize = this.f323n >> 3;
    }

    public void initCipherEncrypt(McEliecePublicKeyParameters mcEliecePublicKeyParameters) {
        this.sr = this.sr != null ? this.sr : new SecureRandom();
        this.f323n = mcEliecePublicKeyParameters.getN();
        this.f322k = mcEliecePublicKeyParameters.getK();
        this.f324t = mcEliecePublicKeyParameters.getT();
        this.cipherTextSize = this.f323n >> 3;
        this.maxPlainTextSize = this.f322k >> 3;
    }

    public byte[] messageDecrypt(byte[] bArr) throws Exception {
        GF2Vector OS2VP = GF2Vector.OS2VP(this.f323n, bArr);
        McEliecePrivateKeyParameters mcEliecePrivateKeyParameters = (McEliecePrivateKeyParameters) this.key;
        GF2mField field = mcEliecePrivateKeyParameters.getField();
        PolynomialGF2mSmallM goppaPoly = mcEliecePrivateKeyParameters.getGoppaPoly();
        GF2Matrix sInv = mcEliecePrivateKeyParameters.getSInv();
        Permutation p1 = mcEliecePrivateKeyParameters.getP1();
        Permutation p2 = mcEliecePrivateKeyParameters.getP2();
        GF2Matrix h = mcEliecePrivateKeyParameters.getH();
        PolynomialGF2mSmallM[] qInv = mcEliecePrivateKeyParameters.getQInv();
        p2 = p1.rightMultiply(p2);
        Vector vector = (GF2Vector) OS2VP.multiply(p2.computeInverse());
        Vector syndromeDecode = GoppaCode.syndromeDecode((GF2Vector) h.rightMultiply(vector), field, goppaPoly, qInv);
        GF2Vector gF2Vector = (GF2Vector) ((GF2Vector) vector.add(syndromeDecode)).multiply(p1);
        OS2VP = (GF2Vector) syndromeDecode.multiply(p2);
        return computeMessage((GF2Vector) sInv.leftMultiply(gF2Vector.extractRightVector(this.f322k)));
    }

    public byte[] messageEncrypt(byte[] bArr) {
        Vector computeMessageRepresentative = computeMessageRepresentative(bArr);
        return ((GF2Vector) ((McEliecePublicKeyParameters) this.key).getG().leftMultiply(computeMessageRepresentative).add(new GF2Vector(this.f323n, this.f324t, this.sr))).getEncoded();
    }
}
