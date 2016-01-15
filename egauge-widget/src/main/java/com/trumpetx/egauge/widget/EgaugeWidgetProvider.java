package com.trumpetx.egauge.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.trumpetx.egauge.widget.util.Callback;
import com.trumpetx.egauge.widget.util.EgaugeApiService;
import com.trumpetx.egauge.widget.util.EgaugeIntents;
import com.trumpetx.egauge.widget.util.NetworkConnection;
import com.trumpetx.egauge.widget.xml.EGaugeResponse;
import com.trumpetx.egauge.widget.xml.Register;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class EgaugeWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = "eGaugeWidget";
    private static final String POWER = "P";
    private DateFormat df = new SimpleDateFormat("hh:mma");
    private static final String rotateCick = "rotateCick";


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
        //Add multiple fields in the future, one for solar payback/net meter/tier setup/auto config provider...etc. Likely will need it's own thing
        final boolean enableBillCalculate = preferences.getBoolean("enable_bill_calculate", true);
        final boolean insideCityOfAustin = preferences.getBoolean("inside_city_of_austin", true);

        final boolean showTime = preferences.getBoolean("show_time_checkbox", true);
        final boolean showSettings = preferences.getBoolean("show_settings_checkbox", true);
        final boolean showRefresh = preferences.getBoolean("show_refresh_checkbox", true);
        final String[] gridRegisters = preferences.getString("egauge_grid_register_text", "Grid").trim().split("\\s*,\\s*");
        final String[] solarRegisters = preferences.getString("egauge_solar_register_text", "Solar").trim().split("\\s*,\\s*");
        final String displayPreference = preferences.getString("display_option_list", "net_usage");




        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        if (showSettings) {
            views.setViewVisibility(R.id.settings_button, View.VISIBLE);
            views.setOnClickPendingIntent(R.id.settings_button, EgaugeIntents.createSettingsPendingIntent(context));
        } else {
            views.setViewVisibility(R.id.settings_button, View.GONE);
        }
        views.setOnClickPendingIntent(R.id.displayLabel,getPengingSelfIntent(context, rotateCick));
        if (showRefresh) {
            views.setViewVisibility(R.id.refresh_button, View.VISIBLE);
            views.setOnClickPendingIntent(R.id.refresh_button, EgaugeIntents.createRefreshPendingIntent(context));
        } else {
            views.setViewVisibility(R.id.refresh_button, View.GONE);
        }



        if (enableSync) {
            for (final int appWidgetId : appWidgetIds) {
                Log.i(LOG_TAG, "Updating eGauge widgets " + appWidgetId);
                try {
                    //Note - look into replacing with asynctask
                    EgaugeApiService apiService = EgaugeApiService.getInstance(context);
                    Object object = apiService.getData();

                    if (showTime) {
                        views.setTextViewText(R.id.updatedLabel, df.format(new Date()));
                        views.setViewVisibility(R.id.updatedLabel, View.VISIBLE);
                    } else {
                        views.setViewVisibility(R.id.updatedLabel, View.GONE);
                    }
                    if (object instanceof String) {
                        views.setTextViewText(R.id.displayLabel, (String) object);
                    } else if (object instanceof EGaugeResponse) {

                        SetDisplay(displayPreference);

                        views.setTextViewText(R.id.lbl_display, label);
                        views.setTextViewText(R.id.displayLabel, displayValue + " " + Register.REGISTER_TYPE_LABELS.get(POWER));
                        views.setTextColor(R.id.displayLabel, (displayValue > 0) ? Color.GREEN : Color.RED);

                            }
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                } catch (NotConfiguredException nce) {
                    Toast.makeText(context, nce.getMessage(), Toast.LENGTH_SHORT);
                } catch (InterruptedException e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
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

    private AbstractMap.SimpleEntry<String,String> SetDisplay(String displayPreference, EGaugeResponse object)
    {
        AbstractMap.SimpleEntry entry;

        Map<String, Register> registerNames = new HashMap<>();

        //Filter out non-grid or non-solar(don't use if it has '+' at the end.) and has POWER cname type(see egauge api sheet and register types)
        for (Register register : ((EGaugeResponse) object).getRegisters()) {
            if (POWER.equals(register.getType()) && !register.getName().endsWith("+")) {
                registerNames.put(register.getName(), register);
            }
        }

        //Over write the matching names, if had a plus, then use that register to overwrite teh previous one. (todo:show example!)
        for (Register register : ((EGaugeResponse) object).getRegisters()) {
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

        long usageTotal = gridTotal + generationTotal;

        long displayValue;
        String label = "";
        //TODO: stick this in cache for rotate to use

        switch (displayPreference) {
            case "usage":
                //move to strings
                label = "House Use";
                displayValue = usageTotal * -1;
                break;
            case "production":
                //Panel output
                label = "Solar Prod";
                displayValue = generationTotal;
                break;
            case "net_usage":
            default:
                displayValue = gridTotal * -1;
                label = "Net Usage";
        }

        switch (displayPreference) {
            case "usage":
                //move to strings
                label = "House Use";
                displayValue = usageTotal * -1;
                = new AbstractMap.SimpleEntry<String,String>("House Use", );
                break;
            case "production":
                //Panel output
                label = "Solar Prod";
                displayValue = generationTotal;
                break;
            case "net_usage":
            default:
                displayValue = gridTotal * -1;
                label = "Net Usage";
        }
    }

    private void rotateDisplay(Context context, Intent intent)
    {
        //widget id is tacked onto end of action
        String id = intent.getAction();
        //int widgetId = Integer.parseInt(intent.getAction().substring(rotateCick.length()));

        String [] rotateList = new String [] {"usage","production", "net usage", "bill"};

        //TODO: push this up to parent action
        AppWidgetManager appWidgetManager =  AppWidgetManager.getInstance(context);
        appWidgetManager.getAppWidgetIds(new ComponentName(context, EgaugeWidgetProvider.class));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        final boolean enableBillCalculate = preferences.getBoolean("enable_bill_calculate", true);
        final boolean insideCityOfAustin = preferences.getBoolean("inside_city_of_austin", true);


        for(int ids :appWidgetManager.getAppWidgetIds(new ComponentName(context, EgaugeWidgetProvider.class))) {

            Log.i(LOG_TAG, "Rotating display on widget " + id);
            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            //Set 'em here
            views.setTextViewText(R.id.lbl_display, enableBillCalculate+"");
            views.setTextViewText(R.id.displayLabel, insideCityOfAustin+"");

            appWidgetManager.getInstance(context).updateAppWidget(ids, views);
        }
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
        } else if (rotateCick.equals(intent.getAction()))
        {
            rotateDisplay(context, intent);
        }
    }

    protected PendingIntent getPengingSelfIntent(Context ctx, String action)
    {
            Intent intent = new Intent(ctx, getClass());
            intent.setAction(action);
            return PendingIntent.getBroadcast(ctx, 0, intent, 0);
    }


}