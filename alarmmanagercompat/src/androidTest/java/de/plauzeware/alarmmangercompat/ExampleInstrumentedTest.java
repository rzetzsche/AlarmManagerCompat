package de.plauzeware.alarmmangercompat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    public static final long INTERVAL = 1000;
    private AlarmManagerCompat alarmManagerCompat;
    private Context appContext;
    private PendingIntent pendingIntent;

    @Before
    public void setup() {
        appContext = InstrumentationRegistry.getTargetContext();
        alarmManagerCompat = new AlarmManagerCompat(appContext);
        final Intent i = new Intent("ALARM");
        pendingIntent = PendingIntent.getBroadcast(appContext, 2131, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    @Test
    public void testExactAlarm() throws Exception {
        final long expected = System.currentTimeMillis() + 10000;
        setUpBroadcastReceiver(expected, 0);
        alarmManagerCompat.setExactAlarm(AlarmManager.RTC_WAKEUP, expected, pendingIntent);
        Thread.sleep(10000+INTERVAL);
    }

    private void setUpBroadcastReceiver(final long expected, final long interval) {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            int i = 0;

            @Override
            public void onReceive(Context context, Intent intent) {
                long actual = System.currentTimeMillis();
                long actualExpected = expected + i * interval;
                Assert.assertTrue("Alarm was expected at " + actualExpected + " but was triggered at " + actual
                        , actual > actualExpected - INTERVAL && actual < actualExpected + INTERVAL);
                i++;
            }
        };
        appContext.registerReceiver(receiver, new IntentFilter("ALARM"));
    }

    @Test
    public void testExactRepeatedAlarm() throws Exception {
        int interval = 10000;
        final long expected = System.currentTimeMillis() + interval;
        setUpBroadcastReceiver(expected, interval);
        alarmManagerCompat.setExactRepeatingAlarm(AlarmManager.RTC_WAKEUP, expected, interval, pendingIntent);
        Thread.sleep(interval * 10);
    }
}
