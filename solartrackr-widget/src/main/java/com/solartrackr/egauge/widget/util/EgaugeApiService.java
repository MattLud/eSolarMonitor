package com.solartrackr.egauge.widget.util;

import android.content.Context;
import android.util.Log;

import com.solartrackr.egauge.widget.NotConfiguredException;
import com.solartrackr.egauge.widget.util.billcalculators.AustinEnergyBillCalculator;
import com.solartrackr.egauge.widget.util.tasks.EGaugeApiComparisonData;
import com.solartrackr.egauge.widget.util.tasks.EGaugeApiHistoricalData;
import com.solartrackr.egauge.widget.xml.EGaugeComparison;
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
        setUrlBase("http://lg1512.d.lighthousesolar.com/");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.AM_PM, Calendar.AM);

        Calendar calendar = Calendar.getInstance();
        //http://lg1512.d.lighthousesolar.com/cgi-bin/egauge-show?C&T=1463646300,1462078800,
        HashMap data = new HashMap<>();

        data.put("T",(int)(calendar.getTimeInMillis()/1000) +"," + (int)(cal.getTimeInMillis()/1000));
        URL egauge = buildUrl("egauge-show",data,true);
        EGaugeComparison monthToDateValues = new EGaugeApiComparisonData().execute(new URL[]{egauge}).get();
        return new AustinEnergyBillCalculator().GetSavings(Math.abs(monthToDateValues.getMTDValues().get(2))/3600000);

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
