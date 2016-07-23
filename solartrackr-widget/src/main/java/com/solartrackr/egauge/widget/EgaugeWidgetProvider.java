package com.solartrackr.egauge.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.solartrackr.egauge.widget.util.EgaugeApiService;
import com.solartrackr.egauge.widget.util.EgaugeIntents;
import com.solartrackr.egauge.widget.util.NetworkConnection;
import com.solartrackr.egauge.widget.util.ReferralService;
import com.solartrackr.egauge.widget.util.extensions.DateExtensions;
import com.solartrackr.egauge.widget.xml.EGaugeResponse;
import com.solartrackr.egauge.widget.xml.Register;
import com.solartrackr.egauge.widget.util.Formatter;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class EgaugeWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = "eGaugeWidget";
    private static final String POWER = "P";
    private DateFormat df = new SimpleDateFormat("hh:mma");
    private static final String ROTATE_RIGHT_DISPLAY = "ROTATE_RIGHT_DISPLAY";
    private static final String SHARE = "SHARE";
    //private static final String ROTATE_LEFT_DISPLAY = "ROTATE_LEFT_DISPLAY";

    private static final String [] rotateList = new String [] {"production", "usage", "net_usage", "savings"};

    /**
     * Called when an update intent is received and also called by onReceive when our clock manager calls the method.
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @Override
    public void onUpdate(Context context, final AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean enableSync = preferences.getBoolean("enable_sync_checkbox", false) && NetworkConnection.hasNetworkConnection(context);

        final boolean showSettings = preferences.getBoolean("show_settings_checkbox", true);
        final boolean showRefresh = preferences.getBoolean("show_refresh_checkbox", true);
        final String displayPreference = preferences.getString("right_display_option_list", "net_usage");

        Log.i(LOG_TAG, "Pulled following preference " + displayPreference);
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        //check if we should enable share button!
        //TODO: Cleanup regenerated text on every build; use intermediate intent
        if(true)
        {

            views.setViewVisibility(R.id.share_button, View.VISIBLE);
            String token = preferences.getString("referral_token",null);
            if(token == null)
            {
                ReferralService rs = new ReferralService();
                token = rs.GetReferralToken();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("referral_token",token);
                editor.commit();
            }
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");

            // Add data to the intent, the receiving app will decide
            // what to do with it.
            //todo: add money savings, etc.
            share.putExtra(Intent.EXTRA_SUBJECT, "I saved  this month with solar!");
            share.putExtra(Intent.EXTRA_TEXT, "You can save too with! ");

            PendingIntent sharePendingIntent = PendingIntent.getActivity(context, 0, Intent.createChooser(share, "Share link!"), 0);
            views.setOnClickPendingIntent(R.id.share_button, sharePendingIntent );
        }


        views.setOnClickPendingIntent(R.id.displayLabel, getPendingSelfIntent(context, ROTATE_RIGHT_DISPLAY));
        views.setOnClickPendingIntent(R.id.lbl_display, getPendingSelfIntent(context, ROTATE_RIGHT_DISPLAY));

        if (showRefresh) {
            views.setViewVisibility(R.id.refresh_button, View.VISIBLE);
            views.setOnClickPendingIntent(R.id.refresh_button, EgaugeIntents.createRefreshPendingIntent(context));
        } else {
            views.setViewVisibility(R.id.refresh_button, View.GONE);
        }


        //change this to something better
        EGaugeResponse object = null;
        BigDecimal kwhSavings = null;
        String refreshTime = df.format(new Date());
        if (enableSync) {
            try {
                EgaugeApiService apiService = EgaugeApiService.getInstance(context);
                //get snapshot of usage
                object = apiService.getData();
                kwhSavings = apiService.getSavingsMonthToDate();
                //get last bill total
                //hard coded to 16
                //leftObject = apiService.getCurrentBill(billTurnOverDate, insideCityOfAustin);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (NotConfiguredException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


            if (object ==null || kwhSavings == null) {
                //views.setTextViewText(R.id.displayLabel, (String) object);
                for (final int appWidgetId : appWidgetIds) {
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            } else if (object instanceof EGaugeResponse) {

                long[] powerValues = GetProperRegisters(preferences, (EGaugeResponse) object);

                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(rotateList[0], powerValues[0]);
                editor.putLong(rotateList[1], powerValues[1]);
                // this way we init our BigDecimal() from a string constructor arg
                editor.putString(rotateList[3], kwhSavings.toString());
                editor.putString("time", refreshTime);
                editor.commit();

                String[] rightDisplayValue = SetDisplay(displayPreference, powerValues, kwhSavings);
                DrawUpdate(views, rightDisplayValue, appWidgetIds, appWidgetManager,refreshTime);
            }
        } else {
            Log.i(LOG_TAG, "eGauge sync not enabled.");
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        enableWidget(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        disableWidget(context);
    }


    private void DrawUpdate(RemoteViews views, String[] rightDisplayValue, int [] appWidgetIds, AppWidgetManager appWidgetManager, String time) {

        for (final int appWidgetId : appWidgetIds) {
            views.setTextViewText(R.id.lbl_display, rightDisplayValue[0]);
            views.setTextViewText(R.id.displayLabel, rightDisplayValue[1]);
            views.setTextViewText(R.id.lastUpdated, time);


            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private long[]GetProperRegisters(SharedPreferences preferences, EGaugeResponse response)
    {
        Map<String, Register> registerNames = new HashMap<>();
        final String[] gridRegisters = preferences.getString("egauge_grid_register_text", "Grid").trim().split("\\s*,\\s*");
        final String[] solarRegisters = preferences.getString("egauge_solar_register_text", "Solar").trim().split("\\s*,\\s*");

        //Filter out non-grid or non-solar(don't use if it has '+' at the end.) and has POWER cname type(see egauge api sheet and register types)
        for (Register register : response.getRegisters()) {
            if (POWER.equals(register.getType()) && !register.getName().endsWith("+")) {
                registerNames.put(register.getName(), register);
            }
        }

        //Over write the matching names, if had a plus, then use that register to overwrite the previous one. (todo:show example!)
        for (Register register : response.getRegisters()) {
            if (POWER.equals(register.getType()) && register.getName().endsWith("+")) {
                String nonPlusName = register.getName().substring(0, register.getName().length() - 1);
                registerNames.put(nonPlusName, register); // Overwrite the non-positive only register (don't want to double count)
            }
        }

        long gridTotal = 0;
        for (String registerName : gridRegisters) {
            if (registerNames.containsKey(registerName)) {
                gridTotal += registerNames.get(registerName).getRateOfChange();
            }
        }

        long generationTotal = 0;
        for (String registerName : solarRegisters) {
            if (registerNames.containsKey(registerName)) {
                long rateOfChange = registerNames.get(registerName).getRateOfChange();
                // This is probably already the case (the + sign register); however, just in case...
                if (rateOfChange > 0) {
                    generationTotal += rateOfChange;
                }
            }
        }

        return new long[]{gridTotal, generationTotal};
    }


    private void rotateDisplay(Context context, Intent intent, String leftOrRight){
        //widget id is tacked onto end of action
        String id = intent.getAction();
        AppWidgetManager appWidgetManager =  AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, EgaugeWidgetProvider.class));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        String displayPreference;
        String[] options;

        displayPreference = preferences.getString("right_display_option_list", "net_usage");
        options = rotateList;

        int index = Arrays.asList(options).indexOf(displayPreference)+1;
        if(index>= options.length)
        {
            index = 0;
        }
        String newDisplayPref = options[index];


        Log.i(LOG_TAG, "Rotating display on widgets to " + newDisplayPref);
        //save our new preference

        SharedPreferences.Editor editor = preferences.edit();

        if(leftOrRight.equals(ROTATE_RIGHT_DISPLAY)) {

            long[] powerValues = new long[]{preferences.getLong(rotateList[0], 0), preferences.getLong(rotateList[1], 0)};
            BigDecimal savings = new BigDecimal( preferences.getString(rotateList[3], "0"));
            String[] display = SetDisplay(newDisplayPref, powerValues, savings);
            String time = preferences.getString("time", df.format(new Date()));

            DrawUpdate(new RemoteViews(context.getPackageName(), R.layout.widget_layout), display, appWidgetIds, appWidgetManager, time);

            editor.putString("right_display_option_list", newDisplayPref);
        }
        editor.commit();
    }

    private String[] SetDisplay(String displayPreference, long[] powerValues, BigDecimal dollarSavings)
    {
        long gridTotal = powerValues[0];
        long generationTotal = powerValues[1];
        long usageTotal = gridTotal + generationTotal;

        String displayValue = "";
        String label = "";

        Log.i(LOG_TAG, "Matching following display " + displayPreference );
        //Also provide info on solar produced vs kwh consumed - you may not be able to do net metering!
        switch (displayPreference) {
            case "usage":
                //move to strings
                label = "Home";
                displayValue = Formatter.asWatts( ((float)usageTotal)).DisplayableValue;
                break;
            case "production":
                //Panel output
                label = "Solar";
                displayValue = Formatter.asWatts( ((float)generationTotal)).DisplayableValue;
                break;
            case "savings":
                final String month = DateExtensions.AsShortMonth( new Date());
                label = String.format("Savings (%s)", month);
                displayValue = Formatter.asDollars( dollarSavings).DisplayableValue;
                break;
            case "net_usage":
            default:
                label = "Net";
                displayValue = Formatter.asWatts( ((float)gridTotal)).DisplayableValue;
        }

        return new String[]{label, displayValue};
    }

    private void enableWidget(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enableSync = preferences.getBoolean("enable_sync_checkbox", false) && NetworkConnection.hasNetworkConnection(context);
        int refreshIntervalSeconds = -1;
        try {
            refreshIntervalSeconds = Integer.parseInt(preferences.getString("sync_frequency_list", "300"));
        } catch (NumberFormatException e) {
        }

        if (enableSync && refreshIntervalSeconds > 0) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.SECOND, 1);
            alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 1000 * refreshIntervalSeconds, EgaugeIntents.createRefreshPendingIntent(context));
            Log.d(LOG_TAG, "eGauge Widget timer set to update widget every " + refreshIntervalSeconds + " seconds");
        }
    }

    private void disableWidget(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(EgaugeIntents.createRefreshPendingIntent(context));
        Log.d(LOG_TAG, "Disabled eGauge Widget timer");
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        disableWidget(context);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(LOG_TAG, "Received intent " + intent);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);

        if (EgaugeIntents.EGAUGE_WIDGET_UPDATE.equals(intent.getAction())) {
            onUpdate(context, appWidgetManager, ids);
        } else if ("eGaugePreferencesUpdated".equals(intent.getAction())) {
            disableWidget(context);
            enableWidget(context);
        } else if (ROTATE_RIGHT_DISPLAY.equals(intent.getAction()))
        {
            rotateDisplay(context, intent, ROTATE_RIGHT_DISPLAY);
        }

    }

    protected PendingIntent getPendingSelfIntent(Context ctx, String action)
    {
        Intent intent = new Intent(ctx, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(ctx, 0, intent, 0);
    }
}