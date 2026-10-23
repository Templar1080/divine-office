package com.example.divineoffice

import android.Manifest
import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*

private val BURGUNDY = Color.parseColor("#6B1A2B")
private val GOLD = Color.parseColor("#C9A227")
private val DGOLD = Color.parseColor("#8A6D1A")
private val PARCHMENT = Color.parseColor("#F6EFE0")
private val INK = Color.parseColor("#2B2118")
private val NOTES = listOf(
    "The night vigil, watching for the Lord",
    "Dawn praise, the light of the Resurrection",
    "The first hour, at sunrise",
    "The Holy Spirit descends, 9 a.m.",
    "Christ is crucified, noon",
    "Christ gives up His spirit, 3 p.m.",
    "The evening sacrifice of praise",
    "Night prayer, rest in the Lord"
)

class MainActivity : Activity() {
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bg(c: Int, r: Int, stroke: Int? = null) = GradientDrawable().apply {
        setColor(c); cornerRadius = dp(r).toFloat(); if (stroke != null) setStroke(dp(1), stroke)
    }
    private fun tv(t: String, size: Float, color: Int, style: Int = Typeface.NORMAL) = TextView(this).apply {
        text = t; textSize = size; setTextColor(color); typeface = Typeface.create(Typeface.SERIF, style)
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.statusBarColor = BURGUNDY
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        if (Build.VERSION.SDK_INT >= 31 && !getSystemService(AlarmManager::class.java).canScheduleExactAlarms())
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(PARCHMENT) }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            setBackgroundColor(BURGUNDY); setPadding(dp(20), dp(28), dp(20), dp(24))
        }
        header.addView(tv("\u2629", 46f, GOLD).apply { gravity = Gravity.CENTER })
        header.addView(tv("Divine Office", 28f, PARCHMENT, Typeface.BOLD).apply { gravity = Gravity.CENTER })
        header.addView(tv("Horae Canonicae \u00B7 Breviarium Romanum 1960", 13f, GOLD).apply {
            gravity = Gravity.CENTER; letterSpacing = 0.05f })
        header.addView(tv("\u2123. Deus, in adjut\u00F3rium meum int\u00E9nde.\n\u211F. D\u00F3mine, ad adjuv\u00E1ndum me fest\u00EDna.", 13f, PARCHMENT, Typeface.ITALIC)
            .apply { gravity = Gravity.CENTER; setPadding(0, dp(14), 0, 0) })
        root.addView(header)
        root.addView(View(this).apply { setBackgroundColor(GOLD); layoutParams = LinearLayout.LayoutParams(-1, dp(3)) })

        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(24)) }
        HOURS.forEach { list.addView(card(it)) }
        list.addView(tv("\u2720  Sit nomen D\u00F3mini benedictum  \u2720", 14f, DGOLD, Typeface.ITALIC)
            .apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, 0) })
        root.addView(ScrollView(this).apply { addView(list); layoutParams = LinearLayout.LayoutParams(-1, 0, 1f) })
        setContentView(root)
        Scheduler.all(this)
    }

    private fun card(x: Hour): LinearLayout {
        val left = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        left.addView(tv(x.name, 18f, BURGUNDY, Typeface.BOLD))
        left.addView(tv(x.latin, 14f, DGOLD, Typeface.ITALIC))
        left.addView(tv(NOTES[x.id], 12f, INK).apply { setPadding(0, dp(4), 0, 0) })

        val pill = tv("", 16f, PARCHMENT, Typeface.BOLD).apply {
            background = bg(BURGUNDY, 20); setPadding(dp(14), dp(8), dp(14), dp(8)); gravity = Gravity.CENTER }
        fun label() { pill.text = "%02d:%02d".format(hourOf(this, x), minuteOf(this, x)) }
        label()
        pill.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                prefs(this).edit().putInt("h${x.id}", h).putInt("m${x.id}", m).apply()
                label(); Scheduler.schedule(this, x)
            }, hourOf(this, x), minuteOf(this, x), true).show()
        }
        val sw = Switch(this).apply {
            isChecked = isOn(this@MainActivity, x)
            setOnCheckedChangeListener { _, on ->
                prefs(this@MainActivity).edit().putBoolean("en${x.id}", on).apply()
                Scheduler.schedule(this@MainActivity, x)
            }
        }
        val right = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL }
        right.addView(pill)
        right.addView(sw, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(8) })

        return LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL; setPadding(dp(16), dp(14), dp(16), dp(14))
            background = bg(Color.WHITE, 16, Color.parseColor("#E3D3A5"))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
            addView(left); addView(right)
        }
    }
}
