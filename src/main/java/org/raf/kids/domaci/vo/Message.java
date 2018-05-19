package org.raf.kids.domaci.vo;

import java.io.Serializable;

public class Message implements Serializable {

    private long uId;
    private Object content;

    public Message(long uId, Object content) {
        this.uId = uId;
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

    @Override
    public String toString() {
        return "Message{" +
                "uId=" + uId +
                ", content=" + content +
                '}';
    }
}
