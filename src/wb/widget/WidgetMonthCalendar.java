
package wb.widget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import wb.widget.model.Day;
import wb.widget.model.SharedPreferencesHelper;
import wb.widget.utils.Utility;
import wb.widget.utils.calendar.AlmanacUtils;
import wb.widget.utils.calendar.CalendarUtil;
import wb.widget.utils.calendar.Constants;

public class WidgetMonthCalendar {

    private static int sTextSizeDayCellW4;
    private static int sTextSizeDayCellW5;
    private static int sTextSizeDayCellW6;
    private static int sTextSizeDayCell;

    public static void loadDimensions(final Context context) {
        final Resources res = context.getResources();

        sTextSizeDayCellW4 = res.getInteger(R.integer.w4_day_cell_text_size);
        sTextSizeDayCellW5 = res.getInteger(R.integer.w5_day_cell_text_size);
        sTextSizeDayCellW6 = res.getInteger(R.integer.w6_day_cell_text_size);
        sTextSizeDayCell = res.getInteger(R.integer.day_cell_text_size);
    }

    public static void performUpdate(Context context, final RemoteViews updateView,
            final SharedPreferencesHelper prefsHelper, final int firstDayOfWeek, final Day today,
            final Day winterComingDate, final Day _1_gengDayAfterSummerComingDay,
            final Day autumnDate) {
        if (sTextSizeDayCellW4 == 0) {
            loadDimensions(context);
        }
        final boolean showAlmanac = SettingsCalendarActivity.loadShowAlmanac(prefsHelper);
        final boolean showOnlyCurMonthDays = SettingsCalendarActivity.loadShowOnlyCurMonthDays(prefsHelper);

        initWeekDayPrompt(context, updateView, firstDayOfWeek);
        initDayViews(context, updateView, today, showOnlyCurMonthDays, firstDayOfWeek,
                        winterComingDate, _1_gengDayAfterSummerComingDay, autumnDate);

        updateView.setTextViewText(R.id.text_lunar_stem_branch,
                Utility.getLunarDateStemBranch(context, today));

        setAlmanacViews(updateView, showAlmanac, today, false);
        setSettingClicker(context, updateView);
    }

    private static void setSettingClicker(final Context appContext, final RemoteViews updateView) {
        // Click settings icon to config the widget.
        final Class<?> activityClass = SettingsCalendarActivity.class;
        Intent startActivityIntent = new Intent(appContext, activityClass);
        final PendingIntent launchSettingsPendingIntent = PendingIntent.getActivity(appContext,
                0 /* no requestCode */, startActivityIntent, 0 /* no flags */);
        if (launchSettingsPendingIntent != null) {
            updateView.setOnClickPendingIntent(R.id.view_settings, launchSettingsPendingIntent);
        }
    }

    private static class DayWithIndex {
        // public final int index;
        public final Day day;
        public final DayViewStyle dayViewStyle;

        public DayWithIndex(final int index, final Day day, final DayViewStyle dayViewStyle) {
            // this.index = index;
            this.day = day;
            this.dayViewStyle = dayViewStyle;
        }

        public void initViewColors(final Context context, final RemoteViews updateView,
                final int viewId) {
            int textColor = context.getColor(dayViewStyle.colorTextResId);
            int backgroundColor = context.getColor(dayViewStyle.colorBackgroundResId);
            updateView.setTextColor(viewId, textColor);
            WidgetMain.setViewBackgroundColor(updateView, viewId, backgroundColor);
        }
    }

    public static Day getWinterComingDate(final Context context, final Day day) {
        if (day.month > 10) {
            return CalendarUtil.getWinterComingDate(context, day.year);
        }
        if (day.month < 3) {
            return CalendarUtil.getWinterComingDate(context, day.year - 1);
        }
        return null;
    }

    public static Day getLunarDayByStem(final Day inputDay, final int stemIndex) {
        Day curDay;
        for (int i = 0; i < 10; i++) {
            curDay = Utility.getDate(inputDay, i);
            if (curDay.lunar_day_stem_index == stemIndex) return curDay;
        }
        return inputDay;
    }

