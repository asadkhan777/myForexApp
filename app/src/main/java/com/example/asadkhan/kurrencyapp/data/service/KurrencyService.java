package com.example.asadkhan.kurrencyapp.data.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.asadkhan.kurrencyapp.data.database.KurrencyContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Kreated by asadkhan on 10 | May |  2017 | at 1:41 PM.
 */

public class KurrencyService extends IntentService {

    private String LOG_TAG = KurrencyService.class.getSimpleName();

    private OkHttpClient client;

    private static final String MY_ACCESS_KEY = "c1ec72e7ce14e7927a3af5a07103d1e6";
    private static final String KURRENCY_BASE_URL = "http://apilayer.net/api/";
    private static final String ENDPOINT = "endpoint";
    private static final long default_timestamp = 1494428472000L;
    private static final long default_update_limit = 3600000L;
    private static long timestamp;
    private static double usd_inr;

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH);

    private static String mKurrencyString;

    public KurrencyService() {
        super("KurrencyService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if ( client == null ) {
            client = new OkHttpClient();
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        getDataFromAPI(intent);
    }

    URL createAPIURL(String apiEndpoint) {

        // Construct the URL for the Apilayer query
        // Read more at:
        // https://currencylayer.com/documentation

        URL weatherUrl;

        final String ACCESS_KEY = "access_key";      // Private API access authenticating key
        final String FROM_DATE = "date";             // YYY-MM-DD
        final String KURRENCY_PAIRS = "currencies";  // eg. USD,AUD,EUR,INR
        final String FORMAT_PARAM = "format";        // 1 for pretty & slow, 0 for ugly & fast

        try {
            Uri builtUri = Uri.parse(KURRENCY_BASE_URL).buildUpon()
                    .appendPath(apiEndpoint)
                    .appendQueryParameter(ACCESS_KEY, MY_ACCESS_KEY)
                    // .appendQueryParameter(FORMAT_PARAM, "1")
                    // .appendQueryParameter(FROM_DATE, "INR")
                    // .appendQueryParameter(KURRENCY_PAIRS, "INR")
                    .build();
                weatherUrl = new URL(builtUri.toString());
            Log.e(LOG_TAG, "Built URI : \n" + builtUri.toString());
            } catch (Exception e){
                Log.e(LOG_TAG, e.getMessage(), e);
                // e.printStackTrace();
                return null;
        }
        return weatherUrl;
    }

    String resolveEndPoint (Intent intent) {
        String endPoint;
        if ( intent.hasExtra(ENDPOINT) ) {
            endPoint = intent.getStringExtra(ENDPOINT);
            if ( endPoint == null || endPoint.equals("") ) {
                throw new NullPointerException(LOG_TAG);
                // Nothing to do here
            }
        }
        else throw new IllegalArgumentException();

        return endPoint;
    }

    void getDataFromLiveDB(){
        try {
            // Log.e(LOG_TAG, "DBG 1");
            Cursor cur = this.getContentResolver()
                    .query(
                            KurrencyContract.LiveCurrencyTable.CONTENT_URI.buildUpon().build(),
                            null, null, null,
                            KurrencyContract.LiveCurrencyTable.COLUMN_TIMESTAMP + " DESC");
            // Log.e(LOG_TAG, "DBG 2 ");
            if (cur != null) {
                // Log.e(LOG_TAG, "DBG 3 " + cur.toString());
                if ( cur.moveToFirst() ) {
                    do {
                        timestamp = cur.getLong(cur.getColumnIndex(KurrencyContract.LiveCurrencyTable.COLUMN_TIMESTAMP));
                        usd_inr = cur.getDouble(cur.getColumnIndex(KurrencyContract.LiveCurrencyTable.COLUMN_USD_INR));
//                        Log.e(LOG_TAG, "Timestamp - " + Long.toString(timestamp));
                        Log.e(LOG_TAG, "Timestamp Date - " + formatDate(timestamp));
//                        Log.e(LOG_TAG, "1 USD = " + Double.toString(usd_inr) + " INR.");
                    } while (cur.moveToNext());
                }
                cur.close();
            }
        }
        catch (SQLiteException sq) {
            Log.e(LOG_TAG, "SQLite Error : " + sq.getMessage(), sq);
            // sq.printStackTrace();
        }

    }

    Uri insertResponseIntoDB(String kurrencyJsonString){
        Uri insertUri = null;
        try {
            Vector<ContentValues> cvData = this.getKurrencyDataFromJson( kurrencyJsonString );
            if (cvData != null) {

                ContentValues entryValues = cvData.get(0);

                long tsLong = entryValues.getAsLong(KurrencyContract.LiveCurrencyTable.COLUMN_TIMESTAMP);
                long now = System.currentTimeMillis();

                Log.e(LOG_TAG, "CV-TS : " + tsLong);
                Log.e(LOG_TAG, "CV : " + formatDate(tsLong));
                Log.e(LOG_TAG, "NOW-TS : " + now);
                Log.e(LOG_TAG, "NOW : " + formatDate(now));
                Log.e(LOG_TAG, "Diff : " + ( now - tsLong ));
                Log.e(LOG_TAG, "Diff / 1000 : " + ( now - tsLong ) / 1000);

                insertUri = getContentResolver()
                        .insert(KurrencyContract.LiveCurrencyTable.CONTENT_URI, entryValues);

                String uriString = "";
                if (insertUri != null) {uriString = insertUri.toString();}
                Log.e(LOG_TAG, "URI created, please check > " +  uriString);
            }
            else {
                Log.e(LOG_TAG, "Kurrency data is null, please check ! " );
            }

        } catch (JSONException je) {
            Log.e(LOG_TAG, "JSON Error : " + je.getMessage(), je);
            //je.printStackTrace();

        } catch (SQLiteConstraintException sq) {
            Log.e(LOG_TAG, "SQLite Error : " + sq.getMessage(), sq);
            // sq.printStackTrace();

        } catch (Exception exc) {
            Log.e(LOG_TAG, "Some other Error : " + exc.getMessage(), exc);
            //exc.printStackTrace();
        }

        return insertUri;
    }

//    private int insertHistoricalValuesIntoDB(Vector<ContentValues> contentValues){
//        int inserted = 0 ;
//        // add to database
//        if ( contentValues != null && contentValues.size() > 0 ) {
//            ContentValues[] cv_array = new ContentValues[contentValues.size()];
//            contentValues.toArray(cv_array);
//            inserted = this.getContentResolver()
//                    .bulkInsert(KurrencyContract.HistoricalCurrencyTable.CONTENT_URI, cv_array);
//        }
//        return inserted;
//    }

    private Vector<ContentValues>
            getKurrencyDataFromJson( String kurrencyJsonStr ) throws JSONException {

        Vector<ContentValues> cVVector;

        // Location information
        final String tnc = "terms";
        final String privacy = "privacy";
        final String success = "success";
        final String hist = "historical";
        final String timestamp = "timestamp";
        final String source = "source";
        final String quotes = "quotes";
        final String kurrencies[] = new String[100];

        String[] selection = new String[]{
                "USDINR",
                "USDEUR",
                "USDJPY",
                "USDGBP",
                "USDAUD",
                "USDCAD"
        };

        try {
            if (kurrencyJsonStr == null) {
                return null;
            }

            JSONObject kurrencyJson = new JSONObject(kurrencyJsonStr);

            System.out.println("Live Currency Exchange Rates");
            long timestampdata = kurrencyJson.getLong("timestamp")*1000;
            // Date from JSON
            Date timeStampDate = new Date(timestampdata);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.ENGLISH);
            String formattedDate = dateFormat.format(timeStampDate);

            // Quotes List from JSON
            JSONObject quotesJSON = kurrencyJson.getJSONObject(quotes);

            Log.e(LOG_TAG,
                    "1 " + kurrencyJson.getString(source) +
                    " in INR : " + Double.toString(quotesJSON.getDouble(selection[0])) +
                    " \n(Date: " + formattedDate + ")");


            // Insert the new weather information into the database
            cVVector = new Vector<>(selection.length);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.


            // Use BigDecimal when doing conversion stuff, after obtaining from DB
            // BigDecimal USD_INR = new BigDecimal(quotesJSON.getDouble(selection[0]));
            double USD_INR = quotesJSON.getDouble(selection[0]);
            double USD_AUD = quotesJSON.getDouble(selection[1]);
            double USD_CAD = quotesJSON.getDouble(selection[2]);
            double USD_EUR = quotesJSON.getDouble(selection[3]);
            double USD_GBP = quotesJSON.getDouble(selection[4]);
            double USD_JPY = quotesJSON.getDouble(selection[5]);

            ContentValues kurrencyValues = new ContentValues();

            kurrencyValues.put(KurrencyContract.LiveCurrencyTable.COLUMN_TIMESTAMP, timestampdata);
            kurrencyValues.put(KurrencyContract.LiveCurrencyTable.COLUMN_ENTRY_TYPE, "M");
            kurrencyValues.put(KurrencyContract.LiveCurrencyTable.COLUMN_USD_INR, USD_INR);
            kurrencyValues.put(KurrencyContract.LiveCurrencyTable.COLUMN_USD_AUD, USD_AUD);
            kurrencyValues.put(KurrencyContract.LiveCurrencyTable.COLUMN_USD_CAD, USD_CAD);
            kurrencyValues.put(KurrencyContract.LiveCurrencyTable.COLUMN_USD_EUR, USD_EUR);
            kurrencyValues.put(KurrencyContract.LiveCurrencyTable.COLUMN_USD_GBP, USD_GBP);
            kurrencyValues.put(KurrencyContract.LiveCurrencyTable.COLUMN_USD_JPY, USD_JPY);

            cVVector.add(kurrencyValues);

            Log.e(LOG_TAG, "CV vector data : \n" + cVVector.toString());
            return cVVector;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            // e.printStackTrace();
            return null;
        }
    }

    private String formatDate(long timestampData) {
        return dateFormat.format(new Date(timestampData));
    }

    private String readFile(String file) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } catch ( IOException io ){
            Log.e(LOG_TAG, io.toString() + " " + io.getMessage());
            // io.printStackTrace();
            reader.close();
        }  finally {
            reader.close();
        }
        return null;
    }

    private String getStringFromFile(String file) {
        String JSONdata = "";
        try{
            JSONdata = readFile(file);
        } catch (Exception exc) {
            Log.e(LOG_TAG, exc.toString() + " " + exc.getMessage());
            // exc.printStackTrace();
        }
        return JSONdata;
    }

    void getDataFromAPI(Intent apiDataIntent){

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (apiDataIntent != null) {

            if ( checkDbValuesExist() ){
                Log.e(LOG_TAG, "Values are present in the DB, no need to hit API");
                getDataFromLiveDB();
                return;
            }

            String endPoint = resolveEndPoint(apiDataIntent);  // Which data do you want?

            URL url = createAPIURL(endPoint);
            // Assume we received JSON data already from API


            try {
                // Get data from API server using OkHttpClient
                String responseJsonStr = getDataOKHttpClient(url);

                 Uri myUri = insertResponseIntoDB(responseJsonStr);
//                Cursor myCursor = this.getContentResolver().query(myUri, null, null, null, null);
//                if ( (myCursor != null) &&  myCursor.moveToFirst() ) {
//                    do {
//                        long dbTimeStamp = myCursor.getLong(
//                                myCursor.getColumnIndex(KurrencyContract.LiveCurrencyTable.COLUMN_TIMESTAMP));
//                        Log.e(LOG_TAG, "DB-TS : " + dbTimeStamp);
//                        Log.e(LOG_TAG, "DB : " + formatDate(dbTimeStamp));
//                    } while (myCursor.moveToNext());
//                    myCursor.close();
//                }
                getDataFromLiveDB();
            } catch (Exception e){
                Log.e(LOG_TAG, "Some error occured ");
            }

        } else throw new NullPointerException(LOG_TAG);
    }

    public boolean checkDbValuesExist() {

        long dbTimeStamp = 0L;
        Long now = System.currentTimeMillis();
        boolean exists;
        String sortOrder = KurrencyContract.LiveCurrencyTable.COLUMN_TIMESTAMP + " DESC";
        Uri kurrencyLatestUri = KurrencyContract.LiveCurrencyTable.buildLiveUri();

        Cursor cur = this.getContentResolver().query(kurrencyLatestUri,
                null, null, null, sortOrder);
        if (cur != null) {
            if ( cur.moveToFirst() ) {
                dbTimeStamp = cur.getLong(cur.getColumnIndex(KurrencyContract.LiveCurrencyTable.COLUMN_TIMESTAMP));
            }
            cur.close();
        }
//        dbTimeStamp /= 1000;
//        now /= 1000;
        exists = ( now - dbTimeStamp ) < default_update_limit;
         Log.e(LOG_TAG, "Exists : " + exists);
         Log.e(LOG_TAG, "DB-TS : " + dbTimeStamp);
         Log.e(LOG_TAG, "DB : " + formatDate(dbTimeStamp));
         Log.e(LOG_TAG, "NOW-TS : " + now);
         Log.e(LOG_TAG, "NOW : " + formatDate(now));
         Log.e(LOG_TAG, "Diff : last update was " + (( now - dbTimeStamp ) / 60000L) + " minutes ago.");
        return exists;
    }


    String getDataOKHttpClient(URL url) {

        String responseJson = null;
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();

            if (response != null && response.isSuccessful()) {
                Log.e(LOG_TAG, "OK DBG 1");
                responseJson = response.body().string();
                response.close();
                response = null;
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error here! Something wrong in JSON response for : \n" + url.toString());
            e.printStackTrace();
        } finally {
            if ( response != null ) {
                response.close();
            }
        }
        return responseJson;
    }


    final String[] listOfKurrencyPairs = new String[]{
                    "USDAED",
                    "USDAFN",
                    "USDALL",
                    "USDAMD",
                    "USDANG",
                    "USDAOA",
                    "USDARS",
                    "USDAUD",
                    "USDAWG",
                    "USDAZN",
                    "USDBAM",
                    "USDBBD",
                    "USDBDT",
                    "USDBGN",
                    "USDBHD",
                    "USDBIF",
                    "USDBMD",
                    "USDBND",
                    "USDBOB",
                    "USDBRL",
                    "USDBSD",
                    "USDBTC",
                    "USDBTN",
                    "USDBWP",
                    "USDBYN",
                    "USDBYR",
                    "USDBZD",
                    "USDCAD",
                    "USDCDF",
                    "USDCHF",
                    "USDCLF",
                    "USDCLP",
                    "USDCNY",
                    "USDCOP",
                    "USDCRC",
                    "USDCUC",
                    "USDCUP",
                    "USDCVE",
                    "USDCZK",
                    "USDDJF",
                    "USDDKK",
                    "USDDOP",
                    "USDDZD",
                    "USDEEK",
                    "USDEGP",
                    "USDERN",
                    "USDETB",
                    "USDEUR",
                    "USDFJD",
                    "USDFKP",
                    "USDGBP",
                    "USDGEL",
                    "USDGGP",
                    "USDGHS",
                    "USDGIP",
                    "USDGMD",
                    "USDGNF",
                    "USDGTQ",
                    "USDGYD",
                    "USDHKD",
                    "USDHNL",
                    "USDHRK",
                    "USDHTG",
                    "USDHUF",
                    "USDIDR",
                    "USDILS",
                    "USDIMP",
                    "USDINR",
                    "USDIQD",
                    "USDIRR",
                    "USDISK",
                    "USDJEP",
                    "USDJMD",
                    "USDJOD",
                    "USDJPY",
                    "USDKES",
                    "USDKGS",
                    "USDKHR",
                    "USDKMF",
                    "USDKPW",
                    "USDKRW",
                    "USDKWD",
                    "USDKYD",
                    "USDKZT",
                    "USDLAK",
                    "USDLBP",
                    "USDLKR",
                    "USDLRD",
                    "USDLSL",
                    "USDLTL",
                    "USDLVL",
                    "USDLYD",
                    "USDMAD",
                    "USDMDL",
                    "USDMGA",
                    "USDMKD",
                    "USDMMK",
                    "USDMNT",
                    "USDMOP",
                    "USDMRO",
                    "USDMUR",
                    "USDMVR",
                    "USDMWK",
                    "USDMXN",
                    "USDMYR",
                    "USDMZN",
                    "USDNAD",
                    "USDNGN",
                    "USDNIO",
                    "USDNOK",
                    "USDNPR",
                    "USDNZD",
                    "USDOMR",
                    "USDPAB",
                    "USDPEN",
                    "USDPGK",
                    "USDPHP",
                    "USDPKR",
                    "USDPLN",
                    "USDPYG",
                    "USDQAR",
                    "USDRON",
                    "USDRSD",
                    "USDRUB",
                    "USDRWF",
                    "USDSAR",
                    "USDSBD",
                    "USDSCR",
                    "USDSDG",
                    "USDSEK",
                    "USDSGD",
                    "USDSHP",
                    "USDSLL",
                    "USDSOS",
                    "USDSRD",
                    "USDSTD",
                    "USDSVC",
                    "USDSYP",
                    "USDSZL",
                    "USDTHB",
                    "USDTJS",
                    "USDTMT",
                    "USDTND",
                    "USDTOP",
                    "USDTRY",
                    "USDTTD",
                    "USDTWD",
                    "USDTZS",
                    "USDUAH",
                    "USDUGX",
                    "USDUSD",
                    "USDUYU",
                    "USDUZS",
                    "USDVEF",
                    "USDVND",
                    "USDVUV",
                    "USDWST",
                    "USDXAF",
                    "USDXAG",
                    "USDXAU",
                    "USDXCD",
                    "USDXDR",
                    "USDXOF",
                    "USDXPF",
                    "USDYER",
                    "USDZAR",
                    "USDZMK",
                    "USDZMW",
                    "USDZWL"
            };

    public static final String JSON_FILE =
            "{\"success\":true,\"terms\":\"https:\\/\\/currencylayer.com\\/terms\",\"privacy\":\"https:\\/\\/currencylayer.com\\/privacy\",\"timestamp\":1494494530,\"source\":\"USD\",\"quotes\":{\"USDAED\":3.672502,\"USDAFN\":67.879997,\"USDALL\":123.699997,\"USDAMD\":483.910004,\"USDANG\":1.777497,\"USDAOA\":165.087006,\"USDARS\":15.498994,\"USDAUD\":1.357199,\"USDAWG\":1.79,\"USDAZN\":1.702099,\"USDBAM\":1.801199,\"USDBBD\":2,\"USDBDT\":81.330002,\"USDBGN\":1.797303,\"USDBHD\":0.376966,\"USDBIF\":1701.900024,\"USDBMD\":1,\"USDBND\":1.4078,\"USDBOB\":6.901015,\"USDBRL\":3.1668,\"USDBSD\":1,\"USDBTC\":0.00055,\"USDBTN\":64.599998,\"USDBWP\":10.488203,\"USDBYN\":1.880498,\"USDBYR\":20020,\"USDBZD\":1.997698,\"USDCAD\":1.37049,\"USDCDF\":1404.079956,\"USDCHF\":1.00766,\"USDCLF\":0.02506,\"USDCLP\":672.950012,\"USDCNY\":6.9018,\"USDCOP\":2940,\"USDCRC\":555.73999,\"USDCUC\":1,\"USDCUP\":1.000447,\"USDCVE\":101.400002,\"USDCZK\":24.463097,\"USDDJF\":177.999613,\"USDDKK\":6.840701,\"USDDOP\":47.130001,\"USDDZD\":109.125999,\"USDEEK\":14.086991,\"USDEGP\":18.049999,\"USDERN\":15.290133,\"USDETB\":22.895901,\"USDEUR\":0.919403,\"USDFJD\":2.110124,\"USDFKP\":0.772304,\"USDGBP\":0.77457,\"USDGEL\":2.426199,\"USDGGP\":0.77458,\"USDGHS\":4.178504,\"USDGIP\":0.772602,\"USDGMD\":44.749537,\"USDGNF\":9208.299805,\"USDGTQ\":7.338503,\"USDGYD\":202.710007,\"USDHKD\":7.78938,\"USDHNL\":23.405001,\"USDHRK\":6.788102,\"USDHTG\":67.300003,\"USDHUF\":285.23999,\"USDIDR\":13349,\"USDILS\":3.607201,\"USDIMP\":0.77458,\"USDINR\":64.389999,\"USDIQD\":1181,\"USDIRR\":32450.999957,\"USDISK\":106.199997,\"USDJEP\":0.77458,\"USDJMD\":129.119995,\"USDJOD\":0.708974,\"USDJPY\":114.164001,\"USDKES\":103.099998,\"USDKGS\":67.708045,\"USDKHR\":4020.999826,\"USDKMF\":450.000325,\"USDKPW\":899.999902,\"USDKRW\":1127.72998,\"USDKWD\":0.303897,\"USDKYD\":0.819801,\"USDKZT\":315.480011,\"USDLAK\":8185.000318,\"USDLBP\":1506.500677,\"USDLKR\":152.580002,\"USDLRD\":90.999913,\"USDLSL\":13.460162,\"USDLTL\":3.048702,\"USDLVL\":0.62055,\"USDLYD\":1.407799,\"USDMAD\":9.925802,\"USDMDL\":18.53498,\"USDMGA\":3136.999495,\"USDMKD\":56.299999,\"USDMMK\":1363.999699,\"USDMNT\":2404.999711,\"USDMOP\":8.022302,\"USDMRO\":357.630005,\"USDMUR\":34.520262,\"USDMVR\":15.359827,\"USDMWK\":717.679993,\"USDMXN\":18.942598,\"USDMYR\":4.34503,\"USDMZN\":70.440002,\"USDNAD\":13.376989,\"USDNGN\":314.99999,\"USDNIO\":29.842199,\"USDNOK\":8.59404,\"USDNPR\":102.900002,\"USDNZD\":1.460095,\"USDOMR\":0.384796,\"USDPAB\":1,\"USDPEN\":3.290498,\"USDPGK\":3.164597,\"USDPHP\":49.849998,\"USDPKR\":104.749935,\"USDPLN\":3.8772,\"USDPYG\":5600.600098,\"USDQAR\":3.640903,\"USDRON\":4.181303,\"USDRSD\":113.017403,\"USDRUB\":57.117001,\"USDRWF\":819.25,\"USDSAR\":3.750097,\"USDSBD\":7.840604,\"USDSCR\":13.152997,\"USDSDG\":6.666698,\"USDSEK\":8.86407,\"USDSGD\":1.40848,\"USDSHP\":0.772598,\"USDSLL\":7430.000334,\"USDSOS\":548.999582,\"USDSRD\":7.470229,\"USDSTD\":22530.800781,\"USDSVC\":8.722195,\"USDSYP\":514.97998,\"USDSZL\":13.369978,\"USDTHB\":34.73026,\"USDTJS\":8.499302,\"USDTMT\":3.4,\"USDTND\":2.3995,\"USDTOP\":2.304302,\"USDTRY\":3.5783,\"USDTTD\":6.719499,\"USDTWD\":30.179987,\"USDTZS\":2227.999734,\"USDUAH\":26.401709,\"USDUGX\":3627.999827,\"USDUSD\":1,\"USDUYU\":28.059999,\"USDUZS\":3759.999613,\"USDVEF\":9.974499,\"USDVND\":22721,\"USDVUV\":108.529999,\"USDWST\":2.594201,\"USDXAF\":602.719971,\"USDXAG\":0.061297,\"USDXAU\":0.000819,\"USDXCD\":2.703525,\"USDXDR\":0.732056,\"USDXOF\":597.450012,\"USDXPF\":109.750284,\"USDYER\":249.949997,\"USDZAR\":13.3812,\"USDZMK\":9.22495,\"USDZMW\":9.190866,\"USDZWL\":322.355011}}";
}
