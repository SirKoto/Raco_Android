package com.koto.sir.racoenpfib.databases;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.koto.sir.racoenpfib.models.Adjunt;

import java.util.Date;

public class AdjuntCursorWrapper extends CursorWrapper {


    public AdjuntCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Adjunt getAdjunt() {
        String nom = getString(getColumnIndex(DBSchema.AdjuntsTable.Cols.NAME));
        String url = getString(getColumnIndex(DBSchema.AdjuntsTable.Cols.URL));
        String mime = getString(getColumnIndex(DBSchema.AdjuntsTable.Cols.MIME));
        long data_m = getLong(getColumnIndex(DBSchema.AdjuntsTable.Cols.DATA_MODIFICACIO));

        Adjunt adjunt = new Adjunt();
        adjunt.setMimeType(mime);
        adjunt.setNom(nom);
        adjunt.setUrl(url);
        adjunt.setLast_modified(new Date(data_m));

        return adjunt;
    }
}