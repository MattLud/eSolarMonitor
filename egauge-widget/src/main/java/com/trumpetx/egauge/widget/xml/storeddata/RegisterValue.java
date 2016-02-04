package com.trumpetx.egauge.widget.xml.storeddata;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by ludlumm on 2/1/2016.
 */
@Root(name="c")
public class RegisterValue {
    @Text
    private long value;

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
