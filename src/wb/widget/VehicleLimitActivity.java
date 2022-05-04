package wb.widget;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import wb.widget.R;
import wb.widget.model.Day;
import wb.widget.model.SharedPreferencesHelper;
import wb.widget.utils.Utility;
import wb.widget.utils.calendar.Constants;
import wb.widget.utils.calendar.Constants.WeekDay;

public class VehicleLimitActivity extends Activity {
    private static final int FIRST_DAY_OF_WEEK = Calendar.MONDAY;

    private static final String PREF_NAME = "vehicle_forbidden_numbers";

    private static final String PREF_KEY_CITY_FORBIDDEN_INFO = "city_forbidden_numbers_info";
    private static final String PREF_KEY_VEHICLE_NUMBER = "vehicle_number";

    private static final int COLOR_CUR_WEEKDAY_BG = Color.BLACK;
    private static final int COLOR_CUR_WEEKDAY_TEXT = Color.WHITE;
    private static final int COLOR_CUR_MONTHDAY_BG = Color.BLACK;
    private static final int COLOR_CUR_MONTHDAY_TEXT = Color.WHITE;

    private static final int COUNT_FORBIDDEN_NUMBER_COUNT = 2;

    private static final String SEP_DIGIT = ",";
    private static final int NO_DIGIT_SET = -1;

    public static enum DayCheckResult {
        NotSet(R.string.prompt_set_limited_numbers, Color.YELLOW),
        Earlier(R.string.prompt_numbers_setting_not_in_time, Color.LTGRAY),
        Later(R.string.prompt_update_limited_numbers, Color.YELLOW),
        WeekEnd(R.string.prompt_no_limit_in_weekend, Color.GRAY),
        PublicHoliday(R.string.prompt_no_limit_in_public_holiday, Color.GRAY),
        Forbidden(R.string.format_vehicle_limit, Color.RED),
        Normal(R.string.format_vehicle_limit, Color.GREEN);

        public final int promptResId;
        public final int promptColor;

        private DayCheckResult(int promptResId, int color) {
            this.promptResId = promptResId;
            promptColor = color;
        }
    }

    public static class DayCheckResultInfo {
        public final DayCheckResult result;
        public final String city;
        public final int[] forbiddenNumbers;

        public DayCheckResultInfo(DayCheckResult result, String city, int[] forbiddenNumbers) {
            this.result = result;
            this.city = city;
            this.forbiddenNumbers = forbiddenNumbers;
        }

        public DayCheckResultInfo(DayCheckResult result, String city) {
            this(result, city, null);
        }

        public String getInfo(final Context context) {
            switch (result) {
                case NotSet:
                case Earlier:
                case Later:
                case WeekEnd:
                    return context.getString(result.promptResId);
                default:
                    String format_prompt = context.getString(result.promptResId);
                    if (format_prompt == null) return null;
                    return String.format(format_prompt, city, forbiddenNumbers[0],
                            forbiddenNumbers[1]);
            }
        }
    }

    private static enum DayType {
        WeekDay,
        MonthDay;
    }

    private static class DayDigits {
        public final int dayValue;
        private final int[] digits = new int[COUNT_FORBIDDEN_NUMBER_COUNT];

        public DayDigits(int dayValue, int... digits) {
            this.dayValue = dayValue;
            setDigits(digits);
        }

        public void setDigits(int... digits) {
            if (digits != null && digits.length > 0) {
                for (int i = 0; i < digits.length; i++) {
                    this.digits[i] = digits[i];
                }
            } else {
                for (int i = 0; i < digits.length; i++) {
                    this.digits[i] = NO_DIGIT_SET;
                }
            }
        }

        public void setDigits(String... digitStrings) {
            if (digitStrings != null && digitStrings.length > 0) {
                for (int i = 0; i < digits.length; i++) {
                    try {
                        digits[i] = Integer.parseInt(digitStrings[i]);
                    } catch (Exception e) {
                        // ...
                    }
                }
            } else {
                for (int i = 0; i < digits.length; i++) {
                    digits[i] = NO_DIGIT_SET;
                }
            }
        }
    }

    private static class MonthDayDigits {
        private static final String SEP_BETWEEN_DIGITS_AND_MONTH_DAYS = ";";
        private static final String SEP_BETWEEN_DIGITS_IN_MONTH_DAYS = ",";
        private static final String SEP_BETWEEN_DAYS_IN_MONTH_DAY = ",";

        public static MonthDayDigits parse(final String[] dayNumbersArray) {
            MonthDayDigits monthDayDigits = new MonthDayDigits();
            String[] digits, days;
            String dayNumbersItems[];
            for (String dayNumbers : dayNumbersArray) {
                dayNumbersItems = dayNumbers.split(SEP_BETWEEN_DIGITS_AND_MONTH_DAYS);
                digits = dayNumbersItems[0].split(SEP_BETWEEN_DIGITS_IN_MONTH_DAYS);
                days = dayNumbersItems[1].split(SEP_BETWEEN_DAYS_IN_MONTH_DAY);
                for (String day : days) {
                    if (TextUtils.isEmpty(day)) continue;
                    monthDayDigits.setMonthDayDigits(day, digits);
                }
            }
            return monthDayDigits;
        }

        public final DayDigits[] monthDayDigits = new DayDigits[31];

        public MonthDayDigits() {
            for (int i = 0; i < monthDayDigits.length; i++) {
                monthDayDigits[i] = new DayDigits(i + 1);
            }
        }

        public void setMonthDayDigits(final String dayString, final String[] digitStrings) {
            final int day;
            try {
                day = Integer.parseInt(dayString);
            } catch (Exception e) {
                return;
            }
            monthDayDigits[day - 1].setDigits(digitStrings);
        }

        public int[] getMonthDayDigits(int dayValue) {
            for (DayDigits dayDigits : monthDayDigits) {
                if (dayDigits.dayValue == dayValue) return dayDigits.digits;
            }
            return null;
        }
    }

    private static class DayViewInfo {
        public static final int NO_LABEL_VIEW = -1;
        public static final int NO_DIGIT_VIEW = -1;
        public static final int NO_LABEL_RES  = -1;

        public final DayType dayType;
        public final int dayValue;

        public int labelViewResId;
        public int digitViewResId;
        public int labelResId;

        // 尾号限行信息
        public final int[] forbiddenDigits = new int[COUNT_FORBIDDEN_NUMBER_COUNT];

        public DayViewInfo(WeekDay weekDay, int digitViewId, int labelResId) {
            this(DayType.WeekDay, weekDay.calendarId, NO_LABEL_VIEW, digitViewId, labelResId);
        }

        public DayViewInfo(WeekDay weekDay) {
            this(DayType.WeekDay, weekDay.calendarId, NO_LABEL_VIEW, NO_DIGIT_VIEW, NO_LABEL_RES);
        }

        public DayViewInfo(int dayValue) {
            this(DayType.MonthDay, dayValue, NO_LABEL_VIEW, NO_DIGIT_VIEW, NO_LABEL_RES);
        }

        private DayViewInfo(DayType dayType, int dayValue, int labelViewId, int digitViewId,
                int labelResId) {
            this.dayType = dayType;
            this.dayValue = dayValue;
            this.digitViewResId = digitViewId;
            this.labelViewResId = labelViewId;
            this.labelResId = labelResId;
            initForbiddenDigits();
        }

        private void initForbiddenDigits() {
            for (int i = 0; i < forbiddenDigits.length; i++) {
                forbiddenDigits[i] = NO_DIGIT_SET;
            }
        }

        public String forbiddenDigitsString() {
            StringBuilder sb = null;
            for (int n : forbiddenDigits) {
                if (n == NO_DIGIT_SET) continue;
                if (sb == null) {
                    sb = new StringBuilder();
                }
                if (sb.length() > 0) sb.append(SEP_DIGIT);
                sb.append(n);
            }
            if (sb == null) return Constants.NULL_STR;
            return sb.toString();
        }

        public static boolean isSameValue(DayViewInfo dayInfo, final Day today) {
            switch (dayInfo.dayType) {
                case WeekDay:
                    return dayInfo.dayValue == today.weekday;
                case MonthDay:
                    return dayInfo.dayValue == today.day;
                default:
                    return false;
            }
        }
    }

    private static interface VehicleLimitActionListener {
        public void onUpdateCity(final CityVehicleLimit cityVehicleLimit);

        public void onDeleteCity(final CityVehicleLimit cityVehicleLimit);

        public void onCity(final CityVehicleLimit cityVehicleLimit, final boolean isDefaultCity);
    }

    private static enum ManagementOption {
        ByWeek(R.id.radio_by_week_day, R.string.prompt_wrong_digits_in_weekdays),
        ByDate(R.id.radio_by_date, R.string.prompt_wrong_digits_in_monthdays);

        private final int radioResId;
        public final int promptWrongResId;

        private ManagementOption(int radioResId, int promptWrongResId) {
            this.radioResId = radioResId;
            this.promptWrongResId = promptWrongResId;
        }

        public static ManagementOption parse(String strOrdinal) {
            if (TextUtils.isEmpty(strOrdinal)) return ByWeek;
            int ordinal = 0;
            try {
                ordinal = Integer.parseInt(strOrdinal);
            } catch (Exception e) {
                return ByWeek;
            }
            for (ManagementOption option : values()) {
                if (option.ordinal() == ordinal) return option;
            }
            return ByWeek;
        }

        public static ManagementOption getOption(int radioResId) {
            for (ManagementOption option : values()) {
                if (option.radioResId == radioResId) return option;
            }
            return ByWeek;
        }
    }

    private static enum LastDigitLetterOption {
        TakeAs0(R.id.radio_last_digit_0),
        TakeLastNumberDigit(R.id.radio_last_number_digit);

        private final int radioResId;

        private LastDigitLetterOption(int radioResId) {
            this.radioResId = radioResId;
        }

