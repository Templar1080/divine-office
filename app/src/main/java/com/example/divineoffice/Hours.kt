package com.example.divineoffice

import android.app.*
import android.content.*
import android.os.Build
import java.util.Calendar

// Canonical hours of the Roman Breviary (1960 rubrics, used with the 1962 Missal)
data class Hour(val id: Int, val name: String, val latin: String, val h: Int, val m: Int)

val HOURS = listOf(
    Hour(0, "Matins & Lauds", "Matutinum et Laudes", 5, 30),
    Hour(1, "Prime", "Prima", 6, 30),
    Hour(2, "Terce", "Tertia", 9, 0),
    Hour(3, "Sext", "Sexta", 12, 0),
    Hour(4, "None", "Nona", 15, 0),
    Hour(5, "Vespers", "Vesperae", 18, 0),
    Hour(6, "Compline", "Completorium", 21, 30)
)

fun prefs(c: Context) = c.getSharedPreferences("office", Context.MODE_PRIVATE)
fun isOn(c: Context, x: Hour) = prefs(c).getBoolean("en${x.id}", true)
fun hourOf(c: Context, x: Hour) = prefs(c).getInt("h${x.id}", x.h)
fun minuteOf(c: Context, x: Hour) = prefs(c).getInt("m${x.id}", x.m)

object Scheduler {
    fun schedule(c: Context, x: Hour) {
        val am = c.getSystemService(AlarmManager::class.java)
        val pi = PendingIntent.getBroadcast(
            c, x.id, Intent(c, AlarmReceiver::class.java).putExtra("id", x.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.cancel(pi)
        if (!isOn(c, x)) return
        val t = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOf(c, x)); set(Calendar.MINUTE, minuteOf(c, x))
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis
        if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms())
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi)
        else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi)
    }
    fun all(c: Context) = HOURS.forEach { schedule(c, it) }
}
