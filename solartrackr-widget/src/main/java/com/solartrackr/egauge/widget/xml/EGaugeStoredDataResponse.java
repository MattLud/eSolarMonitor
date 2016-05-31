package com.solartrackr.egauge.widget.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/*
<group serial="0x65f48906">
    <data columns="3" time_stamp="0x56aa8f60" time_delta="86400" epoch="0x568c2a5c">
     <cname t="P">Grid</cname>
     <cname t="P">Solar</cname>
     <cname t="P">Solar+</cname>
         <r><c>471753181</c><c>706284460</c><c>707441392</c></r>
         <r><c>485069013</c><c>646222043</c><c>647199003</c></r>
         <r><c>489109261</c><c>588886916</c><c>589681322</c></r>
         <r><c>472000070</c><c>563494780</c><c>564120068</c></r>
         <r><c>470078123</c><c>513397952</c><c>513859033</c></r>
         <r><c>454154117</c><c>457058698</c><c>457342813</c></r>
         <r><c>448931326</c><c>398462843</c><c>398576614</c></r>
         <r><c>461892847</c><c>337607073</c><c>337545087</c></r>
         <r><c>444410825</c><c>285825204</c><c>285581779</c></r>
    </data>
</group>
 */
@Root(name = "group")
public class EGaugeStoredDataResponse {
    @ElementList(inline = true)
    private List<StoredDataNode> dataList;

    public List<StoredDataNode> getDataList() {
        return dataList;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public void setDataList(List<StoredDataNode> dataList) {
        this.dataList = dataList;

    }

    @Attribute
    private String serial;
}
