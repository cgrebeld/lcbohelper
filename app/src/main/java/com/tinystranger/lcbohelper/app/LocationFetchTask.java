package com.tinystranger.lcbohelper.app;

import android.location.Location;
import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPInputStream;

class LocationFetchTask extends AsyncTask<Location, Void, List<LCBOEntity>> {
    public interface LocationListener {
        void onLocationsFetched(List<LCBOEntity> locations);
    }

    Location location;
    String itemNumber;
    LocationListener listener;


    public LocationFetchTask(Location aLocation, String aItemNumber, LocationListener listener) {
        location = aLocation;
        itemNumber = aItemNumber;
        this.listener = listener;
    }
    @Override
    protected List<LCBOEntity> doInBackground(Location... params) {
        List<LCBOEntity> results = loadInBackground();
        return results;
    }
    @Override
    protected void onPostExecute(List<LCBOEntity> aResults) {
        if (aResults != null )
            listener.onLocationsFetched(aResults);
    }

    public List<LCBOEntity> loadInBackground() {
        List<LCBOEntity> result = null;
        if (location != null) {
            try {
                String url = String.format(
                        "http://stage.lcbo.com/lcbo-webapp/storesearch.do?latitude=%f&longitude=%f"
                        , location.getLatitude(), location.getLongitude());
                if (itemNumber != null) {
                    url += "&itemNumber=" + itemNumber;
                }
                result = loadXmlFromNetwork(url);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

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

    private List<LCBOEntity> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        LCBOQueryParser parser = new LCBOQueryParser();
        List<LCBOEntity> entries = null;
        try {
            stream = new GZIPInputStream(downloadUrl(urlString));
/*
            StringBuilder inputStringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(stream), "UTF-8"));
            String line = bufferedReader.readLine();
            while(line != null){
                inputStringBuilder.append(line);inputStringBuilder.append('\n');
                line = bufferedReader.readLine();
            }
            Log.d("d", inputStringBuilder.toString());
*/
            entries = parser.parse(LCBOQueryParser.QueryType.kStores, stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return entries;
    }
}
