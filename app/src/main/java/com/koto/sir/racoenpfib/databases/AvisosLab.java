package com.koto.sir.racoenpfib.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.koto.sir.racoenpfib.models.Adjunt;
import com.koto.sir.racoenpfib.models.Avis;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import static com.koto.sir.racoenpfib.databases.DBSchema.*;

public class AvisosLab {
    private static final String TAG = "AvisosLab";
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
    private static AvisosLab sAvisosLab;
    private SQLiteDatabase mDatabase;

    private AvisosLab(Context context) {
        mDatabase = new AvisosDbHelper(context).getWritableDatabase();
    }

    public static AvisosLab get(Context context) {
        if (sAvisosLab == null)
            sAvisosLab = new AvisosLab(context);
        return sAvisosLab;
    }

    private static ContentValues getContentValues(Avis avis) {
        ContentValues values = new ContentValues(8);

        values.put(AvisosTable.Cols.UUID, avis.getUid().toString());
        values.put(AvisosTable.Cols.TITLE, avis.getTitol());
        values.put(AvisosTable.Cols.ASSIGNATURA, avis.getAssignatura());
        values.put(AvisosTable.Cols.TEXT, avis.getText());
        values.put(AvisosTable.Cols.DATA_INSERCIO, avis.getDataInsercio().getTime());
        values.put(AvisosTable.Cols.DATA_MODIFICACIO, avis.getDataModificacio().getTime());
        values.put(AvisosTable.Cols.DATA_CADUCITAT, avis.getDataCaducitat().getTime());
        values.put(AvisosTable.Cols.IS_VIST, avis.isVist());
        return values;
    }

    private static ContentValues getContentValues(Adjunt adjunt, UUID uuid) {
        ContentValues values = new ContentValues();

        values.put(AdjuntsTable.Cols.NAME, adjunt.getNom());
        values.put(AdjuntsTable.Cols.MIME, adjunt.getMimeType());
        values.put(AdjuntsTable.Cols.URL, adjunt.getMimeType());
        values.put(AdjuntsTable.Cols.DATA_MODIFICACIO, adjunt.getLast_modified().getTime());

        values.put(AdjuntsTable.Cols.UUID_FOREIGN, uuid.toString());

        return values;
    }

    private static void parseAdjunt(Adjunt adjunt, JSONObject object) throws JSONException {
        String nom = object.getString("nom");
        String url = object.getString("url");
        String mime = object.getString("tipus_mime");
        Date data_mod = stringToDate(object.getString("data_modificacio"));

        adjunt.setLast_modified(data_mod);
        adjunt.setMimeType(mime);
        adjunt.setNom(nom);
        adjunt.setUrl(url);
    }

    public static void parseAvis(Avis avis, JSONObject object) throws JSONException {
        String titol = object.getString("titol");
        String assig = object.getString("codi_assig");
        String text = object.getString("text");
        Date date_i = stringToDate(object.getString("data_insercio"));
        Date date_m = stringToDate(object.getString("data_modificacio"));
        Date date_c = stringToDate(object.getString("data_caducitat"));

        JSONArray adjunts = object.getJSONArray("adjunts");
        int n = adjunts.length();
        List<Adjunt> adjuntList = new ArrayList<>(n);
        for (int i = 0; i < n; ++i) {
            Adjunt adjunt = new Adjunt();
            parseAdjunt(adjunt, adjunts.getJSONObject(i));
            adjuntList.add(adjunt);
        }
        avis.setAdjunts(adjuntList);
        avis.setTitol(titol);
        avis.setAssignatura(assig);
        avis.setText(text);
        avis.setDataModificacio(date_m);
        avis.setDataInsercio(date_i);
        avis.setDataCaducitat(date_c);
        avis.setVist(false);
    }

    public static Date stringToDate(String dateTxt) {
        try {
            return format.parse(dateTxt);
        } catch (ParseException e) {
            Log.e(TAG, "can't format date", e);
        }
        return new Date(0);
    }

    public void addAvis(Avis avis) {
        UUID uuid = avis.getUid();
        ContentValues values = getContentValues(avis);
        long i = mDatabase.insert(AvisosTable.NAME, null, values);
        if (i == -1) {
            Log.e(TAG, "Error a l'insert");
        }
        List<Adjunt> adjunts = avis.getAdjunts();
        for (Adjunt adjunt : adjunts) {
            values = getContentValues(adjunt, uuid);
            mDatabase.insert(AdjuntsTable.NAME, null, values);
        }

    }