    // 只数到九九.
    private static String getCount9MileStone(final Context context, final Day day,
            final Day winterComingDate) {
        if (winterComingDate == null) return null;
        if (day.month > 2 && day.month < 11) return null;
        int dayCount = Utility.getDayCount(winterComingDate, day);
        if (dayCount < 0) return null;
        int nineDayNum = (dayCount / 9);
        if (nineDayNum > 9 || nineDayNum * 9 != dayCount) return null;
        return CalendarUtil.getCount9Info(context, nineDayNum + 1);
    }

    // 只数到三伏.
    private static String getCountFuMileStone(final Context context, final Day day,
            final Day _1_gengDayAfterSummerComingDay, final Day autumnDate) {
        if (day.month < 6 && day.month > 9) return null;
        Day _1_fuDate = Utility.get1FuDate(_1_gengDayAfterSummerComingDay);
        if (day.equals(_1_fuDate)) return context.getString(R.string._1_fu);
        Day _2_fuDate = Utility.get2FuDate(_1_gengDayAfterSummerComingDay);
        if (day.equals(_2_fuDate)) return context.getString(R.string._2_fu);
        Day _3_fuDate = Utility.get3FuDate(_1_gengDayAfterSummerComingDay, autumnDate);
        if (day.equals(_3_fuDate)) return context.getString(R.string._3_fu);
        return null;
    }

    private static final int[] DAY00 = {
            R.id.day00_last, R.id.day00_cur, R.id.day00_today, R.id.day00_next
    };
    private static final int[] DAY01 = {
            R.id.day01_last, R.id.day01_cur, R.id.day01_today, R.id.day01_next
    };
    private static final int[] DAY02 = {
            R.id.day02_last, R.id.day02_cur, R.id.day02_today, R.id.day02_next
    };
    private static final int[] DAY03 = {
            R.id.day03_last, R.id.day03_cur, R.id.day03_today, R.id.day03_next
    };
    private static final int[] DAY04 = {
            R.id.day04_last, R.id.day04_cur, R.id.day04_today, R.id.day04_next
    };
    private static final int[] DAY05 = {
            R.id.day05_last, R.id.day05_cur, R.id.day05_today, R.id.day05_next
    };
    private static final int[] DAY06 = {
            R.id.day06_last, R.id.day06_cur, R.id.day06_today, R.id.day06_next
    };

    private static final int[] DAY10 = {
            R.id.day10_last, R.id.day10_cur, R.id.day10_today, R.id.day10_next
    };
    private static final int[] DAY11 = {
            R.id.day11_last, R.id.day11_cur, R.id.day11_today, R.id.day11_next
    };
    private static final int[] DAY12 = {
            R.id.day12_last, R.id.day12_cur, R.id.day12_today, R.id.day12_next
    };
    private static final int[] DAY13 = {
            R.id.day13_last, R.id.day13_cur, R.id.day13_today, R.id.day13_next
    };
    private static final int[] DAY14 = {
            R.id.day14_last, R.id.day14_cur, R.id.day14_today, R.id.day14_next
    };
    private static final int[] DAY15 = {
            R.id.day15_last, R.id.day15_cur, R.id.day15_today, R.id.day15_next
    };
    private static final int[] DAY16 = {
            R.id.day16_last, R.id.day16_cur, R.id.day16_today, R.id.day16_next
    };

    private static final int[] DAY20 = {
            R.id.day20_last, R.id.day20_cur, R.id.day20_today, R.id.day20_next
    };
    private static final int[] DAY21 = {
            R.id.day21_last, R.id.day21_cur, R.id.day21_today, R.id.day21_next
    };
    private static final int[] DAY22 = {
            R.id.day22_last, R.id.day22_cur, R.id.day22_today, R.id.day22_next
    };
    private static final int[] DAY23 = {
            R.id.day23_last, R.id.day23_cur, R.id.day23_today, R.id.day23_next
    };
    private static final int[] DAY24 = {
            R.id.day24_last, R.id.day24_cur, R.id.day24_today, R.id.day24_next
    };
    private static final int[] DAY25 = {
            R.id.day25_last, R.id.day25_cur, R.id.day25_today, R.id.day25_next
    };
    private static final int[] DAY26 = {
            R.id.day26_last, R.id.day26_cur, R.id.day26_today, R.id.day26_next
    };

