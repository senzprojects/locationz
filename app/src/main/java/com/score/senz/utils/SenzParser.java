package com.score.senz.utils;

import com.score.senz.enums.SenzTypeEnum;
import com.score.senz.pojos.Senz;

import java.util.HashMap;

/**
 * Created by eranga on 8/27/15.
 */
public class SenzParser {
    public static Senz parseSenz(String senzMessage) {
        Senz senz = new Senz();
        HashMap<String, String> attributes = new HashMap<>();

        // parse senz
        String[] tokens = senzMessage.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (i == 0) {
                // query type in first (SHARE, GET, DATA)
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
                if (token.equals("#time")) {
                    // #time 2453234
                    // we ignore #time attribute now
                    i++;
                } else {
                    if (senz.getSenzType() == SenzTypeEnum.DATA) {
                        // #lat 3.2343 #lon 4.3434
                        attributes.put(token, tokens[i + 1]);
                        i++;
                    } else {
                        // #lat #lon
                        attributes.put(token, "");
                    }
                }
            }
        }

        System.out.println(senz.getSender());
        System.out.println(senz.getReceiver());
        System.out.println(senz.getSenzType());
        System.out.println(senz.getSignature());
        System.out.println(attributes.entrySet());

        // verify digital signature
        //String senz = senzMessage.substring(0, senzMessage.lastIndexOf(" "));

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

    private void setSenzSender(String token, Senz senz) {
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
