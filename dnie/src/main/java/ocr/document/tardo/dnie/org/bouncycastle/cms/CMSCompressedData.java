package org.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.cms.CompressedData;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.operator.InputExpanderProvider;

public class CMSCompressedData {
    CompressedData comData;
    ContentInfo contentInfo;

    public CMSCompressedData(InputStream inputStream) throws CMSException {
        this(CMSUtils.readContentInfo(inputStream));
    }

    public CMSCompressedData(ContentInfo contentInfo) throws CMSException {
        this.contentInfo = contentInfo;
        try {
            this.comData = CompressedData.getInstance(contentInfo.getContent());
        } catch (Exception e) {
            throw new CMSException("Malformed content.", e);
        } catch (Exception e2) {
            throw new CMSException("Malformed content.", e2);
        }
    }

    public CMSCompressedData(byte[] bArr) throws CMSException {
        this(CMSUtils.readContentInfo(bArr));
    }

    public byte[] getContent() throws CMSException {
        try {
            return CMSUtils.streamToByteArray(new InflaterInputStream(((ASN1OctetString) this.comData.getEncapContentInfo().getContent()).getOctetStream()));
        } catch (Exception e) {
            throw new CMSException("exception reading compressed stream.", e);
        }
    }

    public byte[] getContent(int i) throws CMSException {
        try {
            return CMSUtils.streamToByteArray(new InflaterInputStream(((ASN1OctetString) this.comData.getEncapContentInfo().getContent()).getOctetStream()), i);
        } catch (Exception e) {
            throw new CMSException("exception reading compressed stream.", e);
        }
    }

    public byte[] getContent(InputExpanderProvider inputExpanderProvider) throws CMSException {
        try {
            return CMSUtils.streamToByteArray(inputExpanderProvider.get(this.comData.getCompressionAlgorithmIdentifier()).getInputStream(((ASN1OctetString) this.comData.getEncapContentInfo().getContent()).getOctetStream()));
        } catch (Exception e) {
            throw new CMSException("exception reading compressed stream.", e);
        }
    }

    public ContentInfo getContentInfo() {
        return this.contentInfo;
    }

    public ASN1ObjectIdentifier getContentType() {
        return this.contentInfo.getContentType();
    }

    public byte[] getEncoded() throws IOException {
        return this.contentInfo.getEncoded();
    }

    public ContentInfo toASN1Structure() {
        return this.contentInfo;
    }
}