    private static final int[] DAY30 = {
            R.id.day30_last, R.id.day30_cur, R.id.day30_today, R.id.day30_next
    };
    private static final int[] DAY31 = {
            R.id.day31_last, R.id.day31_cur, R.id.day31_today, R.id.day31_next
    };
    private static final int[] DAY32 = {
            R.id.day32_last, R.id.day32_cur, R.id.day32_today, R.id.day32_next
    };
    private static final int[] DAY33 = {
            R.id.day33_last, R.id.day33_cur, R.id.day33_today, R.id.day33_next
    };
    private static final int[] DAY34 = {
            R.id.day34_last, R.id.day34_cur, R.id.day34_today, R.id.day34_next
    };
    private static final int[] DAY35 = {
            R.id.day35_last, R.id.day35_cur, R.id.day35_today, R.id.day35_next
    };
    private static final int[] DAY36 = {
            R.id.day36_last, R.id.day36_cur, R.id.day36_today, R.id.day36_next
    };

    private static final int[] DAY40 = {
            R.id.day40_last, R.id.day40_cur, R.id.day40_today, R.id.day40_next
    };
    private static final int[] DAY41 = {
            R.id.day41_last, R.id.day41_cur, R.id.day41_today, R.id.day41_next
    };
    private static final int[] DAY42 = {
            R.id.day42_last, R.id.day42_cur, R.id.day42_today, R.id.day42_next
    };
    private static final int[] DAY43 = {
            R.id.day43_last, R.id.day43_cur, R.id.day43_today, R.id.day43_next
    };
    private static final int[] DAY44 = {
            R.id.day44_last, R.id.day44_cur, R.id.day44_today, R.id.day44_next
    };
    private static final int[] DAY45 = {
            R.id.day45_last, R.id.day45_cur, R.id.day45_today, R.id.day45_next
    };
    private static final int[] DAY46 = {
            R.id.day46_last, R.id.day46_cur, R.id.day46_today, R.id.day46_next
    };

    private static final int[] DAY50 = {
            R.id.day50_last, R.id.day50_cur, R.id.day50_today, R.id.day50_next
    };
    private static final int[] DAY51 = {
            R.id.day51_last, R.id.day51_cur, R.id.day51_today, R.id.day51_next
    };
    private static final int[] DAY52 = {
            R.id.day52_last, R.id.day52_cur, R.id.day52_today, R.id.day52_next
    };
    private static final int[] DAY53 = {
            R.id.day53_last, R.id.day53_cur, R.id.day53_today, R.id.day53_next
    };
    private static final int[] DAY54 = {
            R.id.day54_last, R.id.day54_cur, R.id.day54_today, R.id.day54_next
    };
    private static final int[] DAY55 = {
            R.id.day55_last, R.id.day55_cur, R.id.day55_today, R.id.day55_next
    };
    private static final int[] DAY56 = {
            R.id.day56_last, R.id.day56_cur, R.id.day56_today, R.id.day56_next
    };

    private static final String DATE_FORMAT = "%2d%n%s%n%s";
    private static final String WEEK_NO_FORMAT = "%02d";

