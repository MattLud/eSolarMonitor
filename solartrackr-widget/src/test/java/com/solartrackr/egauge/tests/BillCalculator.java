package com.solartrackr.egauge.tests;

import com.solartrackr.egauge.widget.util.billcalculators.AustinEnergyBillCalculator;
import com.solartrackr.egauge.widget.util.billcalculators.IBillCalculator;

import junit.framework.Assert;

import org.junit.Test;

import java.math.BigDecimal;
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
        cal.set(Calendar.HOUR,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.AM_PM, Calendar.AM);

        Calendar calendar = Calendar.getInstance();
        //http://lg1512.d.lighthousesolar.com/cgi-bin/egauge-show?C&T=1463646300,1462078800,
        HashMap data = new HashMap<>();
        //data.put("T",(int)(calendar.getTimeInMillis()/1000) +"," + (int)(cal.getTimeInMillis()/1000));
        data.put("T","1468451195,1467349235,");

        data.put("C", null);
        URL egauge = buildUrl("egauge-show",data);

        //Assert.assertNotNull(testValue);

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
            System.out.println(url.toString());
            return new URL(url.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        //TODO: Fix
        return null;
    }


}
