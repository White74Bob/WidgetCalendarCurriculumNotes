
package wb.widget.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import wb.widget.R;
import wb.widget.model.Day;
import wb.widget.utils.calendar.CalendarUtil;
import wb.widget.utils.calendar.Constants;

public class Utility {

    public static int getLunarDayTextId(final int lunar_month, final int lunar_day) {
        if (!CalendarUtil.isValidLunarMonth(lunar_month)) {
            throw new RuntimeException("Invalid lunar month:" + lunar_month);
        }
        if (!CalendarUtil.isValidLunarDay(lunar_day)) {
            throw new RuntimeException("Invalid lunar day:" + lunar_day + ", lunar month:" + lunar_month);
        }
        if (lunar_day == 1) {
            return Constants.LUNAR_MONTH_ID_ARRAY[lunar_month];
        }
        return Constants.LUNAR_ID_ARRAY[lunar_day - 1];
    }

    public static int getSpecialDayId(final boolean isLunar, final int month, final int day) {
        final int[][] id_array = (isLunar ? Constants.LUNAR_IMPORTANT_DAYS[month]
                : Constants.PUBLIC_IMPORTANT_DAYS[month]);
        for (int i = 0; i < id_array.length; i++) {
            if (id_array[i][0] == day) {
                return id_array[i][1];
            }
        }
        return Constants.INVALID_DAY_ID;
    }

    public static int getSpecialDayId(final int[][] day_id_array, final int day) {
        return getSpecialDayId(day_id_array, day, false);
    }

    private static int getSpecialDayId(final int[][] day_id_array, final int day, final boolean isShortInfo) {
        int arrayLen;
        for (int i = 0; i < day_id_array.length; i++) {
            if (day_id_array[i][0] != day) continue;
            arrayLen = day_id_array[i].length;
            if (arrayLen <= 2) {
                if (arrayLen == 2) return day_id_array[i][1];
                break;
            }
            if (isShortInfo) {
                if (arrayLen > 3) {
                    return day_id_array[i][3]; // 返回日期含义(短)
                }
            }
            if (arrayLen > 2) {
                return day_id_array[i][2]; // 返回日期含义(全)
            }
            return day_id_array[i][1];// 返回日期含义
        }
        return Constants.INVALID_DAY_ID;
    }

    // 根据Day是当月第几个星期几，检查是否是某些节日。
    public static int getSpecialWeekDayInMonth(final int inputMonth, final int weekdayNoInMonth,
                    final int weekday, final boolean isShortInfo) {
        int month, weekNo, dayInWeek;
        for (int[] dayInfo : Constants.SPECIAL_DAY_IN_MONTH_WEEK) {
            month = dayInfo[0];
            weekNo = dayInfo[1];
            dayInWeek = dayInfo[2];
            if ((month == (inputMonth + 1)) && (weekNo == weekdayNoInMonth)
                    && (dayInWeek == weekday)) {
                return isShortInfo ? dayInfo[4] : dayInfo[3];
            }
        }
        return Constants.INVALID_DAY_ID;
    }

    public static boolean stringEqualsResource(final Context context, final String str,
            final int resId) {
        String resStr = context.getString(resId);
        if (str == resStr) {
            return true;
        }
        return (str != null && str.equals(resStr));
    }

    public static String getLunarDateText(final Context context, final Day day) {
        int yearStemIndex = CalendarUtil.getStemIndex(day.year, day.month, day.lunar_month);
        String yearStem = Constants.getStemLabel(context, yearStemIndex);

        int yearBranchIndex = CalendarUtil.getBranchIndex(day.year, day.month, day.lunar_month);
        Constants.Branch year_branch = Constants.getBranch(yearBranchIndex);
        String yearBranch = context.getString(year_branch.branchResId);
        // 年份生肖
        String yearAnimal = context.getString(year_branch.animalResId);

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
        return context.getString(R.string.lunar_date_template, yearStem, yearBranch, yearAnimal,
                lunarMonthString, lunarDayString);
    }

    // 注意:农历闰月不计干支.
    public static String getLunarDateStemBranch(final Context context, final Day day) {
        String lunarDayStemBranch = day.getDayStem(context) + day.getDayBranch(context);
        if (day.lunar_month_leap) {
            return context.getString(R.string.lunar_date_stem_branch, lunarDayStemBranch);
        }
        int yearStemIndex = CalendarUtil.getStemIndex(day.year, day.month, day.lunar_month);
        String yearStem = Constants.getStemLabel(context, yearStemIndex);
        String lunarMonthStemBranch = CalendarUtil.getLunarMonthStemBranch(yearStem, day.lunar_month);
        return context.getString(R.string.lunar_month_date_stem_branch, lunarMonthStemBranch,
                lunarDayStemBranch);
    }

