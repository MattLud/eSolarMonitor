package com.solartrackr.egauge.widget.util.support;


public class ValueDTO {

    public ValueDTO(String units, String formattedValue) {
        this.Units = units;
        this.FormattedValue = formattedValue;

        this.DisplayableValue = this.FormattedValue + this.Units;
        if (this.Units.equalsIgnoreCase("$")) {
            this.DisplayableValue = this.Units + this.FormattedValue;
        }
    }

    public String Units;
    public String FormattedValue;
    public String DisplayableValue;
}