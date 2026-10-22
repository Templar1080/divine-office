package com.example.divineoffice

import android.Manifest
import android.app.*
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        if (Build.VERSION.SDK_INT >= 31 && !getSystemService(AlarmManager::class.java).canScheduleExactAlarms())
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 60, 40, 40) }
        root.addView(TextView(this).apply { text = "Divine Office (Breviary 1960)"; textSize = 22f })
        HOURS.forEach { x ->
            val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(0, 24, 0, 0) }
            val sw = Switch(this).apply {
                text = "${x.name}\n${x.latin}"; isChecked = isOn(this@MainActivity, x)
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                setOnCheckedChangeListener { _, on ->
                    prefs(this@MainActivity).edit().putBoolean("en${x.id}", on).apply()
                    Scheduler.schedule(this@MainActivity, x)
                }
            }
            val btn = Button(this)
            fun label() { btn.text = "%02d:%02d".format(hourOf(this, x), minOf(this, x)) }
            label()
            btn.setOnClickListener {
                TimePickerDialog(this, { _, h, m ->
                    prefs(this).edit().putInt("h${x.id}", h).putInt("m${x.id}", m).apply()
                    label(); Scheduler.schedule(this, x)
                }, hourOf(this, x), minOf(this, x), true).show()
            }
            row.addView(sw); row.addView(btn); root.addView(row)
        }
        setContentView(ScrollView(this).apply { addView(root) })
        Scheduler.all(this)
    }
}