    public static String getDayText(final Context context, final Day day) {
        return context.getString(R.string.date_template, day.year, day.month + 1, day.day);
    }

    public static int getSpecialDayId(final Day day) {
        if (day.lunar_month_leap) return Constants.INVALID_DAY_ID;
        int lunarSpecialDayId = getSpecialDayId(true, day.lunar_month, day.lunar_day);
        if (lunarSpecialDayId != Constants.INVALID_DAY_ID) {
            return lunarSpecialDayId;
        }
        int publicSpecialDayId = getSpecialDayId(false, day.month, day.day);
        if (publicSpecialDayId != Constants.INVALID_DAY_ID) {
            return publicSpecialDayId;
        }
        return Constants.INVALID_DAY_ID;
    }

    public static String getSpecialDayShortInfo(final Context context, final Day day) {
        int publicSpecialDayId = getSpecialDayId(Constants.PUBLIC_IMPORTANT_DAYS[day.month],
                        day.day, true);
        if (publicSpecialDayId != Constants.INVALID_DAY_ID) {
            return context.getString(publicSpecialDayId);
        }
        if (!day.lunar_month_leap) {
            int lunarSpecialDayId = getSpecialDayId(true, day.lunar_month, day.lunar_day);
            if (lunarSpecialDayId != Constants.INVALID_DAY_ID) {
                return context.getString(lunarSpecialDayId);
            }
            lunarSpecialDayId = getSpecialDayId(Constants.LUNAR_IMPORTANT_DAYS[day.lunar_month],
                            day.lunar_day, true);
            if (lunarSpecialDayId != Constants.INVALID_DAY_ID) {
                return context.getString(lunarSpecialDayId);
            }
        }
        int weekSpecialDayId = getSpecialWeekDayInMonth(day.month, day.weekday_no_in_month,
                        day.weekday, true);
        if (weekSpecialDayId != Constants.INVALID_DAY_ID) {
            return context.getString(weekSpecialDayId);
        }
        if (CalendarUtil.isSpringFestivalEve(day)) {
            return context.getString(R.string.chu_xi);
        }
        return Constants.NULL_STR;
    }

    public static String getSpecialDayInfo(final Context context, final Day day,
            final Day winterComingDate, final Day _1stGengDayAfterSummerComingDate, final Day autumnDate) {
        StringBuffer sb = new StringBuffer();

        if (!day.lunar_month_leap) {
            int lunarSpecialDayId = getSpecialDayId(true, day.lunar_month, day.lunar_day);
            if (lunarSpecialDayId != Constants.INVALID_DAY_ID) {
                sb.append(context.getString(lunarSpecialDayId));
            }
            lunarSpecialDayId = getSpecialDayId(Constants.LUNAR_IMPORTANT_DAYS[day.lunar_month],
                    day.lunar_day, false);
            if (lunarSpecialDayId != Constants.INVALID_DAY_ID) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(context.getString(lunarSpecialDayId));
            }
        }

