package com.trumpetx.egauge.tests;

import android.graphics.Path;

import com.trumpetx.egauge.widget.util.EgaugeApiService;
import com.trumpetx.egauge.widget.xml.storeddata.CurrentBillInfo;
import com.trumpetx.egauge.widget.xml.storeddata.EGaugeStoredDataResponse;

import junit.framework.Assert;

import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

/**
 * Created by ludlumm on 2/2/2016.
 */
public class EgaugeApiServiceTests {

    @Test
    public void TestStoredData()
    {

        EGaugeStoredDataResponse esdr = this.LoadXmlFromFile("./src/test/resources/TestData.xml");
        Assert.assertNotNull(esdr);
        EgaugeApiService api = EgaugeApiService.getInstance("");
        CurrentBillInfo billTotal = api.ProcessHistoricalData(esdr, false,false);
        Assert.assertEquals("1.01", billTotal.getCurrentBill().toPlainString());
    }




    private EGaugeStoredDataResponse LoadXmlFromFile(String testFile)
    {
        Serializer serializer = new Persister();
        InputStream in = null;
        try {
            in = new FileInputStream(testFile);
            EGaugeStoredDataResponse result = serializer.read(EGaugeStoredDataResponse.class, in);
            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
