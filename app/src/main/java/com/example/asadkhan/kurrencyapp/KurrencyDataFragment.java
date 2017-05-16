package com.example.asadkhan.kurrencyapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.LinearLayout.LayoutParams;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.asadkhan.kurrencyapp.data.database.KurrencyContract;
import com.example.asadkhan.kurrencyapp.data.service.KurrencyService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link KurrencyDataFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link KurrencyDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KurrencyDataFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private String LOG_TAG = KurrencyDataFragment.class.getSimpleName();

    static final String DETAIL_URI = "URI";
    private ShareActionProvider mShareActionProvider;
    private String mForecastStr;
    private Uri mUri;
    
    private static final int KURRENCY_LOADER = 0;

    private static final String ENDPOINT = "endpoint";
    private static final String ENDPOINT_LIVE = "live";
    private static final String ENDPOINT_HIST = "historical";

    private static final String[] KURRENCY_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            KurrencyContract.LiveCurrencyTable.TABLE_NAME + "." + KurrencyContract.LiveCurrencyTable._ID,
            KurrencyContract.LiveCurrencyTable.COLUMN_TIMESTAMP,
            KurrencyContract.LiveCurrencyTable.COLUMN_ENTRY_TYPE,
            KurrencyContract.LiveCurrencyTable.COLUMN_USD_INR,
            KurrencyContract.LiveCurrencyTable.COLUMN_USD_EUR,
            KurrencyContract.LiveCurrencyTable.COLUMN_USD_CAD,
            KurrencyContract.LiveCurrencyTable.COLUMN_USD_GBP,
            KurrencyContract.LiveCurrencyTable.COLUMN_USD_JPY,
            KurrencyContract.LiveCurrencyTable.COLUMN_USD_AUD
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_LIVE_ID = 0;
    static final int COL_TIMESTAMP = 1;
    static final int COL_ENTRY_TYPE = 2;
    static final int COL_USD_INR = 3;
    static final int COL_USD_EUR = 4;
    static final int COL_USD_CAD = 5;
    static final int COL_USD_GBP = 6;
    static final int COL_USD_JPY = 7;
    static final int COL_USD_AUD = 8;

    static final int C_INR = 0;
    static final int C_EUR = 1;
    static final int C_CAD = 2;
    static final int C_GBP = 3;
    static final int C_JPY = 4;
    static final int C_AUD = 5;

    static final int[] DISPLAY_KURRENCIES = {
            COL_USD_INR,
            COL_USD_EUR,
            COL_USD_CAD,
            COL_USD_GBP,
            COL_USD_JPY,
            COL_USD_AUD
    };

    static final String[] DISPLAY_KURRENCY_NAMES = {
            "INR \u20B9",
            "EUR \u20AC",
            "CAD C\u0024",
            "GBP \u00A3",
            "JPY \u00A5",
            "AUD A\u0024",
    };

    ArrayList<String> KurrencyList = new ArrayList<>();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final long default_timestamp = 1494428472000L;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // View objects
    View rootView;
    LinearLayout forexCard;
    LinearLayout forexList;
    TextView kurrencyTextView;
    TextView lastUpdatedTextView;
    Button refreshButton;

    LayoutParams wrapcontent_x2_params;

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH);

    private OnFragmentInteractionListener mListener;

    public KurrencyDataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment KurrencyDataFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KurrencyDataFragment newInstance(String param1, String param2) {
        KurrencyDataFragment fragment = new KurrencyDataFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        wrapcontent_x2_params = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_kurrency_data, container, false);

        forexCard = (LinearLayout) rootView.findViewById(R.id.main_card_view);

        forexList = (LinearLayout) forexCard.findViewById(R.id.list_of_kurrencies);
        forexList.setGravity(Gravity.CENTER);

        // kurrencyTextView = (TextView) forexCard.findViewById(R.id.kurrencyTextViewINR);
        lastUpdatedTextView = (TextView) forexCard.findViewById(R.id.lastUpdatedTextView);
        refreshButton = (Button) forexCard.findViewById(R.id.refreshButton);

        if ( refreshButton != null ) {
            refreshButton.setClickable(true);
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getKurrencyData(getContext());
                }
            });
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkRateStatus();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(KURRENCY_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void checkRateStatus(){
        long lu = getLastUpdated(getActivity())/1000;
        Long tsLong = System.currentTimeMillis()/1000;
        if ( (tsLong - lu) > 60*90 ){
            onRateChanged();
        }
    }

    void onRateChanged(){
        getKurrencyData(getActivity());
        getLoaderManager().restartLoader(KURRENCY_LOADER, null, this);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    void createConversionDetailView(LinearLayout rootViewParam, String kurrencyRateString){

        TextView kurrText = new TextView(getContext());
        kurrText.setLayoutParams(wrapcontent_x2_params);
        kurrText.setPadding(96, 16, 16, 16);
        kurrText.setGravity(Gravity.CENTER_VERTICAL);
        kurrText.setTextSize(26);
        kurrText.setText(kurrencyRateString);

        rootViewParam.addView(kurrText);
    }

    void createConversionRateView(LinearLayout rootViewParam,
                                  int imageResID,
                                  double convRate,
                                  String kurrencySymbol) {

        LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout kurrencylinearLayout = (LinearLayout) vi.inflate(R.layout.kurrency_list_item_layout, null);

        TextView kurrencySymbolView = (TextView) kurrencylinearLayout
                                .findViewById(R.id.list_item_kurrency_symbol_text_view);
        kurrencySymbolView.setText(kurrencySymbol);

        ImageView kurrencyImage = (ImageView) kurrencylinearLayout
                                .findViewById(R.id.list_item_kurrency_icon_image_view);
        kurrencyImage.setPadding(6, 6, 6, 6);
        kurrencyImage.setImageResource(imageResID);

        TextView kurrencyConvText = (TextView) kurrencylinearLayout
                .findViewById(R.id.list_item_kurrency_conversion_text_view);
        kurrencyConvText.setText(Double.toString( convRate ));

        rootViewParam.addView(kurrencylinearLayout);
    }

    public void getKurrencyData(Context c) {
        // Toast.makeText(c, "Getting new data ... please wait.", Toast.LENGTH_SHORT).show();
        Intent serviceIntent = new Intent(getActivity(), KurrencyService.class);
        serviceIntent.putExtra(ENDPOINT, ENDPOINT_LIVE);
        getActivity().startService(serviceIntent);
        // setLastUpdated(default_timestamp);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.e(LOG_TAG, "In onCreateLoader");
        // Sort order:  Ascending, by date.
        String sortOrder = KurrencyContract.LiveCurrencyTable.COLUMN_TIMESTAMP + " DESC";

        // Create the Uri to config the CursorLoader
        Uri kurrencyUri = KurrencyContract.LiveCurrencyTable.buildLiveUri();

        // Obtain a CursorLoader instance using params and return it to the LoaderManager
        return new CursorLoader(getActivity(),
                kurrencyUri,
                KURRENCY_COLUMNS,
                null,
                null,
                sortOrder
        );
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.e(LOG_TAG, "In onLoadFinished");

        int imageResourceID = 0;
        String symbolText = "";
        double conversionRate;

        if ( data != null && data.getCount() > 0 && data.moveToFirst()) {
            if ( rootView != null && forexCard != null  && forexList != null ) {

                forexList.removeAllViews();
                KurrencyList.clear();

                for ( int count = 0; count < DISPLAY_KURRENCIES.length; count++) {

                    imageResourceID = getIconResource( count );
                    symbolText = DISPLAY_KURRENCY_NAMES[ count ];
                    conversionRate = data.getDouble( DISPLAY_KURRENCIES[ count ] );

                    String textContent =
                                    DISPLAY_KURRENCY_NAMES[count]
                                    + " "
                                    + Double.toString( conversionRate );

                    createConversionRateView(forexList, R.mipmap.ic_inr_symbol, conversionRate, symbolText);
                    // createConversionDetailView(forexList, symbolText);
                    KurrencyList.add(textContent);
                }

                long timestamp = data.getLong( COL_TIMESTAMP );
                String formattedDate = formatDate( timestamp );
                long diff = System.currentTimeMillis() - timestamp;
                String lastUpdate =
                                "Last update : " +
                                formattedDate + " " +
                                "\nApprox. " + (diff/60000) +
                                " minutes ago";

                if ( lastUpdatedTextView != null ) {
                    lastUpdatedTextView.setText(lastUpdate);
                    // setLastUpdated(System.currentTimeMillis());
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
         Log.e(LOG_TAG, "Inside Loader reset");
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    public static final String JSON_FILE =
            "{\"success\":true,\"terms\":\"https:\\/\\/currencylayer.com\\/terms\",\"privacy\":\"https:\\/\\/currencylayer.com\\/privacy\",\"timestamp\":1494428472,\"source\":\"USD\",\"quotes\":{\"USDAED\":3.672302,\"USDAFN\":67.879997,\"USDALL\":123.690002,\"USDAMD\":483.910004,\"USDANG\":1.777496,\"USDAOA\":165.087006,\"USDARS\":15.554002,\"USDAUD\":1.3522,\"USDAWG\":1.79,\"USDAZN\":1.702105,\"USDBAM\":1.801904,\"USDBBD\":2,\"USDBDT\":81.209999,\"USDBGN\":1.796304,\"USDBHD\":0.376599,\"USDBIF\":1701.900024,\"USDBMD\":1,\"USDBND\":1.409098,\"USDBOB\":6.896617,\"USDBRL\":3.158903,\"USDBSD\":1,\"USDBTC\":0.000567,\"USDBTN\":64.650002,\"USDBWP\":10.521301,\"USDBYN\":1.879569,\"USDBYR\":20020,\"USDBZD\":1.997698,\"USDCAD\":1.36553,\"USDCDF\":1404.099976,\"USDCHF\":1.00834,\"USDCLF\":0.02501,\"USDCLP\":671.679993,\"USDCNY\":6.900898,\"USDCOP\":2941.199951,\"USDCRC\":562.789978,\"USDCUC\":1,\"USDCUP\":1.000507,\"USDCVE\":101.449997,\"USDCZK\":24.436899,\"USDDJF\":177.850006,\"USDDKK\":6.84573,\"USDDOP\":47.089933,\"USDDZD\":109.301003,\"USDEEK\":14.044995,\"USDEGP\":18.069685,\"USDERN\":15.290246,\"USDETB\":22.700001,\"USDEUR\":0.919895,\"USDFJD\":2.105989,\"USDFKP\":0.771404,\"USDGBP\":0.77221,\"USDGEL\":2.427016,\"USDGGP\":0.772283,\"USDGHS\":4.218498,\"USDGIP\":0.771598,\"USDGMD\":45.000404,\"USDGNF\":9201.999662,\"USDGTQ\":7.338499,\"USDGYD\":202.710007,\"USDHKD\":7.78621,\"USDHNL\":23.406012,\"USDHRK\":6.822304,\"USDHTG\":67.300003,\"USDHUF\":285.670013,\"USDIDR\":13357,\"USDILS\":3.603705,\"USDIMP\":0.772283,\"USDINR\":64.580002,\"USDIQD\":1181,\"USDIRR\":32449.00027,\"USDISK\":105.99996,\"USDJEP\":0.772283,\"USDJMD\":129.119995,\"USDJOD\":0.70801,\"USDJPY\":114.026999,\"USDKES\":103.099998,\"USDKGS\":67.707982,\"USDKHR\":4028.899902,\"USDKMF\":449.999791,\"USDKPW\":900.00014,\"USDKRW\":1132.319946,\"USDKWD\":0.304098,\"USDKYD\":0.820338,\"USDKZT\":318.130005,\"USDLAK\":8189.999714,\"USDLBP\":1506.435724,\"USDLKR\":152.580002,\"USDLRD\":91.999776,\"USDLSL\":13.454963,\"USDLTL\":3.048699,\"USDLVL\":0.62055,\"USDLYD\":1.407955,\"USDMAD\":9.929401,\"USDMDL\":18.704983,\"USDMGA\":3159.999687,\"USDMKD\":56.340061,\"USDMMK\":1366.000362,\"USDMNT\":2404.999817,\"USDMOP\":8.019402,\"USDMRO\":356.999872,\"USDMUR\":34.799999,\"USDMVR\":15.360015,\"USDMWK\":717.679993,\"USDMXN\":18.947297,\"USDMYR\":4.345013,\"USDMZN\":70.440002,\"USDNAD\":13.424957,\"USDNGN\":315.000168,\"USDNIO\":29.838201,\"USDNOK\":8.61731,\"USDNPR\":102.900002,\"USDNZD\":1.4395,\"USDOMR\":0.384602,\"USDPAB\":1,\"USDPEN\":3.282498,\"USDPGK\":3.177199,\"USDPHP\":49.910363,\"USDPKR\":104.599998,\"USDPLN\":3.875297,\"USDPYG\":5570.899902,\"USDQAR\":3.640901,\"USDRON\":4.185506,\"USDRSD\":112.692802,\"USDRUB\":57.582983,\"USDRWF\":819.25,\"USDSAR\":3.749897,\"USDSBD\":7.856049,\"USDSCR\":13.355993,\"USDSDG\":6.659796,\"USDSEK\":8.90756,\"USDSGD\":1.40973,\"USDSHP\":0.771597,\"USDSLL\":7430.000183,\"USDSOS\":545.999609,\"USDSRD\":7.469501,\"USDSTD\":22543.199219,\"USDSVC\":8.721982,\"USDSYP\":514.97998,\"USDSZL\":13.464016,\"USDTHB\":34.740002,\"USDTJS\":8.499301,\"USDTMT\":3.4,\"USDTND\":2.399501,\"USDTOP\":2.314982,\"USDTRY\":3.585802,\"USDTTD\":6.709199,\"USDTWD\":30.223017,\"USDTZS\":2226.99985,\"USDUAH\":26.420322,\"USDUGX\":3626.99979,\"USDUSD\":1,\"USDUYU\":28.059999,\"USDUZS\":3725.000232,\"USDVEF\":9.9745,\"USDVND\":22737,\"USDVUV\":108.519997,\"USDWST\":2.596294,\"USDXAF\":602.97998,\"USDXAG\":0.061734,\"USDXAU\":0.000819,\"USDXCD\":2.708373,\"USDXDR\":0.732056,\"USDXOF\":597.47998,\"USDXPF\":110.196378,\"USDYER\":249.949997,\"USDZAR\":13.437598,\"USDZMK\":9.195009,\"USDZMW\":9.149849,\"USDZWL\":322.355011}}";


    public static Long getLastUpdated(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(
                context.getString(R.string.pref_last_update_key),
                default_timestamp);
    }

    public boolean setLastUpdated(long luValue) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(getString(R.string.pref_last_update_key), luValue);
        // editor.apply();
        return editor.commit();
    }


    int getIconResource(int position){
        switch (position){
            case C_INR:
                return R.mipmap.ic_inr_symbol;
            case C_EUR:
                return R.mipmap.ic_eur_symbol;
            case C_CAD:
                return R.mipmap.ic_launcher;
            case C_GBP:
                return R.mipmap.ic_launcher;
            case C_JPY:
                return R.mipmap.ic_launcher;
            case C_AUD:
                return R.mipmap.ic_launcher;
            default:
                return R.mipmap.ic_launcher;
        }




    }

    private String formatDate(long timestampData) {
        return dateFormat.format(new Date(timestampData));
    }

}
