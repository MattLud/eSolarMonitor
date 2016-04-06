package com.solartrackr.egauge.widget.util.billcalculators;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by ludlumm on 1/13/2016.
 * Look at api service for future automated retrieval; no magic numbers.
 */
public class AustinEnergyBillCalculator implements IBillCalculator{

    //fixed service fee; dollars
    private static  BigDecimal serviceFee = new BigDecimal(10);

    //fixed solar kW/h payout
    //TODO: need stable API to get this; referred to value of solar
    private static BigDecimal solarPayback = new BigDecimal(.109);



    //price by kw/h in dollars

    //fixed usage fees
    private static BigDecimal powerSupplyAdjustment = new BigDecimal(.03139);
    private static  BigDecimal insideRegulatoryCharge = new BigDecimal(.01414);
    //.00057 added
    private static  BigDecimal outsideRegulatoryCharge = insideRegulatoryCharge.add(new BigDecimal(.00057));
    private static  BigDecimal communityBenefit = new BigDecimal(.00554);



    //tier usage fees; inside tiered at 500 kW/h until >2500, outside tiered at 0-500, then 500-1000, >1000
    private static BigDecimal[] insideSummerRates = new BigDecimal[]{new BigDecimal(.033), new BigDecimal(.080), new BigDecimal(.091), new BigDecimal(.110), new BigDecimal(.114)  };
    private static  BigDecimal [] outsideSummerRates =  new BigDecimal[]{new BigDecimal(.03750), new BigDecimal(.08), new BigDecimal(.09325)};

    private static  BigDecimal [] insideWinterRates =  new BigDecimal[]{new BigDecimal(.018), new BigDecimal(.056), new BigDecimal(.072), new BigDecimal(.084), new BigDecimal(.096)};
    private static  BigDecimal [] outsideWinterRates =  new BigDecimal[]{new BigDecimal(.018), new BigDecimal(.0560), new BigDecimal(.0717)};

    private static String [] summerMonths = new String [] {"June", "July", "August", "September"};




    //method to compute Austin Energy bill based on month;
    public BigDecimal CalculateBill(int kwhUsed, int kwhSolarProduced,  boolean insideCity, Date date){
        BigDecimal bdKwhUsed = new BigDecimal(kwhUsed);
        BigDecimal bdKwhSolarProduced = new BigDecimal(kwhSolarProduced);
        BigDecimal bill = serviceFee;
        DateFormat df = new SimpleDateFormat("MMMMM");
        boolean  useSummerRates = Arrays.asList(summerMonths).contains(df.format(date));
        BigDecimal regulatoryCharge;
        BigDecimal [] rateTier;
        int highestTier;


        if(insideCity)
        {
            //0-4 inside tiers
            highestTier = 4;
            regulatoryCharge = insideRegulatoryCharge;
            if(useSummerRates)
            {
                rateTier = insideSummerRates;
            }
            else{
                rateTier = insideWinterRates;
            }
        }
        else{
            //0-2 outside tiers
            highestTier = 2;
            regulatoryCharge = outsideRegulatoryCharge;
            if(useSummerRates)
            {
                rateTier = outsideSummerRates;
            }
            else
            {
                rateTier = outsideWinterRates;
            }
        }

        bill = bill.add(regulatoryCharge.multiply(bdKwhUsed).setScale(2, BigDecimal.ROUND_HALF_UP).add(bdKwhUsed.multiply(powerSupplyAdjustment).setScale(2, BigDecimal.ROUND_HALF_UP)).add(bdKwhUsed.multiply(communityBenefit).setScale(2, BigDecimal.ROUND_HALF_UP)));
        //all tier boundaries are done via 500

        BigDecimal energyUtilization = new BigDecimal(0);
        while(kwhUsed>0)
        {
            //determine tier, with upper bounds being last slot;
            // note that 1001 will still have one kW/h charged and  1 kw/h will still work here.
            int tier= Math.min((kwhUsed - 1)/500, highestTier);
            //if we already 0'ed out the top, use 500
            BigDecimal kwhTier = kwhUsed % 500 == 0 ? new BigDecimal(500) : new BigDecimal(kwhUsed % 500);
            energyUtilization = energyUtilization.add(rateTier[tier].multiply(kwhTier).setScale(2,BigDecimal.ROUND_HALF_UP));
            kwhUsed -= kwhTier.intValue();
        }

        BigDecimal solarCredit = bdKwhSolarProduced.multiply(solarPayback);
        return bill.add(energyUtilization).subtract(solarCredit);
    }



}
