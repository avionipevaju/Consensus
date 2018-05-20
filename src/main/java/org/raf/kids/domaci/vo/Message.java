package org.raf.kids.domaci.vo;

import java.io.Serializable;
import java.util.Random;

public class Message implements Serializable {

    private long uId;
    private MessageType messageType;
    private int traceId;
    private Object content;

    public Message(int traceId, MessageType type, Object content) {
        this.uId = new Random().nextInt();
        this.traceId = traceId;
        this.messageType = type;
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

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public void setTraceId(int traceId) {
        this.traceId = traceId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "uId=" + uId +
                ", messageType=" + messageType +
                ", traceId=" + traceId +
                ", content=" + content +
                '}';
    }
}
