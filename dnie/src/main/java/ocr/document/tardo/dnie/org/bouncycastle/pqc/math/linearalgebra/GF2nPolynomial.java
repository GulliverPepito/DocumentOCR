package org.bouncycastle.pqc.math.linearalgebra;

public class GF2nPolynomial {
    private GF2nElement[] coeff;
    private int size;

    private GF2nPolynomial(int i) {
        this.size = i;
        this.coeff = new GF2nElement[this.size];
    }

    public GF2nPolynomial(int i, GF2nElement gF2nElement) {
        this.size = i;
        this.coeff = new GF2nElement[this.size];
        for (int i2 = 0; i2 < this.size; i2++) {
            this.coeff[i2] = (GF2nElement) gF2nElement.clone();
        }
    }

    public GF2nPolynomial(GF2Polynomial gF2Polynomial, GF2nField gF2nField) {
        this.size = gF2nField.getDegree() + 1;
        this.coeff = new GF2nElement[this.size];
        int i;
        if (gF2nField instanceof GF2nONBField) {
            for (i = 0; i < this.size; i++) {
                if (gF2Polynomial.testBit(i)) {
                    this.coeff[i] = GF2nONBElement.ONE((GF2nONBField) gF2nField);
                } else {
                    this.coeff[i] = GF2nONBElement.ZERO((GF2nONBField) gF2nField);
                }
            }
        } else if (gF2nField instanceof GF2nPolynomialField) {
            for (i = 0; i < this.size; i++) {
                if (gF2Polynomial.testBit(i)) {
                    this.coeff[i] = GF2nPolynomialElement.ONE((GF2nPolynomialField) gF2nField);
                } else {
                    this.coeff[i] = GF2nPolynomialElement.ZERO((GF2nPolynomialField) gF2nField);
                }
            }
        } else {
            throw new IllegalArgumentException("PolynomialGF2n(Bitstring, GF2nField): B1 must be an instance of GF2nONBField or GF2nPolynomialField!");
        }
    }

    public GF2nPolynomial(GF2nPolynomial gF2nPolynomial) {
        this.coeff = new GF2nElement[gF2nPolynomial.size];
        this.size = gF2nPolynomial.size;
        for (int i = 0; i < this.size; i++) {
            this.coeff[i] = (GF2nElement) gF2nPolynomial.coeff[i].clone();
        }
    }

    public final GF2nPolynomial add(GF2nPolynomial gF2nPolynomial) throws RuntimeException {
        GF2nPolynomial gF2nPolynomial2;
        int i;
        if (size() >= gF2nPolynomial.size()) {
            gF2nPolynomial2 = new GF2nPolynomial(size());
            i = 0;
            while (i < gF2nPolynomial.size()) {
                gF2nPolynomial2.coeff[i] = (GF2nElement) this.coeff[i].add(gF2nPolynomial.coeff[i]);
                i++;
            }
            while (i < size()) {
                gF2nPolynomial2.coeff[i] = this.coeff[i];
                i++;
            }
            return gF2nPolynomial2;
        }
        gF2nPolynomial2 = new GF2nPolynomial(gF2nPolynomial.size());
        i = 0;
        while (i < size()) {
            gF2nPolynomial2.coeff[i] = (GF2nElement) this.coeff[i].add(gF2nPolynomial.coeff[i]);
            i++;
        }
        while (i < gF2nPolynomial.size()) {
            gF2nPolynomial2.coeff[i] = gF2nPolynomial.coeff[i];
            i++;
        }
        return gF2nPolynomial2;
    }

    public final void assignZeroToElements() {
        for (int i = 0; i < this.size; i++) {
            this.coeff[i].assignZero();
        }
    }

    public final GF2nElement at(int i) {
        return this.coeff[i];
    }

