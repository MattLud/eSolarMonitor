package com.trimpetx.egauge.tests;

import com.trumpetx.egauge.widget.util.billcalculators.AustinEnergyBillCalculator;
import com.trumpetx.egauge.widget.util.billcalculators.IBillCalculator;

import junit.framework.Assert;

import org.junit.Test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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


}
