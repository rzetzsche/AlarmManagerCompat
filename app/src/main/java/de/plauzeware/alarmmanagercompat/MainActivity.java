package de.plauzeware.alarmmanagercompat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import de.plauzeware.alarmmangercompat.AlarmManagerCompat;

public class MainActivity extends AppCompatActivity {
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(MainActivity.class.getSimpleName(), "Alarm triggered!");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver(receiver, new IntentFilter("ALARM"));
        Intent intent = new Intent("ALARM");
        PendingIntent pendingIntent
                = PendingIntent.getBroadcast(this, 2131, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManagerCompat alarmManagerCompat = new AlarmManagerCompat(this);
        alarmManagerCompat.setExactRepeatingAlarm(
                AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, 60000, pendingIntent);
    }
}
