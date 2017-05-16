package com.example.asadkhan.kurrencyapp.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.asadkhan.kurrencyapp.data.database.KurrencyContract.HistoricalCurrencyTable;
import com.example.asadkhan.kurrencyapp.data.database.KurrencyContract.LiveCurrencyTable;

/**
 * Kreated by asadkhan on 08 | May |  2017 | at 9:22 PM.
 */

public class KurrencyDbHelper extends SQLiteOpenHelper {

    public KurrencyDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "currency.db";

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.e(DATABASE_NAME, "Creating new Database here. Version v" + DATABASE_VERSION);

        final String SQL_CREATE_LIVE_TABLE =
                "CREATE TABLE " +
                KurrencyContract.LiveCurrencyTable.TABLE_NAME + " (" +
                        LiveCurrencyTable._ID + " INTEGER PRIMARY KEY," +
                        LiveCurrencyTable.COLUMN_ENTRY_TYPE + " TEXT NOT NULL, " +
                        LiveCurrencyTable.COLUMN_TIMESTAMP + " BIGINT UNIQUE NOT NULL, " +
                        LiveCurrencyTable.COLUMN_USD_INR + " REAL NOT NULL, " +
                        LiveCurrencyTable.COLUMN_USD_EUR + " REAL NOT NULL, " +
                        LiveCurrencyTable.COLUMN_USD_AUD + " REAL NOT NULL, " +
                        LiveCurrencyTable.COLUMN_USD_GBP + " REAL NOT NULL, " +
                        LiveCurrencyTable.COLUMN_USD_JPY + " REAL NOT NULL, " +
                        LiveCurrencyTable.COLUMN_USD_CAD + " REAL NOT NULL ); ";

        final String SQL_CREATE_HISTORICAL_TABLE =
                "CREATE TABLE " +
                KurrencyContract.HistoricalCurrencyTable.TABLE_NAME + " (" +
                        HistoricalCurrencyTable._ID + " INTEGER PRIMARY KEY," +
                        HistoricalCurrencyTable.COLUMN_TIMESTAMP + " BIGINT UNIQUE NOT NULL, " +
                        HistoricalCurrencyTable.COLUMN_USD_INR + " REAL NOT NULL, " +
                        HistoricalCurrencyTable.COLUMN_USD_EUR + " REAL NOT NULL, " +
                        HistoricalCurrencyTable.COLUMN_USD_AUD + " REAL NOT NULL, " +
                        HistoricalCurrencyTable.COLUMN_USD_GBP + " REAL NOT NULL, " +
                        HistoricalCurrencyTable.COLUMN_USD_JPY + " REAL NOT NULL, " +
                        HistoricalCurrencyTable.COLUMN_USD_CAD + " REAL NOT NULL ); ";

        db.execSQL(SQL_CREATE_LIVE_TABLE);
        db.execSQL(SQL_CREATE_HISTORICAL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LiveCurrencyTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HistoricalCurrencyTable.TABLE_NAME);
        onCreate(db);
    }
}