    public final GF2nPolynomial[] divide(GF2nPolynomial gF2nPolynomial) throws RuntimeException, ArithmeticException {
        GF2nPolynomial[] gF2nPolynomialArr = new GF2nPolynomial[2];
        GF2nPolynomial gF2nPolynomial2 = new GF2nPolynomial(this);
        gF2nPolynomial2.shrink();
        int degree = gF2nPolynomial.getDegree();
        GF2nElement gF2nElement = (GF2nElement) gF2nPolynomial.coeff[degree].invert();
        if (gF2nPolynomial2.getDegree() < degree) {
            gF2nPolynomialArr[0] = new GF2nPolynomial(this);
            gF2nPolynomialArr[0].assignZeroToElements();
            gF2nPolynomialArr[0].shrink();
            gF2nPolynomialArr[1] = new GF2nPolynomial(this);
            gF2nPolynomialArr[1].shrink();
            return gF2nPolynomialArr;
        }
        gF2nPolynomialArr[0] = new GF2nPolynomial(this);
        gF2nPolynomialArr[0].assignZeroToElements();
        GF2nPolynomial gF2nPolynomial3 = gF2nPolynomial2;
        int degree2 = gF2nPolynomial2.getDegree() - degree;
        while (degree2 >= 0) {
            GF2nElement gF2nElement2 = (GF2nElement) gF2nPolynomial3.coeff[gF2nPolynomial3.getDegree()].multiply(gF2nElement);
            GF2nPolynomial scalarMultiply = gF2nPolynomial.scalarMultiply(gF2nElement2);
            scalarMultiply.shiftThisLeft(degree2);
            gF2nPolynomial3 = gF2nPolynomial3.add(scalarMultiply);
            gF2nPolynomial3.shrink();
            gF2nPolynomialArr[0].coeff[degree2] = (GF2nElement) gF2nElement2.clone();
            degree2 = gF2nPolynomial3.getDegree() - degree;
        }
        gF2nPolynomialArr[1] = gF2nPolynomial3;
        gF2nPolynomialArr[0].shrink();
        return gF2nPolynomialArr;
    }

    public final void enlarge(int i) {
        if (i > this.size) {
            Object obj = new GF2nElement[i];
            System.arraycopy(this.coeff, 0, obj, 0, this.size);
            GF2nField field = this.coeff[0].getField();
            int i2;
            if (this.coeff[0] instanceof GF2nPolynomialElement) {
                for (i2 = this.size; i2 < i; i2++) {
                    obj[i2] = GF2nPolynomialElement.ZERO((GF2nPolynomialField) field);
                }
            } else if (this.coeff[0] instanceof GF2nONBElement) {
                for (i2 = this.size; i2 < i; i2++) {
                    obj[i2] = GF2nONBElement.ZERO((GF2nONBField) field);
                }
            }
            this.size = i;
            this.coeff = obj;
        }
    }

    public final boolean equals(Object obj) {
        if (obj == null || !(obj instanceof GF2nPolynomial)) {
            return false;
        }
        GF2nPolynomial gF2nPolynomial = (GF2nPolynomial) obj;
        if (getDegree() != gF2nPolynomial.getDegree()) {
            return false;
        }
        for (int i = 0; i < this.size; i++) {
            if (!this.coeff[i].equals(gF2nPolynomial.coeff[i])) {
                return false;
            }
        }
        return true;
    }

    public final GF2nPolynomial gcd(GF2nPolynomial gF2nPolynomial) throws RuntimeException, ArithmeticException {
        GF2nPolynomial gF2nPolynomial2 = new GF2nPolynomial(this);
        GF2nPolynomial gF2nPolynomial3 = new GF2nPolynomial(gF2nPolynomial);
        gF2nPolynomial2.shrink();
        gF2nPolynomial3.shrink();
        while (!gF2nPolynomial3.isZero()) {
            GF2nPolynomial remainder = gF2nPolynomial2.remainder(gF2nPolynomial3);
            gF2nPolynomial2 = gF2nPolynomial3;
            gF2nPolynomial3 = remainder;
        }
        return gF2nPolynomial2.scalarMultiply((GF2nElement) gF2nPolynomial2.coeff[gF2nPolynomial2.getDegree()].invert());
    }

    public final int getDegree() {
        for (int i = this.size - 1; i >= 0; i--) {
            if (!this.coeff[i].isZero()) {
                return i;
            }
        }
        return -1;
    }

    public int hashCode() {
        return getDegree() + this.coeff.hashCode();
    }

    public final boolean isZero() {
        int i = 0;
        while (i < this.size) {
            if (this.coeff[i] != null && !this.coeff[i].isZero()) {
                return false;
            }
            i++;
        }
        return true;
    }

