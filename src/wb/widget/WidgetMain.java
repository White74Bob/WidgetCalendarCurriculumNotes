
package wb.widget;

import java.util.Calendar;
import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;
import wb.widget.model.Day;
import wb.widget.model.SharedPreferencesHelper;
import wb.widget.utils.Utility;
import wb.widget.utils.calendar.CalendarUtil;
import wb.widget.utils.calendar.Constants;
import wb.widget.utils.calendar.Constants.Constellation;
import wb.widget.utils.calendar.Constants.WeekDay;

public class WidgetMain extends AppWidgetProvider {
    private static final String ACTION_SHOW_CALENDAR = "SHOW_calendar";
    private static final String ACTION_SHOW_CURRICULUM = "SHOW_curriculum";
    private static final String ACTION_SHOW_WEEK_NOTES = "SHOW_week_notes";

    public static enum Show {
        CalendarMonth(R.string.calendar_month, R.id.checkbox_show_month_calendar,
                ACTION_SHOW_CALENDAR, SettingsUtils.PREF_KEY_CALENDAR_VISIBLE),
        WeekNotes(R.string.week_notes, R.id.checkbox_show_week_notes,
                ACTION_SHOW_WEEK_NOTES, SettingsUtils.PREF_KEY_WEEK_NOTES_VISIBLE),
        Curriculum(R.string.curriculum, R.id.checkbox_show_curriculum,
                ACTION_SHOW_CURRICULUM, SettingsUtils.PREF_KEY_CURRICULUM_VISIBLE);

        public final int labelResId;
        public final int checkboxResId;
        public final String action;
        public final String prefKey;

        public boolean visible = true;

        private Show(final int labelResId, final int checkboxResId, final String action,
                final String prefKey) {
            this.labelResId = labelResId;
            this.checkboxResId = checkboxResId;
            this.action = action;
            this.prefKey = prefKey;
        }

        private static final String FORMAT_TO_STRING = "%s(%s)";

        @Override
        public String toString() {
            return String.format(FORMAT_TO_STRING, super.toString(), visible);
        }

        public void loadPreference(final SharedPreferencesHelper prefsHelper) {
            final boolean defaultValue = true;
            if (prefsHelper.contains(prefKey)) {
                visible = (Boolean) prefsHelper.get(prefKey, defaultValue);
            } else {
                visible = defaultValue;
                savePreference(prefsHelper);
            }
        }

        public void savePreference(final SharedPreferencesHelper prefsHelper) {
            prefsHelper.put(prefKey, visible);
        }

        public static Show get(final int ordinal) {
            for (Show show : values()) {
                if (show.ordinal() == ordinal) return show;
            }
            return CalendarMonth;
        }

        public static Show nextVisible(final Show currentShow) {
            int nextOrdinal;
            Show nextShow = currentShow;
            do {
                nextOrdinal = (nextShow.ordinal() + 1) % values().length;
                nextShow = get(nextOrdinal);
                if (nextShow != currentShow && nextShow.visible) {
                    return nextShow;
                }
            } while (nextShow != currentShow);
            return null;
        }

        public static void loadPreferences(final SharedPreferencesHelper prefsHelper) {
            SettingsUtils.loadShowCurrent(prefsHelper);
            for (Show show : values()) {
                show.loadPreference(prefsHelper);
            }
        }

        public static Show getShowFromAction(final String action) {
            if (TextUtils.isEmpty(action)) return null;
            for (Show show : values()) {
                if (show.action.equals(action)) return show;
            }
            return null;
        }
    }

    private static final Handler sAsyncHandler;

    static {
        HandlerThread handlerThread = new HandlerThread("WidgetCurriculumCalendar async");
        handlerThread.start();
        sAsyncHandler = new Handler(handlerThread.getLooper());
    }

    private AppWidgetManager mWidgetManager;
    private int[] mWidgetIds;
    private Context mContext;