        // 看看是几九第几天或者几伏第几天.
        String countDayInfo = CalendarUtil.getCountDayInfo(context, day, winterComingDate,
                _1stGengDayAfterSummerComingDate, autumnDate);
        if (!TextUtils.isEmpty(countDayInfo)) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(countDayInfo);
        }

        String dayTerm = CalendarUtil.getDayTerms(day.year, day.month, day.day);
        if (dayTerm != null) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(dayTerm);
        }
        int publicSpecialDayId = getSpecialDayId(Constants.PUBLIC_IMPORTANT_DAYS[day.month], day.day);
        if (publicSpecialDayId != Constants.INVALID_DAY_ID) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(context.getString(publicSpecialDayId));
        }
        int specialWeekDayInMonthId = getSpecialWeekDayInMonth(day.month, day.weekday_no_in_month,
                        day.weekday, false);
        if (day.day >= 15 && day.day <= 17) {
            Log.d("WB", day + " weekday_no_in_month:" + day.weekday_no_in_month);
        }
        if (specialWeekDayInMonthId != Constants.INVALID_DAY_ID) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(context.getString(specialWeekDayInMonthId));
            if (day.day >= 15 && day.day <= 17) {
                Log.d("WB", day + " specialWeekDayInMonthId:" + context.getString(
                                specialWeekDayInMonthId));
            }
        }
        if (CalendarUtil.isSpringFestivalEve(day)) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(context.getString(R.string.chu_xi));
        }
        return (sb.length() > 0 ? sb.toString() : Constants.NULL_STR);
    }

    public static int getIntFromCharSequence(CharSequence s) {
        String str = ((null == s) ? null : s.toString());
        return TextUtils.isEmpty(str) ? 0 : Integer.parseInt(str);
    }

    private final static Calendar sCalendar = Calendar.getInstance();

    public static Day getToday(final int firstDayOfWeek) {
        final Calendar cal = Calendar.getInstance(Locale.getDefault());
        final Day today = new Day(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        initDay(today, firstDayOfWeek);
        return today;
    }

    public static void initDay(final Day day, final int firstDayOfWeek) {
        initCalendarInstanceForWeekNoInYear(day, firstDayOfWeek);
        day.weekday = sCalendar.get(Calendar.DAY_OF_WEEK);
        day.week_of_month = sCalendar.get(Calendar.WEEK_OF_MONTH);
        day.week_of_year = sCalendar.get(Calendar.WEEK_OF_YEAR);
        day.weekday_no_in_month = sCalendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);
    }

    public static void obtainWeekDay(final Day day) {
        sCalendar.set(day.year, day.month, day.day);
        day.weekday = sCalendar.get(Calendar.DAY_OF_WEEK);
    }

    private static final int MINIMUM_DAYS_IN_FIRST_WEEK_MONDAY = 4;
    private static final int MINIMUM_DAYS_IN_FIRST_WEEK_SUNDAY = 3;
    private static final int MINIMUM_DAYS_IN_FIRST_WEEK_SATURDAY = 2;

    /**
     * Get week number of calendar.
     *
     * @param day the calendar to get week number for.
     * @return week number of day.
     */
    public static int getDayWeekNumberInYear(TimeZone timeZone, final int firstDayOfWeek) {
        Calendar calendar = Calendar.getInstance(timeZone);
        switch (firstDayOfWeek) {
            case Calendar.SUNDAY:
                calendar.setFirstDayOfWeek(Calendar.SUNDAY);
                calendar.setMinimalDaysInFirstWeek(MINIMUM_DAYS_IN_FIRST_WEEK_SUNDAY);
                break;
            case Calendar.SATURDAY:
                calendar.setFirstDayOfWeek(Calendar.SATURDAY);
                calendar.setMinimalDaysInFirstWeek(MINIMUM_DAYS_IN_FIRST_WEEK_SATURDAY);
                break;
            default:
                calendar.setFirstDayOfWeek(Calendar.MONDAY);
                calendar.setMinimalDaysInFirstWeek(MINIMUM_DAYS_IN_FIRST_WEEK_MONDAY);
                break;
        }

        //calendar.setTimeInMillis(day.getTimeInMillis());
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    public static int getDayWeekNumberInYear(Day day, final int firstDayOfWeek) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(day.year, day.month, day.day);
        switch (firstDayOfWeek) {
            case Calendar.SUNDAY:
                calendar.setFirstDayOfWeek(Calendar.SUNDAY);
                calendar.setMinimalDaysInFirstWeek(MINIMUM_DAYS_IN_FIRST_WEEK_SUNDAY);
                break;
            case Calendar.SATURDAY:
                calendar.setFirstDayOfWeek(Calendar.SATURDAY);
                calendar.setMinimalDaysInFirstWeek(MINIMUM_DAYS_IN_FIRST_WEEK_SATURDAY);
                break;
            default:
                calendar.setFirstDayOfWeek(Calendar.MONDAY);
                calendar.setMinimalDaysInFirstWeek(MINIMUM_DAYS_IN_FIRST_WEEK_MONDAY);
                break;
        }
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    public static void initCalendarInstanceForWeekNoInYear(final Day day,
            final int firstDayOfWeek) {
        sCalendar.set(day.year, day.month, day.day);
        switch (firstDayOfWeek) {
            case Calendar.SUNDAY:
                sCalendar.setFirstDayOfWeek(Calendar.SUNDAY);
                sCalendar.setMinimalDaysInFirstWeek(MINIMUM_DAYS_IN_FIRST_WEEK_SUNDAY);
                break;
            case Calendar.SATURDAY:
                sCalendar.setFirstDayOfWeek(Calendar.SATURDAY);
                sCalendar.setMinimalDaysInFirstWeek(MINIMUM_DAYS_IN_FIRST_WEEK_SATURDAY);
                break;
            case Calendar.MONDAY:
            default:
                sCalendar.setFirstDayOfWeek(Calendar.MONDAY);
                sCalendar.setMinimalDaysInFirstWeek(MINIMUM_DAYS_IN_FIRST_WEEK_MONDAY);
                break;
        }
    }

    public static String findPackage(final PackageManager pm, String packageNameLike) {
        if (packageNameLike == null) return null;
        final String lowerPackageNameLike = packageNameLike.toLowerCase();

        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> infoList = pm.queryIntentActivities(intent, 0);
        String packageName;
        for (ResolveInfo resolveInfo : infoList) {
            packageName = resolveInfo.activityInfo.packageName;
            if (packageName == null) continue;
            packageName = packageName.toLowerCase();
            if (packageName.contains(lowerPackageNameLike)) {
                return packageName;
            }
        }
        return null;
    }

    // Get day number between 2 dates.
    // If day1 > day2, the return value is negative.
    // If day1 < day2, the return value is positive.
    public static int getDayCount(final Day day1, final Day day2) {
        if (day1 == null || day2 == null) return 0;
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        // Set the date for both of the calendar instance
        cal1.set(day1.year, day1.month, day1.day);
        cal2.set(day2.year, day2.month, day2.day);

        long millis1 = cal1.getTimeInMillis();
        long millis2 = cal2.getTimeInMillis();

        // Calculate difference in milliseconds
        long diff = millis2 - millis1;

        // Calculate difference in days
        int diffDays = (int) (diff / (24 * 60 * 60 * 1000));
        return diffDays;
    }

    // move为正, 则向后面的日期移动; 为负, 则向之前的日期移动; 为0, 则为当前输入日期.
    public static Day getDate(final Day inputDay, final int move) {
        if (move == 0) return inputDay;
        Calendar cal = Calendar.getInstance();
        cal.set(inputDay.year, inputDay.month, inputDay.day);
        cal.add(Calendar.DATE, move);
        return new Day(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
    }

    // 获得初伏开始日期：夏至后第3个庚日,
    // Input为夏至后第1个庚日, 20天后，即为第3庚日.
    public static Day get1FuDate(final Day _1_gengDayAfterSummerComingDay) {
        final Day _1_fu = getDate(_1_gengDayAfterSummerComingDay, 20);
        return _1_fu;
    }

    // 获得中伏开始日期：夏至后第4个庚日.
    // Input为夏至后第1个庚日, 30天后，即为第4庚日.
    public static Day get2FuDate(final Day _1_gengDayAfterSummerComingDay) {
        final Day _2_fu = getDate(_1_gengDayAfterSummerComingDay, 30);
        return _2_fu;
    }

    // 获得末伏开始日期：
    // 如果夏至后第5个庚日已经过了立秋, 则中伏为10天.
    // 此时三伏开始日期为立秋后第一个庚日.
    // 如果第5个庚日还没到立秋，则第6个庚日为末伏第一天.
    // Input为夏至后第1个庚日.
    public static Day get3FuDate(final Day _1_gengDayAfterSummerComingDay, final Day autumnDay) {
        Day _5_geng = getDate(_1_gengDayAfterSummerComingDay, 40);
        if (_5_geng.greaterEquals(autumnDay)) return _5_geng;
        Day _6_geng = getDate(_1_gengDayAfterSummerComingDay, 50);
        return _6_geng;
    }

    public static <T> String array2String(T[] array) {
        return array2String(array, false);
    }

    public static <T> String array2String(T[] array, final boolean newLine) {
        if (array == null || array.length <= 0) return null;
        StringBuilder sb = new StringBuilder();
        for (T element : array) {
            if (element == null) continue;
            if (newLine && sb.length() > 0) sb.append('\n');
            sb.append(element);
        }
        return sb.toString();
    }

    public static <T> String array2String(T[] array, final String separator) {
        if (array == null || array.length <= 0) return null;
        StringBuilder sb = new StringBuilder();
        for (T element : array) {
            if (element == null) continue;
            if (!TextUtils.isEmpty(separator) && sb.length() > 0) sb.append(separator);
            sb.append(element);
        }
        return sb.toString();
    }

    public static <T> String list2String(ArrayList<T> list, final String separator) {
        if (list == null || list.size() <= 0) return null;
        StringBuilder sb = new StringBuilder();
        for (T element : list) {
            if (element == null) continue;
            if (!TextUtils.isEmpty(separator) && sb.length() > 0) sb.append(separator);
            sb.append(element);
        }
        return sb.toString();
    }

    public static String intArray2String(int[] array) {
        if (array == null || array.length <= 0) return null;
        StringBuilder sb = new StringBuilder();
        for (int element : array) {
            if (sb.length() > 0) sb.append(',');
            sb.append(element);
        }
        return sb.toString();
    }

    public interface ViewInit {
        void initViews(View rootView);
    }

    public interface ViewInitWithPositiveNegative extends ViewInit {
        void onPositiveClick(View rootView);
        void onNegativeClick(View rootView);
    }

    public interface ViewInitWith3Buttons extends ViewInit {
        void onPositiveClick(View rootView);
        void onNeutralClick(View rootView);
        void onNegativeClick(View rootView);
    }

    public interface ViewInitWithDismissListener extends ViewInit {
        void onDismissed(View rootView);
    }

    public static AlertDialog showViewDialog(final Context context, final int viewLayoutId,
            final ViewInit viewInit) {
        return showViewDialog(context, viewLayoutId, null, viewInit, null, null);
    }

    public static AlertDialog showViewDialog(final Context context, final String title,
            final int viewLayoutId, final ViewInit viewInit) {
        return showViewDialog(context, viewLayoutId, title, viewInit, null, null);
    }

    public static AlertDialog showViewDialog(final Context context, final int viewLayoutId,
            final String title, final ViewInit viewInit, String positiveLabel,
            String negativeLabel) {
        final View rootView = View.inflate(context, viewLayoutId, null);
        if (viewInit != null) {
            viewInit.initViews(rootView);
        }
        if (viewInit instanceof ViewInitWithPositiveNegative) {
            if (TextUtils.isEmpty(positiveLabel)) {
                positiveLabel = context.getString(android.R.string.ok);
            }
            DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((ViewInitWithPositiveNegative) viewInit).onPositiveClick(rootView);
                }
            };
            if (TextUtils.isEmpty(negativeLabel)) {
                negativeLabel = context.getString(android.R.string.cancel);
            }
            DialogInterface.OnClickListener negativeButtonListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((ViewInitWithPositiveNegative) viewInit).onNegativeClick(rootView);
                }
            };
            return showViewDialog(context, title, rootView, positiveLabel, positiveButtonListener,
                    negativeLabel, negativeButtonListener);
        }
        if (viewInit instanceof ViewInitWithDismissListener) {
            DialogInterface.OnDismissListener onDismissListener = new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    ((ViewInitWithDismissListener) viewInit).onDismissed(rootView);
                }
            };
            return showViewDialog(context, title, rootView, positiveLabel, null, negativeLabel,
                    null, onDismissListener);
        }
        return showViewDialog(context, title, rootView, positiveLabel, null, negativeLabel, null);
    }

    public static AlertDialog showViewDialog(final Context context, final int viewLayoutId,
            final String title, final ViewInitWith3Buttons viewInitWith3Buttons,
            String positiveLabel, String neutralLabel, String negativeLabel) {
        final View rootView = View.inflate(context, viewLayoutId, null);
        if (viewInitWith3Buttons != null) {
            viewInitWith3Buttons.initViews(rootView);
        }
        if (TextUtils.isEmpty(positiveLabel)) {
            positiveLabel = context.getString(android.R.string.ok);
        }
        DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewInitWith3Buttons.onPositiveClick(rootView);
            }
        };

        DialogInterface.OnClickListener neutralButtonListener = null;
        if (!TextUtils.isEmpty(neutralLabel)) {
            neutralButtonListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    viewInitWith3Buttons.onNeutralClick(rootView);
                }
            };
        }

        if (TextUtils.isEmpty(negativeLabel)) {
            negativeLabel = context.getString(android.R.string.cancel);
        }
        DialogInterface.OnClickListener negativeButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewInitWith3Buttons.onNegativeClick(rootView);
            }
        };
        return showViewDialog(context, title, rootView, positiveLabel, positiveButtonListener,
                neutralLabel, neutralButtonListener, negativeLabel, negativeButtonListener, null);

    }

    public static AlertDialog showViewDialog(final Context context, final int viewLayoutId,
            final String title, final ViewInit viewInit, final String positiveLabel,
            final DialogInterface.OnClickListener positiveListener, final String negativeLabel,
            final DialogInterface.OnClickListener negativeListener) {
        if (viewInit == null) {
            throw new IllegalArgumentException("viewInit can't be null!");
        }
        final View rootView = View.inflate(context, viewLayoutId, null);
        viewInit.initViews(rootView);
        return showViewDialog(context, title, rootView, positiveLabel, positiveListener,
                negativeLabel, negativeListener);
    }

    public static AlertDialog showViewDialog(final Context context, final View view) {
        return showViewDialog(context, android.R.string.dialog_alert_title, view);
    }

    public static AlertDialog showViewDialog(final Context context, final int titleResId,
            final View view) {
        return showViewDialog(context, context.getString(titleResId), view);
    }

    public static AlertDialog showViewDialog(final Context context, final String title,
            final View view) {
        return showViewDialog(context, title, view, null, null, null, null);
    }

    public static AlertDialog showViewDialog(final Context context, final String title,
            final View view, final String positiveLabel,
            final DialogInterface.OnClickListener positiveListener, final String negativeLabel,
            final DialogInterface.OnClickListener negativeListener) {
        return showViewDialog(context, title, view, positiveLabel, positiveListener, negativeLabel,
                negativeListener, null);
    }

    public static AlertDialog showViewDialog(final Context context, final String title,
            final View view, final String positiveLabel,
            final DialogInterface.OnClickListener positiveListener, final String negativeLabel,
            final DialogInterface.OnClickListener negativeListener,
            final DialogInterface.OnDismissListener onDismissListener) {
        return showViewDialog(context, title, view, positiveLabel, positiveListener, null, null,
                negativeLabel, negativeListener, onDismissListener);
    }

    public static AlertDialog showViewDialog(final Context context, final String title,
            final View view,
            final String positiveLabel, final DialogInterface.OnClickListener positiveListener,
            final String neutralLabel, final DialogInterface.OnClickListener neutralListener,
            final String negativeLabel, final DialogInterface.OnClickListener negativeListener,
            final DialogInterface.OnDismissListener onDismissListener) {
        AlertDialog.Builder infoDialog = new Builder(context);
        if (!TextUtils.isEmpty(title)) {
            infoDialog.setTitle(title);
        }
        infoDialog.setView(view);
        if (!TextUtils.isEmpty(negativeLabel)) {
            infoDialog.setNegativeButton(negativeLabel, negativeListener);
        }
        if (!TextUtils.isEmpty(positiveLabel)) {
            infoDialog.setPositiveButton(positiveLabel, positiveListener);
        }
        if (!TextUtils.isEmpty(neutralLabel)) {
            infoDialog.setNeutralButton(neutralLabel, neutralListener);
        }
        if (onDismissListener != null) {
            infoDialog.setOnDismissListener(onDismissListener);
        }
        infoDialog.setCancelable(true);
        AlertDialog dialog = infoDialog.create();
        dialog.show();
        return dialog;
    }

    public static void showInfoDialog(final Context context, final String info) {
        showInfoDialog(context, null, info);
    }

    public static void showInfoDialog(final Context context, final String title,
            final String info) {
        AlertDialog.Builder infoDialog = new Builder(context);
        if (TextUtils.isEmpty(title)) {
            infoDialog.setTitle(android.R.string.dialog_alert_title);
        } else {
            infoDialog.setTitle(title);
        }
        if (!TextUtils.isEmpty(info)) {
            infoDialog.setMessage(info);
        }
        infoDialog.setPositiveButton(android.R.string.ok, null);
        infoDialog.create().show();
    }

    public static void showInfoDialog(final Context context, final int infoResId) {
        showInfoDialog(context, context.getString(infoResId));
    }

    public static void showSelectableInfo(Context context, int titleResId, int infoResId,
                    boolean isHtml) {
        showSelectableInfo(context, context.getString(titleResId), context.getString(infoResId),
                        isHtml);
    }

    public static void showSelectableInfo(Context context, String title, String info) {
        showSelectableInfo(context, title, info, false);
    }

    public static void showSelectableInfo(Context context, String title, String info,
                    boolean isHtml) {
        AlertDialog.Builder infoDialog = new Builder(context);
        if (!TextUtils.isEmpty(info)) {
            final View resourceView = View.inflate(context, R.layout.dialog_info, null);
            TextView infoText = (TextView) resourceView.findViewById(R.id.text_info);
            textViewShow(infoText, info, isHtml);
            infoDialog.setView(resourceView);
        }
        if (title == null) {
            infoDialog.setTitle(android.R.string.dialog_alert_title);
        } else {
            infoDialog.setTitle(title);
        }
        infoDialog.setPositiveButton(android.R.string.ok, null);
        infoDialog.create().show();
    }

    @SuppressWarnings("deprecation")
    public static void textViewShow(final TextView textView, final String content,
                    final boolean isHtml) {
        if (!isHtml) {
            textView.setText(content);
            return;
        }
        Spanned spanned = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY);
        } else {
            spanned = Html.fromHtml(content);
        }
        textView.setText(spanned);
    }

    public static void showConfirmDialog(final Context context, final String confirmPrompt,
            final DialogInterface.OnClickListener positiveButtonListener) {
        showConfirmDialog(context, null, confirmPrompt, positiveButtonListener, null);
    }

    public static void showConfirmDialog(final Context context, final String title,
            final String confirmPrompt,
            final DialogInterface.OnClickListener positiveButtonListener) {
        showConfirmDialog(context, title, confirmPrompt, positiveButtonListener, null);
    }

    public static void showConfirmDialog(final Context context, final String title,
            final String confirmPrompt,
            final DialogInterface.OnClickListener positiveButtonListener,
            final DialogInterface.OnClickListener negativeButtonListener) {
        final AlertDialog.Builder builder = new Builder(context);
        if (TextUtils.isEmpty(title)) {
            builder.setTitle(android.R.string.dialog_alert_title);
        } else {
            builder.setTitle(title);
        }
        builder.setMessage(confirmPrompt);

        builder.setPositiveButton(android.R.string.ok, positiveButtonListener);
        builder.setNegativeButton(android.R.string.cancel, negativeButtonListener);

        builder.show();
    }

    public static void showDatePickerDialog(final Context context, final int year, final int month,
            final int day, final String title, final OnDateSetListener dateSetListener) {
        DatePickerDialog dialog = new DatePickerDialog(context, dateSetListener, year, month, day);
        dialog.setTitle(title);
        dialog.show();
    }

    public static void showTimePickerDialog(final Context context, final String title,
            final TimePickerDialog.OnTimeSetListener timeSetListener, int hour_of_day, int minute) {
        if (hour_of_day < 0 && minute < 0) {
            final Calendar cal = Calendar.getInstance();
            hour_of_day = cal.get(Calendar.HOUR_OF_DAY);
            minute = cal.get(Calendar.MINUTE);
        }
        TimePickerDialog dialog = new TimePickerDialog(context, timeSetListener, hour_of_day,
                        minute, true);
        dialog.setTitle(title);
        dialog.show();
    }

    public static String getEditText(final EditText editText) {
        if (editText == null) return null;
        Editable editable = editText.getText();
        if (editable == null) return null;
        return editable.toString();
    }

    public static String getTextViewText(final TextView textView) {
        if (textView == null) return null;
        Object obj = textView.getText();
        return obj == null ? null : obj.toString();
    }

    // hh : mm
    private static final String FORMAT_DURATION_H_M = "%02d:%02d";
    // hh : mm : ss
    private static final String FORMAT_DURATION_H_M_S = "%02d:%02d:%02d";

    public static String getCurrentTime(final boolean isHhMmSs) {
        Calendar cal = Calendar.getInstance();

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        if (!isHhMmSs) {
            return String.format(FORMAT_DURATION_H_M, hour, minute);
        }
        int second = cal.get(Calendar.SECOND);
        return String.format(FORMAT_DURATION_H_M_S, hour, minute, second);
    }

    public static int randomInt(final int min, final int max) {
        final Random rand = new Random();
        if (min < 0 || min > max) {
            throw new IllegalArgumentException();
        }
        if (min == max) {
            return min;
        }
        int randNumber;
        do {
            randNumber = rand.nextInt() % (max + 1);
        } while (randNumber < min || randNumber > max);
        return randNumber;
    }

    public static boolean randomBoolean() {
        final Random rand = new Random();
        return rand.nextBoolean();
    }
}
