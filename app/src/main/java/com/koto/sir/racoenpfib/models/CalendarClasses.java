package com.koto.sir.racoenpfib.models;

import java.util.Objects;

public class CalendarClasses {
    private int dia;
    private int durada;
    private String codiAssig;
    private String grup;
    private String tipus;
    private String aules;
    private int inici;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarClasses classes = (CalendarClasses) o;
        return dia == classes.dia &&
                durada == classes.durada &&
                inici == classes.inici &&
                Objects.equals(codiAssig, classes.codiAssig) &&
                Objects.equals(grup, classes.grup) &&
                Objects.equals(tipus, classes.tipus) &&
                Objects.equals(aules, classes.aules);
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getDurada() {
        return durada;
    }

    public void setDurada(int durada) {
        this.durada = durada;
    }

    public String getCodiAssig() {
        return codiAssig;
    }

    public void setCodiAssig(String codiAssig) {
        this.codiAssig = codiAssig;
    }

    public String getGrup() {
        return grup;
    }

    public void setGrup(String grup) {
        this.grup = grup;
    }

    public String getTipus() {
        return tipus;
    }

    public void setTipus(String tipus) {
        this.tipus = tipus;
    }

    public String getAules() {
        return aules;
    }

    public void setAules(String aules) {
        this.aules = aules;
    }

    public int getInici() {
        return inici;
    }

    public void setInici(String inici) {
        this.inici = Integer.valueOf(inici.substring(0, 2));
    }
}