    private static void initDayViews(final Context context, final RemoteViews updateView,
            final Day today, final boolean showOnlyCurMonthDays, final int firstDayOfWeek,
            final Day winterComingDate, final Day _1_gengDayAfterSummerComingDay,
            final Day autumnDate) {
        final int[][][] dayViewIds = new int[][][] {
            { DAY00, DAY01, DAY02, DAY03, DAY04, DAY05, DAY06 },
            { DAY10, DAY11, DAY12, DAY13, DAY14, DAY15, DAY16 },
            { DAY20, DAY21, DAY22, DAY23, DAY24, DAY25, DAY26 },
            { DAY30, DAY31, DAY32, DAY33, DAY34, DAY35, DAY36 },
            { DAY40, DAY41, DAY42, DAY43, DAY44, DAY45, DAY46 },
            { DAY50, DAY51, DAY52, DAY53, DAY54, DAY55, DAY56 }
        };

        final int[] weekNoIds = new int[] {
                R.id.week0, R.id.week1, R.id.week2, R.id.week3, R.id.week4, R.id.week5
        };
        // 初始化，先让每一个weekNo都不可见.
        for (int weekNoView : weekNoIds) {
            updateView.setTextViewText(weekNoView, null);
            updateView.setViewVisibility(weekNoView, /* View.GONE */View.INVISIBLE);
        }

        final ArrayList<DayWithIndex> list = new ArrayList<DayWithIndex>(dayViewIds.length * 7);

        calculateMonth(list, today, firstDayOfWeek);

        final int dayNum = list.size();
        final int dayTextSize = getDayCellTextSize(dayNum);

        int count = 0;
        Day day = null;
        String lunarDayInfo = null;
        String dayShortInfo = null;
        String dayLabel = null;
        DayWithIndex dayWithIndex;
        int curDayViewId;
        SettingsCalendarActivity.DayInfo dayInfo;
        for (int[][] days : dayViewIds) {
            // if (count >= dayNum) return; // Commented20190117.
            for (int[] dayIds : days) {
                for (int dayId : dayIds) {// Added 20190117 starts.
                    updateView.setViewVisibility(dayId, View.GONE);
                }
                if (count >= dayNum) continue; // Added 20190117 ends.

                dayWithIndex = list.get(count);
                day = dayWithIndex.day;
                curDayViewId = dayIds[dayWithIndex.dayViewStyle.index];
                if (showOnlyCurMonthDays && day.month != today.month) {
                    updateView.setViewVisibility(curDayViewId, View.INVISIBLE);
                    count++;
                    continue;
                }
                dayInfo = SettingsCalendarActivity.checkDayInfo(context, day, today);
                dayWithIndex.initViewColors(context, updateView, curDayViewId);

                lunarDayInfo = CalendarUtil.getLunarDayText(context, day);
                dayShortInfo = Utility.getSpecialDayShortInfo(context, day);
                if (TextUtils.isEmpty(dayShortInfo) && winterComingDate != null
                        && day.greaterEquals(winterComingDate)) {
                    dayShortInfo = getCount9MileStone(context, day, winterComingDate);
                }
                if (TextUtils.isEmpty(dayShortInfo) && _1_gengDayAfterSummerComingDay != null
                        && day.greaterEquals(_1_gengDayAfterSummerComingDay)) {
                    dayShortInfo = getCountFuMileStone(context, day, _1_gengDayAfterSummerComingDay,
                            autumnDate);
                }
                if (TextUtils.isEmpty(dayShortInfo)) {
                    dayShortInfo = Constants.SPACE_3_FULL;
                } else if (dayShortInfo.length() < Constants.SPACE_3_FULL.length()) {
                    dayShortInfo = Constants.sameLengthString(dayShortInfo);
                }
                dayLabel = String.format(DATE_FORMAT, day.day, lunarDayInfo, dayShortInfo);

                updateView.setViewVisibility(curDayViewId, View.VISIBLE);
                updateView.setTextViewTextSize(curDayViewId, TypedValue.COMPLEX_UNIT_SP,
                        dayTextSize);
                updateView.setTextViewText(curDayViewId, dayLabel);
                if (dayInfo != null) {
                    if (dayInfo.type.textColor != 0) {
                        if (dayInfo.isToday()) {
                            updateView.setTextColor(curDayViewId, Color.BLACK);
                        } else {
                            updateView.setTextColor(curDayViewId, dayInfo.type.textColor);
                        }
                    }
                    int backgroundColor = dayInfo.getBackgroundColor();
                    if (backgroundColor != 0) {
                        WidgetMain.setViewBackgroundColor(updateView, curDayViewId,
                                backgroundColor);
                    }
                }
                count++;
            }

            setWeekNo(updateView, weekNoIds[count / 7 - 1], day.week_of_year);
        }
    }

    private static int getDayCellTextSize(final int dayNum) {
        int weekCount = dayNum / 7;
        // if (weekCount % 7 > 0) weekCount++;
        switch (weekCount) {
            case 4:  return sTextSizeDayCellW4;
            case 5:  return sTextSizeDayCellW5;
            case 6:  return sTextSizeDayCellW6;
            default: return sTextSizeDayCell;
        }
    }

    private static void setWeekNo(final RemoteViews updateView, int weekNoId, int weekOfYear) {
        updateView.setViewVisibility(weekNoId, View.VISIBLE);
        updateView.setTextViewText(weekNoId, String.format(WEEK_NO_FORMAT, weekOfYear));
    }

