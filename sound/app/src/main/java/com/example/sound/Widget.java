package com.example.sound;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {

    private static final String BUTTON_CLICKED = "com.example.sound.BUTTON_CLICKED";
    private static final String PREFS_NAME = "com.example.sound.WidgetPrefs";
    private static final String KEY_COUNT = "count";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int count = prefs.getInt(KEY_COUNT, 0) + 1; // 增加點擊次數

        // 更新 SharedPreferences
        prefs.edit().putInt(KEY_COUNT, count).apply();

        // 設定 RemoteViews
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setTextViewText(R.id.appwidget_text, "你點了 " + count + " 次");

        // 設定按鈕點擊事件
        Intent intent = new Intent(context, Widget.class);
        intent.setAction(BUTTON_CLICKED);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.appwidget_btn, pendingIntent);

        // 更新 Widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (BUTTON_CLICKED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new android.content.ComponentName(context, Widget.class));

            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }
}