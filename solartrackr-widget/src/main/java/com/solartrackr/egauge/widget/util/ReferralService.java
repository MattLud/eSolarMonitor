package com.solartrackr.egauge.widget.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.solartrackr.egauge.widget.R;
import com.solartrackr.egauge.widget.util.support.ReferralToken;
import com.solartrackr.egauge.widget.util.support.ReferralTokenRequest;
import com.solartrackr.egauge.widget.util.tasks.EGaugeApiGetMonthToDate;
import com.solartrackr.egauge.widget.util.tasks.GetReferralToken;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Ref;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by mludlum on 7/23/16.
 */
public class ReferralService implements GetReferralToken.GetReferralTokenResponse {

    private Context ctx;
    public ReferralService(Context context) {
        this.ctx = context;
    }

    public void GetReferralToken(String deviceUUID, String solarSerial , String proxyServerURL)
    {
            ReferralTokenRequest rtr = new ReferralTokenRequest(deviceUUID,solarSerial, proxyServerURL);
            GetReferralToken grt =  new GetReferralToken();
            grt.delegate = this;
            grt.execute(new ReferralTokenRequest[]{rtr});
    }

    @Override
    public void processFinish(ReferralToken output) {
        final RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget_layout);

        Log.i("Referral_Service", output.getToken());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("referral_token", output.getToken());
        String savings = preferences.getString("savings","");
        if(savings != "")
        {
            savings = "$"+savings;
        }
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        // Add data to the intent, the receiving app will decide
        // what to do with it.
        //todo: check if savings are too low - if so, roll back to last month or YTD or just say
        share.putExtra(Intent.EXTRA_TEXT, "I saved "+ savings +" this month with solar! You can save too - Here's who I used for my panels. " + output.getToken());
        PendingIntent sharePendingIntent = PendingIntent.getActivity(ctx, 0, Intent.createChooser(share, "Share link!"), 0);
        views.setOnClickPendingIntent(R.id.share_button, sharePendingIntent);
        editor.commit();
    }
}