    // 0 = last, 1 = current, 2 = today, 3 = next
    private static enum DayViewStyle {
        DayInLastYear(0,  R.color.text_last_year,   R.color.background_last_year),
        DayInNextYear(3,  R.color.text_next_year,   R.color.background_next_year),
        DayInLastMonth(0, R.color.text_last_month,  R.color.background_last_month),
        DayInNextMonth(3, R.color.text_next_month,  R.color.background_next_month),
        DayInCurrMonth(1, R.color.text_curr_month,  R.color.background_curr_month),
        Today(2,R.color.text_today, R.color.background_today);

        public final int index;
        public final int colorTextResId;
        public final int colorBackgroundResId;

        private DayViewStyle(int index, int colorTextResId, int colorBackgroundResId) {
            this.index = index;
            this.colorTextResId = colorTextResId;
            this.colorBackgroundResId = colorBackgroundResId;
        }
    }

    // 0 = last, 1 = current, 2 = today, 3 = next
    private static DayViewStyle getDayViewStyle(final Day day, final Day today) {
        // If the day is in the last year.
        if (day.year < today.year) return DayViewStyle.DayInLastYear;
        // If the day is in the next year.
        if (day.year > today.year) return DayViewStyle.DayInNextYear;
        // If the day is in the last month.
        if (day.month > today.month) return DayViewStyle.DayInLastMonth;
        // If the day is in the next month.
        if (day.month < today.month) return DayViewStyle.DayInNextMonth;
        // The day is in the current month,
        // If the day is today.
        if (day.day == today.day) return DayViewStyle.Today;
        return DayViewStyle.DayInCurrMonth;
    }

    private static final int[][] sSundayFirstWeekDayPromptMapping = {
            {R.id.weekday_0, R.string.sunday,  Color.YELLOW },
            {R.id.weekday_1, R.string.monday    },
            {R.id.weekday_2, R.string.tuesday   },
            {R.id.weekday_3, R.string.wednesday },
            {R.id.weekday_4, R.string.thursday  },
            {R.id.weekday_5, R.string.friday    },
            {R.id.weekday_6, R.string.saturday, Color.YELLOW }
    };

    private static final int[][] sMondayFirstWeekDayPromptMapping = {
            {R.id.weekday_0, R.string.monday    },
            {R.id.weekday_1, R.string.tuesday   },
            {R.id.weekday_2, R.string.wednesday },
            {R.id.weekday_3, R.string.thursday  },
            {R.id.weekday_4, R.string.friday    },
            {R.id.weekday_5, R.string.saturday, Color.YELLOW },
            {R.id.weekday_6, R.string.sunday,   Color.YELLOW },
    };

    private static final int[][] sSaturdayFirstWeekDayPromptMapping = {
            {R.id.weekday_0, R.string.saturday, Color.YELLOW },
            {R.id.weekday_1, R.string.sunday,   Color.YELLOW },
            {R.id.weekday_2, R.string.monday    },
            {R.id.weekday_3, R.string.tuesday   },
            {R.id.weekday_4, R.string.wednesday },
            {R.id.weekday_5, R.string.thursday  },
            {R.id.weekday_6, R.string.friday    },
    };

    private static void initWeekDayPrompt(final Context context, final RemoteViews updateView,
            final int firstDayOfWeek) {
        final int[][] weekDayPromptMapping;
        switch (firstDayOfWeek) {
            case Calendar.SUNDAY:
                weekDayPromptMapping = sSundayFirstWeekDayPromptMapping;
                break;
            case Calendar.MONDAY:
                weekDayPromptMapping = sMondayFirstWeekDayPromptMapping;
                break;
            case Calendar.SATURDAY:
                weekDayPromptMapping = sSaturdayFirstWeekDayPromptMapping;
                break;
            default:
                return;
        }

        int textColorDefault = Color.WHITE;
        int textColor;
        for (int[] weekday : weekDayPromptMapping) {
            updateView.setTextViewText(weekday[0], context.getString(weekday[1]));
            if (weekday.length > 2) {
                textColor = weekday[2];
            } else {
                textColor = textColorDefault;
            }
            updateView.setTextColor(weekday[0], textColor);
        }
    }

    private static int getMonthDayNum(final int year, final int month) {
        // Gregorian Calendar : MINUS 1, set to FIRST OF MONTH
        final GregorianCalendar gregorian_cal = new GregorianCalendar(year, month, 1);
        if (gregorian_cal.isLeapYear(year) && month == Calendar.FEBRUARY) {
            return Constants.MONTH_DAYS[month] + 1;
        }
        return Constants.MONTH_DAYS[month];
    }

