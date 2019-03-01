package org.bouncycastle.crypto.tls;

import java.io.IOException;

class DTLSRecordLayer implements DatagramTransport {
    private static final int MAX_FRAGMENT_LENGTH = 16384;
    private static final int RECORD_HEADER_LENGTH = 13;
    private static final long RETRANSMIT_TIMEOUT = 240000;
    private static final long TCP_MSL = 120000;
    private volatile boolean closed = false;
    private final TlsContext context;
    private DTLSEpoch currentEpoch;
    private volatile ProtocolVersion discoveredPeerVersion = null;
    private volatile boolean failed = false;
    private volatile boolean inHandshake;
    private final TlsPeer peer;
    private DTLSEpoch pendingEpoch;
    private DTLSEpoch readEpoch;
    private final ByteQueue recordQueue = new ByteQueue();
    private DTLSHandshakeRetransmit retransmit = null;
    private DTLSEpoch retransmitEpoch = null;
    private long retransmitExpiry = 0;
    private final DatagramTransport transport;
    private DTLSEpoch writeEpoch;

    DTLSRecordLayer(DatagramTransport datagramTransport, TlsContext tlsContext, TlsPeer tlsPeer, short s) {
        this.transport = datagramTransport;
        this.context = tlsContext;
        this.peer = tlsPeer;
        this.inHandshake = true;
        this.currentEpoch = new DTLSEpoch(0, new TlsNullCipher(tlsContext));
        this.pendingEpoch = null;
        this.readEpoch = this.currentEpoch;
        this.writeEpoch = this.currentEpoch;
    }

    private void closeTransport() {
        if (!this.closed) {
            try {
                if (!this.failed) {
                    warn((short) 0, null);
                }
                this.transport.close();
            } catch (Exception e) {
            }
            this.closed = true;
        }
    }

    private static long getMacSequenceNumber(int i, long j) {
        return (((long) i) << 48) | j;
    }

    private void raiseAlert(short s, short s2, String str, Exception exception) throws IOException {
        this.peer.notifyAlertRaised(s, s2, str, exception);
        sendRecord((short) 21, new byte[]{(byte) s, (byte) s2}, 0, 2);
    }

    private int receiveRecord(byte[] bArr, int i, int i2, int i3) throws IOException {
        int readUint16;
        if (this.recordQueue.size() > 0) {
            if (this.recordQueue.size() >= 13) {
                byte[] bArr2 = new byte[2];
                this.recordQueue.read(bArr2, 0, 2, 11);
                readUint16 = TlsUtils.readUint16(bArr2, 0);
            } else {
                readUint16 = 0;
            }
            readUint16 = Math.min(this.recordQueue.size(), readUint16 + 13);
            this.recordQueue.read(bArr, i, readUint16, 0);
            this.recordQueue.removeData(readUint16);
            return readUint16;
        }
        int receive = this.transport.receive(bArr, i, i2, i3);
        if (receive >= 13) {
            readUint16 = TlsUtils.readUint16(bArr, i + 11) + 13;
            if (receive > readUint16) {
                this.recordQueue.addData(bArr, i + readUint16, receive - readUint16);
                return readUint16;
            }
        }
        return receive;
    }

    private void sendRecord(short s, byte[] bArr, int i, int i2) throws IOException {
        if (i2 >= 1 || s == (short) 23) {
            int epoch = this.writeEpoch.getEpoch();
            long allocateSequenceNumber = this.writeEpoch.allocateSequenceNumber();
            Object encodePlaintext = this.writeEpoch.getCipher().encodePlaintext(getMacSequenceNumber(epoch, allocateSequenceNumber), s, bArr, i, i2);
            if (encodePlaintext.length > 16384) {
                throw new TlsFatalAlert((short) 80);
            }
            Object obj = new byte[(encodePlaintext.length + 13)];
            TlsUtils.writeUint8(s, obj, 0);
            TlsUtils.writeVersion(this.discoveredPeerVersion != null ? this.discoveredPeerVersion : this.context.getClientVersion(), obj, 1);
            TlsUtils.writeUint16(epoch, obj, 3);
            TlsUtils.writeUint48(allocateSequenceNumber, obj, 5);
            TlsUtils.writeUint16(encodePlaintext.length, obj, 11);
            System.arraycopy(encodePlaintext, 0, obj, 13, encodePlaintext.length);
            this.transport.send(obj, 0, obj.length);
            return;
        }
        throw new TlsFatalAlert((short) 80);
    }

    public void close() throws IOException {
        if (!this.closed) {
            if (this.inHandshake) {
                warn((short) 90, "User canceled handshake");
            }
            closeTransport();
        }
    }

    void fail(short s) {
        if (!this.closed) {
            try {
                raiseAlert((short) 2, s, null, null);
            } catch (Exception e) {
            }
            this.failed = true;
            closeTransport();
        }
    }

    ProtocolVersion getDiscoveredPeerVersion() {
        return this.discoveredPeerVersion;
    }

    public int getReceiveLimit() throws IOException {
        return Math.min(16384, this.readEpoch.getCipher().getPlaintextLimit(this.transport.getReceiveLimit() - 13));
    }

    public int getSendLimit() throws IOException {
        return Math.min(16384, this.writeEpoch.getCipher().getPlaintextLimit(this.transport.getSendLimit() - 13));
    }

    void handshakeSuccessful(DTLSHandshakeRetransmit dTLSHandshakeRetransmit) {
        if (this.readEpoch == this.currentEpoch || this.writeEpoch == this.currentEpoch) {
            throw new IllegalStateException();
        }
        if (dTLSHandshakeRetransmit != null) {
            this.retransmit = dTLSHandshakeRetransmit;
            this.retransmitEpoch = this.currentEpoch;
            this.retransmitExpiry = System.currentTimeMillis() + RETRANSMIT_TIMEOUT;
        }
        this.inHandshake = false;
        this.currentEpoch = this.pendingEpoch;
        this.pendingEpoch = null;
    }

