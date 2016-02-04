package com.trumpetx.egauge.widget.xml.storeddata;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 <r>
     <c>444410825</c>
     <c>285825204</c>
     <c>285581779</c>
 </r>
 */
@Root(name="r")
public class HistoricalData {

    public List<RegisterValue> getRegisterValue() {
        return registerValue;
    }

    public void setRegisterValue(List<RegisterValue> registerValue) {
        this.registerValue = registerValue;
    }

    @ElementList(inline = true)
    private List<RegisterValue> registerValue;




}
