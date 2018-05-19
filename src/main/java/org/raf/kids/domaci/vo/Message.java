package org.raf.kids.domaci.vo;

import java.io.Serializable;

public class Message implements Serializable {

    private long uId;
    private int traceId;
    private Object content;

    public Message(long uId, int traceId, Object content) {
        this.uId = uId;
        this.traceId = traceId;
        this.content = content;
    }

    public long getuId() {
        return uId;
    }

    public void setuId(long uId) {
        this.uId = uId;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public int getTraceId() {
        return traceId;
    }

    public void setTraceId(int traceId) {
        this.traceId = traceId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "uId=" + uId +
                ", traceId=" + traceId +
                ", content=" + content +
                '}';
    }
}
