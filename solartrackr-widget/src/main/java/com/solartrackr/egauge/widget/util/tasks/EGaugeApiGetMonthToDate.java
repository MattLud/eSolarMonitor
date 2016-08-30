package com.solartrackr.egauge.widget.util.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.solartrackr.egauge.widget.util.support.CSVTimePeriod;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by mludlum on 5/26/16.
 */



public class EGaugeApiGetMonthToDate extends AsyncTask<URL, Void, CSVTimePeriod> {

    public interface EgaugeApiGetMonthToDateResponse{
        void bucketResponse(CSVTimePeriod output);
    }

    public  EgaugeApiGetMonthToDateResponse delegate=null;

//    @Override
//    protected void onPostExecute(CSVTimePeriod result) {
//        delegate.bucketResponse(result);
//    }

    public static String LOG_TAG = "API_Async";


    @Override
    protected CSVTimePeriod doInBackground(URL[] params) {

        // removed old http client due to deprecation; using google recommended HttpURLconnection
        // Really wish we could get ssl on the data too...
        try {
            Log.i(LOG_TAG, "GET: " + params[0].toString());
            HttpURLConnection egaugeConnection = (HttpURLConnection) params[0].openConnection();
            egaugeConnection.setConnectTimeout(5000);
            egaugeConnection.setReadTimeout(5000);
            InputStream in = new BufferedInputStream(egaugeConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();
            CSVTimePeriod earlier=null,later = null;
            int lineCount =0;
            do{
                String[] RowData = line.split(",");
                //time starts with newer timestamp
                if (lineCount==1)
                {
                    later = new CSVTimePeriod();
                    later.setTime(Double.parseDouble(RowData[0]));
                    later.setUsage(Double.parseDouble(RowData[1]));
                    later.setGeneration(Double.parseDouble(RowData[2]));
                }
                else if (lineCount==2){
                    earlier = new CSVTimePeriod();
                    earlier.setTime(Double.parseDouble(RowData[0]));
                    earlier.setUsage(Double.parseDouble(RowData[1]));
                    earlier.setGeneration(Double.parseDouble(RowData[2]));
                }

                line = reader.readLine();
                lineCount++;
            }while(line != null);

            reader.close();
            in.close();
            egaugeConnection.disconnect();
            return new CSVTimePeriod(
                    later.getTime()-earlier.getTime(),
                    later.getUsage()-earlier.getUsage(),
                    later.getGeneration()-earlier.getGeneration()
            );

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
