package com.score.senzc.pojos;

/**
 * Keep country attributes
 *
 * @author eranga herath(eranga.herath@gmail.com)
 */
public class Country {
    String name;
    String code;

    public Country(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
