package com.trumpetx.egauge.widget.xml.storeddata;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 <r>
     <c>444410825</c>
     <c>285825204</c>
     <c>285581779</c>
 </r>
 */
@Root(name="r")
public class HistoricalData {

    public String getRegisterValue() {
        return registerValue;
    }

    public void setRegisterValue(String registerValue) {
        this.registerValue = registerValue;
    }

    @ElementList(name="c")
    private String registerValue;




}
