package com.trumpetx.egauge.widget.xml.storeddata;

import java.math.BigDecimal;

/**
 * Created by ludlumm on 2/2/2016.
 */
public class CurrentBillInfo {
    private BigDecimal currentBill;
    private int kwhProduced;
    private int kwhConsumed;

    public BigDecimal getCurrentBill() {
        return currentBill;
    }

    public void setCurrentBill(BigDecimal currentBill) {
        this.currentBill = currentBill;
    }

    public int getKwhProduced() {
        return kwhProduced;
    }

    public void setKwhProduced(int kwhProduced) {
        this.kwhProduced = kwhProduced;
    }

    public int getKwhConsumed() {
        return kwhConsumed;
    }

    public void setKwhConsumed(int kwhConsumed) {
        this.kwhConsumed = kwhConsumed;
    }
}
