package com.solartrackr.egauge.widget.util;


public class ValueDTO {

    public ValueDTO(String units, String formattedValue) {
        this.Units = units;
        this.FormattedValue = formattedValue;

        this.DisplayableValue = this.FormattedValue + this.Units;
    }

    public String Units;
    public String FormattedValue;
    public String DisplayableValue;
}