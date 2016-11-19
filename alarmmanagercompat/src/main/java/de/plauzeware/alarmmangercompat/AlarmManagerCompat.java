package de.plauzeware.alarmmangercompat;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.HashMap;

public class AlarmManagerCompat {
    private static final String TAG = AlarmManagerCompat.class.getSimpleName();

    private static final String REPEATED_ALARM_ACTION = "REPEATED_ALARM";
    private static final String PENDING_INTENT_KEY = "PI";
    private static final String PENDING_INTENT_REQUEST_CODE_KEY = "PI_REQUEST_CODE";
    private static final String TRIGGER_MILLIS_KEY = "MILLIS";
    private static final String TRIGGER_INTERVAL_MILLIS_KEY = "INTERVAL";

    private static HashMap<PendingIntent, PendingIntent> pendingIntentHashMap = new HashMap<>();
    private AlarmManager alarmManager;
    private Context context;

    public AlarmManagerCompat(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.context = context;
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra(PENDING_INTENT_KEY)
                        && intent.hasExtra(TRIGGER_INTERVAL_MILLIS_KEY)
                        && intent.hasExtra(TRIGGER_MILLIS_KEY)
                        && intent.hasExtra(PENDING_INTENT_REQUEST_CODE_KEY)) {
                    PendingIntent pi = intent.getParcelableExtra(PENDING_INTENT_KEY);
                    int requestCode = intent.getIntExtra(PENDING_INTENT_REQUEST_CODE_KEY, 0);
                    long triggerAtMillis = intent.getLongExtra(TRIGGER_MILLIS_KEY, 0L);
                    long interval = intent.getLongExtra(TRIGGER_INTERVAL_MILLIS_KEY, 0L);
                    try {
                        pi.send();
                    } catch (PendingIntent.CanceledException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    setExactAlarm(AlarmManager.RTC_WAKEUP, triggerAtMillis + interval
                            , getPendingIntent(triggerAtMillis + interval, requestCode, interval, pi));
                }
            }
        };
        context.registerReceiver(receiver, new IntentFilter(REPEATED_ALARM_ACTION));
    }

    /**
     * Returns android alarm manager to schedule alarms in a normal android fashion.
     *
     * @return legacy alarm manager
     */
    public AlarmManager getAlarmManager() {
        return alarmManager;
    }

    public void setExactAlarm(int type, long triggerAtMillis, PendingIntent operation) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            alarmManager.set(type, triggerAtMillis, operation);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            alarmManager.setExact(type, triggerAtMillis, operation);
        } else {
            setAlarmForDozeDevices(type, triggerAtMillis, operation);
        }
    }

    public void setExactRepeatingAlarm(int type, long triggerAtMillis
            , long intervalMillis, PendingIntent operation) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            alarmManager.setRepeating(type, triggerAtMillis, intervalMillis, operation);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            setExactRepeatingAlarm(type, triggerAtMillis, intervalMillis, operation);
        } else {
            setRepeatingAlarm(type, triggerAtMillis, intervalMillis, operation);
        }
    }

    private void setRepeatingAlarm(int type, long triggerAtMillis, long intervalMillis, PendingIntent operation) {

        PendingIntent pendingIntent = getPendingIntent(triggerAtMillis,
                (int) (System.currentTimeMillis()), intervalMillis, operation);
        pendingIntentHashMap.put(operation, pendingIntent);
        setExactAlarm(type, triggerAtMillis, pendingIntent);
    }

    private PendingIntent getPendingIntent(long triggerAtMillis, int requestCode
            , long intervalMillis, PendingIntent operation) {
        Intent intent = new Intent(REPEATED_ALARM_ACTION);
        intent.putExtra(PENDING_INTENT_KEY, operation);
        intent.putExtra(PENDING_INTENT_REQUEST_CODE_KEY, requestCode);
        intent.putExtra(TRIGGER_MILLIS_KEY, triggerAtMillis);
        intent.putExtra(TRIGGER_INTERVAL_MILLIS_KEY, intervalMillis);
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void setInexactRepeatingAlarm(int type, long triggerAtMillis
            , long intervalMillis, PendingIntent operation) {
        alarmManager.setInexactRepeating(type, triggerAtMillis, intervalMillis, operation);
    }

    /**
     * Sets inexact alarm above android kitkat and exact before
     *
     * @param type
     * @param triggerAtMillis
     * @param operation
     */
    public void setInexactAlarm(int type, long triggerAtMillis, PendingIntent operation) {
        alarmManager.set(type, triggerAtMillis, operation);
    }

    public void cancel(PendingIntent operation) {
        if (pendingIntentHashMap.containsKey(operation)) {
            alarmManager.cancel(pendingIntentHashMap.get(operation));
            pendingIntentHashMap.remove(operation);
        }
        alarmManager.cancel(operation);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setWindow(int type, long windowStartMillis, long windowLengthMillis, PendingIntent operation) {
        alarmManager.setWindow(type, windowStartMillis, windowLengthMillis, operation);
    }

    public void setTimeZone(String timeZone) {
        alarmManager.setTimeZone(timeZone);
    }

    public void setTime(long millis) {
        alarmManager.setTime(millis);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void setAlarmForDozeDevices(int type, long triggerAtMillis, PendingIntent operation) {
        long triggerMillis = triggerAtMillis;
        if (type == AlarmManager.ELAPSED_REALTIME_WAKEUP || type == AlarmManager.ELAPSED_REALTIME) {
            triggerMillis = System.currentTimeMillis() + (triggerAtMillis - SystemClock.elapsedRealtime());
        }
        Intent intent = new Intent();
        PendingIntent pi = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis()
                , intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(triggerMillis, pi);
        alarmManager.setAlarmClock(alarmClockInfo, operation);
    }
}