    public static int getRemainingDaysInLastMonth(final int year, final int month,
            final int firstDayOfWeek) {
        final GregorianCalendar gregorian_cal = new GregorianCalendar(year, month, 1);
        int remainingCount =  gregorian_cal.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek/* Calendar.SUNDAY */;
        if (remainingCount > 0) return remainingCount;
        return 7 + remainingCount;
    }

    private static void calculateMonth(final ArrayList<DayWithIndex> list, final Day today,
            final int firstDayOfWeek) {
        final int currentMonth = today.month;
        int daysInMonth = getMonthDayNum(today.year, currentMonth);

        int prevMonth = (currentMonth + Constants.MONTH_ID_ARRAY.length - 1)
                % Constants.MONTH_DAYS.length;
        int nextMonth = (currentMonth + 1) % Constants.MONTH_ID_ARRAY.length;
        int prevYear = ((prevMonth > currentMonth) ? today.year - 1 : today.year);
        int nextYear = ((nextMonth < currentMonth) ? today.year + 1 : today.year);
        int daysInPrevMonth = getMonthDayNum(prevYear, prevMonth);

        final int weekDayNum = Constants.WeekDay.values().length;

        // The number of days to leave blank at the start of current month.
        int trailingSpaces = getRemainingDaysInLastMonth(today.year, currentMonth, firstDayOfWeek);
        trailingSpaces %= weekDayNum;

        // Trailing Month days
        int dayIndex;
        Day day;
        for (int i = 1; i <= trailingSpaces; i++) {
            dayIndex = daysInPrevMonth - trailingSpaces + i;
            day = new Day(prevYear, prevMonth, dayIndex);
            addDayToList(list, i - 1, day, getDayViewStyle(day, today), firstDayOfWeek);
        }

        // Current Month Days
        for (int i = 1; i <= daysInMonth; i++) {
            day = new Day(today.year, currentMonth, i);
            addDayToList(list, daysInPrevMonth + i - 1, day, getDayViewStyle(day, today),
                    firstDayOfWeek);
        }

        // Days in next month in the last line of the current month.
        int leadingMonthDayNum = (weekDayNum - list.size() % weekDayNum) % weekDayNum;
        // Next Month days - Leading in the end of this month.
        for (int i = 0; i < leadingMonthDayNum; i++) {
            day = new Day(nextYear, nextMonth, i + 1);
            addDayToList(list, daysInPrevMonth + daysInMonth + i, day,
                    getDayViewStyle(day, today), firstDayOfWeek);
        }
        calculateDayWeekNumberInMonth(list, currentMonth, today);
    }

    // 计算当月中的每一天是在本月的第几个星期几。
    // 只能计算当前月的每一天，因为没有上月和下月的相关信息。
    private static void calculateDayWeekNumberInMonth(final ArrayList<DayWithIndex> list,
            final int curMonth, final Day today) {
        final int[][] weekDayCountArray = Constants.WeekDay.getWeekDayCountArray();
        for (DayWithIndex dayWithIndex : list) {
            Day day = dayWithIndex.day;
            if (day.month != curMonth) continue;
            Constants.WeekDay.countIncrease(weekDayCountArray, day.weekday);
            day.weekday_no_in_month = Constants.WeekDay.getWeekDayCount(weekDayCountArray,
                    day.weekday);
            if (day.equals(today)) {
                today.weekday_no_in_month = day.weekday_no_in_month;
            }
        }
    }

    private static void addDayToList(final ArrayList<DayWithIndex> list, final int index,
            final Day day, final DayViewStyle dayViewStyle, final int firstDayOfWeek) {
        Utility.initDay(day, firstDayOfWeek);
        list.add(new DayWithIndex(index, day, dayViewStyle));
    }

    private static class TextSizeSettingByLength {
        public final int textLen;
        public final int textSize; // in sp

        public TextSizeSettingByLength(int textSize) {
            this(-1, textSize);
        }

        public TextSizeSettingByLength(int textLen, int textSize) {
            this.textLen = textLen;
            this.textSize = textSize;
        }
    }

    private static class TextSizeSettingByLineNum {
        public final int textLineNumMin;
        public final int textLineNumMax;
        public final int textSize; // in sp