        public static LastDigitLetterOption parse(String strOrdinal) {
            if (TextUtils.isEmpty(strOrdinal)) return TakeAs0;
            int ordinal = 0;
            try {
                ordinal = Integer.parseInt(strOrdinal);
            } catch (Exception e) {
                return TakeAs0;
            }
            for (LastDigitLetterOption option : values()) {
                if (option.ordinal() == ordinal) return option;
            }
            return TakeAs0;
        }

        public static LastDigitLetterOption getOption(int radioResId) {
            for (LastDigitLetterOption option : values()) {
                if (option.radioResId == radioResId) return option;
            }
            return TakeAs0;
        }
    }

    private static final int[][] sMonthDayLine1Views = {
            {R.id.month_line1},
            {R.id.view_01, R.id.date_01, R.id.date_01_digits},
            {R.id.view_02, R.id.date_02, R.id.date_02_digits},
            {R.id.view_03, R.id.date_03, R.id.date_03_digits},
            {R.id.view_04, R.id.date_04, R.id.date_04_digits},
            {R.id.view_05, R.id.date_05, R.id.date_05_digits},
            {R.id.view_06, R.id.date_06, R.id.date_06_digits},
            {R.id.view_07, R.id.date_07, R.id.date_07_digits},
    };

    private static final int[][] sMonthDayLine2Views = {
            {R.id.month_line2},
            {R.id.view_11, R.id.date_11, R.id.date_11_digits},
            {R.id.view_12, R.id.date_12, R.id.date_12_digits},
            {R.id.view_13, R.id.date_13, R.id.date_13_digits},
            {R.id.view_14, R.id.date_14, R.id.date_14_digits},
            {R.id.view_15, R.id.date_15, R.id.date_15_digits},
            {R.id.view_16, R.id.date_16, R.id.date_16_digits},
            {R.id.view_17, R.id.date_17, R.id.date_17_digits},
    };

    private static final int[][] sMonthDayLine3Views = {
            {R.id.month_line3},
            {R.id.view_21, R.id.date_21, R.id.date_21_digits},
            {R.id.view_22, R.id.date_22, R.id.date_22_digits},
            {R.id.view_23, R.id.date_23, R.id.date_23_digits},
            {R.id.view_24, R.id.date_24, R.id.date_24_digits},
            {R.id.view_25, R.id.date_25, R.id.date_25_digits},
            {R.id.view_26, R.id.date_26, R.id.date_26_digits},
            {R.id.view_27, R.id.date_27, R.id.date_27_digits},
    };

    private static final int[][] sMonthDayLine4Views = {
            {R.id.month_line4},
            {R.id.view_31, R.id.date_31, R.id.date_31_digits},
            {R.id.view_32, R.id.date_32, R.id.date_32_digits},
            {R.id.view_33, R.id.date_33, R.id.date_33_digits},
            {R.id.view_34, R.id.date_34, R.id.date_34_digits},
            {R.id.view_35, R.id.date_35, R.id.date_35_digits},
            {R.id.view_36, R.id.date_36, R.id.date_36_digits},
            {R.id.view_37, R.id.date_37, R.id.date_37_digits},
    };

    private static final int[][] sMonthDayLine5Views = {
            {R.id.month_line5},
            {R.id.view_41, R.id.date_41, R.id.date_41_digits},
            {R.id.view_42, R.id.date_42, R.id.date_42_digits},
            {R.id.view_43, R.id.date_43, R.id.date_43_digits},
            {R.id.view_44, R.id.date_44, R.id.date_44_digits},
            {R.id.view_45, R.id.date_45, R.id.date_45_digits},
            {R.id.view_46, R.id.date_46, R.id.date_46_digits},
            {R.id.view_47, R.id.date_47, R.id.date_47_digits},
    };

    private static final int[][] sMonthDayLine6Views = {
            {R.id.month_line6},
            {R.id.view_51, R.id.date_51, R.id.date_51_digits},
            {R.id.view_52, R.id.date_52, R.id.date_52_digits},
            {R.id.view_53, R.id.date_53, R.id.date_53_digits},
            {R.id.view_54, R.id.date_54, R.id.date_54_digits},
            {R.id.view_55, R.id.date_55, R.id.date_55_digits},
            {R.id.view_56, R.id.date_56, R.id.date_56_digits},
            {R.id.view_57, R.id.date_57, R.id.date_57_digits},
    };

    private static final int[][][] sMonthDayViews = {
            sMonthDayLine1Views,
            sMonthDayLine2Views,
            sMonthDayLine3Views,
            sMonthDayLine4Views,
            sMonthDayLine5Views,
            sMonthDayLine6Views,
    };

    private static class CityVehicleLimit {
        private final DayViewInfo[] mWeekDayInfoArray = {
                new DayViewInfo(WeekDay.Mon, R.id.mon_digits, R.id.view_monday),
                new DayViewInfo(WeekDay.Tue, R.id.tue_digits, R.id.view_tuesday),
                new DayViewInfo(WeekDay.Wed, R.id.wed_digits, R.id.view_wednesday),
                new DayViewInfo(WeekDay.Thu, R.id.thu_digits, R.id.view_thursday),
                new DayViewInfo(WeekDay.Fri, R.id.fri_digits, R.id.view_friday),
                new DayViewInfo(WeekDay.Sat),
                new DayViewInfo(WeekDay.Sun),
        };

        private final DayViewInfo[] mMonthDayInfoArray = {
                new DayViewInfo(1), new DayViewInfo(2),
                new DayViewInfo(3), new DayViewInfo(4),
                new DayViewInfo(5), new DayViewInfo(6),
                new DayViewInfo(7), new DayViewInfo(8),
                new DayViewInfo(9), new DayViewInfo(10),
                new DayViewInfo(11), new DayViewInfo(12),
                new DayViewInfo(13), new DayViewInfo(14),
                new DayViewInfo(15), new DayViewInfo(16),
                new DayViewInfo(17), new DayViewInfo(18),
                new DayViewInfo(19), new DayViewInfo(20),
                new DayViewInfo(21), new DayViewInfo(22),
                new DayViewInfo(23), new DayViewInfo(24),
                new DayViewInfo(25), new DayViewInfo(26),
                new DayViewInfo(27), new DayViewInfo(28),
                new DayViewInfo(29), new DayViewInfo(30),
                new DayViewInfo(31)
        };

        public String city;

        public Day startDay;
        public Day endDay;

        public ManagementOption managementOption = ManagementOption.ByWeek;

        public LastDigitLetterOption lastDigitLetterOption = LastDigitLetterOption.TakeAs0;

        public boolean weekendDigitsForbidden;

        private boolean mDisplayFolded = true;

        private DayCheckResultInfo getForbiddenNumbers(final Day day, final String vehicleNumber) {
            if (day.isWeekend() && !weekendDigitsForbidden) {
                return new DayCheckResultInfo(DayCheckResult.WeekEnd, city);
            }
            int[] forbiddenNumbers = getForbiddenDigits(day);
            int forbiddenNumberCount = forbiddenNumbers == null ? 0 : forbiddenNumbers.length;
            if (forbiddenNumberCount != COUNT_FORBIDDEN_NUMBER_COUNT) {
                return null;// Constants.NULL_STR;
            }
            for (int i : forbiddenNumbers) {
                if (i < 0) return null;// Constants.NULL_STR;
            }
            if (vehicleForbiddenToday(day, vehicleNumber)) {
                return new DayCheckResultInfo(DayCheckResult.Forbidden, city, forbiddenNumbers);
            }
            return new DayCheckResultInfo(DayCheckResult.Normal, city, forbiddenNumbers);
        }

        private int[] getForbiddenDigits(Day day) {
            if (day.isWeekend() && !weekendDigitsForbidden) {
                return null; // 周末不限行
            }
            switch (managementOption) {
                case ByWeek:
                    return getWeekDayForbiddenNumbers(day.weekday);
                case ByDate:
                    return getMonthDayForbiddenNumbers(day.day);
                default:
                    return null;
            }
        }

        private int[] getForbiddenDigits(ManagementOption managementOption, int dayValue) {
            switch (managementOption) {
                case ByWeek:
                    return getWeekDayForbiddenNumbers(dayValue);
                case ByDate:
                    return getMonthDayForbiddenNumbers(dayValue);
                default:
                    return null;
            }
        }

        private int[] getForbiddenDigitsNextDay(ManagementOption managementOption, int dayValue) {
            switch (managementOption) {
                case ByWeek:
                    // return getNextWeekDayForbiddenNumbers(dayValue);
                    return getPrevWeekDayForbiddenNumbers(dayValue);
                case ByDate:
                    return getNextMonthDayForbiddenNumbers(dayValue);
                default:
                    return null;
            }
        }

        private String forbiddenNumbersString(final DayViewInfo[] dayInfoArray, final int dayId) {
            for (DayViewInfo dayInfo : dayInfoArray) {
                if (dayInfo.dayValue != dayId) continue;
                return dayInfo.forbiddenDigitsString();
            }
            return Constants.NULL_STR;
        }

        public DayCheckResultInfo getLimitedNumbers(final Day day, final String vehicleNumber) {
            if (startDay == null || endDay == null) {
                return new DayCheckResultInfo(DayCheckResult.NotSet, city);
            }
            if (day.laterThan(endDay)) {
                return new DayCheckResultInfo(DayCheckResult.Later, city);
            }
            if (day.earlierThan(startDay)) {
                return new DayCheckResultInfo(DayCheckResult.Earlier, city);
            }
            return getForbiddenNumbers(day, vehicleNumber);
        }

        private static final int INVALID_LAST_DIGIT = -1;

