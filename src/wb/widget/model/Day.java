package wb.widget.model;

import android.content.Context;
import android.icu.util.Calendar;
import android.text.TextUtils;

import wb.widget.R;
import wb.widget.utils.calendar.CalendarUtil;
import wb.widget.utils.calendar.Constants;
import wb.widget.utils.calendar.Constants.WeekDay;

public class Day {
    public int year;
    public int month;
    public int day;

    public int week_of_month;
    public int weekday_no_in_month; // 当日是本月的第几个星期几
    public int week_of_year;
    public int weekday;

    public int lunar_year;
    public int lunar_month;
    public int lunar_day;
    public boolean lunar_month_leap;
    public int lunar_day_stem_index = -1; // 干支记日.
    public int lunar_day_branch_index = -1; // 干支记日.

    public int islam_year;
    public int islam_month;
    public int islam_day;

    public Day(final int year, final int month, final int day) {
        this(false, year, month, day);
    }

    public Day(final boolean isLunar, final int year, final int month, final int day) {
        if (isLunar) {
            this.year = year;
            this.lunar_month = month;
            this.lunar_day = day;
            CalendarUtil.lunarToSolar2(this);
        } else {
            this.year = year;
            this.month = month;
            this.day = day;
            if (year > 1900) {
                CalendarUtil.solarToLunar(this);
            }
        }

        final Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);