    public final GF2nPolynomial multiply(GF2nPolynomial gF2nPolynomial) throws RuntimeException {
        int size = size();
        if (size != gF2nPolynomial.size()) {
            throw new IllegalArgumentException("PolynomialGF2n.multiply: this and b must have the same size!");
        }
        GF2nPolynomial gF2nPolynomial2 = new GF2nPolynomial((size << 1) - 1);
        for (int i = 0; i < size(); i++) {
            for (int i2 = 0; i2 < gF2nPolynomial.size(); i2++) {
                if (gF2nPolynomial2.coeff[i + i2] == null) {
                    gF2nPolynomial2.coeff[i + i2] = (GF2nElement) this.coeff[i].multiply(gF2nPolynomial.coeff[i2]);
                } else {
                    gF2nPolynomial2.coeff[i + i2] = (GF2nElement) gF2nPolynomial2.coeff[i + i2].add(this.coeff[i].multiply(gF2nPolynomial.coeff[i2]));
                }
            }
        }
        return gF2nPolynomial2;
    }

    public final GF2nPolynomial multiplyAndReduce(GF2nPolynomial gF2nPolynomial, GF2nPolynomial gF2nPolynomial2) throws RuntimeException, ArithmeticException {
        return multiply(gF2nPolynomial).reduce(gF2nPolynomial2);
    }

    public final GF2nPolynomial quotient(GF2nPolynomial gF2nPolynomial) throws RuntimeException, ArithmeticException {
        GF2nPolynomial[] gF2nPolynomialArr = new GF2nPolynomial[2];
        return divide(gF2nPolynomial)[0];
    }

    public final GF2nPolynomial reduce(GF2nPolynomial gF2nPolynomial) throws RuntimeException, ArithmeticException {
        return remainder(gF2nPolynomial);
    }

    public final GF2nPolynomial remainder(GF2nPolynomial gF2nPolynomial) throws RuntimeException, ArithmeticException {
        GF2nPolynomial[] gF2nPolynomialArr = new GF2nPolynomial[2];
        return divide(gF2nPolynomial)[1];
    }

    public final GF2nPolynomial scalarMultiply(GF2nElement gF2nElement) throws RuntimeException {
        GF2nPolynomial gF2nPolynomial = new GF2nPolynomial(size());
        for (int i = 0; i < size(); i++) {
            gF2nPolynomial.coeff[i] = (GF2nElement) this.coeff[i].multiply(gF2nElement);
        }
        return gF2nPolynomial;
    }

    public final void set(int i, GF2nElement gF2nElement) {
        if ((gF2nElement instanceof GF2nPolynomialElement) || (gF2nElement instanceof GF2nONBElement)) {
            this.coeff[i] = (GF2nElement) gF2nElement.clone();
            return;
        }
        throw new IllegalArgumentException("PolynomialGF2n.set f must be an instance of either GF2nPolynomialElement or GF2nONBElement!");
    }

    public final GF2nPolynomial shiftLeft(int i) {
        int i2 = 0;
        if (i <= 0) {
            return new GF2nPolynomial(this);
        }
        GF2nPolynomial gF2nPolynomial = new GF2nPolynomial(this.size + i, this.coeff[0]);
        gF2nPolynomial.assignZeroToElements();
        while (i2 < this.size) {
            gF2nPolynomial.coeff[i2 + i] = this.coeff[i2];
            i2++;
        }
        return gF2nPolynomial;
    }

    public final void shiftThisLeft(int i) {
        if (i > 0) {
            int i2 = this.size;
            GF2nField field = this.coeff[0].getField();
            enlarge(this.size + i);
            for (i2--; i2 >= 0; i2--) {
                this.coeff[i2 + i] = this.coeff[i2];
            }
            int i3;
            if (this.coeff[0] instanceof GF2nPolynomialElement) {
                for (i3 = i - 1; i3 >= 0; i3--) {
                    this.coeff[i3] = GF2nPolynomialElement.ZERO((GF2nPolynomialField) field);
                }
            } else if (this.coeff[0] instanceof GF2nONBElement) {
                for (i3 = i - 1; i3 >= 0; i3--) {
                    this.coeff[i3] = GF2nONBElement.ZERO((GF2nONBField) field);
                }
            }
        }
    }

    public final void shrink() {
        int i = this.size - 1;
        while (this.coeff[i].isZero() && i > 0) {
            i--;
        }
        i++;
        if (i < this.size) {
            Object obj = new GF2nElement[i];
            System.arraycopy(this.coeff, 0, obj, 0, i);
            this.coeff = obj;
            this.size = i;
        }
    }

    public final int size() {
        return this.size;
    }
}