        private int getLastDigit(final String vehicleNumber) {
            int charCount = vehicleNumber == null ? 0 : vehicleNumber.length();
            if (charCount <= 0) return NO_DIGIT_SET;
            final char lastDigitChar = vehicleNumber.charAt(charCount - 1);
            if (lastDigitChar >= '0' && lastDigitChar <= '9') {
                return lastDigitChar - '0';
            }
            switch (lastDigitLetterOption) {
                case TakeAs0:
                    return 0;
                case TakeLastNumberDigit:
                    char lastNumberDigitChar;
                    for (int i = charCount - 2; i >= 0; i--) {
                        lastNumberDigitChar = vehicleNumber.charAt(i);
                        if (lastNumberDigitChar >= '0' && lastNumberDigitChar <= '9') {
                            return lastDigitChar - '0';
                        }
                    }
                    return INVALID_LAST_DIGIT;
                default:
                    return INVALID_LAST_DIGIT;
            }
        }

        private void getMonthDayInfoArray(final View rootView, final Day today) {
            final int trailingDays = getRemainingDaysInLastMonth(today.year,
                    today.month, FIRST_DAY_OF_WEEK);
            final int dayCount = getMonthDayNum(today.year, today.month);

            mMonthVisibleLines.clear();

            int index = 0;
            int count = 0;
            int lineViewId;
            View dayView;
            for (int[][] line : sMonthDayViews) {
                if (count >= 31) break;
                lineViewId = line[0][0];
                for (int i = 1; i < line.length; i++) {
                    if (index >= trailingDays) {
                        if (!mMonthVisibleLines.contains(lineViewId)) {
                            mMonthVisibleLines.add(lineViewId);
                        }
                        dayView = rootView.findViewById(line[i][0]);
                        if (count < dayCount) {
                            dayView.setVisibility(View.VISIBLE);
                            mMonthDayInfoArray[count].labelViewResId = line[i][1];
                            mMonthDayInfoArray[count].digitViewResId = line[i][2];
                        } else {
                            dayView.setVisibility(View.INVISIBLE);
                        }
                        count++;
                    }
                    index++;
                }
            }
        }

        public boolean vehicleForbiddenToday(final Day today, final String vehicleNumber) {
            if (today.isWeekend() && !weekendDigitsForbidden) {
                return false;
            }
            final int lastDigit = getLastDigit(vehicleNumber);
            if (lastDigit < 0) return false;
            int[] forbiddenNumbers = getForbiddenDigits(today);
            int forbiddenNumberCount = forbiddenNumbers == null ? 0 : forbiddenNumbers.length;
            if (forbiddenNumberCount != COUNT_FORBIDDEN_NUMBER_COUNT) {
                return false;
            }
            for (int digit : forbiddenNumbers) {
                if (digit == lastDigit) return true;
            }
            return false;
        }

        private static final String SEP_ITEM = "/";
        private static final String SEP_DATE = ";";
        private static final String SEP_CITY_INFO = ",";
        private static final String SEP_CITY = "#";

        // Format:
        // city,managementOption.ordinal,lastDigitOption.ordinal
        private static final String FORMAT_CITY_INFO = "%1$s" + SEP_CITY_INFO + "%2$d"
                + SEP_CITY_INFO + "%3$d";
        private String cityInfo() {
            return String.format(FORMAT_CITY_INFO, city, managementOption.ordinal(),
                    lastDigitLetterOption.ordinal());
        }

        // Format:
        // day1digit1,day1digit2;day2digit1,day2digit2;...;
        private String numberSettingsInfo() {
            if (managementOption == ManagementOption.ByWeek) {
                String monNumbers = forbiddenNumbersString(mWeekDayInfoArray, Calendar.MONDAY);
                String tueNumbers = forbiddenNumbersString(mWeekDayInfoArray, Calendar.TUESDAY);
                String wedNumbers = forbiddenNumbersString(mWeekDayInfoArray, Calendar.WEDNESDAY);
                String thuNumbers = forbiddenNumbersString(mWeekDayInfoArray, Calendar.THURSDAY);
                String friNumbers = forbiddenNumbersString(mWeekDayInfoArray, Calendar.FRIDAY);
                return getSettingsString(monNumbers, tueNumbers, wedNumbers, thuNumbers, friNumbers);
            }
            if (managementOption == ManagementOption.ByDate) {
                String[] numbersArray = new String[mMonthDayInfoArray.length];
                for (int i = 0; i < numbersArray.length; i++) {
                    numbersArray[i] = forbiddenNumbersString(mMonthDayInfoArray, i + 1);
                }
                return getSettingsString(numbersArray);
            }
            return Constants.NULL_STR;
        }

        private static String getSettingsString(String...numbers) {
            if (numbers == null || numbers.length <= 0) return Constants.NULL_STR;
            StringBuilder sb = new StringBuilder();
            for (String number : numbers) {
                sb.append(number).append(SEP_DATE);
            }
            return sb.toString();
        }

        // Format:           #1(city settings)                        #2                  #3(number settings)
        //         city,ordinal_management,ordinal_last_digit/startDate;endDate/forbidden_number_settings#
        private static final String FORMAT_TO_STRING = "%1$s" + SEP_ITEM + "%2$s" + SEP_DATE
                + "%3$s" + SEP_ITEM + "%4$s";

        @Override
        public String toString() {
            return String.format(FORMAT_TO_STRING, cityInfo(), startDay.ymd(), endDay.ymd(),
                    numberSettingsInfo());
        }

        private static void parseCityBasic(final CityVehicleLimit cityVehicleLimit, String input) {
            String[] items = input.split(SEP_CITY_INFO);
            cityVehicleLimit.city = items[0];
            cityVehicleLimit.managementOption = ManagementOption.parse(items[1]);
            cityVehicleLimit.lastDigitLetterOption = LastDigitLetterOption.parse(items[2]);
        }

        private static void parseDates(final CityVehicleLimit cityVehicleLimit, String input) {
            String[] dates = input.split(SEP_DATE);
            cityVehicleLimit.startDay = parseDate(dates[0]);
            cityVehicleLimit.endDay = parseDate(dates[1]);
        }

        private static void parseForbiddenNumbers(final CityVehicleLimit cityVehicleLimit,
                final String input) {
            cityVehicleLimit.clearForbiddenNumbers();

            String[] dayDigits = input.split(SEP_DATE);
            switch (cityVehicleLimit.managementOption) {
                case ByWeek:
                    cityVehicleLimit.setForbiddenNumbersWeekDay(Calendar.MONDAY,
                            dayDigits[0].split(SEP_DIGIT));
                    cityVehicleLimit.setForbiddenNumbersWeekDay(Calendar.TUESDAY,
                            dayDigits[1].split(SEP_DIGIT));
                    cityVehicleLimit.setForbiddenNumbersWeekDay(Calendar.WEDNESDAY,
                            dayDigits[2].split(SEP_DIGIT));
                    cityVehicleLimit.setForbiddenNumbersWeekDay(Calendar.THURSDAY,
                            dayDigits[3].split(SEP_DIGIT));
                    cityVehicleLimit.setForbiddenNumbersWeekDay(Calendar.FRIDAY,
                            dayDigits[4].split(SEP_DIGIT));
                    break;
                case ByDate:
                    for (int i = 0; i < dayDigits.length; i++) {
                        cityVehicleLimit.setForbiddenNumbersMonthDay(i + 1,
                                dayDigits[i].split(SEP_DIGIT));
                    }
                    break;
            }
        }

        public static CityVehicleLimit parseFromString(String input) {
            String[] items = input.split(SEP_ITEM);
            final CityVehicleLimit cityVehicleLimit = new CityVehicleLimit();
            parseCityBasic(cityVehicleLimit, items[0]);
            parseDates(cityVehicleLimit, items[1]);
            parseForbiddenNumbers(cityVehicleLimit, items[2]);

            return cityVehicleLimit;
        }

        private static Day parseDate(String strDate) {
            String[] elements = strDate.split(SEP_DIGIT);
            int year = Integer.parseInt(elements[0]);
            int month = Integer.parseInt(elements[1]) - 1;
            int date = Integer.parseInt(elements[2]);
            return new Day(year, month, date);
        }

        public void loadCityInfo(final String strCityInfo) {
            String[] elements = strCityInfo.split(SEP_CITY_INFO);
            city = elements[0];
            managementOption = ManagementOption.parse(elements.length > 1 ? elements[1] : null);
            lastDigitLetterOption = LastDigitLetterOption
                    .parse(elements.length > 2 ? elements[2] : null);
        }

        public void clearForbiddenNumbers() {
            for (DayViewInfo dayInfo : mWeekDayInfoArray) {
                dayInfo.initForbiddenDigits();
            }
            for (DayViewInfo dayInfo : mMonthDayInfoArray) {
                dayInfo.initForbiddenDigits();
            }
        }

        public void loadWeekDayForbiddenNumbers(final String monNumbers, final String tueNumbers,
                final String wedNumbers, final String thuNumbers, final String friNumbers) {
            setForbiddenNumbersWeekDay(Calendar.MONDAY,    monNumbers.split(SEP_DIGIT));
            setForbiddenNumbersWeekDay(Calendar.TUESDAY,   tueNumbers.split(SEP_DIGIT));
            setForbiddenNumbersWeekDay(Calendar.WEDNESDAY, wedNumbers.split(SEP_DIGIT));
            setForbiddenNumbersWeekDay(Calendar.THURSDAY,  thuNumbers.split(SEP_DIGIT));
            setForbiddenNumbersWeekDay(Calendar.FRIDAY,    friNumbers.split(SEP_DIGIT));
        }

        public void loadMonthDayForbiddenNumbers(final String[] dayNumbers) {
            MonthDayDigits monthDayDigits = MonthDayDigits.parse(dayNumbers);
            int dayValue;
            for (int i = 0; i < mMonthDayInfoArray.length; i++) {
                dayValue = i + 1;
                setForbiddenNumbersMonthDay(dayValue, monthDayDigits.getMonthDayDigits(dayValue));
            }
        }

