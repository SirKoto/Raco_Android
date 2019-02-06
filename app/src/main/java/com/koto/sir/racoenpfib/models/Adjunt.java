package com.koto.sir.racoenpfib.models;

import java.io.Serializable;
import java.util.Date;

public class Adjunt implements Serializable {
    private String mimeType;
    private String nom;
    private String url;
    private Date last_modified;

    @Override
    public String toString() {
        return "Adjunt{" +
                "mimeType='" + mimeType + '\'' +
                ", nom='" + nom + '\'' +
                ", url='" + url + '\'' +
                ", last_modified=" + last_modified +
                '}';
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getLast_modified() {
        return last_modified;
    }

    public void setLast_modified(Date last_modified) {
        this.last_modified = last_modified;
    }
}
