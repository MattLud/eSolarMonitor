package com.solartrackr.egauge.widget.util.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.solartrackr.egauge.widget.util.support.ReferralToken;
import com.solartrackr.egauge.widget.util.support.ReferralTokenRequest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by mludlum on 7/26/16.
 */
public class GetReferralToken extends AsyncTask<ReferralTokenRequest, Void, ReferralToken> {
    public static String LOG_TAG = "API_referral";

    @Override
    protected ReferralToken doInBackground(ReferralTokenRequest[] request) {

        try
       {
           HttpURLConnection apiConnection = (HttpURLConnection) new URL("https://api.solartrackr.net/referralurl/short").openConnection();
           apiConnection.setConnectTimeout(5000);
           apiConnection.setReadTimeout(5000);
           apiConnection.setRequestMethod("POST");
           apiConnection.setDoInput(true);
           apiConnection.setDoOutput(true);
/*

deviceID
appVersion
solarDeviceID OR proxyServerURL
 */
           Uri.Builder builder = new Uri.Builder()
                   .appendQueryParameter("deviceID", request[0].getDeviceUUID())
                   .appendQueryParameter("appVersion", "1.0")
                   .appendQueryParameter("solarDeviceID", request[0].getSolarSerial())
                   .appendQueryParameter("proxyServerURL", request[0].getProxyServerURL());
           String query = builder.build().getEncodedQuery();

           OutputStream os = apiConnection.getOutputStream();
           BufferedWriter writer = new BufferedWriter(
                   new OutputStreamWriter(os, "UTF-8"));
           writer.write(query);
           writer.flush();
           writer.close();
           os.close();

           apiConnection.connect();
           String response = "" ;
           int responseCode=apiConnection.getResponseCode();
           if (responseCode == HttpsURLConnection.HTTP_OK) {
               String line;
               BufferedReader br=new BufferedReader(new InputStreamReader(apiConnection.getInputStream()));
               while ((line=br.readLine()) != null) {
                   response+=line;
               }
               br.close();
           }
           else {
               response="N/A";
           }
           apiConnection.disconnect();
           Log.i(LOG_TAG, " API got this back " + response);

           ReferralToken rt = new ReferralToken(response);
           return rt;
       } catch (IOException e) {
           Log.i(LOG_TAG, " API error");
            //disconnect, connect, or other general IO errors.
            e.printStackTrace();
       } catch (Exception e) {
           Log.i(LOG_TAG, " API error");
            //issue in input buffer
            e.printStackTrace();
       }
        return new ReferralToken("N/A");
    }

    public GetReferralTokenResponse delegate=null;

    @Override
    protected void onPostExecute(ReferralToken result) {
        delegate.processFinish(result);
    }

    public interface GetReferralTokenResponse    {
        void processFinish(ReferralToken output);
    }

}
