package com.solartrackr.egauge.widget.util;

import android.content.Context;
import android.util.Log;

import com.solartrackr.egauge.widget.NotConfiguredException;
import com.solartrackr.egauge.widget.util.billcalculators.AustinEnergyBillCalculator;
import com.solartrackr.egauge.widget.util.tasks.EGaugeApiGetMonthToDate;
import com.solartrackr.egauge.widget.util.tasks.EGaugeApiHistoricalData;
import com.solartrackr.egauge.widget.xml.EGaugeResponse;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
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
        URL egauge = buildUrl("egauge",data,false);
        EGaugeResponse hd = new EGaugeApiHistoricalData().execute(egauge).get();
    }

    public BigDecimal getSavingsMonthToDate() throws ExecutionException, InterruptedException {

        Calendar firstOfMonth = Calendar.getInstance();
        firstOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        firstOfMonth.set(Calendar.HOUR, 0);
        firstOfMonth.set(Calendar.MINUTE, 0);
        firstOfMonth.set(Calendar.SECOND, 0);
        firstOfMonth.set(Calendar.MILLISECOND, 0);
        firstOfMonth.set(Calendar.AM_PM, Calendar.AM);

        Calendar currentTime = Calendar.getInstance();
        //http://lg1512.d.lighthousesolar.com/cgi-bin/egauge-show?C&T=1463646300,1462078800,
        HashMap data = new HashMap<>();

        //offset a few  seconds due to CSV issues from android not being in proper time with Egauge device
        data.put("T", (int) ((currentTime.getTimeInMillis()-10000) / 1000) + "," + (int) (firstOfMonth.getTimeInMillis() / 1000));
        data.put("c",null);
        URL egauge = buildUrl("egauge-show",data,false);
        CSVTimePeriod monthToDateValues = new EGaugeApiGetMonthToDate().execute(new URL[]{egauge}).get();
        Log.i(LOG_TAG, "Got generation MTD: " + monthToDateValues.getGeneration());

        return new AustinEnergyBillCalculator().GetSavings(monthToDateValues.getGeneration());

    }


    public EGaugeResponse getData() throws ExecutionException, InterruptedException {

        HashMap data = new HashMap<>();
        data.put("inst", null);
        data.put("v1", null);
        URL egauge = buildUrl("egauge",data,false);
        return new EGaugeApiHistoricalData().execute(new URL[]{egauge}).get();
    }


    private URL buildUrl(String target,  Map<String, String> params,boolean compressed) {
        StringBuilder url = new StringBuilder(urlBase).append(target);
        String appender = "?";
        if(compressed)
        {
            appender+="C&";
        }
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
