package com.tinystranger.lcbohelper.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;

public class MainActivity extends FragmentActivity
        implements View.OnClickListener, LoaderManager.LoaderCallbacks<List<LCBOEntity>>
{
    private LocationClient mLocationClient = null;
    public static Location mCurrentLocation = null;
    public static List<LCBOEntity> stores = null;
    static Bitmap defaultThumbnail = null;
    static final double defaultLatitude = 43.64919634;
    static final double  defaultLongitude = -79.37793732;
    static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        instance = this;
        final AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.productSearchEditText);

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
                        mCurrentLocation = new Location("");
                        mCurrentLocation.setLatitude(defaultLatitude);
                        mCurrentLocation.setLongitude(defaultLongitude);
                        LocationFetchTask task = new LocationFetchTask(mCurrentLocation, null, new LocationFetchTask.LocationListener() {
                            @Override
                            public void onLocationsFetched(List<LCBOEntity> locations) {
                                MainActivity.stores = locations;
                            }
                        });
                        task.execute(mCurrentLocation);
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

    public void doFavorites(View view) {
        Intent i = new Intent(getApplicationContext(),FavoritesActivity.class);
        startActivity(i);
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

            if (null == defaultThumbnail) {
                defaultThumbnail = BitmapFactory.decodeResource(getResources(),
                        R.drawable.default_thumbnail);
            }
            ProductDetailActivity.lastBitmap = defaultThumbnail;

            Bundle args = new Bundle();
            String url = String.format(
                    "http://stage.lcbo.com/lcbo-webapp/productdetail.do?itemNumber=%s"
                    , scanContent);
            args.putString("url", url);
            Loader loader = getSupportLoaderManager().initLoader(0, args, this);
            // call forceLoad() to start processing
            loader.forceLoad();
        }
    }

    @Override
    public Loader<List<LCBOEntity>> onCreateLoader(int id, Bundle args) {
        return new LCBOXmlLoader(this, args, LCBOQueryParser.QueryType.kProducts);
    }

    @Override
    public void onLoadFinished(Loader<List<LCBOEntity>> loader, List<LCBOEntity> data) {
        if (data  != null && !data.isEmpty()) {
            ProductDetailActivity.lastResult = data.get(0);

            Intent i = new Intent(getApplicationContext(), ProductDetailActivity.class);
            startActivity(i);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<LCBOEntity>> loader) {

    }
}
