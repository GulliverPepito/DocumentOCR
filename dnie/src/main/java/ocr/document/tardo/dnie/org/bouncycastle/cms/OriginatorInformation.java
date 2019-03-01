package org.bouncycastle.cms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.cms.OriginatorInfo;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.CertificateList;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.CollectionStore;
import org.bouncycastle.util.Store;

public class OriginatorInformation {
    private OriginatorInfo originatorInfo;

    OriginatorInformation(OriginatorInfo originatorInfo) {
        this.originatorInfo = originatorInfo;
    }

    public Store getCRLs() {
        ASN1Set cRLs = this.originatorInfo.getCRLs();
        if (cRLs == null) {
            return new CollectionStore(new ArrayList());
        }
        Collection arrayList = new ArrayList(cRLs.size());
        Enumeration objects = cRLs.getObjects();
        while (objects.hasMoreElements()) {
            ASN1Primitive toASN1Primitive = ((ASN1Encodable) objects.nextElement()).toASN1Primitive();
            if (toASN1Primitive instanceof ASN1Sequence) {
                arrayList.add(new X509CRLHolder(CertificateList.getInstance(toASN1Primitive)));
            }
        }
        return new CollectionStore(arrayList);
    }

    public Store getCertificates() {
        ASN1Set certificates = this.originatorInfo.getCertificates();
        if (certificates == null) {
            return new CollectionStore(new ArrayList());
        }
        Collection arrayList = new ArrayList(certificates.size());
        Enumeration objects = certificates.getObjects();
        while (objects.hasMoreElements()) {
            ASN1Primitive toASN1Primitive = ((ASN1Encodable) objects.nextElement()).toASN1Primitive();
            if (toASN1Primitive instanceof ASN1Sequence) {
                arrayList.add(new X509CertificateHolder(Certificate.getInstance(toASN1Primitive)));
            }
        }
        return new CollectionStore(arrayList);
    }

    public OriginatorInfo toASN1Structure() {
        return this.originatorInfo;
    }
}
