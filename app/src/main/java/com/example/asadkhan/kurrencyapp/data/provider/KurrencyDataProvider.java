package com.example.asadkhan.kurrencyapp.data.provider;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.asadkhan.kurrencyapp.data.database.KurrencyContract;
import com.example.asadkhan.kurrencyapp.data.database.KurrencyDbHelper;

/**
 * Kreated by asadkhan on 09 | May |  2017 | at 6:06 PM.
 */

public class KurrencyDataProvider extends ContentProvider {

    private final String LOG_TAG = KurrencyDataProvider.class.getSimpleName();

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private KurrencyDbHelper mOpenHelper;

    static final int CURRENT_RATES = 100;
    static final int RATE_AT_DATE = 101;
    static final int RATE_FROM_DATE = 102;

    private static SQLiteQueryBuilder sjdnsj;

    @Override
    public boolean onCreate() {
        mOpenHelper = new KurrencyDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case CURRENT_RATES:
                return KurrencyContract.LiveCurrencyTable.CONTENT_ITEM_TYPE;

            case RATE_AT_DATE:
                return KurrencyContract.LiveCurrencyTable.CONTENT_ITEM_TYPE;

            case RATE_FROM_DATE:
                return KurrencyContract.LiveCurrencyTable.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown URI : " + uri);
        }

    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor returnCursor;
        switch (sUriMatcher.match(uri)) {

            case CURRENT_RATES:
            {
                returnCursor = getLiveKurrency(projection, selection, selectionArgs, sortOrder);
                break;
            }

            case RATE_AT_DATE:
            {
                returnCursor = getHistoricalKurrencyAtDate(uri, projection, sortOrder);
                break;
            }

            case RATE_FROM_DATE:
            {
                returnCursor = getHistoricalKurrencyFromDate(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        setCursorUriNotify(returnCursor, uri);
        return returnCursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Log.e(LOG_TAG, "DBG 1 \n" + uri.toString());
        final SQLiteDatabase mySQLiteDB = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;
        long _id;

        switch (match){
            case CURRENT_RATES:{
                // Log.e(LOG_TAG, "DBG 2 \n" + match);
                try {
                    _id = mySQLiteDB.insert(KurrencyContract.LiveCurrencyTable.TABLE_NAME, null, values);
                    if ( _id > 0 )
                        returnUri = KurrencyContract.LiveCurrencyTable.buildLiveUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Something went wrong");
                }
                break;
            }
            case RATE_AT_DATE: {
                // Log.e(LOG_TAG, "DBG 2 " + match);
                _id = mySQLiteDB.insert(KurrencyContract.HistoricalCurrencyTable.TABLE_NAME, null, values);
                // Log.e(LOG_TAG, "DBG 3 " + match);
                if ( _id > 0 )
                    returnUri = KurrencyContract.HistoricalCurrencyTable.buildHistoricalUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if ( contentResolverNotNull() ) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase mySQLiteDB = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        int deletions;

        if ( selection == null ) { selection = "1"; }
        switch (match){
            case CURRENT_RATES:{
                deletions = mySQLiteDB.delete(KurrencyContract.LiveCurrencyTable.TABLE_NAME,
                        selection,
                        selectionArgs);
                if ( deletions == 1 ){
                    Log.e(LOG_TAG, "Deleted "+ deletions +" row successfully");
                } else {
                    Log.e(LOG_TAG, "Deleted " + deletions + " rows successfully");
                }
                break;
            }
            case RATE_AT_DATE:{
                deletions = mySQLiteDB.delete(KurrencyContract.HistoricalCurrencyTable.TABLE_NAME,
                        selection,
                        selectionArgs);
                if ( deletions == 1 ){
                    Log.e(LOG_TAG, "Deleted "+ deletions +" row successfully");
                } else {
                    Log.e(LOG_TAG, "Deleted " + deletions + " rows successfully");
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if ( contentResolverNotNull() ) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deletions;
    }

    @Override
    public int update(@NonNull Uri uri, 
                      @Nullable ContentValues values, 
                      @Nullable String selection, 
                      @Nullable String[] selectionArgs) {
        
        final SQLiteDatabase mySQLiteDB = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        
        int updations;
        if ( values == null ) {
            return 0;
        }
        switch (match){
            case CURRENT_RATES:{
                updations = mySQLiteDB.update(KurrencyContract.LiveCurrencyTable.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                if ( updations == 1 ){
                    Log.e(LOG_TAG, "Updated "+ updations +" row successfully");
                } else {
                    Log.e(LOG_TAG, "Updated " + updations + " rows successfully");
                }
                break;
            }
            case RATE_AT_DATE:{
                updations = mySQLiteDB.update(KurrencyContract.HistoricalCurrencyTable.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                if ( updations == 1 ){
                    Log.e(LOG_TAG, "Updated "+ updations +" row successfully");
                } else {
                    Log.e(LOG_TAG, "Updated " + updations + " rows successfully");
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return updations;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri,
                          @NonNull ContentValues[] values) {

        final SQLiteDatabase mySQLiteDB = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        long _id;
        int returnCount = 0;
        ContentValues currentCV = new ContentValues();

        switch (match){
            case RATE_AT_DATE: {
                mySQLiteDB.beginTransaction();
                try {
                    for (ContentValues contentValues : values) {
                        currentCV = contentValues;
                        _id = mySQLiteDB.insert(
                                KurrencyContract.HistoricalCurrencyTable.TABLE_NAME,
                                null,
                                contentValues
                        );
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    mySQLiteDB.setTransactionSuccessful();
                } catch (Exception e) {
                    String failure_point = "";
                    if (currentCV != null){ failure_point = currentCV.toString(); }
                    Log.e(LOG_TAG, "Failure in the contentProvider bulkInsert for : " + failure_point);
                } finally {
                    mySQLiteDB.endTransaction();
                }
                if ( contentResolverNotNull() ) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return returnCount;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }

    /*
        Students: Here is where you need to create the UriMatcher. This UriMatcher will
        match each URI to the WEATHER, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE,
        and LOCATION integer constants defined above.  You can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */
    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        final String authority = KurrencyContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, KurrencyContract.PATH_LIVE, CURRENT_RATES);

        matcher.addURI(authority, KurrencyContract.PATH_HISTORICAL + "/*/#", RATE_FROM_DATE);

        matcher.addURI(authority, KurrencyContract.PATH_HISTORICAL + "/#", RATE_AT_DATE);

        // 3) Return the new matcher!
        return matcher;
    }

    private Cursor getLiveKurrency(String[] projection, String selection, String[] selectionArgs,
                              String sortOrder) {
        return mOpenHelper.getReadableDatabase().query(
                KurrencyContract.LiveCurrencyTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    // fromDate >= ?
    private static final String sHistoricalKurrencyFromDate =
            KurrencyContract.HistoricalCurrencyTable.TABLE_NAME+
                    "." + KurrencyContract.HistoricalCurrencyTable.COLUMN_TIMESTAMP + " >= ?";

    // atDate = ?
    private static final String sHistoricalKurrencyAtDate =
            KurrencyContract.HistoricalCurrencyTable.TABLE_NAME+
                    "." + KurrencyContract.HistoricalCurrencyTable.COLUMN_TIMESTAMP + " = ?";

    private Cursor getHistoricalKurrency(String[] projection, String selection, String[] selectionArgs,
                               String sortOrder) {
        return mOpenHelper.getReadableDatabase().query(
                KurrencyContract.HistoricalCurrencyTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getHistoricalKurrencyAtDate(Uri uri, String[] projection, String sortOrder) {
        String[] selectionArgs;
        String selection;

        long atDate = KurrencyContract.HistoricalCurrencyTable.getAtDateFromUri(uri);
        if ( atDate != 0 ) {
            selection = sHistoricalKurrencyAtDate;
            selectionArgs = new String[]{Long.toString(atDate)};
        } else {
            return null;
        }

        return getHistoricalKurrency(
                projection,
                selection,
                selectionArgs,
                sortOrder
        );
    }


    private Cursor getHistoricalKurrencyFromDate(Uri uri, String[] projection, String sortOrder) {
        String[] selectionArgs;
        String selection;

        long startDate = KurrencyContract.HistoricalCurrencyTable.getStartDateFromUri(uri);
        if ( startDate != 0 ) {
            selection = sHistoricalKurrencyFromDate;
            selectionArgs = new String[]{Long.toString(startDate)};
        } else {
            return null;
        }

        return getHistoricalKurrency(
                projection,
                selection,
                selectionArgs,
                sortOrder
        );
    }

    public boolean contentResolverNotNull() {
        if ( getContext() != null ) {
            if (getContext().getContentResolver() != null) {
                return true;
            }
        }
        return false;
    }

    void setCursorUriNotify(Cursor uriCursor, Uri uri) {
        if ( uriCursor != null ) {
            String name = uriCursor.toString();
            Context mContext = getContext();
            if ( mContext != null ) {
                ContentResolver contentResolver = mContext.getContentResolver();
                if ( contentResolver != null) {
                    uriCursor.setNotificationUri(contentResolver, uri);
                }
                else throw new NullPointerException(name + " cursor, ContentResolver is null");
            } else throw new NullPointerException(name + " cursor, Context is null");
        } else throw new NullPointerException("Null Cursor Passed.");
    }

    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
