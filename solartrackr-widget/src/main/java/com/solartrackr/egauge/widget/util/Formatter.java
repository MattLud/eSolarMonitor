package com.solartrackr.egauge.widget.util;

import java.text.DecimalFormat;
import java.math.RoundingMode;


public class Formatter
{
    public static float convertToKWHours(String deltaCompressedWattSeconds) {
        // deltaCompressedWattSeconds = same as cValue from historical API with &C option
        //let netKWH = -( ceil( CGFloat(Int(delegate.cValues[ 0].value)!)/3600000.0))
        // - sign as this is delta compressed value
        // string to int to Double
        // divide by 3600000.0 (for floating precision)
        final int val = Integer.parseInt(deltaCompressedWattSeconds, 10);
        return ((float) val) / 3600000f;
    }

    public static ValueDTO asWatts(float arg)
    {
        final String kwLabel = "kW";
        final String wattLabel = "W";

        String label = "";
        String roundedWatts = "";

        if (Math.abs(arg) >= 1000f)
        {
            label = kwLabel;
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);
            roundedWatts = df.format( Formatter.round( arg/1000f, 2));
        }
        else
        {
            label = wattLabel;
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);
            roundedWatts = df.format( Formatter.round( arg, 0));
        }

        // DEBUG
//        System.out.println("Raw: " + String.valueOf( arg));
//        System.out.println("Rounded watts: " + roundedWatts);
//        System.out.println("Units: " + label);

        return new ValueDTO(label, roundedWatts);
    }

    public static ValueDTO asDollars(Double arg) {
        return new ValueDTO("$", Integer.toString(arg.intValue()));
    }

    public static float round(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        float factor = (float) Math.pow(10, places);
        value = value * factor;
        float tmp = Math.round( value);

        return (tmp / factor);
    }
}