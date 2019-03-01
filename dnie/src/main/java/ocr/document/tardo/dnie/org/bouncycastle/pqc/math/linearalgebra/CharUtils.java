package org.bouncycastle.pqc.math.linearalgebra;

public final class CharUtils {
    private CharUtils() {
    }

    public static char[] clone(char[] cArr) {
        Object obj = new char[cArr.length];
        System.arraycopy(cArr, 0, obj, 0, cArr.length);
        return obj;
    }

    public static boolean equals(char[] cArr, char[] cArr2) {
        if (cArr.length != cArr2.length) {
            return false;
        }
        boolean z = true;
        for (int length = cArr.length - 1; length >= 0; length--) {
            z &= cArr[length] == cArr2[length] ? 1 : 0;
        }
        return z;
    }

    public static byte[] toByteArray(char[] cArr) {
        byte[] bArr = new byte[cArr.length];
        for (int length = cArr.length - 1; length >= 0; length--) {
            bArr[length] = (byte) cArr[length];
        }
        return bArr;
    }

    public static byte[] toByteArrayForPBE(char[] cArr) {
        int i;
        byte[] bArr = new byte[cArr.length];
        for (i = 0; i < cArr.length; i++) {
            bArr[i] = (byte) cArr[i];
        }
        int length = bArr.length * 2;
        byte[] bArr2 = new byte[(length + 2)];
        for (i = 0; i < bArr.length; i++) {
            int i2 = i * 2;
            bArr2[i2] = (byte) 0;
            bArr2[i2 + 1] = bArr[i];
        }
        bArr2[length] = (byte) 0;
        bArr2[length + 1] = (byte) 0;
        return bArr2;
    }
}