        public TextSizeSettingByLineNum(int textLineNumMin, int textLineNumMax, int textSize) {
            this.textLineNumMin = textLineNumMin;
            this.textLineNumMax = textLineNumMax;
            this.textSize = textSize;
        }
    }

    private static void setAlmanacViews(final RemoteViews updateView, final boolean showAlmanac,
            final Day day, final boolean forTodayWidget) {
        if (showAlmanac || forTodayWidget) {
            updateView.setViewVisibility(R.id.almanac_day_text_0, View.VISIBLE);
            updateView.setViewVisibility(R.id.almanac_day_text_1, View.VISIBLE);
            updateView.setViewVisibility(R.id.almanac_good_text, View.VISIBLE);
            updateView.setViewVisibility(R.id.view_can_do, View.VISIBLE);
            updateView.setViewVisibility(R.id.almanac_bad_text, View.VISIBLE);
            updateView.setViewVisibility(R.id.view_not_do, View.VISIBLE);
            calculateAlmanac(updateView, day, forTodayWidget);
            return;
        }
        updateView.setViewVisibility(R.id.almanac_day_text_0, View.INVISIBLE);
        updateView.setViewVisibility(R.id.almanac_day_text_1, View.INVISIBLE);
        updateView.setViewVisibility(R.id.almanac_good_text, View.GONE);
        updateView.setViewVisibility(R.id.view_can_do, View.GONE);
        updateView.setViewVisibility(R.id.almanac_bad_text, View.GONE);
        updateView.setViewVisibility(R.id.view_not_do, View.GONE);
    }

    private static TextSizeSettingByLength[] sOtherTextSizeSettings = {
            new TextSizeSettingByLength(50,  8), // min, 多于50字，最小size
            new TextSizeSettingByLength(40,  9),
            new TextSizeSettingByLength(30, 10), 
            new TextSizeSettingByLength(15, 12), // 少于15字，12sp
            new TextSizeSettingByLength(13),     // normal
    };

    private static TextSizeSettingByLineNum[] sAlmanacTextSizeSettings = {
            new TextSizeSettingByLineNum(15, -1, 9), // min 多于15行，最小size
            new TextSizeSettingByLineNum(12, 15, 10), // 多于12行少于 15行
            new TextSizeSettingByLineNum(9, 11, 12), // 多于9行少于 11行
            new TextSizeSettingByLineNum(7, 9, 13), // 多于7行少于 9行
            new TextSizeSettingByLineNum(-1, 7, 14), // 少于 7行，最大size
    };

    public static void adjustTextViewTextSize(final RemoteViews updateView, final int resId,
            final String text) {
        final TextSizeSettingByLength[] textSizeSettings;
        switch (resId) {
            case R.id.other_text:
                textSizeSettings = sOtherTextSizeSettings;
                break;
            default:
                textSizeSettings = null;
                break;
        }
        if (textSizeSettings == null) return;
        adjustTextViewTextSize(updateView, resId, text, textSizeSettings);
    }

    private static void adjustTextViewTextSize(final RemoteViews updateView, final int resId,
            final String text, final TextSizeSettingByLength[] textSizeSettings) {
        final int textLen = text == null ? 0 : text.length();
        int textSize = 7;
        for (TextSizeSettingByLength setting : textSizeSettings) {
            if (textLen >= setting.textLen) {
                textSize = setting.textSize;
                break;
            }
        }
        updateView.setTextViewTextSize(resId, TypedValue.COMPLEX_UNIT_SP, textSize);
    }

    // Return the line count of the text;
    private static int adjustTextViewTextSize(final RemoteViews updateView, final int resId,
            final String text, final TextSizeSettingByLineNum[] textSizeSettings) {
        final int textLineNum = getTextLineNum(text, CH_CHINESE_PUNCTUATION_MARK);
        int textSize = 0;
        for (TextSizeSettingByLineNum setting : textSizeSettings) {
            textSize = setting.textSize;
            if (setting.textLineNumMin == -1 && textLineNum < setting.textLineNumMax) {
                break; // 最大size.
            }
            if (setting.textLineNumMax == -1 && textLineNum >= setting.textLineNumMin) {
                break; // 最小size.
            }
            if (textLineNum >= setting.textLineNumMin && textLineNum < setting.textLineNumMax) {
                break; // 一个中间size.
            }
        }
        if (textSize > 0) {
            updateView.setTextViewTextSize(resId, TypedValue.COMPLEX_UNIT_SP, textSize);
        }
        return textLineNum;
    }

