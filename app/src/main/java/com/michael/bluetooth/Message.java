package com.michael.bluetooth;

import java.io.Serializable;

public class Message implements Serializable
{
    private MyDate timeSend;
    private String senderId;
    private String senderName;
    private String msg;

    public Message(String senderId, String senderName, String msg)
    {
        this.timeSend = new MyDate();
        this.senderId = senderId;
        this.senderName = senderName;
        this.msg = msg;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMsg() {
        return msg;
    }

    public MyDate getTimeSend() {
        return timeSend;
    }

    @Override
    public String toString() {
        return "[" + timeSend + "] " + senderName + ": " + msg + "\n";
    }
}