    public List<Adjunt> getAdjunts(UUID uuid) {
        List<Adjunt> adjunts = new ArrayList<>();

        try (AdjuntCursorWrapper cursor = queryAdjunts(
                AdjuntsTable.Cols.UUID_FOREIGN + "= ?",
                new String[]{uuid.toString()},
                null
        )) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                adjunts.add(cursor.getAdjunt());
                cursor.moveToNext();
            }
        }

        return adjunts;

    }

    public void updateAvis(Avis avis) {
        String uuid = avis.getUid().toString();
        ContentValues values = getContentValues(avis);

        mDatabase.update(AvisosTable.NAME, values,
                AvisosTable.Cols.UUID + " = ?",
                new String[]{uuid});
    }

    public List<String> getNomAssigsAvisos() {
        List<String> strings = new ArrayList<>();

        try (Cursor cursor = (Cursor) mDatabase.query(
                true,
                AvisosTable.NAME,
                new String[]{AvisosTable.Cols.ASSIGNATURA},
                null,
                null, null, null, null, null)) {


            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                strings.add(cursor.getString(cursor.getColumnIndex(AvisosTable.Cols.ASSIGNATURA)));
                cursor.moveToNext();
            }
        }
        return strings;
    }

    public void deleteDuplicateRows() {
        try {
            mDatabase.rawQuery(
                    "delete from " + AvisosTable.NAME +
                            " where " + AvisosTable.Cols.UUID + " not in (" +
                            "select min(" + AvisosTable.Cols.UUID + ")" +
                            " from " + AvisosTable.NAME +
                            " group by " + AvisosTable.Cols.ASSIGNATURA + ", " +
                            AvisosTable.Cols.TITLE + ", " +
                            AvisosTable.Cols.DATA_MODIFICACIO +
                            ")", null).close();

            mDatabase.rawQuery("delete from " + AdjuntsTable.NAME +
                    " where " + AdjuntsTable.Cols.UUID_FOREIGN + " not in (" +
                    "select " + AvisosTable.Cols.UUID +
                    " from " + AvisosTable.NAME +
                    ")", null).close();


        } catch (Exception e) {
            Log.e(TAG, "Error a l'esborrar dades ", e);
        }
    }

    public List<Avis> getAvisos(@Nullable String assig, int n) {
        boolean b = assig == null;
        String whereClause;
        String[] whereArgs;
        if (b) {
            whereClause = null;
            whereArgs = null;
        } else {
            whereClause = AvisosTable.Cols.ASSIGNATURA + "= ?";
            whereArgs = new String[]{assig};
        }
        String limit = n <= 0 ? null : Integer.toString(n);

        List<Avis> avisos = new ArrayList<>();
        Log.d(TAG, "getAvisos " + assig + " limit: " + limit + "where " + whereClause);
        try (AvisCursorWrapper cursor = queryAvisos(whereClause, whereArgs,
                AvisosTable.Cols.DATA_MODIFICACIO + " desc", limit)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                avisos.add(cursor.getAvis());
                cursor.moveToNext();
            }
        }
        return avisos;
    }


    public Avis getAvis(UUID uuid) {
        Avis avis = null;

        try (AvisCursorWrapper cursor = queryAvisos(
                AvisosTable.Cols.UUID + " = ?",
                new String[]{uuid.toString()},
                null
        )) {
            cursor.moveToFirst();
            if(!cursor.isAfterLast())
                avis = cursor.getAvis();
        }

        return avis;
    }

    public List<Avis> getAvisos(@Nullable String assig) {
        return getAvisos(assig, -1);
    }

    private AvisCursorWrapper queryAvisos(String whereClause, String[] whereArgs, @Nullable String orderBy) {
        return queryAvisos(whereClause, whereArgs, orderBy, null);
    }

    private AvisCursorWrapper queryAvisos(String whereClause, String[] whereArgs,
                                          @Nullable String orderBy, @Nullable String limit) {
        Cursor cursor = mDatabase.query(
                AvisosTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,//AvisosTable.Cols.ASSIGNATURA,
                null,
                orderBy, //AvisosTable.Cols.DATA_MODIFICACIO + "desc"
                limit
        );

        return new AvisCursorWrapper(cursor);
    }

    private AdjuntCursorWrapper queryAdjunts(String whereClause, String[] whereArgs,
                                             @Nullable String orderBy) {
        Cursor cursor = mDatabase.query(
                AdjuntsTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                orderBy
        );

        return new AdjuntCursorWrapper(cursor);
    }
}
