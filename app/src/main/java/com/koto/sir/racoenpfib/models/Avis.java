package com.koto.sir.racoenpfib.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Avis implements Serializable {
    private UUID mUid;
    private String titol;
    private String assignatura;
    private String text;
    private Date dataCaducitat;
    private Date dataInsercio;
    private Date dataModificacio;
    private List<Adjunt> mAdjunts;
    private boolean mVist;

    public Avis(UUID uuid) {
        mAdjunts = new ArrayList<>(0);
        mUid = uuid;
        dataCaducitat = new Date();
        dataInsercio = new Date();
        dataModificacio = new Date();
    }

    public boolean isVist() {
        return mVist;
    }

    public void setVist(boolean vist) {
        mVist = vist;
    }

    public UUID getUid() {
        return mUid;
    }

    public void setUid(UUID uid) {
        mUid = uid;
    }

    public String getTitol() {
        return titol;
    }

    public void setTitol(String titol) {
        this.titol = titol;
    }

    public String getAssignatura() {
        return assignatura;
    }

    public void setAssignatura(String assignatura) {
        this.assignatura = assignatura;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Adjunt> getAdjunts() {
        return mAdjunts;
    }

    public void setAdjunts(List<Adjunt> adjunts) {
        mAdjunts = adjunts;
    }

    public Date getDataCaducitat() {
        return dataCaducitat;
    }

    public void setDataCaducitat(Date dataCaducitat) {
        this.dataCaducitat = dataCaducitat;
    }

    public Date getDataInsercio() {
        return dataInsercio;
    }

    public void setDataInsercio(Date dataInsercio) {
        this.dataInsercio = dataInsercio;
    }

    public Date getDataModificacio() {
        return dataModificacio;
    }

    public void setDataModificacio(Date dataModificacio) {
        this.dataModificacio = dataModificacio;
    }
}
