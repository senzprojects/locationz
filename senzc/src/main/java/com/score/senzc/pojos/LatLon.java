package com.score.senzc.pojos;

/**
 * Created with IntelliJ IDEA.
 * User: eranga
 * Date: 10/22/13
 * Time: 10:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class LatLon {
    String lat;
    String lon;

    public LatLon(String lat, String lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }
}
