package com.score.senz.pojos;

import com.score.senz.enums.SenzTypeEnum;

import java.util.HashMap;

/**
 * Created by eranga on 8/27/15.
 */
public class Senz {
    private String payload;
    private String signature;
    private SenzTypeEnum senzType;
    private String sender;
    private String receiver;
    private HashMap<String, String> attributes;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, String> attributes) {
        this.attributes = attributes;
    }

}
