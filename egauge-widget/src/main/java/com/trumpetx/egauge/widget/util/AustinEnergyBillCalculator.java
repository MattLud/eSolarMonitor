package com.trumpetx.egauge.widget.util;

import java.util.Date;

/**
 * Created by ludlumm on 1/13/2016.
 * Look at api service for future automated retrieval; no magic numbers.
 */
public class AustinEnergyBillCalculator {

    //fixed service fee; dollars
    private static  double serviceFee = 10;



    //price by kw/h in dollars

    //fixed usage fees
    private static  double powerSupplyAdjustment = .03139;
    private static  double insideRegulatoryCharge = .01414;
    //.00057 added
    private static  double outsideRegulatoryCharge = insideRegulatoryCharge + .00057;
    private static  double communityBenefit = .00554;



    //tier usage fees; inside tiered at 500 kW/h until >2500, outside tiered at 0-500, then 500-1000, >1000
    private static double [] insideSummerRates = new double[]{.033, .080, .091, .110, .114  };
    private static  double [] outsideSummerRates =  new double[]{.03750, .08, .09325};

    private static  double [] insideWinterRates =  new double[]{.018, .056, .072, .084, .096};
    private static  double [] outsideWinterRates =  new double[]{.018, .0560, .0717};

    private static String [] summerMonths = new String [] {"June", "July", "August", "September"};




    //method to compute Austin Energy bill based on month;
    public static double CalculateBill(int kwh, boolean insideCity, Date date){
        double bill = serviceFee;

        if(insideCity)
        {
            bill += (kwh * insideRegulatoryCharge) + (kwh * powerSupplyAdjustment);
        }




        return 0;
    }



}
