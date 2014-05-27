package com.tinystranger.lcbohelper.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

/**
 * Created by chris on 5/18/2014.
 */
public class LocationDetailActivity extends ActionBarActivity
        implements LoaderManager.LoaderCallbacks<List<LCBOEntity>>{

    public static LCBOEntity location;
    //private GoogleMap supportMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_location_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        updateData();

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        Bundle args = new Bundle();
        String url = String.format(
                "http://stage.lcbo.com/lcbo-webapp/storedetail.do?locationNumber=%s"
                , location.locationNumber);
        args.putString("url", url);
        Loader loader = getSupportLoaderManager().initLoader(0, args, this);
        // call forceLoad() to start processing
        loader.forceLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
    }

    // Called when a new Loader needs to be created
    @Override
    public LCBOXmlLoader onCreateLoader(int id, Bundle args) {

        return new LCBOXmlLoader(this, args, LCBOQueryParser.QueryType.kStores);
    }

    @Override
    public void onLoadFinished(Loader<List<LCBOEntity>> loader, List<LCBOEntity> data)
    {
        if (data != null && data.size() > 0) {
            final LCBOEntity e = data.get(0);
            TextView txt = (TextView)findViewById(R.id.sundayHours);
            if (e.sundayOpenHour.equalsIgnoreCase("00:00") && e.sundayCloseHour.equalsIgnoreCase("00:00"))
                txt.setText("Closed");
            else
                txt.setText(e.sundayOpenHour + "-" + e.saturdayCloseHour);
            txt = (TextView)findViewById(R.id.mondayHours);
            if (e.mondayOpenHour.equalsIgnoreCase("00:00") && e.mondayCloseHour.equalsIgnoreCase("00:00"))
                txt.setText("Closed");
            else
                txt.setText(e.mondayOpenHour + "-" + e.mondayCloseHour);
            txt = (TextView)findViewById(R.id.tuesdayHours);
            if (e.tuesdayOpenHour.equalsIgnoreCase("00:00") && e.tuesdayCloseHour.equalsIgnoreCase("00:00"))
                txt.setText("Closed");
            else
                txt.setText(e.tuesdayOpenHour + "-" + e.tuesdayCloseHour);
            txt = (TextView)findViewById(R.id.wednesdayHours);
            if (e.wednesdayOpenHour.equalsIgnoreCase("00:00") && e.wednesdayCloseHour.equalsIgnoreCase("00:00"))
                txt.setText("Closed");
            else
                txt.setText(e.wednesdayOpenHour + "-" + e.wednesdayCloseHour);
            txt = (TextView)findViewById(R.id.thursdayHours);
            if (e.thursdayOpenHour.equalsIgnoreCase("00:00") && e.thursdayCloseHour.equalsIgnoreCase("00:00"))
                txt.setText("Closed");
            else
                txt.setText(e.thursdayOpenHour + "-" + e.thursdayCloseHour);
            txt = (TextView)findViewById(R.id.fridayHours);
            if (e.fridayOpenHour.equalsIgnoreCase("00:00") && e.fridayCloseHour.equalsIgnoreCase("00:00"))
                txt.setText("Closed");
            else
                txt.setText(e.fridayOpenHour + "-" + e.fridayCloseHour);
            txt = (TextView)findViewById(R.id.saturdayHours);
            if (e.saturdayOpenHour.equalsIgnoreCase("00:00") && e.saturdayCloseHour.equalsIgnoreCase("00:00"))
                txt.setText("Closed");
            else
                txt.setText(e.saturdayOpenHour + "-" + e.saturdayCloseHour);

            txt = (TextView)findViewById(R.id.locationPhone);
            txt.setText("(" + e.phoneAreaCode + ") " + e.phoneNumber1);
            ImageButton btn = (ImageButton)findViewById(R.id.locationPhoneButton);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + e.phoneAreaCode.replace("-","") + e.phoneNumber1.replace("-","")));
                    startActivity(callIntent);
                }
            });
            btn = (ImageButton)findViewById(R.id.locationOpenInMaps);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%s" /*"geo:%f,%f"*/, e.latitude, e.longitude, Uri.encode(e.locationAddress1));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<List<LCBOEntity>> loader) {

    }
/*
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (supportMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            supportMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (supportMap != null) {
                LatLng storeLatLong = new LatLng( location.latitude, location.longitude);
                MarkerOptions mo = new MarkerOptions()
                        .position( storeLatLong )
                        .title(location.locationName)
                        ;//.snippet(String.valueOf(location.productQuantity) + " " + ProductDetailActivity.lastResult.itemName + " Available");
                supportMap.addMarker( mo );
                supportMap.moveCamera(CameraUpdateFactory.newLatLngZoom(storeLatLong, 15));
            }
        }
    }
*/

    void updateData() {
        TextView txt = (TextView)findViewById(R.id.detailName);
        txt.setText(location.locationName);
        txt = (TextView)findViewById(R.id.locationAddress);
        txt.setText(location.locationAddress1);
        txt = (TextView)findViewById(R.id.locationDistance);
        txt.setText(Float.valueOf(location.distance) + " km Away");
    }
}