    void initPendingEpoch(TlsCipher tlsCipher) {
        if (this.pendingEpoch != null) {
            throw new IllegalStateException();
        }
        this.pendingEpoch = new DTLSEpoch(this.writeEpoch.getEpoch() + 1, tlsCipher);
    }

    public int receive(byte[] bArr, int i, int i2, int i3) throws IOException {
        byte[] bArr2 = null;
        while (true) {
            int min = Math.min(i2, getReceiveLimit()) + 13;
            if (bArr2 == null || bArr2.length < min) {
                bArr2 = new byte[min];
            }
            try {
                if (this.retransmit != null && System.currentTimeMillis() > this.retransmitExpiry) {
                    this.retransmit = null;
                    this.retransmitEpoch = null;
                }
                min = receiveRecord(bArr2, 0, min, i3);
                if (min >= 0) {
                    if (min >= 13 && min == TlsUtils.readUint16(bArr2, 11) + 13) {
                        short readUint8 = TlsUtils.readUint8(bArr2, 0);
                        switch (readUint8) {
                            case (short) 20:
                            case (short) 21:
                            case (short) 22:
                            case (short) 23:
                                int readUint16 = TlsUtils.readUint16(bArr2, 3);
                                DTLSEpoch dTLSEpoch = readUint16 == this.readEpoch.getEpoch() ? this.readEpoch : (readUint8 == (short) 22 && this.retransmitEpoch != null && readUint16 == this.retransmitEpoch.getEpoch()) ? this.retransmitEpoch : null;
                                if (dTLSEpoch != null) {
                                    long readUint48 = TlsUtils.readUint48(bArr2, 5);
                                    if (!dTLSEpoch.getReplayWindow().shouldDiscard(readUint48)) {
                                        ProtocolVersion readVersion = TlsUtils.readVersion(bArr2, 1);
                                        if (this.discoveredPeerVersion != null && !this.discoveredPeerVersion.equals(readVersion)) {
                                            break;
                                        }
                                        Object decodeCiphertext = dTLSEpoch.getCipher().decodeCiphertext(getMacSequenceNumber(dTLSEpoch.getEpoch(), readUint48), readUint8, bArr2, 13, min - 13);
                                        dTLSEpoch.getReplayWindow().reportAuthenticated(readUint48);
                                        if (this.discoveredPeerVersion == null) {
                                            this.discoveredPeerVersion = readVersion;
                                        }
                                        switch (readUint8) {
                                            case (short) 20:
                                                if (decodeCiphertext.length == 1 && decodeCiphertext[0] == (byte) 1 && this.pendingEpoch != null) {
                                                    this.readEpoch = this.pendingEpoch;
                                                    break;
                                                }
                                            case (short) 21:
                                                if (decodeCiphertext.length == 2) {
                                                    short s = (short) decodeCiphertext[0];
                                                    short s2 = (short) decodeCiphertext[1];
                                                    this.peer.notifyAlertReceived(s, s2);
                                                    if (s == (short) 2) {
                                                        fail(s2);
                                                        throw new TlsFatalAlert(s2);
                                                    } else if (s2 == (short) 0) {
                                                        closeTransport();
                                                        break;
                                                    } else {
                                                        continue;
                                                    }
                                                } else {
                                                    continue;
                                                }
                                            case (short) 22:
                                                if (this.inHandshake) {
                                                    break;
                                                } else if (this.retransmit != null) {
                                                    this.retransmit.receivedHandshakeRecord(readUint16, decodeCiphertext, 0, decodeCiphertext.length);
                                                    break;
                                                } else {
                                                    continue;
                                                }
                                            case (short) 23:
                                                if (!this.inHandshake) {
                                                    break;
                                                }
                                                continue;
                                            default:
                                                break;
                                        }
                                        if (!(this.inHandshake || this.retransmit == null)) {
                                            this.retransmit = null;
                                            this.retransmitEpoch = null;
                                        }
                                        System.arraycopy(decodeCiphertext, 0, bArr, i, decodeCiphertext.length);
                                        return decodeCiphertext.length;
                                    }
                                    continue;
                                } else {
                                    continue;
                                }
                            default:
                                break;
                        }
                    }
                }
                return min;
            } catch (IOException e) {
                throw e;
            }
        }
    }

    void resetWriteEpoch() {
        if (this.retransmitEpoch != null) {
            this.writeEpoch = this.retransmitEpoch;
        } else {
            this.writeEpoch = this.currentEpoch;
        }
    }

    public void send(byte[] bArr, int i, int i2) throws IOException {
        short s = (short) 23;
        if (this.inHandshake || this.writeEpoch == this.retransmitEpoch) {
            s = (short) 22;
            if (TlsUtils.readUint8(bArr, i) == (short) 20) {
                DTLSEpoch dTLSEpoch = null;
                if (this.inHandshake) {
                    dTLSEpoch = this.pendingEpoch;
                } else if (this.writeEpoch == this.retransmitEpoch) {
                    dTLSEpoch = this.currentEpoch;
                }
                if (dTLSEpoch == null) {
                    throw new IllegalStateException();
                }
                byte[] bArr2 = new byte[]{(byte) 1};
                sendRecord((short) 20, bArr2, 0, bArr2.length);
                this.writeEpoch = dTLSEpoch;
            }
        }
        sendRecord(s, bArr, i, i2);
    }

    void warn(short s, String str) throws IOException {
        raiseAlert((short) 1, s, str, null);
    }
}
