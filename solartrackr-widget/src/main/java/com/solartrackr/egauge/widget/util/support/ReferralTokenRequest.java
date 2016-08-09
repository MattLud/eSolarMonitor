package com.solartrackr.egauge.widget.util.support;

/**
 * Created by mludlum on 7/27/16.
 */
public class ReferralTokenRequest {
    public ReferralTokenRequest(String deviceUUID, String solarSerial, String proxyServerURL) {
        this.deviceUUID = deviceUUID;
        this.solarSerial = solarSerial;
        this.proxyServerURL = proxyServerURL;
    }

    private String deviceUUID;
    private String solarSerial;
    private String proxyServerURL;

    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }

    public String getSolarSerial() {
        return solarSerial;
    }

    public void setSolarSerial(String solarSerial) {
        this.solarSerial = solarSerial;
    }

    public String getProxyServerURL() {
        return proxyServerURL;
    }

    public void setProxyServerURL(String proxyServerURL) {
        this.proxyServerURL = proxyServerURL;
    }
}
