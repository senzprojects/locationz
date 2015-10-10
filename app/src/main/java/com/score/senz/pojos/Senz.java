package com.score.senz.pojos;

import com.score.senz.enums.SenzTypeEnum;

import java.util.HashMap;

/**
 * Created by eranga on 8/27/15.
 */
public class Senz {
    private String signature;
    private SenzTypeEnum senzType;
    private User sender;
    private User receiver;
    private HashMap<String, String> attributes;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public SenzTypeEnum getSenzType() {
        return senzType;
    }

    public void setSenzType(SenzTypeEnum senzType) {
        this.senzType = senzType;
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, String> attributes) {
        this.attributes = attributes;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }
}
