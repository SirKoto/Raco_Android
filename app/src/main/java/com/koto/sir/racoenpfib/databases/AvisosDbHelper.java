package com.koto.sir.racoenpfib.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.koto.sir.racoenpfib.databases.DBSchema.*;

public class AvisosDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "AvisosDbHelper";
    private static final int VERSION = 1;
    private static final String DB_NAME = "avisosBase.db";

    public AvisosDbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "Creating avisos Table");
        db.execSQL("create table " + AvisosTable.NAME + "(" +
                "_id integer primary key autoincrement, " +
                AvisosTable.Cols.UUID + ", " +
                AvisosTable.Cols.ASSIGNATURA + ", " +
                AvisosTable.Cols.TITLE + ", " +
                AvisosTable.Cols.TEXT + ", " +
                AvisosTable.Cols.DATA_INSERCIO + ", " +
                AvisosTable.Cols.DATA_MODIFICACIO + ", " +
                AvisosTable.Cols.DATA_CADUCITAT + ", " +
                AvisosTable.Cols.IS_VIST + ")"
        );

        Log.i(TAG, "Creating Adjunts Table");
        db.execSQL("create table " + AdjuntsTable.NAME + "(" +
                        "_id integer primary key autoincrement, " +
                        AdjuntsTable.Cols.UUID_FOREIGN + " not null, " +
                        AdjuntsTable.Cols.NAME + ", " +
                        AdjuntsTable.Cols.DATA_MODIFICACIO + ", " +
                        AdjuntsTable.Cols.MIME + ", " +
//                AdjuntsTable.Cols.TAMANY + ", " +
                        AdjuntsTable.Cols.URL + /*", " + //TOT AIXO ES PER FERHO AMB CLAUS FORANES... PERO NO SE SHO xD
                        "foreign key (" + AdjuntsTable.Cols.UUID_FOREIGN + ") " +
                        "references " + AvisosTable.NAME + "(" + AvisosTable.Cols.UUID + ")" +*/
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
