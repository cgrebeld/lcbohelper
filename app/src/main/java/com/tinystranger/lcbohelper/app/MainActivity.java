package com.tinystranger.lcbohelper.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.integration.android.IntentIntegrator;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class MainActivity extends FragmentActivity implements View.OnClickListener
{
    private LocationClient mLocationClient = null;
    public static Location mCurrentLocation = null;
    public static List<LCBOEntity> stores = null;
    static Bitmap defaultThumbnail = null;

    class CompletionFetcher
    {
        private InputStream downloadUrl(String urlString) throws IOException {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "LCBO/2.5 CFNetwork/672.1.14 Darwin/14.0.0");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("Accept-Language", "en-us");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            InputStream stream = conn.getInputStream();
            return stream;
        }

        public ArrayList<String> retrieveResults(String constraint)
        {
            ArrayList<String> candidates = new ArrayList<String>();

            String url = null;
            try {
                url = String.format(
                        "http://www.lcbo.com/lcbo-ear/product/itemnameAutoComplete.do?language=EN&ITEM_NAME=%s&ITEM_NAME_RESULTS=15&ITEM_NAME_LENGTH=45"
                        , URLEncoder.encode(constraint, "UTF-8"));

                InputStream stream = null;
                ProductCompletionParser parser = new ProductCompletionParser();
                List<String> entries = null;
                try {
                    stream = new GZIPInputStream(downloadUrl(url));
                    entries = parser.parse(stream);
                    if (entries != null)
                    {
                        candidates.addAll(entries);
                    }
                    // Makes sure that the InputStream is closed after the app is
                    // finished using it.
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return candidates;
        }
    }

    class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private List<String> mData;

        public AutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            mData = new ArrayList<String>();
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public String getItem(int index) {
            return mData.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter myFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if(constraint != null) {
                        // A class that queries a web API, parses the data and returns an ArrayList<String>
                        CompletionFetcher fetcher = new CompletionFetcher();
                        mData = fetcher.retrieveResults(constraint.toString());
                        // Now assign the values and count to the FilterResults object
                        filterResults.values = mData;
                        filterResults.count = mData.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence contraint, FilterResults results) {
                    if(results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return myFilter;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);

        final AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.productSearchEditText);
        editText.setAdapter(new AutoCompleteAdapter(this, android.R.layout.simple_dropdown_item_1line));
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    doSearch(null);
                    return true;
                }
                return false;
            }
        });

        final Button scanBtn = (Button)findViewById(R.id.buttonScanBarcode);
        scanBtn.setOnClickListener(this);

        mLocationClient = new LocationClient(getApplicationContext(),
                new GooglePlayServicesClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Log.d("Location", "onConnected");
                            if (mLocationClient.isConnected()) {
                                if (mCurrentLocation == null)
                                {
                                    mCurrentLocation = mLocationClient.getLastLocation();
                                    LocationFetchTask task = new LocationFetchTask(mCurrentLocation, null, new LocationFetchTask.LocationListener() {
                                        @Override
                                        public void onLocationsFetched(List<LCBOEntity> locations) {
                                            MainActivity.stores = locations;
                                        }
                                    });
                                    task.execute(mCurrentLocation);
                                }
                            }
                        }
                        @Override
                        public void onDisconnected() {
                            Log.d("Location", "onDisconnected");
                        }
                    },
                new GooglePlayServicesClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d("Location", "onConnectionFailed");
                    }
                });
    }

    public void doSearch(View view) {
        final AutoCompleteTextView txt = (AutoCompleteTextView) findViewById(R.id.productSearchEditText);
        String search = txt.getText().toString();
        if (search.length() > 0)
        {
            Bundle args = new Bundle();
            args.putString("search", search);
            Intent i = new Intent(getApplicationContext(),ProductResultsActivity.class);
            i.putExtra("search", search);
            startActivity(i);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.buttonScanBarcode) {
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            //we have a result
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            //Toast toast = Toast.makeText(getApplicationContext(),
            //        scanFormat + ":" + scanContent, Toast.LENGTH_SHORT);
            //toast.show();
            Log.d("scan",scanFormat + ":" + scanContent );
            // UPC_A:876153000026
            LCBOEntity item = new LCBOEntity();
            item.itemNumber = scanContent;
            ProductDetailActivity.lastResult = item;
            if (null == defaultThumbnail) {
                defaultThumbnail = BitmapFactory.decodeResource(getResources(),
                        R.drawable.default_thumbnail);
            }
            ProductDetailActivity.lastBitmap = defaultThumbnail;
            Intent i = new Intent(getApplicationContext(),ProductDetailActivity.class);
            startActivity(i);
        }
    }
}
