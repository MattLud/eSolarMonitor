package com.trumpetx.egauge.widget.util;

import android.content.Context;
import android.util.Log;
import com.trumpetx.egauge.widget.NotConfiguredException;
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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
    private static final Map<String, String> DATA;
    //look into having option for this; v1 was used but failed when lightgauge stopped returning gen object
    static {
        DATA = new HashMap<>();
        DATA.put("inst", null);

        DATA.put("v1", null);
    }

    private String urlBase;

    //look into replacing with
    public void getData(final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.callback(getXml("egauge", DATA));
                } catch (Exception e) {
                    //should look into letting setting this to error object so widget can properly handle UI
                    callback.callback(e.getMessage());
                }
            }
        }, "GetDataThread").start();
    }

    private Object getXml(String target, Map<String, String> params) throws Exception {
        StringBuilder url = new StringBuilder(urlBase).append(target);
        String appender = "?";
        for (Map.Entry<String, String> entry : params.entrySet()) {
            url.append(appender).append(entry.getKey());
            if (entry.getValue() != null) {
                url.append("=").append(entry.getValue());
            }
            appender = "&";
        }

        // removed old http client due to deprecation; using google recommended HttpURLconnection
        // Really wish we could get ssl on the data too...
        URL egauge = new URL(url.toString());
        Log.d(LOG_TAG, "GET: " + url.toString());
        HttpURLConnection egaugeConnection = (HttpURLConnection) egauge.openConnection();
        egaugeConnection.setConnectTimeout(5000);
        egaugeConnection.setReadTimeout(5000);
        egaugeConnection.setRequestMethod("GET");
        Serializer serializer = new Persister();
        InputStream in = new BufferedInputStream(egaugeConnection.getInputStream());
        EGaugeResponse result = new EGaugeResponse();
        result = serializer.read(EGaugeResponse.class, in);
        in.close();
        egaugeConnection.disconnect();
        return result;
    }

    private void setUrlBase(String urlBase) {
        this.urlBase = (urlBase.endsWith("/") ? urlBase : urlBase + "/") + "cgi-bin/";
    }
}
