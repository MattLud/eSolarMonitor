package com.solartrackr.egauge.widget.util.billcalculators;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by ludlumm on 1/14/2016.
 */
public interface IBillCalculator {
     abstract BigDecimal CalculateBill(int kwhUsed, int kwhSolarProduced,  boolean insideCity, Date date);
}
