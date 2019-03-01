package org.bouncycastle.pqc.crypto.rainbow.util;

import java.lang.reflect.Array;

public class ComputeInField {
    /* renamed from: A */
    private short[][] f129A;
    /* renamed from: x */
    short[] f130x;

    private void computeZerosAbove() throws RuntimeException {
        for (int length = this.f129A.length - 1; length > 0; length--) {
            for (int i = length - 1; i >= 0; i--) {
                short s = this.f129A[i][length];
                short invElem = GF2Field.invElem(this.f129A[length][length]);
                if (invElem == (short) 0) {
                    throw new RuntimeException("The matrix is not invertible");
                }
                for (int i2 = length; i2 < this.f129A.length * 2; i2++) {
                    this.f129A[i][i2] = GF2Field.addElem(this.f129A[i][i2], GF2Field.multElem(s, GF2Field.multElem(this.f129A[length][i2], invElem)));
                }
            }
        }
    }

    private void computeZerosUnder(boolean z) throws RuntimeException {
        int length = z ? this.f129A.length * 2 : this.f129A.length + 1;
        for (int i = 0; i < this.f129A.length - 1; i++) {
            for (int i2 = i + 1; i2 < this.f129A.length; i2++) {
                short s = this.f129A[i2][i];
                short invElem = GF2Field.invElem(this.f129A[i][i]);
                if (invElem == (short) 0) {
                    throw new RuntimeException("Matrix not invertible! We have to choose another one!");
                }
                for (int i3 = i; i3 < length; i3++) {
                    this.f129A[i2][i3] = GF2Field.addElem(this.f129A[i2][i3], GF2Field.multElem(s, GF2Field.multElem(this.f129A[i][i3], invElem)));
                }
            }
        }
    }

    private void substitute() throws RuntimeException {
        short invElem = GF2Field.invElem(this.f129A[this.f129A.length - 1][this.f129A.length - 1]);
        if (invElem == (short) 0) {
            throw new RuntimeException("The equation system is not solvable");
        }
        this.f130x[this.f129A.length - 1] = GF2Field.multElem(this.f129A[this.f129A.length - 1][this.f129A.length], invElem);
        for (int length = this.f129A.length - 2; length >= 0; length--) {
            short s = this.f129A[length][this.f129A.length];
            for (int length2 = this.f129A.length - 1; length2 > length; length2--) {
                s = GF2Field.addElem(s, GF2Field.multElem(this.f129A[length][length2], this.f130x[length2]));
            }
            invElem = GF2Field.invElem(this.f129A[length][length]);
            if (invElem == (short) 0) {
                throw new RuntimeException("Not solvable equation system");
            }
            this.f130x[length] = GF2Field.multElem(s, invElem);
        }
    }

    public short[][] addSquareMatrix(short[][] sArr, short[][] sArr2) {
        if (sArr.length == sArr2.length && sArr[0].length == sArr2[0].length) {
            short[][] sArr3 = (short[][]) Array.newInstance(Short.TYPE, new int[]{sArr.length, sArr.length});
            for (int i = 0; i < sArr.length; i++) {
                for (int i2 = 0; i2 < sArr2.length; i2++) {
                    sArr3[i][i2] = GF2Field.addElem(sArr[i][i2], sArr2[i][i2]);
                }
            }
            return sArr3;
        }
        throw new RuntimeException("Addition is not possible!");
    }

    public short[] addVect(short[] sArr, short[] sArr2) {
        if (sArr.length != sArr2.length) {
            throw new RuntimeException("Multiplication is not possible!");
        }
        short[] sArr3 = new short[sArr.length];
        for (int i = 0; i < sArr3.length; i++) {
            sArr3[i] = GF2Field.addElem(sArr[i], sArr2[i]);
        }
        return sArr3;
    }

