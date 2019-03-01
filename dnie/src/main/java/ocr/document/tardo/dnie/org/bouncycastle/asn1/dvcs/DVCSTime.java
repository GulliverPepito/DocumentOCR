package org.bouncycastle.asn1.dvcs;

import java.util.Date;
import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.cms.ContentInfo;

public class DVCSTime extends ASN1Object implements ASN1Choice {
    private ASN1GeneralizedTime genTime;
    private Date time;
    private ContentInfo timeStampToken;

    public DVCSTime(Date date) {
        this(new ASN1GeneralizedTime(date));
    }

    public DVCSTime(ASN1GeneralizedTime aSN1GeneralizedTime) {
        this.genTime = aSN1GeneralizedTime;
    }

    public DVCSTime(ContentInfo contentInfo) {
        this.timeStampToken = contentInfo;
    }

    public static DVCSTime getInstance(Object obj) {
        return obj instanceof DVCSTime ? (DVCSTime) obj : obj instanceof ASN1GeneralizedTime ? new DVCSTime(DERGeneralizedTime.getInstance(obj)) : obj != null ? new DVCSTime(ContentInfo.getInstance(obj)) : null;
    }

    public static DVCSTime getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(aSN1TaggedObject.getObject());
    }

    public ASN1GeneralizedTime getGenTime() {
        return this.genTime;
    }

    public ContentInfo getTimeStampToken() {
        return this.timeStampToken;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.genTime != null ? this.genTime : this.timeStampToken != null ? this.timeStampToken.toASN1Primitive() : null;
    }

    public String toString() {
        return this.genTime != null ? this.genTime.toString() : this.timeStampToken != null ? this.timeStampToken.toString() : null;
    }
}
