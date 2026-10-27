package com.example.divineoffice

import android.app.*
import android.content.*
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.graphics.drawable.Icon
import android.net.Uri

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val x = HOURS.firstOrNull { it.id == i.getIntExtra("id", -1) } ?: return
        val nm = c.getSystemService(NotificationManager::class.java)
        // Same sound as the phone's default alarm tone; plays even in silent mode (alarm volume)
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        nm.deleteNotificationChannel("office") // old channel with the default notification sound
        nm.createNotificationChannel(
            NotificationChannel("office_alarm", "Divine Office alarm", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(alarmSound, attrs); enableVibration(true)
            })
        // Tap opens Divinum Officium (default there is the 1960 rubrics); change the URL if you prefer another source
        val tap = PendingIntent.getActivity(c, x.id,
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.divinumofficium.com/cgi-bin/horas/officium.pl")),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val stop = PendingIntent.getBroadcast(c, x.id + 100,
            Intent(c, StopReceiver::class.java).putExtra("id", x.id),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n = Notification.Builder(c, "office_alarm")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time for ${x.name}")
            .setContentText(x.latin + " \u2014 tap Stop to silence")
            .setCategory(Notification.CATEGORY_ALARM)
            .addAction(Notification.Action.Builder(Icon.createWithResource(c, R.drawable.ic_notification), "Stop", stop).build())
            .setTimeoutAfter(10 * 60 * 1000L) // safety: stops by itself after 10 minutes
            .setContentIntent(tap).setAutoCancel(true).build()
        n.flags = n.flags or Notification.FLAG_INSISTENT // repeat the sound until dismissed
        nm.notify(x.id, n)
        Scheduler.schedule(c, x) // next day
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) = Scheduler.all(c)
}

class StopReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        c.getSystemService(NotificationManager::class.java).cancel(i.getIntExtra("id", -1))
    }
}
