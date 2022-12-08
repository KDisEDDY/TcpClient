package com.ljy.tcpclientlib.packages;

import com.ljy.tcpclientlib.packages.bodyLinked.AbsBodyLinked;
import java.io.Serializable;
import java.nio.ByteBuffer;

public class TcpPackage implements Serializable {
    private HeadPackage mHeadPackage = null;

    private BodyPackage mBodyPackage = null;
    private String mkt;

    public String getMkt() {
        return mkt;
    }

    public void setMkt(String mkt) {
        this.mkt = mkt;
    }


    public TcpPackage() {
        mHeadPackage = new HeadPackage();
        mBodyPackage = new BodyPackage();
    }

    public int getPackageLength() {
        return mHeadPackage.LENGTH + mBodyPackage.getLength();
    }

    public void setHead(byte flags, short code, byte version, int bodyLength) {
        mHeadPackage.setAppFlags(flags);
        mHeadPackage.setProtocolCode(code);
        mHeadPackage.setProtocolVersion(version);
        mHeadPackage.setPackageBodyLength(bodyLength);
    }

    public void setBody(AbsBodyLinked linked) {
        mBodyPackage.setBodyBuffer(linked);
    }

    public ByteBuffer toByteBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(mHeadPackage.LENGTH + mBodyPackage.getLength());
        byteBuffer.put((ByteBuffer) mHeadPackage.toByteBuffer().flip());
        byteBuffer.position(mHeadPackage.LENGTH);
        if (mBodyPackage.toByteBuffer() != null) {
            byteBuffer.put((ByteBuffer) mBodyPackage.toByteBuffer().flip());
        }
        return byteBuffer;
    }

    public BodyPackage getBodyPackage() {
        return mBodyPackage;
    }

    public HeadPackage getHeadPackage() {
        return mHeadPackage;
    }

    public void setBodyPackage(BodyPackage bodyPackage) {
        mBodyPackage = bodyPackage;
    }

    public void setHeadPackage(HeadPackage headPackage) {
        mHeadPackage = headPackage;
    }

    @Override
    public String toString() {
        String result = "";
        result = "head:" + mHeadPackage + "  \tbody:" + mBodyPackage + "mkt" + getMkt();
        return result;
    }
}
