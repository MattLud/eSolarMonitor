package com.trumpetx.egauge.widget.util.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.trumpetx.egauge.widget.xml.EGaugeResponse;
import com.trumpetx.egauge.widget.xml.Register;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;



/**
 * Created by ludlumm on 1/8/2016.
 */
public class EGaugeApiHistoricalData extends AsyncTask<URL, Void, EGaugeResponse> {

    public static String LOG_TAG = "API_Async";
    @Override
    protected EGaugeResponse doInBackground(URL[] params) {

        // removed old http client due to deprecation; using google recommended HttpURLconnection
        // Really wish we could get ssl on the data too...
        try {
        Log.d(LOG_TAG, "GET: " + params[0].toString());
        HttpURLConnection egaugeConnection = (HttpURLConnection) params[0].openConnection();
        egaugeConnection.setConnectTimeout(5000);
        egaugeConnection.setReadTimeout(5000);
        egaugeConnection.setRequestMethod("GET");
        Serializer serializer = new Persister();
        InputStream in = new BufferedInputStream(egaugeConnection.getInputStream());
        EGaugeResponse result = serializer.read(EGaugeResponse.class, in);

        in.close();
        egaugeConnection.disconnect();
        return result;
        } catch (IOException e) {
            //disconnect, connect, or other general IO errors.
            e.printStackTrace();
        } catch (Exception e) {
            //issue in input buffer
            e.printStackTrace();
        }
        return null;
    }
}
