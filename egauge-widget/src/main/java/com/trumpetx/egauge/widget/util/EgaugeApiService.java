package com.trumpetx.egauge.widget.util;

import android.content.Context;
import android.util.Log;
import com.trumpetx.egauge.widget.NotConfiguredException;
import com.trumpetx.egauge.widget.util.tasks.EGaugeApiHistoricalData;
import com.trumpetx.egauge.widget.xml.EGaugeResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class EgaugeApiService {

    private static EgaugeApiService singleton;

    public static EgaugeApiService getInstance(Context context) throws NotConfiguredException {
        if (singleton == null) {
            singleton = new EgaugeApiService();
        }
        singleton.setUrlBase(PreferencesUtil.getEgaugeUrl(context));

        return singleton;
    }

    private static final String LOG_TAG = "eGaugeApiService";

    //look into having option for this; v1 was used but failed when lightgauge stopped returning gen object


    private String urlBase;


    public void getCurrentBill(int dataa) throws ExecutionException, InterruptedException
    {
        HashMap data = new HashMap<>();
        data.put("n", 30);
        data.put("D", null);
        URL egauge = buildUrl("egauge",data);
        EGaugeResponse hd = new EGaugeApiHistoricalData().execute(egauge).get();
    }


    public EGaugeResponse getData() throws ExecutionException, InterruptedException {

        HashMap data = new HashMap<>();
        data.put("inst", null);
        data.put("v1", null);
        URL egauge = buildUrl("egauge",data);
        return new EGaugeApiHistoricalData().execute(new URL[]{egauge}).get();
    }


    private URL buildUrl(String target,  Map<String, String> params) {
        StringBuilder url = new StringBuilder(urlBase).append(target);
        String appender = "?";
        for (Map.Entry<String, String> entry : params.entrySet()) {
            url.append(appender).append(entry.getKey());
            if (entry.getValue() != null) {
                url.append("=").append(entry.getValue());
            }
            appender = "&";
        }
        try {
            return new URL(url.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        //TODO: Fix
        return null;
    }

    private void setUrlBase(String urlBase) {
        this.urlBase = (urlBase.endsWith("/") ? urlBase : urlBase + "/") + "cgi-bin/";
    }
}