        calculateLunarDayStemBranch(cal);
        calculateIslamicDate(cal);
    }

    // 干支纪日
    private void calculateLunarDayStemBranch(final Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        final int dayCyclical = (int) (cal.getTime().getTime() / 86400000 + 25567 + 10);
        lunar_day_stem_index = dayCyclical % 10;
        lunar_day_branch_index = dayCyclical % 12;
    }

    private void calculateIslamicDate(final Calendar cal) {
        // Sorry. NOT done now.
    }

    public String getDayStem(final Context context) {
        return Constants.getStemLabel(context, lunar_day_stem_index);
    }

    public String getDayBranch(final Context context) {
        return Constants.getBranchLabel(context, lunar_day_branch_index);
    }

    public Constants.Stem getDayStem() {
        return Constants.getStem(lunar_day_stem_index);
    }

    public Constants.Branch getDayBranch() {
        return Constants.getBranch(lunar_day_branch_index);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Day)) {
            return false;
        }
        Day dayObj = (Day)obj;
        return dayObj.year == year && dayObj.month == month && dayObj.day == day;
    }

    private static final String FORMAT_TO_STRING = "%d/%d/%d %d-%d";

    private static final String FORMAT_TO_STRING_STEM_BRANCH = FORMAT_TO_STRING + "%s%s日";

    @Override
    public String toString() {
        if (lunar_day_stem_index >= 0 && lunar_day_branch_index >= 0) {
            return String.format(FORMAT_TO_STRING_STEM_BRANCH, year, month + 1, day,
                    lunar_month + 1, lunar_day,
                    CalendarUtil.sStems[lunar_day_stem_index],
                    CalendarUtil.sBranches[lunar_day_branch_index]);
        }
        return String.format(FORMAT_TO_STRING, year, month + 1, day, lunar_month + 1, lunar_day);
    }

    private static final String SEP_YMD = ",";
    private static final String FORMAT_YMD = "%d" + SEP_YMD + "%02d" + SEP_YMD + "%02d";

    public String ymd() {
        return String.format(FORMAT_YMD, year, month + 1, day);
    }

    public String ymd(final Context context, final int yyyymmddFormatResId) {
        return context.getString(yyyymmddFormatResId, year, month + 1, day);
    }

    public static Day parseYmd(final String yyyyMmDd) {
        if (TextUtils.isEmpty(yyyyMmDd)) return null;
        if (yyyyMmDd.length() != 10) return null;
        String[] elements = yyyyMmDd.split(SEP_YMD);
        int year = Integer.parseInt(elements[0]);
        int month = Integer.parseInt(elements[1]) - 1;
        int day = Integer.parseInt(elements[2]);
        return new Day(year, month, day);
    }

    private static final String FORMAT_YMDW_CN = "%4d年%02d月%02d日(%s)";

    public String ymdw_chinese(final Context context) {
        return String.format(FORMAT_YMDW_CN, year, month + 1, day,
                context.getString(WeekDay.getLabel(weekday)));
    }

    private static final String FORMAT_YMD_CN = "%4d年%d月%d日";

    public String ymd_chinese() {
        return String.format(FORMAT_YMD_CN, year, month + 1, day);
    }

    public static String getLunarDateText(final Context context, final Day day) {
        String lunarMonthString = Constants.NULL_STR;
        if (CalendarUtil.isValidLunarMonth(day.lunar_month)) {
            lunarMonthString = context.getString(Constants.LUNAR_MONTH_ID_ARRAY[day.lunar_month]);
            if (day.lunar_month_leap) {
                lunarMonthString = String.format(Constants.FORMAT_LEAP_LUNAR_MONTH,
                        lunarMonthString);
            }
        }
        String lunarDayString = Constants.NULL_STR;
        if (CalendarUtil.isValidLunarDay(day.lunar_day)) {
            lunarDayString = context.getString(Constants.LUNAR_ID_ARRAY[day.lunar_day - 1]);
        }
        return lunarMonthString + lunarDayString;
    }

    public String monthDayInfo(final Context context) {
        return context.getString(R.string.date_md_info, month + 1, day);
    }

    public String getMonthDayLunarInfo(final Context context) {
        return context.getString(R.string.date_md_lunar_info, month + 1, day,
                getLunarDateText(context, this));
    }

    private static final String FORMAT_MD_STEM_BRANCH = "%s %s%s日";

    public String md_stem_branch(final Context context) {
        if (lunar_day_stem_index >= 0 && lunar_day_branch_index >= 0) {
            return String.format(FORMAT_MD_STEM_BRANCH, getMonthDayLunarInfo(context),
                    Constants.getStemLabel(context, lunar_day_stem_index),
                    Constants.getBranchLabel(context, lunar_day_branch_index));
        }
        return getMonthDayLunarInfo(context);
    }

    public boolean isSameDay(Day dayInput) {
        return compare(this, dayInput) == COMPARE_EQUALS;
    }

    public boolean isSameMonthDay(Day dayInput) {
        return compareMonthDay(this, dayInput) == COMPARE_EQUALS;
    }

    public boolean greaterEquals(Day dayInput) {
        int result = compare(this, dayInput);
        return result >= COMPARE_EQUALS;
    }

    public boolean laterThan(Day dayInput) {
        int result = compare(this, dayInput);
        return result > COMPARE_EQUALS;
    }

    public boolean lessEquals(Day dayInput) {
        int result = compare(this, dayInput);
        return result <= COMPARE_EQUALS;
    }

    public boolean earlierThan(Day dayInput) {
        int result = compare(this, dayInput);
        return result < COMPARE_EQUALS;
    }

    public boolean isWeekend() {
        return weekday == Calendar.SATURDAY || weekday == Calendar.SUNDAY;
    }

    private static final int COMPARE_GREATER = 1;
    private static final int COMPARE_EQUALS = 0;
    private static final int COMPARE_LESS = -1;

    // day1 > day2, return 1(greater);
    // day1 == day2, return 0(equals);
    // day1 < day2, return -1(less);
    public static int compare(Day day1, Day day2) {
        if (day1.year > day2.year) return COMPARE_GREATER;
        if (day1.year == day2.year) {
            return compareMonthDay(day1, day2);
        }
        return COMPARE_LESS;
    }

    private static int compareMonthDay(Day day1, Day day2) {
        if (day1.month > day2.month) return COMPARE_GREATER;
        if (day1.month == day2.month) {
            if (day1.day > day2.day) return COMPARE_GREATER;
            if (day1.day == day2.day) return COMPARE_EQUALS;
            return COMPARE_LESS;
        }
        return COMPARE_LESS;
    }

    public static Day parseDateYYYYMMDD(String dayYYYYMMDD) {
        int year = Integer.parseInt(dayYYYYMMDD.substring(0, 4));
        int month = Integer.parseInt(dayYYYYMMDD.substring(4, 6)) - 1;
        int day = Integer.parseInt(dayYYYYMMDD.substring(6));
        return new Day(year, month, day);
    }
}
