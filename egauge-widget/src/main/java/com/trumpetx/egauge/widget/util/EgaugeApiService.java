package com.trumpetx.egauge.widget.util;

import android.content.Context;

import com.trumpetx.egauge.widget.NotConfiguredException;
import com.trumpetx.egauge.widget.util.billcalculators.AustinEnergyBillCalculator;
import com.trumpetx.egauge.widget.util.tasks.EGaugeApiInstanteousData;
import com.trumpetx.egauge.widget.util.tasks.EGaugeApiStoredData;
import com.trumpetx.egauge.widget.xml.EGaugeResponse;
import com.trumpetx.egauge.widget.xml.storeddata.CurrentBillInfo;
import com.trumpetx.egauge.widget.xml.storeddata.EGaugeStoredDataResponse;
import com.trumpetx.egauge.widget.xml.storeddata.HistoricalData;
import com.trumpetx.egauge.widget.xml.storeddata.StoredDataNode;

import java.math.BigDecimal;
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


    public CurrentBillInfo getCurrentBill(int dayBillTurnsOver, boolean insideAustin) throws Exception {


        /** to compute a bill we need the following
         * - day of the month it turns over so we can get history from now to that day last month
         * - what day it currently is
         * - get all kwh usage between that day last month and now.
         */

        int days = this.getDaysSinceBill(dayBillTurnsOver);

        HashMap data = new HashMap<>();
        //TODO - do time zeroing so usage is measured @ time the bill turned over at. may need to add a day and zero time out to midnight
        //think about DST also as you get an hour of missed time once a year - not a great deal as the bill maybe a couple of kWh short.
        data.put("n", days+"");
        data.put("d", null);
        //ensures that we get an easy index for use!
        data.put("a", null);
        //TODO: add parameter to get only day bill turned over; not inbetween time
        URL egauge = buildUrl("egauge-show", data);
        EGaugeStoredDataResponse temp;

        //populated object with historical data
        temp  = new EGaugeApiStoredData().execute(new URL[]{egauge}).get();
        if(temp==null)
        {
            throw new Exception("Error obtaining historical data");
        }
        return ProcessHistoricalData(temp,false, insideAustin);
    }

    //note that we'll only support one register aggregation; need to also break out of Austin Energy billing
    public CurrentBillInfo ProcessHistoricalData(EGaugeStoredDataResponse resp, boolean useNetMeter, boolean insideCityOfAustin)
    {
        int solarKwh = 0;
        int used= 0;
        int solarGrid = 1;
        int produced = 0;
        long currentUsage =  Long.MIN_VALUE;
        long currentProduced = Long.MIN_VALUE;

        long previousUsage = Long.MAX_VALUE;
        long previousProduced = Long.MAX_VALUE;


        //at some point actually scan them and set the index
        //nodeData.getRegister_names()



        //default is 0....


        StoredDataNode nodeData = resp.getDataList().get(0);

        //FIXME: Need to compute the daily cost as days that turnover into summer billing; confirm Scenario
        //get first record(most current reading)
        //HistoricalData todayData = nodeData.getHistoricalData().get(0);
        ////get last record(day bill turned over)
        HistoricalData billDayUsage = nodeData.getHistoricalData().get(nodeData.getHistoricalData().size() - 1);

        //simple framework doesn't give us an "in- order" historical array; find our min/max
        for (HistoricalData dataNode :nodeData.getHistoricalData()) {
            long use = dataNode.getRegisterValue().get(produced).getValue();
            long solr = dataNode.getRegisterValue().get(solarGrid).getValue();
            if (currentUsage<=use)
            {
                currentUsage =use;
            }
            if(previousUsage>=use)
            {
                previousUsage = use;
            }

            if (currentProduced<=solr)
            {
                currentProduced =solr;
            }
            if(previousProduced>=solr)
            {
                previousProduced = solr;
            }
        }



        //3600000 to convert from watt-seconds to kWh
        solarKwh = Integer.parseInt(((currentProduced -  previousProduced)/3600000) + "");
        used = Integer.parseInt(((currentUsage - previousUsage) / 3600000) + "");

        //note that austin doesn't actually do this...
        if(useNetMeter)
        {

        }
        else{
            CurrentBillInfo bill = new CurrentBillInfo();

            bill.setCurrentBill(new AustinEnergyBillCalculator().CalculateBill((int) used, (int) solarKwh, insideCityOfAustin, new Date()));
            bill.setKwhConsumed(used);
            bill.setKwhProduced(solarKwh);
            return bill;
        }
        return null;
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
