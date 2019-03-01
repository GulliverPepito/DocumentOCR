package org.bouncycastle.pqc.math.linearalgebra;

import java.util.Vector;

public abstract class GF2nField {
    protected GF2Polynomial fieldPolynomial;
    protected Vector fields;
    protected int mDegree;
    protected Vector matrices;

    protected abstract void computeCOBMatrix(GF2nField gF2nField);

    protected abstract void computeFieldPolynomial();

    public final GF2nElement convert(GF2nElement gF2nElement, GF2nField gF2nField) throws RuntimeException {
        if (gF2nField == this) {
            return (GF2nElement) gF2nElement.clone();
        }
        if (this.fieldPolynomial.equals(gF2nField.fieldPolynomial)) {
            return (GF2nElement) gF2nElement.clone();
        }
        if (this.mDegree != gF2nField.mDegree) {
            throw new RuntimeException("GF2nField.convert: B1 has a different degree and thus cannot be coverted to!");
        }
        int indexOf = this.fields.indexOf(gF2nField);
        if (indexOf == -1) {
            computeCOBMatrix(gF2nField);
            indexOf = this.fields.indexOf(gF2nField);
        }
        GF2Polynomial[] gF2PolynomialArr = (GF2Polynomial[]) this.matrices.elementAt(indexOf);
        GF2nElement gF2nElement2 = (GF2nElement) gF2nElement.clone();
        if (gF2nElement2 instanceof GF2nONBElement) {
            ((GF2nONBElement) gF2nElement2).reverseOrder();
        }
        GF2Polynomial gF2Polynomial = new GF2Polynomial(this.mDegree, gF2nElement2.toFlexiBigInt());
        gF2Polynomial.expandN(this.mDegree);
        GF2Polynomial gF2Polynomial2 = new GF2Polynomial(this.mDegree);
        for (int i = 0; i < this.mDegree; i++) {
            if (gF2Polynomial.vectorMult(gF2PolynomialArr[i])) {
                gF2Polynomial2.setBit((this.mDegree - 1) - i);
            }
        }
        if (gF2nField instanceof GF2nPolynomialField) {
            return new GF2nPolynomialElement((GF2nPolynomialField) gF2nField, gF2Polynomial2);
        }
        if (gF2nField instanceof GF2nONBField) {
            GF2nElement gF2nONBElement = new GF2nONBElement((GF2nONBField) gF2nField, gF2Polynomial2.toFlexiBigInt());
            gF2nONBElement.reverseOrder();
            return gF2nONBElement;
        }
        throw new RuntimeException("GF2nField.convert: B1 must be an instance of GF2nPolynomialField or GF2nONBField!");
    }

    public final boolean equals(Object obj) {
        if (obj == null || !(obj instanceof GF2nField)) {
            return false;
        }
        GF2nField gF2nField = (GF2nField) obj;
        return (gF2nField.mDegree == this.mDegree && this.fieldPolynomial.equals(gF2nField.fieldPolynomial)) ? (!(this instanceof GF2nPolynomialField) || (gF2nField instanceof GF2nPolynomialField)) ? !(this instanceof GF2nONBField) || (gF2nField instanceof GF2nONBField) : false : false;
    }

    public final int getDegree() {
        return this.mDegree;
    }

    public final GF2Polynomial getFieldPolynomial() {
        if (this.fieldPolynomial == null) {
            computeFieldPolynomial();
        }
        return new GF2Polynomial(this.fieldPolynomial);
    }

    protected abstract GF2nElement getRandomRoot(GF2Polynomial gF2Polynomial);

    public int hashCode() {
        return this.mDegree + this.fieldPolynomial.hashCode();
    }

    protected final GF2Polynomial[] invertMatrix(GF2Polynomial[] gF2PolynomialArr) {
        int i;
        int i2 = 0;
        GF2Polynomial[] gF2PolynomialArr2 = new GF2Polynomial[gF2PolynomialArr.length];
        GF2Polynomial[] gF2PolynomialArr3 = new GF2Polynomial[gF2PolynomialArr.length];
        for (i = 0; i < this.mDegree; i++) {
            try {
                gF2PolynomialArr2[i] = new GF2Polynomial(gF2PolynomialArr[i]);
                gF2PolynomialArr3[i] = new GF2Polynomial(this.mDegree);
                gF2PolynomialArr3[i].setBit((this.mDegree - 1) - i);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        while (i2 < this.mDegree - 1) {
            i = i2;
            while (i < this.mDegree && !gF2PolynomialArr2[i].testBit((this.mDegree - 1) - i2)) {
                i++;
            }
            if (i >= this.mDegree) {
                throw new RuntimeException("GF2nField.invertMatrix: Matrix cannot be inverted!");
            }
            if (i2 != i) {
                GF2Polynomial gF2Polynomial = gF2PolynomialArr2[i2];
                gF2PolynomialArr2[i2] = gF2PolynomialArr2[i];
                gF2PolynomialArr2[i] = gF2Polynomial;
                gF2Polynomial = gF2PolynomialArr3[i2];
                gF2PolynomialArr3[i2] = gF2PolynomialArr3[i];
                gF2PolynomialArr3[i] = gF2Polynomial;
            }
            for (i = i2 + 1; i < this.mDegree; i++) {
                if (gF2PolynomialArr2[i].testBit((this.mDegree - 1) - i2)) {
                    gF2PolynomialArr2[i].addToThis(gF2PolynomialArr2[i2]);
                    gF2PolynomialArr3[i].addToThis(gF2PolynomialArr3[i2]);
                }
            }
            i2++;
        }
        for (i2 = this.mDegree - 1; i2 > 0; i2--) {
            for (i = i2 - 1; i >= 0; i--) {
                if (gF2PolynomialArr2[i].testBit((this.mDegree - 1) - i2)) {
                    gF2PolynomialArr2[i].addToThis(gF2PolynomialArr2[i2]);
                    gF2PolynomialArr3[i].addToThis(gF2PolynomialArr3[i2]);
                }
            }
        }
        return gF2PolynomialArr3;
    }
}
