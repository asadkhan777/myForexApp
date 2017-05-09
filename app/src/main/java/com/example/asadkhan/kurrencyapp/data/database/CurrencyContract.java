package com.example.asadkhan.kurrencyapp.data.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;


/**
 * Kreated by asadkhan on 08 | May |  2017 | at 7:20 PM.
 */

public class CurrencyContract {

    public static final String CONTENT_AUTHORITY = "com.example.asadkhan.kurrencyapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_LIVE = "live";

    public static final String PATH_HISTORICAL = "historical";

    public static final String[] ENTRY_TYPES = {
            "MORNING",
            "NOON",
            "EVENING"
    };

    public static final class LiveCurrencyTable implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LIVE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LIVE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LIVE;

        // Table name
        public static final String TABLE_NAME = "live";

        /* Columns :

        _ID
        TIME_STAMP
        ENTRY_TYPE
        USD_EUR
        USD_JPY
        USD_GBP
        USD_AUD
        USD_CAD
        USD_INR

        */

        // check this for TS > https://stackoverflow.com/a/11556717/7768690
        public static final String COLUMN_TIMESTAMP = "TIME_STAMP";

        public static final String COLUMN_ENTRY_TYPE = "ENTRY_TYPE";

        public static final String COLUMN_USD_EUR = "USD_EUR";
                                            
        public static final String COLUMN_USD_JPY = "USD_JPY";
                                            
        public static final String COLUMN_USD_GBP = "USD_GBP";
                                            
        public static final String COLUMN_USD_AUD = "USD_AUD";
                                            
        public static final String COLUMN_USD_CAD = "USD_CAD";
                                            
        public static final String COLUMN_USD_INR = "USD_INR";

        public static Uri buildLiveUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class HistoricalCurrencyTable implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_HISTORICAL).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HISTORICAL;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HISTORICAL;

        // Table name
        public static final String TABLE_NAME = "historical";

        /* Columns :

        _ID
        TIME_STAMP
        USD_EUR
        USD_JPY
        USD_GBP
        USD_AUD
        USD_CAD
        USD_INR

        */

        public static final String COLUMN_TIMESTAMP = "TIME_STAMP";

        public static final String COLUMN_USD_EUR = "USD_EUR";

        public static final String COLUMN_USD_JPY = "USD_JPY";

        public static final String COLUMN_USD_GBP = "USD_GBP";

        public static final String COLUMN_USD_AUD = "USD_AUD";

        public static final String COLUMN_USD_CAD = "USD_CAD";

        public static final String COLUMN_USD_INR = "USD_INR";

        /*
        *   URI section
        * */

        static String atDateParam = "atdate";
        static String fromDateparam = "fromdate";

        public static Uri buildHistoricalUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildHistoricalAtDate(long atDate) {
//            atDate = (new Date().getTime() / 1000) - 60*60*24 ;
            return CONTENT_URI
                    .buildUpon()
                    .appendQueryParameter(atDateParam, Long.toString(atDate))
                    .build();
        }

        public static Uri buildHistoricalFromDate(long fromDate) {
//            atDate = (new Date().getTime() / 1000) - 60*60*24 ;
            return CONTENT_URI
                    .buildUpon()
                    .appendQueryParameter(fromDateparam, Long.toString(fromDate))
                    .build();
        }

        public static long getAtDateFromUri(Uri uri) {
            String param = uri.getQueryParameter(atDateParam);
            if ( param != null && param.length() > 0 ) {
                return Long.parseLong(param);
            } else {
                return 0;
            }
        }

        public static long getFromDateFromUri(Uri uri) {
            String param = uri.getQueryParameter(fromDateparam);
            if ( param != null && param.length() > 0 ) {
                return Long.parseLong(param);
            } else {
                return 0;
            }
        }
    }

}
