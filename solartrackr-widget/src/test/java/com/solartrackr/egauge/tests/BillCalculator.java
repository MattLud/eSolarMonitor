package com.solartrackr.egauge.tests;

import com.solartrackr.egauge.widget.util.billcalculators.AustinEnergyBillCalculator;
import com.solartrackr.egauge.widget.util.billcalculators.IBillCalculator;
import com.solartrackr.egauge.widget.xml.EGaugeComparison;

import junit.framework.Assert;

import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ludlumm on 1/14/2016.
 */

public class BillCalculator {

    @Test
    public void TestBillCalculator_Summer_Outside() throws ParseException {
        IBillCalculator calc = new AustinEnergyBillCalculator();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date d = sdf.parse("01/07/2015");
        BigDecimal bill =  calc.CalculateBill(1131, 0, false, d);
        Assert.assertEquals(139.38, bill.doubleValue());

        //test tier boundary
        bill =  calc.CalculateBill(1000, 0, false, d);
        Assert.assertEquals(120.39, bill.doubleValue());

        //1kwh charged at top tier rate - should be 14 cents instead of 13 cents higher
        bill =  calc.CalculateBill(1001, 0, false, d);
        Assert.assertEquals(120.53, bill.doubleValue());
    }

    @Test
    public void Test_ComparisonData()
    {

        setUrlBase("http://lg1512.d.lighthousesolar.com/");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        Calendar calendar = Calendar.getInstance();
        //http://lg1512.d.lighthousesolar.com/cgi-bin/egauge-show?C&T=1463646300,1462078800,
        HashMap data = new HashMap<>();
        data.put("T",calendar.getTimeInMillis() +"," + cal.getTimeInMillis());
        data.put("C", null);
        URL egauge = buildUrl("egauge",data);

        EGaugeComparison testValue =  Getdata(new URL[]{egauge});
        Assert.assertNotNull(testValue);

    }

    private String urlBase = "http://lg1512.d.lighthousesolar.com/";

    private void setUrlBase(String urlBase) {
        this.urlBase = (urlBase.endsWith("/") ? urlBase : urlBase + "/") + "cgi-bin/";
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

    public EGaugeComparison Getdata(URL[] params)
    {
        try {

            HttpURLConnection egaugeConnection = (HttpURLConnection) params[0].openConnection();
            egaugeConnection.setConnectTimeout(5000);
            egaugeConnection.setReadTimeout(5000);
            egaugeConnection.setRequestMethod("GET");
            Serializer serializer = new Persister();
            InputStream in = new BufferedInputStream(egaugeConnection.getInputStream());
            EGaugeComparison result = serializer.read(EGaugeComparison.class, in);

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