    private static final char CH_CHINESE_PUNCTUATION_MARK = '、'; // 中文顿号

    private static void calculateAlmanac(final RemoteViews updateView, final Day day,
            final boolean forTodayWidget) {
        final AlmanacUtils.DayAlmanac dayAlmanac = AlmanacUtils.getAlmanacData(day);
        setText(updateView, R.id.almanac_day_text_0, dayAlmanac.dayBranch);
        setText(updateView, R.id.almanac_day_text_1, dayAlmanac.huangDay);
        if (forTodayWidget) {
            setTodayAlmanacText(updateView, R.id.almanac_good_text, dayAlmanac.huangDay.canDo);
            setTodayAlmanacText(updateView, R.id.almanac_bad_text, dayAlmanac.huangDay.notDo);
        } else {
            setAlmanacText(updateView, R.id.almanac_good_text, dayAlmanac.huangDay.canDo);
            setAlmanacText(updateView, R.id.almanac_bad_text, dayAlmanac.huangDay.notDo);
        }
    }

    private static void setAlmanacText(final RemoteViews updateView, final int viewId,
            final String text) {
        adjustTextViewTextSize(updateView, viewId, text, sAlmanacTextSizeSettings);
        setText(updateView, viewId, text.replace(CH_CHINESE_PUNCTUATION_MARK, '\n'));
    }

    private static void setTodayAlmanacText(final RemoteViews updateView, final int viewId,
            final String text) {
        adjustTextViewTextSize(updateView, viewId, text, sAlmanacTextSizeSettings);
        updateView.setTextViewText(viewId, text);
    }

    private static int getTextLineNum(final String text, final char lineSeparatorChar) {
        if (TextUtils.isEmpty(text)) return 1;
        final int textLen = text.length();
        int returnCount = 0;
        char curCh;
        for (int i = 0; i < textLen; i++) {
            curCh = text.charAt(i);
            if (curCh == lineSeparatorChar || curCh == '\n') returnCount++;
        }
        return returnCount + 1;
    }

    public static void setText(final RemoteViews updateView, final int resId, final String text) {
        updateView.setTextColor(resId, getTextColor(resId));
        updateView.setTextViewText(resId, text);
    }

    public static void setText(final RemoteViews updateView, final int resId, final String text,
                    final int textSizeSP) {
        updateView.setTextViewTextSize(resId, TypedValue.COMPLEX_UNIT_SP, textSizeSP);
        setText(updateView, resId, text);
    }

    private static void setText(final RemoteViews updateView, final int resId,
            final AlmanacUtils.DayBranch dayBranch) {
        if (dayBranch == null) {
            updateView.setViewVisibility(resId, View.INVISIBLE);
        } else {
            updateView.setViewVisibility(resId, View.VISIBLE);
            if (dayBranch.dayType != null) {
                updateView.setTextColor(resId, dayBranch.dayType.colorText);
            }
            updateView.setTextViewText(resId, dayBranch.label);
        }
    }

    private static void setText(final RemoteViews updateView, final int resId,
            final AlmanacUtils.HuangDay huangDay) {
        if (huangDay.dayType != null) {
            updateView.setTextColor(resId, huangDay.dayType.colorText);
        }
        updateView.setTextViewText(resId, huangDay.shortInfo());
    }

    private static final int[][] COLOR_MAPPING = {
                    { R.id.week_text, Color.LTGRAY },
                    { R.id.constellation_text, 0xFFDDDDDD },
                    { R.id.constellation_stone, 0xFFAAAAAA },
                    { R.id.constellation_flower, Color.rgb(0xFF, 0xC0, 0xCB) },
                    { R.id.lunar_text, Color.CYAN },
                    { R.id.other_text, Color.MAGENTA },
                    { R.id.almanac_good_text, Color.GREEN },
                    { R.id.almanac_bad_text, Color.GRAY }
    };

    private static int getTextColor(final int resId) {
        for (int[] mapping : COLOR_MAPPING) {
            if (resId == mapping[0]) {
                return mapping[1];
            }
        }
        return 0;
    }
}
