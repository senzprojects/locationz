package com.score.senz.utils;

import com.score.senz.enums.SenzTypeEnum;
import com.score.senz.pojos.Senz;

import java.util.HashMap;

/**
 * Created by eranga on 8/27/15.
 */
public class SenzParser {
    public static Senz parseSenz(String senzMessage) {
        // init sez with empty attributes
        Senz senz = new Senz();
        senz.setAttributes(new HashMap<String, String>());

        // parse senz
        String[] tokens = senzMessage.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (i == 0) {
                // query type at first (SHARE, GET, DATA)
                senz.setSenzType(SenzTypeEnum.valueOf(token.toUpperCase()));
            } else if (i == tokens.length - 1) {
                // signature at the end
                senz.setSignature(token);
            } else if (tokens[i].startsWith("@")) {
                // @0775432012
                senz.setReceiver(token.substring(1));
            } else if (token.startsWith("^")) {
                // ^senz, ^0775432015
                senz.setSender(token.substring(1));
            } else if (token.startsWith("#")) {
                if (token.equals("#time") || token.equals("pubkey")) {
                    // #time 2453234, #pubkey ac23edf432fdg
                    senz.getAttributes().put(token, tokens[i + 1]);
                    i++;
                } else {
                    if (senz.getSenzType() == SenzTypeEnum.DATA) {
                        // #lat 3.2343 #lon 4.3434
                        senz.getAttributes().put(token, tokens[i + 1]);
                        i++;
                    } else {
                        // #lat #lon
                        senz.getAttributes().put(token, "");
                    }
                }
            }
        }

        System.out.println(senz.getSender());
        System.out.println(senz.getReceiver());
        System.out.println(senz.getSenzType());
        System.out.println(senz.getSignature());
        System.out.println(senz.getAttributes().entrySet());

        // verify digital signature
        //String senz = senzMessage.substring(0, senzMessage.lastIndexOf(" "));
        //String senzSignature = senzMessage.substring(senzMessage.lastIndexOf(" ") + 1);

        return null;
    }

    public static String getSenzMessage(Senz senz) {
        return null;
    }

    private void setQueryType(String senzMessage, Senz senz) {
        // query type in first (SHARE, GET, DATA)
        String senzType = senzMessage.substring(0, senzMessage.indexOf(" "));
        senz.setSenzType(SenzTypeEnum.valueOf(senzType.toUpperCase()));
    }

    private void getSenzSignature(String senzMessage, Senz senz) {
        senz.setSignature(senzMessage.substring(senzMessage.lastIndexOf(" ") + 1));
    }

    private void setSenzSender(String senzMessage, Senz senz) {
        String s = senzMessage.substring(senzMessage.indexOf("@"), senzMessage.)
        String sender = token.substring(1);
        senz.setSender(sender);
    }

    private void setSenzReceiver(String token, Senz senz) {
        String sender = token.substring(1);
        senz.setSender(sender);
    }


    public static void main(String args[]) {
        String senzMessage1 = "DATA" + " " +
                "#pubkey" + " " + "keyyyyy" + " " +
                "#time" + " " + "timestamp" + " " +
                "@senz" + " " +
                "^0775432015" + " " +
                "signatureeee";

        String senzMessage2 = "SHARE" + " " +
                "#lat" + " " +
                "#lon" + " " +
                "#time" + " " + "timestamp" + " " +
                "@senz" + " " +
                "^0775432015" + " " +
                "signatureeee";

        parseSenz(senzMessage1);
        parseSenz(senzMessage2);

    }
}
