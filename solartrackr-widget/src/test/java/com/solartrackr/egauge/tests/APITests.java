package com.solartrackr.egauge.tests;


import android.net.Uri;
import android.util.Log;

import com.solartrackr.egauge.widget.util.support.ReferralToken;

import junit.framework.Assert;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by mludlum on 7/30/16.
 */
public class APITests {

    @Test
    public void Test() {
        try {
            HttpURLConnection apiConnection = (HttpURLConnection) new URL("http://192.168.1.106:5000/referralcode").openConnection();
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
                    .appendQueryParameter("deviceID", "abcd")
                    .appendQueryParameter("appVersion", "1.0")
                    .appendQueryParameter("solarDeviceID", "abcd")
                    .appendQueryParameter("proxyServerURL", "abcd");
            String query = builder.build().getEncodedQuery();

            OutputStream os = apiConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            apiConnection.connect();
            String response = "";
            int responseCode = apiConnection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(apiConnection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";

            }


            ReferralToken rt = new ReferralToken(response);
            Assert.assertNotNull(response);
        } catch (
                IOException e
                )

        {
            //disconnect, connect, or other general IO errors.
            e.printStackTrace();
        } catch (
                Exception e
                )

        {
            //issue in input buffer
            e.printStackTrace();
        }
    }
}