        public void setForbiddenNumbersWeekDay(final int weekdayCalendarId, final String[] strNumbers) {
            if (strNumbers == null || strNumbers.length <= 0) return;
            for (DayViewInfo dayInfo : mWeekDayInfoArray) {
                if (dayInfo.dayValue != weekdayCalendarId) continue;
                for (int i = 0; i < dayInfo.forbiddenDigits.length; i++) {
                    if (TextUtils.isEmpty(strNumbers[i])) continue;
                    try {
                        dayInfo.forbiddenDigits[i] = Integer.parseInt(strNumbers[i]);
                    } catch (Exception e) {
                        // dayInfo.forbiddenDigits[i] = NO_DIGIT_SET;
                    }
                }
                return;
            }
        }

        public void setForbiddenNumbersMonthDay(final int dayId, final String[] strNumbers) {
            if (strNumbers == null || strNumbers.length <= 0) return;
            for (DayViewInfo dayInfo : mMonthDayInfoArray) {
                if (dayInfo.dayValue != dayId) continue;
                for (int i = 0; i < dayInfo.forbiddenDigits.length; i++) {
                    if (TextUtils.isEmpty(strNumbers[i])) continue;
                    try {
                        dayInfo.forbiddenDigits[i] = Integer.parseInt(strNumbers[i]);
                    } catch (Exception e) {
                        // dayInfo.forbiddenDigits[i] = NO_DIGIT_SET;
                    }
                }
                return;
            }
        }

        public void setForbiddenNumbersWeekDay(final int weekdayId, final int[] selectedDigits) {
            if (selectedDigits == null || selectedDigits.length <= 0) return;
            for (DayViewInfo dayInfo : mWeekDayInfoArray) {
                if (dayInfo.dayValue != weekdayId) continue;
                for (int i = 0; i < dayInfo.forbiddenDigits.length; i++) {
                    dayInfo.forbiddenDigits[i] = selectedDigits[i];
                }
                return;
            }
        }

        public void setForbiddenNumbersMonthDay(final int monthdayId, final int[] selectedDigits) {
            if (selectedDigits == null || selectedDigits.length <= 0) return;
            for (DayViewInfo dayInfo : mMonthDayInfoArray) {
                if (dayInfo.dayValue != monthdayId) continue;
                for (int i = 0; i < dayInfo.forbiddenDigits.length; i++) {
                    dayInfo.forbiddenDigits[i] = selectedDigits[i];
                }
                return;
            }
        }

        private int[] getWeekDayForbiddenNumbers(final int weekDayId) {
            if (weekDayId == Calendar.SATURDAY || weekDayId == Calendar.SUNDAY) {
                if (!weekendDigitsForbidden) return null; // 周末不限行
            }
            for (DayViewInfo dayInfo : mWeekDayInfoArray) {
                if (dayInfo.dayValue == weekDayId) {
                    return dayInfo.forbiddenDigits;
                }
            }
            return null;
        }

        private int[] getMonthDayForbiddenNumbers(int monthDayId) {
            for (DayViewInfo dayInfo : mMonthDayInfoArray) {
                if (dayInfo.dayValue == monthDayId) {
                    return dayInfo.forbiddenDigits;
                }
            }
            return null;
        }

        @SuppressWarnings("unused")
        private int[] getNextWeekDayForbiddenNumbers(final int weekDayId) {
            final int nextWeekdayId = WeekDay.getNextWeekDay(weekDayId,
                    !weekendDigitsForbidden).calendarId;
            for (DayViewInfo dayInfo : mWeekDayInfoArray) {
                if (dayInfo.dayValue == nextWeekdayId) {
                    return dayInfo.forbiddenDigits;
                }
            }
            return null;
        }

        private int[] getPrevWeekDayForbiddenNumbers(final int weekDayId) {
            final int prevWeekdayId = WeekDay.getPrevWeekDay(weekDayId,
                    !weekendDigitsForbidden).calendarId;
            for (DayViewInfo dayInfo : mWeekDayInfoArray) {
                if (dayInfo.dayValue == prevWeekdayId) {
                    return dayInfo.forbiddenDigits;
                }
            }
            return null;
        }

        private int[] getNextMonthDayForbiddenNumbers(final int monthDayId) {
            // getNextMonthDayId
            int curMonthDayIndex = -1;
            for (int i = 0; i < mMonthDayInfoArray.length; i++) {
                if (mMonthDayInfoArray[i].dayValue == monthDayId) {
                    curMonthDayIndex = i;
                    break;
                }
            }
            int nextMonthDayIndex = (curMonthDayIndex + 1) % mMonthDayInfoArray.length;
            return mMonthDayInfoArray[nextMonthDayIndex].forbiddenDigits;
        }

        private final ArrayList<Integer> mMonthVisibleLines = new ArrayList<Integer>(6);

        private void checkFoldedForMonth(final View rootView) {
            View viewLine;
            for (Integer monthLine : mMonthVisibleLines) {
                viewLine = rootView.findViewById(monthLine);
                if (mDisplayFolded) {
                    viewLine.setVisibility(View.GONE);
                } else {
                    viewLine.setVisibility(View.VISIBLE);
                }
            }
        }

        private int findLineViewId(final int labelViewId) {
            for (int[][] line : sMonthDayViews) {
                for (int[] days : line) {
                    for (int i = 1; i < days.length; i++) {
                        if (days[i] == labelViewId) return line[0][0];
                    }
                }
            }
            return 0;
        }

        private void setTodayRowVisible(final View rootView, final int todayLabelViewId) {
            int lineViewId = findLineViewId(todayLabelViewId);
            View viewLine = rootView.findViewById(lineViewId);
            viewLine.setVisibility(View.VISIBLE);
        }

        private boolean foundInMonthDayViews(final int viewId) {
            for (DayViewInfo dayInfo : mMonthDayInfoArray) {
                if (dayInfo.labelViewResId == viewId || dayInfo.digitViewResId == viewId) {
                    return true;
                }
            }
            return false;
        }

        private boolean isWeekEnd(final int viewIndex) {
            return viewIndex >= 6;
        }

        private void initMonthDayViewsVisibility(final View rootView) {
            boolean lineVisible, dayVisible;
            int lineViewId;
            for (int[][] line : sMonthDayViews) {
                lineViewId = line[0][0];
                lineVisible = mMonthVisibleLines.contains(lineViewId);
                View lineView = rootView.findViewById(lineViewId);
                if (lineVisible) {
                    lineView.setVisibility(View.VISIBLE);
                    for (int i = 1; i < line.length; i++) {
                        dayVisible = false;
                        for (int dayView : line[i]) {
                            if (foundInMonthDayViews(dayView)) {
                                dayVisible = true;
                                break;
                            }
                        }
                        View dayView = rootView.findViewById(line[i][0]);
                        if (dayVisible) {
                            dayView.setVisibility(View.VISIBLE);
                            if (isWeekEnd(i)) {
                                TextView digitsView = (TextView) rootView.findViewById(line[i][2]);
                                digitsView.setTextAppearance(R.style.weekend_digits);
                            }
                        } else {
                            dayView.setVisibility(View.INVISIBLE);
                        }
                    }
                } else {
                    lineView.setVisibility(View.GONE);
                }
            }
        }

        private void initDayViews(final View rootView, final Day today) {
            final View weekDayView = rootView.findViewById(R.id.forbidden_number_settings_week);
            final View monthDayView = rootView.findViewById(R.id.forbidden_number_settings_month);

            final DayViewInfo[] dayInfoArray;
            final int colorTodayBg, colorTodayText;

            boolean needSetTodayLineVisible = false;
            switch (managementOption) {
                case ByDate:
                    getMonthDayInfoArray(rootView, today);
                    dayInfoArray = mMonthDayInfoArray;
                    colorTodayBg = COLOR_CUR_MONTHDAY_BG;
                    colorTodayText = COLOR_CUR_MONTHDAY_TEXT;
                    monthDayView.setVisibility(View.VISIBLE);
                    needSetTodayLineVisible = mDisplayFolded;
                    weekDayView.setVisibility(View.GONE);
                    initMonthDayViewsVisibility(rootView);
                    checkFoldedForMonth(rootView);
                    break;
                case ByWeek:
                    dayInfoArray = mWeekDayInfoArray;
                    colorTodayBg = COLOR_CUR_WEEKDAY_BG;
                    colorTodayText = COLOR_CUR_WEEKDAY_TEXT;
                    monthDayView.setVisibility(View.GONE);
                    weekDayView.setVisibility(View.VISIBLE);
                    break;
                default:
                    dayInfoArray = null;
                    return;
            }
            TextView textViewDayLabel, textViewDay;
            for (DayViewInfo dayInfo : dayInfoArray) {
                if (dayInfo.digitViewResId < 0) continue;
                if (dayInfo.labelViewResId != DayViewInfo.NO_LABEL_VIEW) {
                    textViewDayLabel = (TextView) rootView.findViewById(dayInfo.labelViewResId);
                    if (dayInfo.labelResId == DayViewInfo.NO_LABEL_RES) {
                        textViewDayLabel.setText(Integer.toString(dayInfo.dayValue));
                    } else {
                        textViewDayLabel.setText(dayInfo.labelResId);
                    }
                }
                textViewDay = (TextView) rootView.findViewById(dayInfo.digitViewResId);
                textViewDay.setText(dayInfo.forbiddenDigitsString());
                if (DayViewInfo.isSameValue(dayInfo, today)) {
                    textViewDay.setBackgroundColor(colorTodayBg);
                    textViewDay.setTextColor(colorTodayText);
                    if (needSetTodayLineVisible) {
                        setTodayRowVisible(rootView, dayInfo.labelViewResId);
                    }
                }
            }
        }

