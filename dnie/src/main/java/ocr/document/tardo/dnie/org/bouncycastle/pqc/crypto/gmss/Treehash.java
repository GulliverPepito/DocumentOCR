package org.bouncycastle.pqc.crypto.gmss;

import java.lang.reflect.Array;
import java.util.Vector;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.pqc.crypto.gmss.util.GMSSRandom;
import org.bouncycastle.util.Integers;
import org.bouncycastle.util.encoders.Hex;

public class Treehash {
    private byte[] firstNode;
    private int firstNodeHeight;
    private Vector heightOfNodes;
    private boolean isFinished;
    private boolean isInitialized;
    private int maxHeight;
    private Digest messDigestTree;
    private byte[] seedActive;
    private boolean seedInitialized;
    private byte[] seedNext;
    private int tailLength;
    private Vector tailStack;

    public Treehash(Vector vector, int i, Digest digest) {
        this.tailStack = vector;
        this.maxHeight = i;
        this.firstNode = null;
        this.isInitialized = false;
        this.isFinished = false;
        this.seedInitialized = false;
        this.messDigestTree = digest;
        this.seedNext = new byte[this.messDigestTree.getDigestSize()];
        this.seedActive = new byte[this.messDigestTree.getDigestSize()];
    }

    public Treehash(Digest digest, byte[][] bArr, int[] iArr) {
        int i = 0;
        this.messDigestTree = digest;
        this.maxHeight = iArr[0];
        this.tailLength = iArr[1];
        this.firstNodeHeight = iArr[2];
        if (iArr[3] == 1) {
            this.isFinished = true;
        } else {
            this.isFinished = false;
        }
        if (iArr[4] == 1) {
            this.isInitialized = true;
        } else {
            this.isInitialized = false;
        }
        if (iArr[5] == 1) {
            this.seedInitialized = true;
        } else {
            this.seedInitialized = false;
        }
        this.heightOfNodes = new Vector();
        for (int i2 = 0; i2 < this.tailLength; i2++) {
            this.heightOfNodes.addElement(Integers.valueOf(iArr[i2 + 6]));
        }
        this.firstNode = bArr[0];
        this.seedActive = bArr[1];
        this.seedNext = bArr[2];
        this.tailStack = new Vector();
        while (i < this.tailLength) {
            this.tailStack.addElement(bArr[i + 3]);
            i++;
        }
    }

    public void destroy() {
        this.isInitialized = false;
        this.isFinished = false;
        this.firstNode = null;
        this.tailLength = 0;
        this.firstNodeHeight = -1;
    }

    public byte[] getFirstNode() {
        return this.firstNode;
    }

    public int getFirstNodeHeight() {
        return this.firstNode == null ? this.maxHeight : this.firstNodeHeight;
    }

    public int getLowestNodeHeight() {
        return this.firstNode == null ? this.maxHeight : this.tailLength == 0 ? this.firstNodeHeight : Math.min(this.firstNodeHeight, ((Integer) this.heightOfNodes.lastElement()).intValue());
    }

    public byte[] getSeedActive() {
        return this.seedActive;
    }

    public byte[][] getStatByte() {
        byte[][] bArr = (byte[][]) Array.newInstance(Byte.TYPE, new int[]{this.tailLength + 3, this.messDigestTree.getDigestSize()});
        bArr[0] = this.firstNode;
        bArr[1] = this.seedActive;
        bArr[2] = this.seedNext;
        for (int i = 0; i < this.tailLength; i++) {
            bArr[i + 3] = (byte[]) this.tailStack.elementAt(i);
        }
        return bArr;
    }

    public int[] getStatInt() {
        int[] iArr = new int[(this.tailLength + 6)];
        iArr[0] = this.maxHeight;
        iArr[1] = this.tailLength;
        iArr[2] = this.firstNodeHeight;
        if (this.isFinished) {
            iArr[3] = 1;
        } else {
            iArr[3] = 0;
        }
        if (this.isInitialized) {
            iArr[4] = 1;
        } else {
            iArr[4] = 0;
        }
        if (this.seedInitialized) {
            iArr[5] = 1;
        } else {
            iArr[5] = 0;
        }
        for (int i = 0; i < this.tailLength; i++) {
            iArr[i + 6] = ((Integer) this.heightOfNodes.elementAt(i)).intValue();
        }
        return iArr;
    }

