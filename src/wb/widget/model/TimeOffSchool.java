package wb.widget.model;

import android.graphics.Color;
import android.text.TextUtils;

public class TimeOffSchool {
    public String timeOffSchool;
    public int colorText;
    public int colorBackground;

    private static final String SEP_ = ",";
    private static final String FORMAT_TO_STRING = "%s" + SEP_ + "%d" + SEP_ + "%d";
    private static final String SEP_HOUR_MINUTE = ":";
    private static final String FORMAT_TIME = "%02d" + SEP_HOUR_MINUTE + "%02d";

    // Default colors for timeOffSchool.
    private static final int DEFAULT_COLOR_TIME_OFF_SCHOOL_TEXT = Color.BLACK;
    private static final int DEFAULT_COLOR_TIME_OFF_SCHOOL_BACKGROUND = Color.WHITE;

    public TimeOffSchool() {
        colorText = DEFAULT_COLOR_TIME_OFF_SCHOOL_TEXT;
        colorBackground = DEFAULT_COLOR_TIME_OFF_SCHOOL_BACKGROUND;
    }

    @Override
    public String toString() {
        return String.format(FORMAT_TO_STRING, timeOffSchool, colorText, colorBackground);
    }

    private int mHourOfDay, mMinute;

    public void parseHourMinute() {
        if (TextUtils.isEmpty(timeOffSchool) || timeOffSchool.indexOf(SEP_HOUR_MINUTE) < 0) {
            mHourOfDay = -1;
            mMinute = -1;
        } else {
            String[] elements = timeOffSchool.split(SEP_HOUR_MINUTE);
            mHourOfDay = Integer.parseInt(elements[0]);
            mMinute = Integer.parseInt(elements[1]);
        }
    }

    public void setTime(final int hour_of_day, final int minute) {
        mHourOfDay = hour_of_day;
        mMinute = minute;
        timeOffSchool = String.format(FORMAT_TIME, mHourOfDay, mMinute);
    }

    public int getHourOfDay() {
        return mHourOfDay;
    }

    public int getMinute() {
        return mMinute;
    }

    private static final String FORMAT_STRING = "放学%s";

    public String timeOffSchoolString() {
        return String.format(FORMAT_STRING, timeOffSchool);
    }
    
    public static TimeOffSchool copy(final TimeOffSchool input) {
        if (input == null) return null;
        TimeOffSchool timeOffSchool = new TimeOffSchool();
        timeOffSchool.timeOffSchool = input.timeOffSchool;
        timeOffSchool.colorText = input.colorText;
        timeOffSchool.colorBackground = input.colorBackground;
        return timeOffSchool;
    }
    
    public static TimeOffSchool parse(final String input) {
        if (TextUtils.isEmpty(input)) return null;
        
        String[] elements = input.split(SEP_);

        TimeOffSchool timeOffSchool = new TimeOffSchool();
        if (elements.length > 0) {
            timeOffSchool.timeOffSchool = elements[0];
        }

        if (elements.length > 1) {
            timeOffSchool.colorText = Integer.parseInt(elements[1]);
        }

        if (elements.length > 2) {
            timeOffSchool.colorBackground = Integer.parseInt(elements[2]);
        }

        return timeOffSchool;
    }
}
