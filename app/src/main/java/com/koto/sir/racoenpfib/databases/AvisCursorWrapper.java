package com.koto.sir.racoenpfib.databases;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.koto.sir.RacoEnpFibApp;
import com.koto.sir.racoenpfib.models.Adjunt;
import com.koto.sir.racoenpfib.models.Avis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.koto.sir.racoenpfib.databases.DBSchema.*;

public class AvisCursorWrapper extends CursorWrapper {

    public AvisCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Avis getAvis() {
        String uuidString = getString(getColumnIndex(AvisosTable.Cols.UUID));
        String title = getString(getColumnIndex(AvisosTable.Cols.TITLE));
        String assignatura = getString(getColumnIndex(AvisosTable.Cols.ASSIGNATURA));
        String text = getString(getColumnIndex(AvisosTable.Cols.TEXT));
        long data_i = getLong(getColumnIndex(AvisosTable.Cols.DATA_INSERCIO));
        long data_m = getLong(getColumnIndex(AvisosTable.Cols.DATA_MODIFICACIO));
        long data_c = getLong(getColumnIndex(AvisosTable.Cols.DATA_CADUCITAT));
        int is_vist = getInt(getColumnIndex(AvisosTable.Cols.IS_VIST));

        Avis avis = new Avis(UUID.fromString(uuidString));
        avis.setText(text);
        avis.setAssignatura(assignatura);
        avis.setTitol(title);
        avis.setDataInsercio(new Date(data_i));
        avis.setDataModificacio(new Date(data_m));
        avis.setDataCaducitat(new Date(data_c));
        avis.setVist(is_vist != 0);


        List<Adjunt> adjuntList = AvisosLab.get(RacoEnpFibApp.getAppContext()).getAdjunts(avis.getUid());
        avis.setAdjunts(adjuntList);
        return avis;
    }
}
