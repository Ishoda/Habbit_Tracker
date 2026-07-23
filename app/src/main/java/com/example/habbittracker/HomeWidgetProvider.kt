package com.example.habbittracker


import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import com.example.habbittracker.R
import java.io.File
import kotlin.math.roundToInt

class HomeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id)
        }
    }

    companion object {
        private const val PREFS = "home_widget_prefs"
        private const val KEY_BOTTLE = "bottle_level"  // int 0..100
        private const val KEY_MOOD = "mood"
        private const val KEY_QUOTE = "quote"
        private const val KEY_HABIT1 = "habit1"
        private const val KEY_HABIT2 = "habit2"
        private const val CACHE_FILE = "widget_bottle.png"

        fun updateAllWidgets(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, HomeWidgetProvider::class.java))
            for (id in ids) updateAppWidget(context, mgr, id)
        }

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val bottleLevel = prefs.getInt(KEY_BOTTLE, 50)
            val mood = prefs.getString(KEY_MOOD, "Neutral") ?: "Neutral"
            val quote = prefs.getString(KEY_QUOTE, "Keep moving forward.") ?: "Keep moving forward."
            val habit1 = prefs.getString(KEY_HABIT1, "Drink water") ?: "Drink water"
            val habit2 = prefs.getString(KEY_HABIT2, "Do a quick stretch") ?: "Do a quick stretch"

            val rv = RemoteViews(context.packageName, R.layout.widget_home)

            // Mood + quote in one line
            rv.setTextViewText(R.id.tv_mood_quote, "$mood — $quote")

            // Two habit lines
            rv.setTextViewText(R.id.tv_habit1, "• $habit1")
            rv.setTextViewText(R.id.tv_habit2, "• $habit2")

            // Try cached bitmap from hydration page
            val cacheFile = File(context.cacheDir, CACHE_FILE)
            val bitmap = if (cacheFile.exists()) {
                try {
                    BitmapFactory.decodeFile(cacheFile.absolutePath)
                } catch (e: Exception) {
                    null
                }
            } else null

            if (bitmap != null) {
                rv.setImageViewBitmap(R.id.iv_bottle, bitmap)
            } else {
                // 8-frame drawable fallback: add drawables named bottle_0 .. bottle_7 in res/drawable
                val bottles = listOf(
                    R.drawable.bottle_c1,
                    R.drawable.bottle_c2,
                    R.drawable.bottle_c3,
                    R.drawable.bottle_c4,
                    R.drawable.bottle_c5,
                    R.drawable.bottle_c6,
                    R.drawable.bottle_c7,
                    R.drawable.bottle_c8
                )
                val level = bottleLevel.coerceIn(0, 100)
                val idx = ((level / 100f) * (bottles.size - 1)).roundToInt().coerceIn(0, bottles.lastIndex)
                val drawableRes = bottles[idx]
                rv.setImageViewResource(R.id.iv_bottle, drawableRes)
                // show percent when using drawable fallback
                rv.setTextViewText(R.id.tv_mood_quote, "$mood — $quote (${bottleLevel}%)")
            }

            // Optional: open app when widget clicked (launch main package activity)
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            if (launchIntent != null) {
                val pending = PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                rv.setOnClickPendingIntent(R.id.iv_bottle, pending)
                rv.setOnClickPendingIntent(R.id.tv_mood_quote, pending)
            }

            appWidgetManager.updateAppWidget(appWidgetId, rv)
        }
    }
}