    private void setup(Context context) {
        mContext = context;
        mWidgetManager = AppWidgetManager.getInstance(context);
        mWidgetIds = mWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetMain.class));
    }

    private class WorkerRunnable implements Runnable {
        public final Context context;
        public final Intent intent;

        public WorkerRunnable(final Context context, final Intent intent) {
            this.context = context;
            this.intent = intent;
        }

        // public WorkerRunnable(final Context context) {
        //    this(context, null);
        // }

        @Override
        public void run() {
            onReceiveAsync(context, intent);
            final boolean updateEverySecond = updateWidgetEverySecond(context);
            final boolean hourM0S0 = isRightHour();
            if (updateEverySecond) { // 每秒检查.
                Runnable updateRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (hourM0S0) { // 整点更新.
                            onReceiveAsync(context, null);
                        }
                    }
                };
                /* 每秒检查 */
                sAsyncHandler.postAtTime(updateRunnable, SystemClock.uptimeMillis() + SECOND);
            }
        }
    }

    // 判断是否整点.
    private static boolean isRightHour() {
        Calendar cal = Calendar.getInstance();
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        return (minute == 0 && second == 0);
    }

    private boolean updateWidgetEverySecond(final Context context) {
        String chineseTimeFormat = context.getString(R.string.format_cn_time);
        return !TextUtils.isEmpty(chineseTimeFormat);
    }

    private static final long SECOND = 1000L;

    private static void refreshWidget(final Context context) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        context.sendBroadcast(intent);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent == null ? null : intent.getAction();
        if (TextUtils.equals(WidgetCurriculum.ACTION_PREV_CURRICULUM, action)) {
            SettingsCurriculumActivity.prevCurriculum(context);
            return;
        }
        if (TextUtils.equals(WidgetCurriculum.ACTION_NEXT_CURRICULUM, action)) {
            SettingsCurriculumActivity.nextCurriculum(context);
            return;
        }
        if (TextUtils.equals(WidgetCurriculum.ACTION_SWITCH_CLASS_TIME_SHOW, action)) {
            SettingsCurriculumActivity.switchClassTimeShow(context);
            return;
        }
        final Show show = Show.getShowFromAction(action);
        if (show != null) {
            SettingsUtils.saveShowCurrentPreference(context, show.ordinal());
            refreshWidget(context);
            return;
        }
        sAsyncHandler.removeCallbacks(null);
        sAsyncHandler.post(new WorkerRunnable(context, intent));
    }

    private void onReceiveAsync(Context context, Intent intent) {
        setup(context);
        refresh();
    }

    private void refresh() {
        RemoteViews remoteViews = performUpdate(mContext, mWidgetManager, mWidgetIds);
        for (int id : mWidgetIds) {
            mWidgetManager.updateAppWidget(id, remoteViews);
        }
    }

    private static void checkShowCurrentVisibility(final SharedPreferencesHelper prefsHelper,
            final Show showCurrent) {
        if (showCurrent.visible) return;
        showCurrent.visible = true;
        showCurrent.savePreference(prefsHelper);
    }

    private static RemoteViews performUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        final RemoteViews updateView = new RemoteViews(context.getPackageName(),
                R.layout.widget_layout);

        final SharedPreferencesHelper prefsHelper = SettingsUtils.getPrefsHelper(context);
        final int firstDayOfWeek = SettingsCalendarActivity.loadFirstDayOfWeek(prefsHelper);
        final Day today = Utility.getToday(firstDayOfWeek);
        final Day winterComingDate = WidgetMonthCalendar.getWinterComingDate(context, today);
        final Day summerComingDate = CalendarUtil.getSummerComingDate(context, today.year);
        final Day _1_gengDayAfterSummerComingDay = WidgetMonthCalendar.getLunarDayByStem(
                summerComingDate, 6/* 庚为7，从0开始 */);
        final Day autumnDate = CalendarUtil.getAutumnDate(context, today.year);

        Show.loadPreferences(prefsHelper);
        final Show showCurrent = SettingsUtils.loadShowCurrent(prefsHelper);
        checkShowCurrentVisibility(prefsHelper, showCurrent);
        
        updateHeader(context, updateView, prefsHelper, today, showCurrent, winterComingDate,
                     _1_gengDayAfterSummerComingDay, autumnDate);
        
        showCurrent(context, updateView, showCurrent, prefsHelper, firstDayOfWeek, today,
                    winterComingDate, _1_gengDayAfterSummerComingDay, autumnDate);
        
        setNextViewClicker(context, updateView, Show.nextVisible(showCurrent));
        appWidgetManager.updateAppWidget(appWidgetIds, updateView);
        return updateView;
    }

    private static void showCurrent(final Context context, final RemoteViews updateView,
                    final Show showCurrent, final SharedPreferencesHelper prefsHelper,
                    final int firstDayOfWeek, final Day today, final Day winterComingDate,
                    final Day _1_gengDayAfterSummerComingDay, final Day autumnDate) {
        switch (showCurrent) {
            case CalendarMonth:
                updateView.setViewVisibility(R.id.layout_month, View.VISIBLE);
                updateView.setViewVisibility(R.id.layout_curriculum, View.GONE);
                updateView.setViewVisibility(R.id.layout_week_notes, View.GONE);
                WidgetMonthCalendar.performUpdate(context, updateView, prefsHelper, firstDayOfWeek,
                        today, winterComingDate, _1_gengDayAfterSummerComingDay, autumnDate);
                break;
            case Curriculum:
                updateView.setViewVisibility(R.id.layout_curriculum, View.VISIBLE);
                updateView.setViewVisibility(R.id.layout_month, View.GONE);
                updateView.setViewVisibility(R.id.layout_week_notes, View.GONE);
                SettingsCurriculumActivity.loadSemesterDates(prefsHelper);
                WidgetCurriculum.performUpdate(context, today, firstDayOfWeek, prefsHelper,
                                updateView);
                break;
            case WeekNotes:
                updateView.setViewVisibility(R.id.layout_curriculum, View.GONE);
                updateView.setViewVisibility(R.id.layout_month, View.GONE);
                updateView.setViewVisibility(R.id.layout_week_notes, View.VISIBLE);
                WidgetWeekNotes.performUpdate(context, today, firstDayOfWeek, prefsHelper,
                                updateView);
                break;
        }
    }

    private static void updateHeader(Context context, final RemoteViews updateView,
            final SharedPreferencesHelper prefsHelper, final Day today, final Show showCurrent,
            final Day winterComingDate, final Day _1_gengDayAfterSummerComingDay, 
            final Day autumnDate) {
        final boolean showVehicleLimit = SettingsUtils.loadShowVehicleLimit(prefsHelper);
        final boolean showConstellation = SettingsUtils.loadShowConstellation(prefsHelper);
        final boolean showConstellationStone = SettingsUtils.loadShowConstellationStone(prefsHelper);
        final boolean showConstellationFlower = SettingsUtils.loadShowConstellationFlower(prefsHelper);

        updateView.setTextViewText(R.id.info_text, Utility.getDayText(context, today));
        String chineseTime = CalendarUtil.getChineseTime(context, today.lunar_day_stem_index);
        updateView.setTextViewText(R.id.text_cn_time, chineseTime);

        setWeekInfo(context, updateView, today, showCurrent, prefsHelper);

        Constellation constellation = Constellation.getConstellation(today);
        setConstellation(context, updateView, showConstellation, showConstellationStone,
                showConstellationFlower, constellation);

        WidgetMonthCalendar.setText(updateView, R.id.lunar_text,
                Utility.getLunarDateText(context, today));

        String specialDayInfo = Utility.getSpecialDayInfo(context, today, winterComingDate,
                _1_gengDayAfterSummerComingDay, autumnDate);
        if (TextUtils.isEmpty(specialDayInfo)) {
            updateView.setTextViewText(R.id.other_text, null/* Utility.getCurrentTime(true) */);
        } else {
            WidgetMonthCalendar.adjustTextViewTextSize(updateView, R.id.other_text, specialDayInfo);
            WidgetMonthCalendar.setText(updateView, R.id.other_text, specialDayInfo);
        }

        checkVehicleLimitShow(context, updateView, showVehicleLimit, today);
        setViewClickers(context, updateView, showVehicleLimit);
    }

    private static void setWeekInfo(final Context context, final RemoteViews updateView,
                    final Day day, final Show showCurrent, final SharedPreferencesHelper prefsHelper) {
        final String weekDayLabel = WeekDay.getLabel(context, day.weekday);
        String weekInfo = context.getString(R.string.week_format_short, day.week_of_year,
                weekDayLabel);
        switch (showCurrent) {
            case Curriculum:
                String weekInfoSemester = SettingsCurriculumActivity.getWeekInfoInSemester(
                                prefsHelper, day);
                if (weekInfoSemester != null) {
                    weekInfo = context.getString(R.string.week_format_semester, weekInfoSemester,
                            weekDayLabel);
                }
                break;
            default:
                break;
        }
        WidgetMonthCalendar.setText(updateView, R.id.week_text, weekInfo);
    }

    private static void checkVehicleLimitShow(final Context context, final RemoteViews updateView,
            final boolean showVehicleLimit, final Day today) {
        final int viewShowVehicleLimit = R.id.show_vehicle_limit;
        if (showVehicleLimit) {
            Constants.TextViewInfo infoVehicleLimit = VehicleLimitActivity
                    .getVehicleForbiddenInfo(context, today);
            if (infoVehicleLimit == null) {
                updateView.setViewVisibility(viewShowVehicleLimit, View.GONE);
            } else {
                updateView.setViewVisibility(viewShowVehicleLimit, View.VISIBLE);
                updateView.setTextViewText(viewShowVehicleLimit, infoVehicleLimit.text);
                if (infoVehicleLimit.textColor != 0) {
                    updateView.setTextColor(viewShowVehicleLimit, infoVehicleLimit.textColor);
                }
                if (infoVehicleLimit.backgroundColor != 0) {
                    setViewBackgroundColor(updateView, viewShowVehicleLimit,
                            infoVehicleLimit.backgroundColor);
                }
            }
        } else {
            updateView.setViewVisibility(viewShowVehicleLimit, View.GONE);
        }
    }

    private static void setConstellation(final Context context, final RemoteViews updateView,
            final boolean showConstellation, final boolean showConstellationStone,
            final boolean showConstellationFlower, final Constellation constellation) {
        final Resources res = context.getResources();

        if (showConstellation) {
            updateView.setViewVisibility(R.id.constellation_text, View.VISIBLE);
            String label = context.getString(constellation.labelResId);
            WidgetMonthCalendar.setText(updateView, R.id.constellation_text, label);
        } else {
            updateView.setViewVisibility(R.id.constellation_text, View.GONE);
        }
        if (showConstellationStone) {
            updateView.setViewVisibility(R.id.constellation_stone, View.VISIBLE);
            int defaultTextSizeSp = res.getInteger(R.integer.constellation_stone_text_size_sp);
            int maxTextLen = res.getInteger(R.integer.max_constellation_stone_text_length);
            setConstellationStoneText(context, updateView, defaultTextSizeSp, maxTextLen,
                            constellation.stoneResId);
        } else {
            updateView.setViewVisibility(R.id.constellation_stone, View.GONE);
        }
        if (showConstellationFlower) {
            updateView.setViewVisibility(R.id.constellation_flower, View.VISIBLE);
            String flower = context.getString(constellation.flowerResId);
            WidgetMonthCalendar.setText(updateView, R.id.constellation_flower, flower);
        } else {
            updateView.setViewVisibility(R.id.constellation_flower, View.GONE);
        }
    }

    private static void setConstellationStoneText(final Context context,
                    final RemoteViews updateView, final int defaultSizeSP, final int maxLength,
                    final int stoneResId) {
        String stone = context.getString(stoneResId);
        final int textLen = stone.length();
        int textSizeSP = 0;
        if (textLen >= maxLength + 1) {
            textSizeSP = defaultSizeSP - 3;
        } else if (textLen <= maxLength - 1) {
            textSizeSP = defaultSizeSP - 1;
        } else {
            textSizeSP = defaultSizeSP;
        }
        WidgetMonthCalendar.setText(updateView, R.id.constellation_stone, stone, textSizeSP);
    }

    private static void setViewClickers(final Context appContext, final RemoteViews updateView,
            final boolean showVehicleLimit) {
        // Click some view to Launch calendar app.
        PendingIntent launchCalendarIntent = getIntentToLaunchCalendar(appContext);
        if (launchCalendarIntent != null) {
            updateView.setOnClickPendingIntent(R.id.info_text, launchCalendarIntent);
            updateView.setOnClickPendingIntent(R.id.lunar_text, launchCalendarIntent);
        }

        // Click some view to show vehicle limit.
        if (showVehicleLimit) {
            PendingIntent launchVehicleLimitIntent = getIntentToShowVehicleLimit(appContext);
            if (launchVehicleLimitIntent != null) {
                updateView.setOnClickPendingIntent(R.id.show_vehicle_limit,
                        launchVehicleLimitIntent);
            }
        }

        // Click settings icon to config the widget.
        PendingIntent launchSettings = getIntentToSettings(appContext);
        if (launchSettings != null) {
            updateView.setOnClickPendingIntent(R.id.view_settings, launchSettings);
        }
    }

    private static void setNextViewClicker(final Context context, final RemoteViews updateView,
            final Show nextShow) {
        final int viewId = R.id.view_show;
        if (nextShow == null) {
            updateView.setTextViewText(viewId, null);
            updateView.setViewVisibility(viewId, View.GONE);
        } else {
            updateView.setTextViewText(viewId, context.getString(nextShow.labelResId));
            updateView.setViewVisibility(viewId, View.VISIBLE);

            Intent intentShow = new Intent(context, WidgetMain.class);
            intentShow.setAction(nextShow.action);
            PendingIntent pendingShowIntent = PendingIntent.getBroadcast(context, 0, intentShow, 0);
            updateView.setOnClickPendingIntent(viewId, pendingShowIntent);
        }
    }

    // Firstly check whether any of the known calendar apps is installed;
    // If not found, then check the installed apps to find an application
    // for which the package name contains "calendar".
    private static PendingIntent getIntentToLaunchCalendar(final Context context) {
        final String[] knownPackageNames = {
                "com.android.calendar",
                "com.sonymobile.calendar",
                "com.google.android.calendar", // deprecated after sdk 8?
        };
        final PackageManager pm = context.getPackageManager();
        Intent intent = null;
        for (String packageName : knownPackageNames) {
            intent = pm.getLaunchIntentForPackage(packageName);
            if (intent != null) break;
        }
        if (intent == null) {
            final String _calendar_ = ".calendar";
            List<PackageInfo> packageList = pm.getInstalledPackages(PackageManager.GET_META_DATA);
            for (PackageInfo packageInfo : packageList) {
                if (!packageInfo.packageName.toLowerCase().contains(_calendar_)) continue;
                intent = pm.getLaunchIntentForPackage(packageInfo.packageName);
                if (intent != null) break;
            }
        }
        if (intent == null) {
            final String packageNameLike = "calendar";
            final String calendarPackageName = Utility.findPackage(pm, packageNameLike);
            if (!TextUtils.isEmpty(calendarPackageName)) {
                intent = pm.getLaunchIntentForPackage(calendarPackageName);
            }
        }
        if (intent == null) {
            return null;
        }
        final PendingIntent launchCalendarPendingIntent = PendingIntent.getActivity(context,
                0 /* no requestCode */, intent, 0 /* no flags */);
        return launchCalendarPendingIntent;
    }

    private static PendingIntent getIntentToShowVehicleLimit(final Context context) {
        final Class<?> activityClass = VehicleLimitActivity.class;
        Intent startActivityIntent = new Intent(context, activityClass);
        final PendingIntent launchVehicleLimitPendingIntent = PendingIntent.getActivity(context,
                0 /* no requestCode */, startActivityIntent, 0 /* no flags */);
        return launchVehicleLimitPendingIntent;
    }

    private static PendingIntent getIntentToSettings(final Context context) {
        final Class<?> activityClass = SettingsCalendarActivity.class;
        Intent startActivityIntent = new Intent(context, activityClass);
        final PendingIntent launchSettingsPendingIntent = PendingIntent.getActivity(context,
                0 /* no requestCode */, startActivityIntent, 0 /* no flags */);
        return launchSettingsPendingIntent;
    }

    private static final String VIEW_METHOD_SET_BACKGROUND_COLOR = "setBackgroundColor";

    public static void setViewBackgroundColor(final RemoteViews updateView, final int viewId,
            final int backgroundColor) {
        updateView.setInt(viewId, VIEW_METHOD_SET_BACKGROUND_COLOR, backgroundColor);
    }
}
