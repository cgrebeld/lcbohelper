package com.tinystranger.lcbohelper.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Created by chris on 5/4/2014.
 */
class LCBOXmlLoader extends AsyncTaskLoader<List<LCBOEntity>> {
    private String url;
    private LCBOQueryParser.QueryType queryType;
    public LCBOXmlLoader(Context context, Bundle args, LCBOQueryParser.QueryType QueryType) {
        super(context);
        // do some initializations here
        url = args.getString("url");
        queryType = QueryType;
    }

    public List<LCBOEntity> loadInBackground() {
        List<LCBOEntity> result = null;
        try {
            result = loadXmlFromNetwork(url);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
            entries = parser.parse(queryType, stream);
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