        public void initViews(final View rootView, final boolean isDefaultCity,
                final VehicleLimitActionListener vehicleLimitActionListener, final Day today) {
            final CityVehicleLimit cityVehicleLimit = this;
            final View viewUpdate = rootView.findViewById(R.id.control_update_vehicle_limit);
            viewUpdate.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    vehicleLimitActionListener.onUpdateCity(cityVehicleLimit);
                }
            });

            final View viewDelete = rootView.findViewById(R.id.delete_config);
            if (isDefaultCity) {
                viewDelete.setVisibility(View.GONE);
                viewDelete.setOnClickListener(null);
            } else {
                viewDelete.setVisibility(View.VISIBLE);
                viewDelete.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        vehicleLimitActionListener.onDeleteCity(cityVehicleLimit);
                    }
                });
            }

            final View viewFoldUnfold = rootView.findViewById(R.id.fold_unfold_config);
            if (cityVehicleLimit.managementOption == ManagementOption.ByDate) {
                viewFoldUnfold.setVisibility(View.VISIBLE);
                viewFoldUnfold.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDisplayFolded = !mDisplayFolded;
                        setFoldUnfoldConfigLabel((TextView) viewFoldUnfold);
                        initDayViews(rootView, today);
                    }
                });
                setFoldUnfoldConfigLabel((TextView) viewFoldUnfold);
            } else {
                viewFoldUnfold.setVisibility(View.INVISIBLE);
            }

            TextView textViewCity = (TextView) rootView.findViewById(R.id.city);
            if (isDefaultCity) {
                textViewCity.setTypeface(textViewCity.getTypeface(), Typeface.BOLD_ITALIC);
            } else {
                textViewCity.setTypeface(textViewCity.getTypeface(), Typeface.NORMAL);
            }
            textViewCity.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    vehicleLimitActionListener.onCity(cityVehicleLimit, isDefaultCity);
                }
            });
            initSettingViews(rootView.getContext(), rootView, textViewCity, today);
        }

        private void setFoldUnfoldConfigLabel(final TextView textViewFoldUnfold) {
            if (mDisplayFolded) {
                textViewFoldUnfold.setText(R.string.vehicle_limit_unfold);
            } else {
                textViewFoldUnfold.setText(R.string.vehicle_limit_fold);
            }
        }

        private void initCityView(final View rootView, TextView textViewCity) {
            if (textViewCity == null) {
                textViewCity = (TextView) rootView.findViewById(R.id.city);
            }
            textViewCity.setText(city);
        }

        private void initSettingViews(final Context context, final View rootView,
                TextView textViewCity, final Day today) {
            initCityView(rootView, textViewCity);

            TextView textViewStartDay;
            textViewStartDay = (TextView) rootView.findViewById(R.id.start_date);
            textViewStartDay.setText(getDayString(context, startDay));

            TextView textViewEndDay;
            textViewEndDay = (TextView) rootView.findViewById(R.id.end_date);
            textViewEndDay.setText(getDayString(context, endDay));

            initDayViews(rootView, today);
        }

        private static String getDayString(final Context context, final Day day) {
            if (day == null) {
                return context.getString(R.string.date_not_set);
            }
            return context.getString(R.string.format_yyyy_mm_dd, day.year, day.month + 1, day.day);
        }
    }

    public static class VehicleLimit {
        public String vehicleNumber;

        private final int mSelectedCityIndex = 0;

        private final ArrayList<CityVehicleLimit> mCityVehicleLimits = new ArrayList<CityVehicleLimit>();

        public void clear() {
            mCityVehicleLimits.clear();
        }

        public void add(final CityVehicleLimit cityVehicleLimit) {
            mCityVehicleLimits.add(cityVehicleLimit);
        }

        public boolean deleteCity(final String city) {
            ArrayList<CityVehicleLimit> foundCityVehicleLimits = new ArrayList<CityVehicleLimit>();
            for (CityVehicleLimit cityVehicleLimit : mCityVehicleLimits) {
                if (TextUtils.equals(city, cityVehicleLimit.city)) {
                    foundCityVehicleLimits.add(cityVehicleLimit);
                }
            }
            final int foundCount = foundCityVehicleLimits.size();
            if (foundCount <= 0) return false;
            int removedCount = 0;
            for (CityVehicleLimit cityVehicleLimit : foundCityVehicleLimits) {
                if (mCityVehicleLimits.remove(cityVehicleLimit)) {
                    removedCount++;
                }
            }
            return removedCount == foundCount;
        }

        public boolean hasVehicleLimit() {
            return mCityVehicleLimits.size() > 0;
        }

        public DayCheckResultInfo getLimitedNumbers(Day day) {
            CityVehicleLimit selectedCity = mCityVehicleLimits.get(mSelectedCityIndex);
            return selectedCity.getLimitedNumbers(day, vehicleNumber);
        }

        public boolean cityExists(final String city) {
            for (CityVehicleLimit cityVehicleLimit : mCityVehicleLimits) {
                if (TextUtils.equals(cityVehicleLimit.city, city)) return true;
            }
            return false;
        }

        public String cityVehicleLimitsString() {
            StringBuilder sb = new StringBuilder();
            for (CityVehicleLimit cityVehicleLimit : mCityVehicleLimits) {
                sb.append(cityVehicleLimit);
                sb.append(CityVehicleLimit.SEP_CITY);
            }
            return sb.toString();
        }

        public CityVehicleLimit getCityVehicleLimit() {
            return mCityVehicleLimits.get(mSelectedCityIndex);
        }

        public void setCurrentCity(final CityVehicleLimit cityVehicleLimit) {
            mCityVehicleLimits.remove(cityVehicleLimit);
            mCityVehicleLimits.add(0, cityVehicleLimit);
        }

        public void initViews(final Context context, final LinearLayout layoutView,
                final Day today, final VehicleLimitActionListener vehicleLimitActionListener) {
            final CityVehicleLimit defaultCity = mCityVehicleLimits.get(mSelectedCityIndex);

            boolean isCurrentCity = false;
            for (final CityVehicleLimit cityVehicleLimit : mCityVehicleLimits) {
                isCurrentCity = defaultCity == cityVehicleLimit;
                View rootView = View.inflate(context, R.layout.city_vehicle_limit, null);
                cityVehicleLimit.initViews(rootView, isCurrentCity, vehicleLimitActionListener,
                        today);
                layoutView.addView(rootView);
            }
        }

        public static void initVehicleNumber(String inputVehicleNumber) {
            sVehicleLimit.vehicleNumber = inputVehicleNumber;
        }

        public static void initFromString(String input) {
            String[] cityItems = input.split(CityVehicleLimit.SEP_CITY);

            sVehicleLimit.clear();
            for (String cityItem : cityItems) {
                if (TextUtils.isEmpty(cityItem)) continue;
                sVehicleLimit.add(CityVehicleLimit.parseFromString(cityItem));
            }
        }
    }

    private interface ViewCityForbiddenSettingsListener {
        public void postPositiveClick(final String city);
        public void postNeutralClick(final String city);
        public void postNegativeClick();
    }

    private static enum ConfigMode {
        Update(R.string.update_start_date, R.string.update_end_date, R.string.update_forbidden_digits),
        Add(R.string.set_start_date, R.string.set_end_date, R.string.set_forbidden_digits);

        public final int changeStartDateLabelResId;
        public final int changeEndDateLabelResId;
        public final int changeDigitsLabelResId;

        private ConfigMode(int changeStartDateLabelResId, int changeEndDateLabelResId,
                int changeDigitsLabelResId) {
            this.changeStartDateLabelResId = changeStartDateLabelResId;
            this.changeEndDateLabelResId = changeEndDateLabelResId;
            this.changeDigitsLabelResId = changeDigitsLabelResId;
        }
    }

    private static class ViewCityForbiddenSettings implements Utility.ViewInitWith3Buttons {
        private EditText mEditCity;

        private TextView mTextViewStartDate;
        private TextView mTextViewEndDate;

        private Day mStartDay;
        private Day mEndDay;

        private static final String FORMAT_DATE = "%s: %s";

        private final Context mContext;
        private final Day mToday;
        private final CityVehicleLimit mCityVehicleLimit;
        private final ConfigMode mConfigMode;
        private final ViewCityForbiddenSettingsListener mListener;

        private ManagementOption mManagementOption;
        private LastDigitLetterOption mLastDigitLetterOption;

        private DayDigitViewInfo[] mViewInfoArray;

        public ViewCityForbiddenSettings(final Context context, final Day today,
                final ViewCityForbiddenSettingsListener listener,
                final CityVehicleLimit cityVehicleLimit) {
            mContext = context;
            mCityVehicleLimit = cityVehicleLimit;
            mListener = listener;
            mToday = today;

            mConfigMode = cityVehicleLimit.city == null ? ConfigMode.Add : ConfigMode.Update;

            setManagementOption(cityVehicleLimit.managementOption);
            setLastDigitLetterOption(cityVehicleLimit.lastDigitLetterOption);
        }

        private void setManagementOption(ManagementOption managementOption) {
            mManagementOption = managementOption;
            if (mManagementOption == null) {
                mManagementOption = ManagementOption.ByWeek;
            }
        }

        private void setLastDigitLetterOption(LastDigitLetterOption lastDigitLetterOption) {
            mLastDigitLetterOption = lastDigitLetterOption;
            if (mLastDigitLetterOption == null) {
                mLastDigitLetterOption = LastDigitLetterOption.TakeAs0;
            }
        }

        private void viewNumberSettingsRefresh(final View rootView, final boolean loopNext) {
            View viewWeek = rootView.findViewById(R.id.forbidden_number_settings_in_week);
            View viewMonth = rootView.findViewById(R.id.forbidden_number_settings_in_month);

            if (mManagementOption == ManagementOption.ByWeek) {
                viewWeek.setVisibility(View.VISIBLE);
                viewMonth.setVisibility(View.GONE);
            } else {
                viewWeek.setVisibility(View.GONE);
                viewMonth.setVisibility(View.VISIBLE);
            }
            if (mViewInfoArray != null) {
                for (DayDigitViewInfo viewInfo : mViewInfoArray) {
                    viewInfo.invisibleView();
                }
            }
            mViewInfoArray = DayDigitViewInfo.getDayViewInfoArray(mCityVehicleLimit, mManagementOption);
            int forbiddenDigits[];
            // init digit views;
            for (DayDigitViewInfo viewInfo : mViewInfoArray) {
                viewInfo.initView(rootView);
                if (loopNext) {
                    forbiddenDigits = mCityVehicleLimit.getForbiddenDigitsNextDay(mManagementOption,
                            viewInfo.dayId);
                } else {
                    forbiddenDigits = mCityVehicleLimit.getForbiddenDigits(mManagementOption,
                            viewInfo.dayId);
                }
                viewInfo.setCheckedDigits(forbiddenDigits);
            }
        }

        @Override
        public void initViews(final View rootView) {
            viewNumberSettingsRefresh(rootView, false);

            final TextView viewDigitsSetting = (TextView) rootView
                    .findViewById(R.id.label_digits_setting);
            viewDigitsSetting.setText(mConfigMode.changeDigitsLabelResId);

            if (mConfigMode == ConfigMode.Add) {
                mEditCity = (EditText) rootView.findViewById(R.id.edit_city);
            } else {
                mStartDay = new Day(mCityVehicleLimit.startDay.year,
                        mCityVehicleLimit.startDay.month,
                        mCityVehicleLimit.startDay.day);

                mEndDay = new Day(mCityVehicleLimit.endDay.year, mCityVehicleLimit.endDay.month,
                        mCityVehicleLimit.endDay.day);

                final View textViewCity = rootView.findViewById(R.id.view_city);
                textViewCity.setVisibility(View.GONE);
            }

            mTextViewStartDate = (TextView) rootView.findViewById(R.id.start_date);
            mTextViewStartDate.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog(mStartDay == null ? mToday : mStartDay,
                            mContext.getString(mConfigMode.changeStartDateLabelResId),
                            new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear,
                                int dayOfMonth) {
                            if (year != mStartDay.year || monthOfYear != mStartDay.month
                                    || dayOfMonth != mStartDay.day) {
                                mStartDay = new Day(year, monthOfYear, dayOfMonth);
                                setTexts();
                            }
                        }
                    });
                }
            });

            mTextViewEndDate = (TextView) rootView.findViewById(R.id.end_date);
            mTextViewEndDate.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog(mEndDay == null ? mToday : mEndDay,
                            mContext.getString(mConfigMode.changeEndDateLabelResId),
                            new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear,
                                int dayOfMonth) {
                            if (year != mEndDay.year || monthOfYear != mEndDay.month
                                    || dayOfMonth != mEndDay.day) {
                                mEndDay = new Day(year, monthOfYear, dayOfMonth);
                                setTexts();
                            }
                        }
                    });
                }
            });
            setTexts();

            final RadioGroup radioGroupManagement = (RadioGroup) rootView
                    .findViewById(R.id.radiogroup_management);
            radioGroupManagement.check(mManagementOption.radioResId);
            radioGroupManagement.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    mManagementOption = ManagementOption.getOption(checkedId);
                    viewNumberSettingsRefresh(rootView, false);
                    checkLoopNextAvailable(rootView);
                }
            });

            final RadioGroup radioGroupLastDigit = (RadioGroup) rootView
                    .findViewById(R.id.radiogroup_last_digit);
            radioGroupLastDigit.check(mLastDigitLetterOption.radioResId);
            radioGroupLastDigit.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    mLastDigitLetterOption = LastDigitLetterOption.getOption(checkedId);
                }
            });

            checkLoopNextAvailable(rootView);
        }

        private void checkLoopNextAvailable(final View rootView) {
            final boolean loopNextAvailable;
            if (mConfigMode == ConfigMode.Update) {
                if (mCityVehicleLimit.managementOption == mManagementOption) {
                    loopNextAvailable = true;
                } else {
                    loopNextAvailable = false;
                }
            } else {
                loopNextAvailable = false;
            }
            final View viewLoopNext = rootView.findViewById(R.id.next_loop);
            if (loopNextAvailable) {
                viewLoopNext.setVisibility(View.VISIBLE);
                viewLoopNext.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewNumberSettingsRefresh(rootView, true);
                        viewLoopNext.setVisibility(View.GONE);
                    }
                });
            } else {
                viewLoopNext.setVisibility(View.GONE);
            }
        }

        private static String formatDate(Context context, Day day, int dateLabelResId) {
            return String.format(FORMAT_DATE, context.getString(dateLabelResId),
                    CityVehicleLimit.getDayString(context, day));
        }

        private void setTexts() {
            mTextViewStartDate.setText(formatDate(mContext, mStartDay, R.string.start_date));
            mTextViewEndDate.setText(formatDate(mContext, mEndDay, R.string.end_date));
        }

        private boolean checkInputConfig() {
            final String city;
            if (mCityVehicleLimit.city == null) {
                city = Utility.getEditText(mEditCity);
                if (TextUtils.isEmpty(city)) {
                    Utility.showInfoDialog(mContext, R.string.prompt_input_city);
                    return false;
                }
                if (sVehicleLimit.cityExists(city)) {
                    Utility.showInfoDialog(mContext, R.string.prompt_input_city_exists);
                    return false;
                }
            } else {
                city = mCityVehicleLimit.city;
            }

            if (mEndDay.lessEquals(mStartDay)) {
                Utility.showInfoDialog(mContext, R.string.prompt_end_date_le_start_date);
                return false;
            }
            if (!digitsConfigAllRight(mManagementOption, mViewInfoArray)) {
                Utility.showInfoDialog(mContext, mManagementOption.promptWrongResId);
                return false;
            }

            if (!TextUtils.equals(mCityVehicleLimit.city, city)) {
                mCityVehicleLimit.city = city;
            }
            mCityVehicleLimit.managementOption = mManagementOption;
            mCityVehicleLimit.lastDigitLetterOption = mLastDigitLetterOption;

            mCityVehicleLimit.startDay = new Day(mStartDay.year, mStartDay.month, mStartDay.day);
            mCityVehicleLimit.endDay = new Day(mEndDay.year, mEndDay.month, mEndDay.day);

            for (DayDigitViewInfo viewInfo : mViewInfoArray) {
                if (mCityVehicleLimit.managementOption == ManagementOption.ByWeek) {
                    mCityVehicleLimit.setForbiddenNumbersWeekDay(viewInfo.dayId,
                            viewInfo.checkedDigits);
                } else if (mCityVehicleLimit.managementOption == ManagementOption.ByDate) {
                    mCityVehicleLimit.setForbiddenNumbersMonthDay(viewInfo.dayId,
                            viewInfo.checkedDigits);
                }
            }
            return true;
        }

        // Replace the current vehicle limit with the input config for the city.
        @Override
        public void onPositiveClick(View rootView) {
            if (checkInputConfig()) {
                mListener.postPositiveClick(mCityVehicleLimit.city);
            }
        }

        @Override
        public void onNegativeClick(View rootView) {
            mListener.postNegativeClick();
        }

        // Append a new vehicle limit for the city.
        @Override
        public void onNeutralClick(View rootView) {
            if (checkInputConfig()) {
                mListener.postNeutralClick(mCityVehicleLimit.city);
            }
        }

        private void showDatePickerDialog(final Day day, final String title,
                final DatePickerDialog.OnDateSetListener dateSetListener) {
            DatePickerDialog dialog = new DatePickerDialog(mContext, dateSetListener, day.year,
                    day.month, day.day);
            dialog.setTitle(title);
            dialog.show();
        }
    };

    private static final VehicleLimit sVehicleLimit = new VehicleLimit();

    private static void getDefaultVehicleLimits(final Context context) {
        Resources res = context.getResources();

        final CityVehicleLimit bjVehicleLimit = loadCityVehicleLimitFromXml(res,
                R.array.numbers_forbidden_bj);
        final CityVehicleLimit xaVehicleLimit = loadCityVehicleLimitFromXml(res,
                R.array.numbers_forbidden_xa);
        final CityVehicleLimit lzVehicleLimit = loadCityVehicleLimitFromXml(res,
                R.array.numbers_forbidden_lz);
        final CityVehicleLimit hzVehicleLimit = loadCityVehicleLimitFromXml(res,
                R.array.numbers_forbidden_hz);

        sVehicleLimit.add(bjVehicleLimit);
        sVehicleLimit.add(xaVehicleLimit);
        sVehicleLimit.add(lzVehicleLimit);
        sVehicleLimit.add(hzVehicleLimit);

        saveForbiddenNumbersPreferences(context);
    }

    private static CityVehicleLimit loadCityVehicleLimitFromXml(final Resources res,
            final int cityVehicleLimitResArrayId) {
        String[] array = res.getStringArray(cityVehicleLimitResArrayId);
        final CityVehicleLimit cityVehicleLimit = new CityVehicleLimit();
        cityVehicleLimit.loadCityInfo(array[0]);

        cityVehicleLimit.startDay = CityVehicleLimit.parseDate(array[1]);
        cityVehicleLimit.endDay = CityVehicleLimit.parseDate(array[2]);

        cityVehicleLimit.clearForbiddenNumbers();
        switch (cityVehicleLimit.managementOption) {
            case ByWeek:
                cityVehicleLimit.loadWeekDayForbiddenNumbers(array[3], array[4], array[5], array[6],
                        array[7]);
                break;
            case ByDate:
                final String[] dayNumbers = new String[array.length - 3];
                for (int i = 0; i < dayNumbers.length; i++) {
                    dayNumbers[i] = array[i + 3];
                }
                cityVehicleLimit.loadMonthDayForbiddenNumbers(dayNumbers);
                break;
        }
        return cityVehicleLimit;
    }

    public static SharedPreferencesHelper getPreferencesHelper(final Context context) {
        return new SharedPreferencesHelper(context, PREF_NAME);
    }

    private static boolean loadVehicleLimit(final Context context) {
        final SharedPreferencesHelper prefHelper = getPreferencesHelper(context);
        if (prefHelper.contains(PREF_KEY_VEHICLE_NUMBER)) {
            VehicleLimit.initVehicleNumber((String) prefHelper.get(PREF_KEY_VEHICLE_NUMBER, null));
        }
        if (prefHelper.contains(PREF_KEY_CITY_FORBIDDEN_INFO)) {
            try {
                String cityInfo = (String) prefHelper.get(PREF_KEY_CITY_FORBIDDEN_INFO, null);
                VehicleLimit.initFromString(cityInfo);
            } catch (Exception e) {
                Log.d("WB", "Exception in loading pref!", e);
                return false;
            }
            return true;
        }
        return false;
    }

    private static void saveForbiddenNumbersPreferences(final Context context) {
        getPreferencesHelper(context).put(PREF_KEY_CITY_FORBIDDEN_INFO,
                sVehicleLimit.cityVehicleLimitsString());
    }

    private static void saveVehicleNumber(final Context context) {
        getPreferencesHelper(context).put(PREF_KEY_VEHICLE_NUMBER, sVehicleLimit.vehicleNumber);
    }

    private static void initVehicleLimit(final Context context) {
        if (!loadVehicleLimit(context)) {
            getDefaultVehicleLimits(context);
        }
    }

    public static Constants.TextViewInfo getVehicleForbiddenInfo(final Context context,
            final Day day) {
        if (!sVehicleLimit.hasVehicleLimit()) {
            initVehicleLimit(context);
        }
        DayCheckResultInfo resultInfo = sVehicleLimit.getLimitedNumbers(day);
        String info = resultInfo == null ? null : resultInfo.getInfo(context);
        if (TextUtils.isEmpty(info)) {
            return null;
        }
        int textColor;
        if (SettingsCalendarActivity.isPublicHoliday(context, day)) {
            DayCheckResult dayCheckResult = DayCheckResult.PublicHoliday;
            info = context.getString(dayCheckResult.promptResId);
            textColor = dayCheckResult.promptColor;
        } else {
            textColor = resultInfo.result.promptColor;
        }
        return new Constants.TextViewInfo(info, textColor);
    }

    private Day mToday;

    private LinearLayout mCityViewLayout;

    private final VehicleLimitActionListener mVehicleLimitActionListener = new VehicleLimitActionListener() {
        @Override
        public void onUpdateCity(CityVehicleLimit cityVehicleLimit) {
            showUpdateDigitsDialog(cityVehicleLimit);
        }

        @Override
        public void onDeleteCity(CityVehicleLimit cityVehicleLimit) {
            showDeleteDialog(cityVehicleLimit.city);
        }

        @Override
        public void onCity(final CityVehicleLimit cityVehicleLimit,
                final boolean isDefaultCity) {
            showCityDialog(cityVehicleLimit, isDefaultCity);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.main_vehicle_limit);

        if (!sVehicleLimit.hasVehicleLimit()) {
            initVehicleLimit(this);
        }

        initToday();
        initViews();

        super.onCreate(savedInstanceState);
    }

    private void initViews() {
        TextView textViewToday = (TextView) findViewById(R.id.today_date);
        textViewToday.setText(getTodayText());

        TextView textViewVehicleNumber = (TextView) findViewById(R.id.vehicle_number);
        initVehicleNumberView(textViewVehicleNumber);
        textViewVehicleNumber.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showVehicleNumberSettingDialog();
            }
        });
        initCityViews();
    }

    private void initCityViews() {
        if (mCityViewLayout == null) {
            mCityViewLayout = (LinearLayout) findViewById(R.id.view_cities);
        } else {
            mCityViewLayout.removeAllViews();
        }
        sVehicleLimit.initViews(this, mCityViewLayout, mToday, mVehicleLimitActionListener);
    }

    private void initVehicleNumberView(TextView textViewVehicleNumber) {
        if (textViewVehicleNumber == null) {
            textViewVehicleNumber = (TextView) findViewById(R.id.vehicle_number);
        }
        if (!TextUtils.isEmpty(sVehicleLimit.vehicleNumber)) {
            textViewVehicleNumber.setText(sVehicleLimit.vehicleNumber);
        }
    }

    private void initToday() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DATE);
        mToday = new Day(year, month, day);
        mToday.weekday = cal.get(Calendar.DAY_OF_WEEK);
    }

    private String getTodayText() {
        String weekDay = WeekDay.getFullLabel(this, mToday.weekday);
        return getString(R.string.format_today, mToday.year, mToday.month + 1, mToday.day, weekDay);
    }

    private static class DayDigitViewInfo {
        private static final int[][] sCheckBoxMapping = {
                /*  digit, view_res_id */
                {1,  R.id.digit_1},
                {2,  R.id.digit_2},
                {3,  R.id.digit_3},
                {4,  R.id.digit_4},
                {5,  R.id.digit_5},
                {6,  R.id.digit_6},
                {7,  R.id.digit_7},
                {8,  R.id.digit_8},
                {9,  R.id.digit_9},
                {0,  R.id.digit_0},
        };

        final int dayId;
        final int viewDayId;
        View viewDay;
        int[] checkedDigits;

        public DayDigitViewInfo(final CityVehicleLimit cityVehicleLimit, final int monthDayId,
                final int viewDayId) {
            dayId = monthDayId;
            this.viewDayId = viewDayId;
        }

        public DayDigitViewInfo(final CityVehicleLimit cityVehicleLimit, final WeekDay weekDay,
                final int viewDayId) {
            dayId = weekDay.calendarId;
            this.viewDayId = viewDayId;
        }

        public void initView(final View rootView) {
            viewDay = rootView.findViewById(viewDayId);
            viewDay.setVisibility(View.VISIBLE);
        }

        public void invisibleView() {
            if (viewDay == null) return;
            viewDay.setVisibility(View.INVISIBLE);
            initCheckedDigits();
        }

        public boolean isDigitSelected(final int[] forbiddenDigits, final int digit) {
            for (int n : forbiddenDigits) {
                if (n == digit) return true;
            }
            return false;
        }

        private void initCheckedDigits() {
            CheckBox checkbox;
            for (int[] checkboxInfo : sCheckBoxMapping) {
                checkbox = (CheckBox) viewDay.findViewById(checkboxInfo[1]);
                checkbox.setChecked(false);
            }
        }

        public void setCheckedDigits(final int[] forbiddenDigits) {
            CheckBox checkbox;
            for (int[] checkboxInfo : sCheckBoxMapping) {
                checkbox = (CheckBox) viewDay.findViewById(checkboxInfo[1]);
                checkbox.setChecked(isDigitSelected(forbiddenDigits, checkboxInfo[0]));
            }
        }

        public void getCheckedDigits() {
            ArrayList<Integer> digits = new ArrayList<Integer>(sCheckBoxMapping.length);
            CheckBox checkbox;
            for (int[] checkboxInfo : sCheckBoxMapping) {
                checkbox = (CheckBox)viewDay.findViewById(checkboxInfo[1]);
                if (checkbox.isChecked()) {
                    digits.add(checkboxInfo[0]);
                }
            }
            checkedDigits = new int[digits.size()];
            for (int i = 0; i < checkedDigits.length; i++) {
                checkedDigits[i] = digits.get(i);
            }
        }

        public static DayDigitViewInfo[] getDayViewInfoArray(CityVehicleLimit cityVehicleLimit,
                ManagementOption managementOption) {
            if (managementOption == ManagementOption.ByWeek) {
                final DayDigitViewInfo[] viewWeekDayInfoArray = {
                        new DayDigitViewInfo(cityVehicleLimit, WeekDay.Mon, R.id.view_monday),
                        new DayDigitViewInfo(cityVehicleLimit, WeekDay.Tue, R.id.view_tuesday),
                        new DayDigitViewInfo(cityVehicleLimit, WeekDay.Wed, R.id.view_wednesday),
                        new DayDigitViewInfo(cityVehicleLimit, WeekDay.Thu, R.id.view_thursday),
                        new DayDigitViewInfo(cityVehicleLimit, WeekDay.Fri, R.id.view_friday),
                };
                return viewWeekDayInfoArray;
            }
            if (managementOption == ManagementOption.ByDate) {
                final DayDigitViewInfo[] viewMonthDayInfoArray = {
                        new DayDigitViewInfo(cityVehicleLimit,  1, R.id.view_date_01),
                        new DayDigitViewInfo(cityVehicleLimit,  2, R.id.view_date_02),
                        new DayDigitViewInfo(cityVehicleLimit,  3, R.id.view_date_03),
                        new DayDigitViewInfo(cityVehicleLimit,  4, R.id.view_date_04),
                        new DayDigitViewInfo(cityVehicleLimit,  5, R.id.view_date_05),
                        new DayDigitViewInfo(cityVehicleLimit, 6, R.id.view_date_06),
                        new DayDigitViewInfo(cityVehicleLimit, 7, R.id.view_date_07),
                        new DayDigitViewInfo(cityVehicleLimit, 8, R.id.view_date_08),
                        new DayDigitViewInfo(cityVehicleLimit, 9, R.id.view_date_09),
                        new DayDigitViewInfo(cityVehicleLimit, 10, R.id.view_date_10),
                        new DayDigitViewInfo(cityVehicleLimit, 11, R.id.view_date_11),
                        new DayDigitViewInfo(cityVehicleLimit, 12, R.id.view_date_12),
                        new DayDigitViewInfo(cityVehicleLimit, 13, R.id.view_date_13),
                        new DayDigitViewInfo(cityVehicleLimit, 14, R.id.view_date_14),
                        new DayDigitViewInfo(cityVehicleLimit, 15, R.id.view_date_15),
                        new DayDigitViewInfo(cityVehicleLimit, 16, R.id.view_date_16),
                        new DayDigitViewInfo(cityVehicleLimit, 17, R.id.view_date_17),
                        new DayDigitViewInfo(cityVehicleLimit, 18, R.id.view_date_18),
                        new DayDigitViewInfo(cityVehicleLimit, 19, R.id.view_date_19),
                        new DayDigitViewInfo(cityVehicleLimit, 20, R.id.view_date_20),
                        new DayDigitViewInfo(cityVehicleLimit, 21, R.id.view_date_21),
                        new DayDigitViewInfo(cityVehicleLimit, 22, R.id.view_date_22),
                        new DayDigitViewInfo(cityVehicleLimit, 23, R.id.view_date_23),
                        new DayDigitViewInfo(cityVehicleLimit, 24, R.id.view_date_24),
                        new DayDigitViewInfo(cityVehicleLimit, 25, R.id.view_date_25),
                        new DayDigitViewInfo(cityVehicleLimit, 26, R.id.view_date_26),
                        new DayDigitViewInfo(cityVehicleLimit, 27, R.id.view_date_27),
                        new DayDigitViewInfo(cityVehicleLimit, 28, R.id.view_date_28),
                        new DayDigitViewInfo(cityVehicleLimit, 29, R.id.view_date_29),
                        new DayDigitViewInfo(cityVehicleLimit, 30, R.id.view_date_30),
                        new DayDigitViewInfo(cityVehicleLimit, 31, R.id.view_date_31),
                };
                return viewMonthDayInfoArray;
            }
            return null;
        }
    }

    private void showUpdateDigitsDialog(final CityVehicleLimit cityVehicleLimit) {
        final Context context = this;

        final String title = getString(R.string.title_update_vehicle_limit, cityVehicleLimit.city);
        final String replaceLabel = getString(R.string.city_vehicle_limit_replace);
        // 一个城市可以有多个不同日期的尾号限制可见？还没想好怎么实现...
        final String neutralLabel = null;// getString(R.string.city_vehicle_limit_append);
        final String negativeLabel = getString(android.R.string.cancel);

        final ViewCityForbiddenSettingsListener listener = new ViewCityForbiddenSettingsListener() {
            @Override
            public void postPositiveClick(String city) {
                cityViewChanged(city);
            }

            @Override
            public void postNeutralClick(String city) {
                cityViewChanged(city);
            }

            @Override
            public void postNegativeClick() {
                // ...
            }
        };
        final ViewCityForbiddenSettings viewConfig = new ViewCityForbiddenSettings(context, mToday,
                listener, cityVehicleLimit);
        Utility.showViewDialog(context, R.layout.view_city_vehicle_forbidden_digits, title,
                viewConfig, replaceLabel, neutralLabel, negativeLabel);
    }

    private void showDeleteDialog(final String city) {
        final Context context = this;
        final String confirmPrompt = context.getString(R.string.prompt_delete_city_number_settings,
                city);
        final DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (sVehicleLimit.deleteCity(city)) {
                    cityViewChanged(city);
                } else {
                    Utility.showInfoDialog(context, context
                            .getString(R.string.prompt_failed_to_delete_city_settings, city));
                }
            }
        };
        Utility.showConfirmDialog(context, confirmPrompt, positiveButtonListener);
    }

    private void cityViewChanged(final String city) {
        final Context context = this;
        initCityViews();
        saveForbiddenNumbersPreferences(context);
        Constants.refreshWidget(context);
    }

    private void showVehicleNumberSettingDialog() {
        final Context context = this;

        final String title;
        if (TextUtils.isEmpty(sVehicleLimit.vehicleNumber)) {
            title = getString(R.string.vehicle_number_set);
        } else {
            title = getString(R.string.vehicle_number_change);
        }

        final String positiveLabel = getString(android.R.string.ok);
        final String negativeLabel = getString(android.R.string.cancel);

        final Utility.ViewInitWithPositiveNegative viewInit = new Utility.ViewInitWithPositiveNegative() {
            private EditText editVehicleNumber;
            @Override
            public void initViews(View rootView) {
                editVehicleNumber = (EditText) rootView.findViewById(R.id.edit_vehicle_number);
                editVehicleNumber.setText(sVehicleLimit.vehicleNumber);
            }

            @Override
            public void onPositiveClick(View rootView) {
                String inputVehicleNumber = Utility.getEditText(editVehicleNumber);
                if (TextUtils.isEmpty(inputVehicleNumber)) return;
                if (TextUtils.equals(inputVehicleNumber, sVehicleLimit.vehicleNumber)) return;
                sVehicleLimit.vehicleNumber = inputVehicleNumber;

                initVehicleNumberView(null);

                saveVehicleNumber(context);

                Constants.refreshWidget(context);
            }

            @Override
            public void onNegativeClick(View rootView) {
                // ...
            }

        };
        Utility.showViewDialog(context, R.layout.view_vehicle_number, title,
                viewInit, positiveLabel, negativeLabel);
    }

    private void showCityDialog(final CityVehicleLimit cityVehicleLimit,
            final boolean isCurrentCity) {
        final Context context = this;

        final String title;
        if (TextUtils.isEmpty(cityVehicleLimit.city)) {
            title = getString(R.string.city_set);
        } else {
            title = getString(R.string.city_change);
        }

        final String positiveLabel = getString(android.R.string.ok);
        final String negativeLabel = getString(android.R.string.cancel);

        final Utility.ViewInitWithPositiveNegative viewInit = new Utility.ViewInitWithPositiveNegative() {
            private EditText editCity;
            private CheckBox checkBoxCurrentCity;

            @Override
            public void initViews(View rootView) {
                editCity = (EditText) rootView.findViewById(R.id.edit_city);
                editCity.setText(cityVehicleLimit.city);

                checkBoxCurrentCity = (CheckBox) rootView
                        .findViewById(R.id.checkbox_set_as_current_city);
                checkBoxCurrentCity.setChecked(isCurrentCity);
            }

            @Override
            public void onPositiveClick(View rootView) {
                String inputCity = Utility.getEditText(editCity);
                if (TextUtils.isEmpty(inputCity)) return;
                if (TextUtils.equals(inputCity, cityVehicleLimit.city)
                        && checkBoxCurrentCity.isChecked() == isCurrentCity) {
                    return; // No change, no need to save.
                }
                cityVehicleLimit.city = inputCity;
                sVehicleLimit.setCurrentCity(cityVehicleLimit);
                cityViewChanged(cityVehicleLimit.city);
            }

            @Override
            public void onNegativeClick(View rootView) {
                // ...
            }

        };
        Utility.showViewDialog(context, R.layout.view_city, title, viewInit, positiveLabel,
                negativeLabel);
    }

    private static boolean isDigitRepeated(final int[] array1, final int[] array2) {
        for (int i = 0; i < array1.length; i++) {
            for (int j = 0; j < array2.length; j++) {
                if (array1[i] == array2[j]) return true;
            }
        }
        return false;
    }

    private static boolean digitsConfigAllRight(final ManagementOption managementOption,
            final DayDigitViewInfo[] viewInfoArray) {
        for (DayDigitViewInfo viewInfo : viewInfoArray) {
            viewInfo.getCheckedDigits();
        }

        // 检查是否有空选择，或者是否有重复的尾号被不同工作日选择.
        // 有重复或者空选择的话返回false; 否则说明检查pass.
        for (int i = 0; i < viewInfoArray.length - 1; i++) {
            int[] checkedDigits = viewInfoArray[i].checkedDigits;
            if (checkedDigits == null
                    || checkedDigits.length != COUNT_FORBIDDEN_NUMBER_COUNT) {
                return false;
            }

            if (managementOption == ManagementOption.ByDate) continue;

            for (int j = i + 1; j < viewInfoArray.length; j++) {
                int[] curDigits = viewInfoArray[j].checkedDigits;
                if (curDigits == null || curDigits.length != COUNT_FORBIDDEN_NUMBER_COUNT) {
                    return false;
                }
                if (isDigitRepeated(checkedDigits, curDigits)) return false;
            }
        }

        return true;
    }

    private void showAddCityDialog() {
        final Context context = this;
        final CityVehicleLimit newCityVehicleLimit = new CityVehicleLimit();

        final String title = getString(R.string.title_add_vehicle_limit);
        final String positiveLabel = getString(android.R.string.ok);
        final String negativeLabel = getString(android.R.string.cancel);

        final ViewCityForbiddenSettingsListener listener = new ViewCityForbiddenSettingsListener() {
            @Override
            public void postPositiveClick(String city) {
                sVehicleLimit.add(newCityVehicleLimit);
                cityViewChanged(city);
            }

            @Override
            public void postNegativeClick() {
                // ...
            }

            @Override
            public void postNeutralClick(String city) {
                // ...
            }
        };
        final ViewCityForbiddenSettings viewConfig = new ViewCityForbiddenSettings(context, mToday,
                listener, newCityVehicleLimit);

        Utility.showViewDialog(context, R.layout.view_city_vehicle_forbidden_digits, title,
                viewConfig, positiveLabel, negativeLabel);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_city:
                showAddCityDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_vehicle_limit, menu);
        return true;
    }

    // 根据本月1号的星期几，可以知道上个月还需要看多少天在本月.
    private static int getRemainingDaysInLastMonth(final int year, final int month,
            final int firstDayOfWeek) {
        final GregorianCalendar gregorian_cal = new GregorianCalendar(year, month, 1);
        int remainingCount = gregorian_cal.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek/* Calendar.
         * SUNDAY */;
        if (remainingCount > 0) return remainingCount;
        return 7 + remainingCount;
    }

    private static int getMonthDayNum(final int year, final int month) {
        // Gregorian Calendar : MINUS 1, set to FIRST OF MONTH
        final GregorianCalendar gregorian_cal = new GregorianCalendar(year, month, 1);
        if (gregorian_cal.isLeapYear(year) && month == Calendar.FEBRUARY) {
            return Constants.MONTH_DAYS[month] + 1;
        }
        return Constants.MONTH_DAYS[month];
    }
}
