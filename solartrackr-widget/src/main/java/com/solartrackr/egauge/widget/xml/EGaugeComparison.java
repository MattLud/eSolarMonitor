package com.solartrackr.egauge.widget.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 * Created by mludlum on 5/25/16.
 */
@Root(name = "data", strict = false)
public class EGaugeComparison {
    /*
<group serial="0x65f48906">
<data columns="3" time_stamp="0x573d785c" time_delta="60" delta="true" epoch="0x568c2a5c">
 <cname t="P">Grid</cname>
 <cname t="P">Solar</cname>
 <cname t="P">Solar+</cname>
</data>
<data time_stamp="0x573d785c" time_delta="60">
 <r>
 <c>121585688</c>
 <c>7094219202</c>
 <c>7114255581</c>
 </r>
</data>
<data time_stamp="0x57258d50" time_delta="60">
 <r>
 <c>-222214687</c>
 <c>-1038347707</c>
 <c>-1041263960</c></r>
</data>
</group>
 */

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    @Attribute
    private String serial;

    @Element(name = "v")
    @Path("r[1]")
    private long Grid;

    public long getGrid() {
        return Grid;
    }

    public void setGrid(long grid) {
        Grid = grid;
    }

    public long getSolar() {
        return SolarPlus;
    }

    public void setSolar(long olderRecord) {
        SolarPlus = olderRecord;
    }

    @Element(name = "v")
    @Path("r[2]")
    private long Solar;

    public long getSolarPlus() {
        return SolarPlus;
    }

    public void setSolarPlus(long solarPlus) {
        SolarPlus = solarPlus;
    }

    @Element(name = "v")
    @Path("r[3]")
    private long SolarPlus;




}