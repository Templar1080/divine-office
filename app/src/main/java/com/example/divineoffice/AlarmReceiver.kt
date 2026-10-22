package com.example.divineoffice

import android.app.*
import android.content.*
import android.net.Uri

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val x = HOURS.firstOrNull { it.id == i.getIntExtra("id", -1) } ?: return
        val nm = c.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("office", "Divine Office", NotificationManager.IMPORTANCE_HIGH))
        // Tap opens Divinum Officium (default there is the 1960 rubrics); change the URL if you prefer another source
        val tap = PendingIntent.getActivity(c, x.id,
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.divinumofficium.com/cgi-bin/horas/officium.pl")),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        nm.notify(x.id, Notification.Builder(c, "office")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time for ${x.name}")
            .setContentText(x.latin)
            .setContentIntent(tap).setAutoCancel(true).build())
        Scheduler.schedule(c, x) // next day
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) = Scheduler.all(c)
}
