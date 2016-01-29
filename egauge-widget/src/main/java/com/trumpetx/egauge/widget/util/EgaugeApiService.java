package com.trumpetx.egauge.widget.util;

import android.content.Context;

import com.trumpetx.egauge.widget.NotConfiguredException;
import com.trumpetx.egauge.widget.util.tasks.EGaugeApiInstanteousData;
import com.trumpetx.egauge.widget.util.tasks.EGaugeApiStoredData;
import com.trumpetx.egauge.widget.xml.EGaugeResponse;
import com.trumpetx.egauge.widget.xml.storeddata.EGaugeStoredDataResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
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

    public static EgaugeApiService getInstance(String baseUrl) {
        if (singleton == null) {
            singleton = new EgaugeApiService();
        }
        singleton.setUrlBase(baseUrl);

        return singleton;
    }

    private static final String LOG_TAG = "eGaugeApiService";

    //look into having option for this; v1 was used but failed when lightgauge stopped returning gen object


    private String urlBase;


    public EGaugeStoredDataResponse getCurrentBill(int dayBillTurnsOver) throws ExecutionException, InterruptedException
    {


        /** to compute a bill we need the following
         * - day of the month it turns over so we can get history from now to that day last month
         * - what day it currently is
         * - get all kwh usage between that day last month and now.
         */

        int days = this.getDaysSinceBill(dayBillTurnsOver);

        HashMap data = new HashMap<>();
        data.put("n", days+"");
        data.put("D", null);
        URL egauge = buildUrl("egauge-show", data);
        EGaugeStoredDataResponse temp;

        temp  = new EGaugeApiStoredData().execute(new URL[]{egauge}).get();
        return temp;
    }

    public int getDaysSinceBill(int dayBillTurnsOver)
    {
        /** to compute a bill we need the following
         * - day of the month it turns over so we can get history from now to that day last month
         * - what day it currently is
         * - get all kwh usage between that day last month and now.
         */

        int days = 0;
        Date current = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(current);
        int currentDay = c.get(Calendar.DAY_OF_MONTH);
        //if we're still in the same month as our bill, just subtract the days
        if(currentDay>=dayBillTurnsOver)
        {
            days = currentDay - dayBillTurnsOver;
        }
        //if the date it turned over in the previous month, get the number of days...
        else {
            c.add(Calendar.MONTH, -1);
            //TODO: need 28-31st checks
            c.set(Calendar.DAY_OF_MONTH, dayBillTurnsOver);
            c.set(Calendar.HOUR_OF_DAY, 0);

            long timeDays = current.getTime() - c.getTimeInMillis();
            //get our division logic and truncate any remainder
            // 86400000 converts it to hours
            long daysL =  timeDays / (1000 * 60 * 60 * 24);
            days = (int) daysL;
        }
        return days;
    }


    public EGaugeResponse getData() throws ExecutionException, InterruptedException {

        HashMap data = new HashMap<>();
        data.put("inst", null);
        data.put("v1", null);
        URL egauge = buildUrl("egauge",data);
        return new EGaugeApiInstanteousData().execute(new URL[]{egauge}).get();
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
