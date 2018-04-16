package com.example.user.bustrackingwithpresence;

/**
 * Created by User on 3/26/2018.
 */

public class Teacking {

    String eMail,uID,lat,lng;

    public Teacking() {
    }

    public Teacking(String eMail, String uID, String lat, String lng) {
        this.eMail = eMail;
        this.uID = uID;
        this.lat = lat;
        this.lng = lng;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