    public Vector getTailStack() {
        return this.tailStack;
    }

    public void initialize() {
        if (this.seedInitialized) {
            this.heightOfNodes = new Vector();
            this.tailLength = 0;
            this.firstNode = null;
            this.firstNodeHeight = -1;
            this.isInitialized = true;
            System.arraycopy(this.seedNext, 0, this.seedActive, 0, this.messDigestTree.getDigestSize());
            return;
        }
        System.err.println("Seed " + this.maxHeight + " not initialized");
    }

    public void initializeSeed(byte[] bArr) {
        System.arraycopy(bArr, 0, this.seedNext, 0, this.messDigestTree.getDigestSize());
        this.seedInitialized = true;
    }

    public void setFirstNode(byte[] bArr) {
        if (!this.isInitialized) {
            initialize();
        }
        this.firstNode = bArr;
        this.firstNodeHeight = this.maxHeight;
        this.isFinished = true;
    }

    public String toString() {
        int i = 0;
        String str = "Treehash    : ";
        for (int i2 = 0; i2 < this.tailLength + 6; i2++) {
            str = str + getStatInt()[i2] + " ";
        }
        while (i < this.tailLength + 3) {
            str = getStatByte()[i] != null ? str + new String(Hex.encode(getStatByte()[i])) + " " : str + "null ";
            i++;
        }
        return str + "  " + this.messDigestTree.getDigestSize();
    }

    public void update(GMSSRandom gMSSRandom, byte[] bArr) {
        if (this.isFinished) {
            System.err.println("No more update possible for treehash instance!");
        } else if (this.isInitialized) {
            byte[] bArr2 = new byte[this.messDigestTree.getDigestSize()];
            gMSSRandom.nextSeed(this.seedActive);
            if (this.firstNode == null) {
                this.firstNode = bArr;
                this.firstNodeHeight = 0;
            } else {
                Object obj;
                Object obj2;
                int i = 0;
                while (this.tailLength > 0 && i == ((Integer) this.heightOfNodes.lastElement()).intValue()) {
                    obj = new byte[(this.messDigestTree.getDigestSize() << 1)];
                    System.arraycopy(this.tailStack.lastElement(), 0, obj, 0, this.messDigestTree.getDigestSize());
                    this.tailStack.removeElementAt(this.tailStack.size() - 1);
                    this.heightOfNodes.removeElementAt(this.heightOfNodes.size() - 1);
                    System.arraycopy(obj2, 0, obj, this.messDigestTree.getDigestSize(), this.messDigestTree.getDigestSize());
                    this.messDigestTree.update(obj, 0, obj.length);
                    obj2 = new byte[this.messDigestTree.getDigestSize()];
                    this.messDigestTree.doFinal(obj2, 0);
                    int i2 = i + 1;
                    this.tailLength--;
                    i = i2;
                }
                this.tailStack.addElement(obj2);
                this.heightOfNodes.addElement(Integers.valueOf(i));
                this.tailLength++;
                if (((Integer) this.heightOfNodes.lastElement()).intValue() == this.firstNodeHeight) {
                    obj = new byte[(this.messDigestTree.getDigestSize() << 1)];
                    System.arraycopy(this.firstNode, 0, obj, 0, this.messDigestTree.getDigestSize());
                    System.arraycopy(this.tailStack.lastElement(), 0, obj, this.messDigestTree.getDigestSize(), this.messDigestTree.getDigestSize());
                    this.tailStack.removeElementAt(this.tailStack.size() - 1);
                    this.heightOfNodes.removeElementAt(this.heightOfNodes.size() - 1);
                    this.messDigestTree.update(obj, 0, obj.length);
                    this.firstNode = new byte[this.messDigestTree.getDigestSize()];
                    this.messDigestTree.doFinal(this.firstNode, 0);
                    this.firstNodeHeight++;
                    this.tailLength = 0;
                }
            }
            if (this.firstNodeHeight == this.maxHeight) {
                this.isFinished = true;
            }
        } else {
            System.err.println("Treehash instance not initialized before update");
        }
    }

    public void updateNextSeed(GMSSRandom gMSSRandom) {
        gMSSRandom.nextSeed(this.seedNext);
    }

    public boolean wasFinished() {
        return this.isFinished;
    }

    public boolean wasInitialized() {
        return this.isInitialized;
    }
}
