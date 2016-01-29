package com.trumpetx.egauge.widget.xml.storeddata;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * <cname t="P">Grid</cname>
 */
@Root(name = "cname")
public class StoredRegisterName {
    @Attribute(name="t")
    private String registerType;

    @Text
    private String name;

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