    public short[][] inverse(short[][] sArr) {
        try {
            this.f129A = (short[][]) Array.newInstance(Short.TYPE, new int[]{sArr.length, sArr.length * 2});
            if (sArr.length != sArr[0].length) {
                throw new RuntimeException("The matrix is not invertible. Please choose another one!");
            }
            int i;
            int i2;
            for (i = 0; i < sArr.length; i++) {
                for (i2 = 0; i2 < sArr.length; i2++) {
                    this.f129A[i][i2] = sArr[i][i2];
                }
                for (i2 = sArr.length; i2 < sArr.length * 2; i2++) {
                    this.f129A[i][i2] = (short) 0;
                }
                this.f129A[i][this.f129A.length + i] = (short) 1;
            }
            computeZerosUnder(true);
            for (i = 0; i < this.f129A.length; i++) {
                short invElem = GF2Field.invElem(this.f129A[i][i]);
                for (i2 = i; i2 < this.f129A.length * 2; i2++) {
                    this.f129A[i][i2] = GF2Field.multElem(this.f129A[i][i2], invElem);
                }
            }
            computeZerosAbove();
            short[][] sArr2 = (short[][]) Array.newInstance(Short.TYPE, new int[]{this.f129A.length, this.f129A.length});
            for (i = 0; i < this.f129A.length; i++) {
                for (int length = this.f129A.length; length < this.f129A.length * 2; length++) {
                    sArr2[i][length - this.f129A.length] = this.f129A[i][length];
                }
            }
            return sArr2;
        } catch (RuntimeException e) {
            return (short[][]) null;
        }
    }

    public short[][] multMatrix(short s, short[][] sArr) {
        short[][] sArr2 = (short[][]) Array.newInstance(Short.TYPE, new int[]{sArr.length, sArr[0].length});
        for (int i = 0; i < sArr.length; i++) {
            for (int i2 = 0; i2 < sArr[0].length; i2++) {
                sArr2[i][i2] = GF2Field.multElem(s, sArr[i][i2]);
            }
        }
        return sArr2;
    }

    public short[] multVect(short s, short[] sArr) {
        short[] sArr2 = new short[sArr.length];
        for (int i = 0; i < sArr2.length; i++) {
            sArr2[i] = GF2Field.multElem(s, sArr[i]);
        }
        return sArr2;
    }

    public short[][] multVects(short[] sArr, short[] sArr2) {
        if (sArr.length != sArr2.length) {
            throw new RuntimeException("Multiplication is not possible!");
        }
        short[][] sArr3 = (short[][]) Array.newInstance(Short.TYPE, new int[]{sArr.length, sArr2.length});
        for (int i = 0; i < sArr.length; i++) {
            for (int i2 = 0; i2 < sArr2.length; i2++) {
                sArr3[i][i2] = GF2Field.multElem(sArr[i], sArr2[i2]);
            }
        }
        return sArr3;
    }

    public short[] multiplyMatrix(short[][] sArr, short[] sArr2) throws RuntimeException {
        if (sArr[0].length != sArr2.length) {
            throw new RuntimeException("Multiplication is not possible!");
        }
        short[] sArr3 = new short[sArr.length];
        for (int i = 0; i < sArr.length; i++) {
            for (int i2 = 0; i2 < sArr2.length; i2++) {
                sArr3[i] = GF2Field.addElem(sArr3[i], GF2Field.multElem(sArr[i][i2], sArr2[i2]));
            }
        }
        return sArr3;
    }

    public short[][] multiplyMatrix(short[][] sArr, short[][] sArr2) throws RuntimeException {
        if (sArr[0].length != sArr2.length) {
            throw new RuntimeException("Multiplication is not possible!");
        }
        this.f129A = (short[][]) Array.newInstance(Short.TYPE, new int[]{sArr.length, sArr2[0].length});
        for (int i = 0; i < sArr.length; i++) {
            for (int i2 = 0; i2 < sArr2.length; i2++) {
                for (int i3 = 0; i3 < sArr2[0].length; i3++) {
                    this.f129A[i][i3] = GF2Field.addElem(this.f129A[i][i3], GF2Field.multElem(sArr[i][i2], sArr2[i2][i3]));
                }
            }
        }
        return this.f129A;
    }

    public short[] solveEquation(short[][] sArr, short[] sArr2) {
        try {
            if (sArr.length != sArr2.length) {
                throw new RuntimeException("The equation system is not solvable");
            }
            int i;
            this.f129A = (short[][]) Array.newInstance(Short.TYPE, new int[]{sArr.length, sArr.length + 1});
            this.f130x = new short[sArr.length];
            for (int i2 = 0; i2 < sArr.length; i2++) {
                for (i = 0; i < sArr[0].length; i++) {
                    this.f129A[i2][i] = sArr[i2][i];
                }
            }
            for (i = 0; i < sArr2.length; i++) {
                this.f129A[i][sArr2.length] = GF2Field.addElem(sArr2[i], this.f129A[i][sArr2.length]);
            }
            computeZerosUnder(false);
            substitute();
            return this.f130x;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
