package wb.widget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import wb.widget.model.ColorPickerDialog;
import wb.widget.model.Day;
import wb.widget.model.SharedPreferencesHelper;
import wb.widget.utils.Utility;
import wb.widget.utils.Utility.ViewInitWithPositiveNegative;
import wb.widget.utils.calendar.AlmanacUtils;
import wb.widget.utils.calendar.CalendarUtil;
import wb.widget.utils.calendar.Constants;
import wb.widget.utils.calendar.Constants.Constellation;
import wb.widget.utils.calendar.Constants.WeekDay;
import wb.widget.utils.calendar.NaYinUtils;

public class SettingsCalendarActivity extends Activity {
    // For MonthCalendar.
    // Preferences for special days.
    private static final String PREF_KEY_FIRST_DAY_OF_WEEK = "setting_first_day_of_week";
    private static final String PREF_KEY_PUBLIC_HOLIDAYS = "settings_public_holidays";
    private static final String PREF_KEY_WORK_DAYS = "settings_work_days";
    private static final String PREF_KEY_COMPANY_HOLIDAYS = "settings_company_holidays";
    private static final String PREF_KEY_BIRTHDAYS = "settings_birthdays";
    private static final String PREF_KEY_MEMORIALS = "settings_memorials";
    private static final String PREF_KEY_OTHERS = "settings_others";
    // Color preferences.
    private static final String PREF_KEY_COLOR_PUBLIC_HOLIDAY3 = "color_public_holiday3";
    private static final String PREF_KEY_COLOR_PUBLIC_HOLIDAY2 = "color_public_holiday2";
    private static final String PREF_KEY_COLOR_WORK_DAY = "color_work_day";
    private static final String PREF_KEY_COLOR_COMPANY_HOLIDAY = "color_company_holiday";
    private static final String PREF_KEY_COLOR_BIRTHDAY = "color_birthday";
    private static final String PREF_KEY_COLOR_MEMORIAL = "color_memorial";
    private static final String PREF_KEY_COLOR_OTHER = "color_other";
    // Other preferences.
    private static final String PREF_KEY_SHOW_ALMANAC = "show_almanac";
    private static final String PREF_KEY_SHOW_ONLY_CUR_MONTH_DAYS = "show_only_cur_month_days";

    private static final String SEP_HOLIDAY = ";";
    private static final String SEP_HOLIDYA_TYPE = ",";

    private static int sFirstDayOfWeek = -1;

    private static boolean sShowAlmanac;
    private static boolean sShowOnlyCurMonthDays;

    private static final int[][] sFirstDayMapping = {
            {Calendar.SUNDAY,   R.id.radio_sunday},
            {Calendar.MONDAY,   R.id.radio_monday},
            {Calendar.SATURDAY, R.id.radio_saturday},
    };

    private static final int COLOR_PUBLIC_HOLIDAY3_PAST = Color.rgb(240, 128, 128);
    private static final int COLOR_PUBLIC_HOLIDAY3_COMING = Color.RED;
    private static final int COLOR_PUBLIC_HOLIDAY2_PAST = Color.rgb(230, 120, 120);
    private static final int COLOR_PUBLIC_HOLIDAY2_COMING = Color.rgb(0x8B, 0, 0);
    private static final int COLOR_WORKING_DAY = Color.rgb(0xAD, 0xD8, 0xE6);/* light blue */
    private static final int COLOR_COMPANY_HOLIDAY = Color.YELLOW;
    private static final int COLOR_MEMORIAL_PAST = Color.rgb(0xFF, 0xA5, 0);
    private static final int COLOR_MEMORIAL_COMING = Color.rgb(0x5A, 0x4C, 0);
    private static final int COLOR_BIRTHDAY_PAST = Color.rgb(0xD8, 0x8B, 0xAD);
    private static final int COLOR_BIRTHDAY_COMING = Color.rgb(0x8B, 0xAD, 0xD8);
    private static final int COLOR_OTHER_PAST = Color.rgb(0xA5, 0x4C, 0xFF);
    private static final int COLOR_OTHER_COMING = Color.rgb(0x4C, 0xFF, 0xA5);

    public static enum DayType {
        PublicHoliday3(R.string.type_public_holiday, R.id.radio_public_holiday_3,
                PREF_KEY_PUBLIC_HOLIDAYS,
                Color.WHITE, COLOR_PUBLIC_HOLIDAY3_PAST, COLOR_PUBLIC_HOLIDAY3_COMING),
        PublicHoliday2(R.string.type_public_holiday, R.id.radio_public_holiday_2,
                PREF_KEY_PUBLIC_HOLIDAYS,
                Color.WHITE, COLOR_PUBLIC_HOLIDAY2_PAST, COLOR_PUBLIC_HOLIDAY2_COMING),
        Work(R.string.type_work_day, R.id.radio_work_day,
                PREF_KEY_WORK_DAYS, Color.WHITE, COLOR_WORKING_DAY),
        CompanyHoliday(R.string.type_company_holiday, R.id.radio_company_holiday,
                PREF_KEY_COMPANY_HOLIDAYS, Color.GREEN, COLOR_COMPANY_HOLIDAY),
        Birthday(R.string.type_birthday, R.id.radio_birthday, PREF_KEY_BIRTHDAYS,
                Color.BLUE, COLOR_BIRTHDAY_PAST, COLOR_BIRTHDAY_COMING),
        Memorial(R.string.type_memorial, R.id.radio_memorial, PREF_KEY_MEMORIALS,
                Color.BLACK, COLOR_MEMORIAL_PAST, COLOR_MEMORIAL_COMING),
        Other(R.string.type_other, R.id.radio_other_day, PREF_KEY_OTHERS,
                Color.GRAY, COLOR_OTHER_PAST, COLOR_OTHER_COMING),
        Normal(R.string.type_normal, -1, null);

        public final int labelResId;
        public final int radio_id;
        public final String prefLabel;
        public final int textColor;
        public final int backgroundColorPast;
        public final int backgroundColorComing;

        private DayType(final int labelResId, final int radio_id, final String prefLabel,
                final int textColor, final int backgroundColorPast,
                final int backgroundColorComing) {
            this.labelResId = labelResId;
            this.radio_id = radio_id;
            this.prefLabel = prefLabel;
            this.textColor = textColor;
            this.backgroundColorPast = backgroundColorPast;
            this.backgroundColorComing = backgroundColorComing;
        }

        private DayType(final int labelResId, final int radio_id, final String prefLabel,
                final int textColor, final int backgroundColor) {
            this.labelResId = labelResId;
            this.radio_id = radio_id;
            this.prefLabel = prefLabel;
            this.textColor = textColor;
            this.backgroundColorPast = backgroundColor;
            this.backgroundColorComing = backgroundColor;
        }

        private DayType(final int labelResId, final int radio_id, final String prefLabel) {
            this(labelResId, radio_id, prefLabel, 0, 0, 0);
        }

        public static DayType getTypeFromOrdinal(final int ordinal) {
            for (DayType type : values()) {
                if (type.ordinal() == ordinal) return type;
            }
            return Normal;
        }

        public static DayType getTypeFromPrefKey(final String prefKey) {
            for (DayType type : values()) {
                if (TextUtils.equals(type.prefLabel, prefKey)) return type;
            }
            return Normal;
        }

        public static DayType getTypeFromRadio(final int radio_id) {
            for (DayType type : values()) {
                if (type.radio_id == radio_id) return type;
            }
            return Normal;
        }

        public static DayType[] getTypesWithColors() {
            DayType[] all = values();
            ArrayList<DayType> typeList = new ArrayList<DayType>(all.length);
            for (DayType type : all) {
                if (type.textColor != 0 || type.backgroundColorComing != 0
                        || type.backgroundColorPast != 0) {
                    typeList.add(type);
                }
            }
            return typeList.toArray(new DayType[typeList.size()]);
        }
    }

    private static class DayTypeColorInfo {
        public final DayType type;
        public final String prefKey;

        public int textColor;
        public int backgroundColorPast;
        public int backgroundColorComing;

        public DayTypeColorInfo(DayType type, String prefKey) {
            this.type = type;
            this.prefKey = prefKey;
            textColor = type.textColor;
            backgroundColorPast = type.backgroundColorPast;
            backgroundColorComing = type.backgroundColorComing;
        }

        // Format: typeOrdinal, textColor, backgroundColorPast, backgroundColorComing
        private static final String FORMAT_COLOR_STRING = "%d,%d,%d,%d";

        public String colorString() {
            return String.format(FORMAT_COLOR_STRING, type.ordinal(), textColor,
                    backgroundColorPast, backgroundColorComing);
        }

        public int getBackgroundColor(final DayTime dayTime) {
            switch (dayTime) {
                case Past:
                    if (backgroundColorPast != 0) {
                        return backgroundColorPast;
                    }
                    if (type.backgroundColorPast != 0) {
                        return type.backgroundColorPast;
                    }
                    break;
                case Today:
                case Coming:
                    if (backgroundColorComing != 0) {
                        return backgroundColorComing;
                    }
                    if (type.backgroundColorComing != 0) {
                        return type.backgroundColorComing;
                    }
                    break;
                default:
                    break;
            }
            return 0;
        }

        private static void setColors(DayType type, int textColor, int backgroundColorPast,
                int backgroundColorComing) {
            for (DayTypeColorInfo info : sDayTypeColorInfos) {
                if (info.type == type) {
                    info.textColor = textColor;
                    info.backgroundColorComing = backgroundColorComing;
                    info.backgroundColorPast = backgroundColorPast;
                }
            }
        }

        public static void parse(final String input) {
            String[] elements = input.split(",");
            DayType type = DayType.getTypeFromOrdinal(Integer.parseInt(elements[0]));
            int textColor = Integer.parseInt(elements[1]);
            int backgroundColorPast = Integer.parseInt(elements[2]);
            int backgroundColorComing = Integer.parseInt(elements[3]);
            setColors(type, textColor, backgroundColorPast, backgroundColorComing);
        }

        public static void init() {
            for (DayTypeColorInfo info : sDayTypeColorInfos) {
                info.textColor = info.type.textColor;
                info.backgroundColorComing = info.type.backgroundColorComing;
                info.backgroundColorPast = info.type.backgroundColorPast;
            }
        }

        public static DayTypeColorInfo getTypeColorInfo(final DayType dayType) {
            for (DayTypeColorInfo info : sDayTypeColorInfos) {
                if (info.type == dayType) return info;
            }
            return null;
        }
    }

    private static DayTypeColorInfo[] sDayTypeColorInfos = {
            new DayTypeColorInfo(DayType.PublicHoliday3, PREF_KEY_COLOR_PUBLIC_HOLIDAY3),
            new DayTypeColorInfo(DayType.PublicHoliday2, PREF_KEY_COLOR_PUBLIC_HOLIDAY2),
            new DayTypeColorInfo(DayType.Work, PREF_KEY_COLOR_WORK_DAY),
            new DayTypeColorInfo(DayType.CompanyHoliday, PREF_KEY_COLOR_COMPANY_HOLIDAY),
            new DayTypeColorInfo(DayType.Birthday, PREF_KEY_COLOR_BIRTHDAY),
            new DayTypeColorInfo(DayType.Memorial, PREF_KEY_COLOR_MEMORIAL),
            new DayTypeColorInfo(DayType.Other, PREF_KEY_COLOR_OTHER),
    };

    private static class SpecialDayInfo {
        private static final String FORMAT_TO_STRING = "%s, count:%d";
        public final String prefKey;
        public final ArrayList<DayInfo> list;

        public SpecialDayInfo(final String prefKey, ArrayList<DayInfo> list) {
            this.prefKey = prefKey;
            this.list = list;
        }

        @Override
        public String toString() {
            return String.format(FORMAT_TO_STRING, prefKey, list.size());
        }
    }

    public static enum DayTime {
        Past,
        Today,
        Coming;
    }

    public static class DayInfo {
        protected static final String FORMAT_DAY_INFO = "%s %s %s";
        protected static final String FORMAT_LABEL_OTHER = "%s %s";
        protected static final String SEP_DAY_INFO = ",";
        protected static final String FORMAT_PREFERENCE = "%s" + SEP_DAY_INFO + "%4d%02d%02d"
                + SEP_DAY_INFO + "%d";

        public final DayType type;
        public final Day day;
        public final String label;

        private DayTime mDayTime;

        public DayInfo(DayType type, Day day, String label) {
            this.type = type;
            this.day = day;
            this.label = label;

            Utility.obtainWeekDay(day);
        }

        public void checkDayTime(final Day today) {
            if (day.earlierThan(today)) {
                mDayTime = DayTime.Past;
            } else if (day.isSameDay(today)) {
                mDayTime = DayTime.Today;
            } else {
                mDayTime = DayTime.Coming;
            }
        }

        public boolean isToday() {
            return mDayTime == DayTime.Today;
        }

        public String getOtherInfo() {
            return null;
        }

        private String label_other_info() {
            String otherInfo = getOtherInfo();
            if (TextUtils.isEmpty(otherInfo)) return label;
            return String.format(FORMAT_LABEL_OTHER, label, otherInfo);
        }

        public String formatPreference() {
            return String.format(FORMAT_PREFERENCE, label, day.year, day.month + 1, day.day,
                    type.ordinal());
        }

        @Override
        public String toString() {
            return String.format(FORMAT_DAY_INFO, label_other_info(), day, type.toString());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof DayInfo)) return false;
            DayInfo input = (DayInfo) obj;
            if (!input.day.equals(day)) return false;
            if (!TextUtils.equals(input.label, label)) return false;
            if (input.type != type) return false;
            return true;
        }

        public String info(final Context context) {
            String typeLabel = context.getString(type.labelResId);
            return String.format(FORMAT_DAY_INFO, day.ymdw_chinese(context), label_other_info(),
                    typeLabel);
        }

        public int getBackgroundColor() {
            switch (mDayTime) {
                case Past:
                    if (type.backgroundColorPast != 0) {
                        return type.backgroundColorPast;
                    }
                    break;
                case Today:
                case Coming:
                    if (type.backgroundColorComing != 0) {
                        return type.backgroundColorComing;
                    }
                    break;
                default:
                    break;
            }
            return 0;
        }

        protected static DayInfo parseFromPreference(String input) {
            String[] elements = input.split(SEP_DAY_INFO);
            String label = elements[0];
            int year = Integer.parseInt(elements[1].substring(0, 4));
            int month = Integer.parseInt(elements[1].substring(4, 6)) - 1;
            int day = Integer.parseInt(elements[1].substring(6));
            int typeOrdinal = Integer.parseInt(elements[2]);
            DayType type = DayType.getTypeFromOrdinal(typeOrdinal);
            return new DayInfo(type, new Day(year, month, day), label);
        }
    }

    public static class CompanyHolidayInfo extends DayInfo {
        protected static final String FORMAT_DAY_INFO = "%s;%s;%s;%s";
        protected static final String FORMAT_PREFERENCE = "%s" + SEP_DAY_INFO + "%s" + SEP_DAY_INFO
                + "%4d%02d%02d" + SEP_DAY_INFO + "%s";

        public final String company;

        public CompanyHolidayInfo(final DayType type, Day day, String label, String company) {
            super(type, day, label);
            this.company = company;
        }

        @Override
        public String getOtherInfo() {
            return company;
        }

        @Override
        public String formatPreference() {
            return String.format(FORMAT_PREFERENCE, label, company, day.year, day.month + 1,
                    day.day, type.ordinal());
        }

        @Override
        public String toString() {
            return String.format(FORMAT_DAY_INFO, label, company, day, type.toString());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CompanyHolidayInfo)) return false;
            CompanyHolidayInfo input = (CompanyHolidayInfo) obj;
            if (!TextUtils.equals(company, input.company)) return false;
            return super.equals(obj);
        }

        protected static CompanyHolidayInfo parseFromPreference(String input) {
            String[] elements = input.split(SEP_DAY_INFO);
            String label = elements[0];
            String company = elements[1];
            int year = Integer.parseInt(elements[2].substring(0, 4));
            int month = Integer.parseInt(elements[2].substring(4, 6)) - 1;
            int day = Integer.parseInt(elements[2].substring(6));
            int typeOrdinal = Integer.parseInt(elements[3]);
            DayType type = DayType.getTypeFromOrdinal(typeOrdinal);
            return new CompanyHolidayInfo(type, new Day(year, month, day), label, company);
        }
    }

    public static class OtherDayInfo extends DayInfo {
        protected static final String FORMAT_DAY_INFO = "%s;%s;%s;%s";
        protected static final String FORMAT_PREFERENCE = "%s" + SEP_DAY_INFO + "%s" + SEP_DAY_INFO
                + "%4d%02d%02d" + SEP_DAY_INFO + "%s";

        public final String otherInfo;

        public OtherDayInfo(final DayType type, Day day, String label, String otherInfo) {
            super(type, day, label);
            this.otherInfo = otherInfo;
        }

        @Override
        public String getOtherInfo() {
            return otherInfo;
        }

        @Override
        public String formatPreference() {
            return String.format(FORMAT_PREFERENCE, label, otherInfo, day.year, day.month + 1,
                    day.day, type.ordinal());
        }

        @Override
        public String toString() {
            return String.format(FORMAT_DAY_INFO, label, otherInfo, day, type.toString());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof OtherDayInfo)) return false;
            OtherDayInfo input = (OtherDayInfo) obj;
            if (!TextUtils.equals(otherInfo, input.otherInfo)) return false;
            return super.equals(obj);
        }

        protected static OtherDayInfo parseFromPreference(String input) {
            String[] elements = input.split(SEP_DAY_INFO);
            String label = elements[0];
            String otherInfo = elements[1];
            int year = Integer.parseInt(elements[2].substring(0, 4));
            int month = Integer.parseInt(elements[2].substring(4, 6)) - 1;
            int day = Integer.parseInt(elements[2].substring(6));
            int typeOrdinal = Integer.parseInt(elements[3]);
            DayType type = DayType.getTypeFromOrdinal(typeOrdinal);
            return new OtherDayInfo(type, new Day(year, month, day), label, otherInfo);
        }
    }

    private static boolean sSettingsLoaded = false;

    private static final ArrayList<DayInfo> sPublicHolidays = new ArrayList<DayInfo>();

    private static final ArrayList<DayInfo> sWorkDays = new ArrayList<DayInfo>();

    private static final ArrayList<DayInfo> sCompanyHolidays = new ArrayList<DayInfo>();

    private static final ArrayList<DayInfo> sBirthDates = new ArrayList<DayInfo>();

    private static final ArrayList<DayInfo> sMemorials = new ArrayList<DayInfo>();

    private static final ArrayList<DayInfo> sOtherSpecialDays = new ArrayList<DayInfo>();

    private static final SpecialDayInfo[] sSpecialDayInfos = {
            new SpecialDayInfo(PREF_KEY_PUBLIC_HOLIDAYS, sPublicHolidays),
            new SpecialDayInfo(PREF_KEY_WORK_DAYS, sWorkDays),
            new SpecialDayInfo(PREF_KEY_COMPANY_HOLIDAYS, sCompanyHolidays),
            new SpecialDayInfo(PREF_KEY_BIRTHDAYS, sBirthDates),
            new SpecialDayInfo(PREF_KEY_MEMORIALS, sMemorials),
            new SpecialDayInfo(PREF_KEY_OTHERS, sOtherSpecialDays),
    };

    private ListView mListViewSpecialDays;

    private DayInfoListAdapter mSpecialDaysAdapter;

    private boolean mSpecialDaysVisible = true;

    private static void checkDayTime(final Day today) {
        for (SpecialDayInfo specialDayInfo : sSpecialDayInfos) {
            for (DayInfo dayInfo : specialDayInfo.list) {
                dayInfo.checkDayTime(today);
            }
        }
    }

    private static boolean deleteSpecialDay(final Context context, final DayInfo dayInfo) {
        for (SpecialDayInfo specialDayInfo : sSpecialDayInfos) {
            if (specialDayInfo.list.contains(dayInfo)) {
                specialDayInfo.list.remove(dayInfo);
                DayType dayType = DayType.getTypeFromPrefKey(specialDayInfo.prefKey);
                saveSpecialDays(context, dayType, specialDayInfo.list);
                return true;
            }
        }
        return false;
    }

    private static boolean addSpecialDay(final Context context, final DayInfo dayInfo) {
        SpecialDayInfo foundSpecialDayInfo = null;
        for (SpecialDayInfo specialDayInfo : sSpecialDayInfos) {
            if (TextUtils.equals(specialDayInfo.prefKey, dayInfo.type.prefLabel)) {
                foundSpecialDayInfo = specialDayInfo;
                break;
            }
        }
        if (foundSpecialDayInfo == null) return false;
        foundSpecialDayInfo.list.add(dayInfo);
        saveSpecialDays(context, dayInfo.type, foundSpecialDayInfo.list);
        return true;
    }

    private Day mToday;

    private final SpecialDayChangeListener mSpecialDayChangeListener = new SpecialDayChangeListener() {
        @Override
        public void whenSpecialDayChanged(final Context context) {
            mSpecialDaysAdapter.collectSpecialDays();
            Constants.refreshWidget(context);
        }
    };

    private final ColorChangedListener mColorChangedListener = new ColorChangedListener() {
        @Override
        public void colorChanged() {
            mSpecialDaysAdapter.notifyDataSetInvalidated();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.settings_calendar);

        mToday = Utility.getToday(Calendar.getInstance().getFirstDayOfWeek());
        loadSettings(this, mToday);

        initViews();

        super.onCreate(savedInstanceState);
    }

    private void initViews() {
        SettingsUtils.initGeneric(this);

        final View viewAlmanacWords = findViewById(R.id.view_almanac_words);
        viewAlmanacWords.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlmanacWords();
            }
        });

        initShowOnlyCurMonthDays();
        initShowAlmanac();
        initFirstDayOfWeek();

        initCalendarSearch();
        initLunarAssistance();

        mListViewSpecialDays = (ListView) findViewById(R.id.list_special_days);
        if (mSpecialDaysAdapter == null) {
            mSpecialDaysAdapter = new DayInfoListAdapter(this, mToday, mSpecialDayChangeListener,
                    mColorChangedListener);
        }
        mSpecialDaysAdapter.collectSpecialDays();
        mListViewSpecialDays.setAdapter(mSpecialDaysAdapter);
        updateSpecialDayViews();

        initButtons();
    }

    private void initCalendarSearch() {
        final View viewCalendarSearch = findViewById(R.id.view_calendar_search);
        final RadioGroup radioGroupSearch = findViewById(R.id.radiogroup_calendar_search);
        final CheckBox checkBoxWithTime = findViewById(R.id.checkbox_with_time);
        radioGroupSearch.check(R.id.radio_sunar_lunar);
        viewCalendarSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final int checked = radioGroupSearch.getCheckedRadioButtonId();
                boolean isSunar2Lunar = checked == R.id.radio_sunar_lunar;
                boolean withTime = checkBoxWithTime.isChecked();
                showCalendarSearchDialog(isSunar2Lunar, withTime);
            }
        });
    }

    private void initLunarAssistance() {
        final TextView viewCount9 = findViewById(R.id.view_count_9);
        final int year_count9;
        if (mToday.month < 3) {
            year_count9 = mToday.year - 1;
        } else {
            year_count9 = mToday.year;
        }
        if (mToday.month < 10 && mToday.month > 2) {
            viewCount9.setVisibility(View.GONE);
        } else {
            viewCount9.setText(getString(R.string.format_count_9, year_count9));
        }
        viewCount9.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                show9Info(year_count9);
            }
        });
        final TextView viewCountFu = findViewById(R.id.view_count_fu);
        final int year_count_fu = mToday.year;
        if (mToday.month < 10) {
            viewCountFu.setText(getString(R.string.format_count_fu, year_count_fu));
        } else {
            viewCountFu.setVisibility(View.GONE);
        }
        viewCountFu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFuInfo(year_count_fu);
            }
        });
    }

    private void updateSpecialDayViews() {
        View viewPrompt = findViewById(R.id.view_no_special_days);
        View viewHeader = findViewById(R.id.header_special_days);
        View viewSpecialDaysFold = findViewById(R.id.view_fold);
        View viewSpecialDaysUnfold = findViewById(R.id.view_unfold);
        Button buttonClear = (Button) findViewById(R.id.button_clear_special_days);
        if (mSpecialDaysVisible) {
            mListViewSpecialDays.setVisibility(View.VISIBLE);
        } else {
            mListViewSpecialDays.setVisibility(View.GONE);
        }
        if (mSpecialDaysAdapter.getCount() > 0) {
            viewHeader.setVisibility(View.VISIBLE);
            viewPrompt.setVisibility(View.GONE);

            buttonClear.setVisibility(View.VISIBLE);

            if (mSpecialDaysVisible) {
                viewSpecialDaysFold.setVisibility(View.VISIBLE);
                viewSpecialDaysUnfold.setVisibility(View.GONE);
            } else {
                viewSpecialDaysFold.setVisibility(View.GONE);
                viewSpecialDaysUnfold.setVisibility(View.VISIBLE);
            }
        } else {
            viewHeader.setVisibility(View.GONE);
            viewPrompt.setVisibility(View.VISIBLE);

            buttonClear.setVisibility(View.INVISIBLE);
        }
    }

    private void initButtons() {
        final Context context = this;

        final View viewSpecialDaysFold = findViewById(R.id.view_fold);
        viewSpecialDaysFold.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpecialDaysVisible = false;
                updateSpecialDayViews();
            }
        });
        final View viewSpecialDaysUnfold = findViewById(R.id.view_unfold);
        viewSpecialDaysUnfold.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpecialDaysVisible = true;
                updateSpecialDayViews();
            }
        });

        Button buttonAdd = (Button) findViewById(R.id.button_add_special_day);
        buttonAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditSpecialDay(context, mToday, null, mSpecialDayChangeListener);
            }
        });

        final Button buttonClear = (Button) findViewById(R.id.button_clear_special_days);
        buttonClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = context.getString(R.string.title_clear_special_days);
                String promptConfirm = context.getString(R.string.prompt_clear_special_days);
                DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearSpecialDays(SettingsUtils.getPrefsHelper(context));
                        Constants.refreshWidget(context);
                    }
                };
                Utility.showConfirmDialog(context, title, promptConfirm, positiveButtonListener);
            }
        });

        final Button buttonImport = (Button) findViewById(R.id.button_import_special_days);
        buttonImport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.showInfoDialog(context, buttonImport.getText().toString(),
                        "Hey! Not implemented!!");
            }
        });

        final Button buttonExport = (Button) findViewById(R.id.button_export_special_days);
        buttonExport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.showInfoDialog(context, buttonExport.getText().toString(),
                        "Hey! Not implemented!!");
            }
        });

        Button buttonRestoreDefault = (Button) findViewById(
                R.id.button_restore_default_special_days);
        buttonRestoreDefault.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showRestoreDialog(context);
            }
        });
    }

    private interface SpecialDayChangeListener {
        public void whenSpecialDayChanged(final Context context);
    }

    private static void showAddSpecialDay(final Context context, final Day today,
            final Day day, final SpecialDayChangeListener specialDayChangeListener) {
        final int viewLayoutId = R.layout.edit_special_day;
        final String title = context.getString(R.string.title_add_special_day);
        final ViewInitWithPositiveNegative viewInit = new ViewInitWithPositiveNegative() {
            private TextView textViewDate;
            private EditText editTextLabel;
            private RadioGroup radioGroupType;
            private RadioGroup radioGroupPublicHolidayType;
            private EditText editCompany;
            private EditText editOtherInfo;

            private int year, month, dayInMonth;

            @Override
            public void initViews(View rootView) {
                textViewDate = rootView.findViewById(R.id.text_date_cn);
                year = day.year;
                month = day.month;
                dayInMonth = day.day;
                setDateText();
                textViewDate.setEnabled(false);

                editTextLabel = rootView.findViewById(R.id.edit_special_day_label);
                radioGroupType = rootView.findViewById(R.id.radiogroup_special_day_type);
                radioGroupPublicHolidayType = rootView
                        .findViewById(R.id.radiogroup_public_holiday_type);
                final View viewCompany = rootView.findViewById(R.id.view_company);
                editCompany = rootView.findViewById(R.id.edit_company);
                final View viewOtherInfo = rootView.findViewById(R.id.view_other_info);
                editOtherInfo = rootView.findViewById(R.id.edit_other_info);

                initViews(viewCompany, viewOtherInfo);

                checkViewsVisibility(radioGroupType.getCheckedRadioButtonId(), viewCompany,
                        viewOtherInfo);
                radioGroupType.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        checkViewsVisibility(checkedId, viewCompany, viewOtherInfo);
                    }
                });
            }

            private void initViews(final View viewCompany, final View viewOtherInfo) {
                radioGroupType.check(R.id.radio_other_day);
                radioGroupPublicHolidayType.check(R.id.radio_public_holiday_3);
            }

            private void checkViewsVisibility(final int checkedRadioButtonId,
                    final View viewCompany, final View viewOtherInfo) {
                viewCompany.setVisibility(View.INVISIBLE);
                viewOtherInfo.setVisibility(View.INVISIBLE);
                radioGroupPublicHolidayType.setVisibility(View.INVISIBLE);
                if (checkedRadioButtonId == R.id.radio_public_holiday) {
                    radioGroupPublicHolidayType.setVisibility(View.VISIBLE);
                } else if (checkedRadioButtonId == R.id.radio_company_holiday) {
                    viewCompany.setVisibility(View.VISIBLE);
                } else if (checkedRadioButtonId == R.id.radio_other_day) {
                    viewOtherInfo.setVisibility(View.VISIBLE);
                }
            }

            private static final String FORMAT_YMD_CN = "%4d年%02d月%02d日";

            private void setDateText() {
                String dateText = String.format(FORMAT_YMD_CN, year, month + 1, dayInMonth);
                textViewDate.setText(dateText);
            }

            private String getInputLabel() {
                Editable editable = editTextLabel.getEditableText();
                if (editable == null) return null;
                return editable.toString();
            }

            private String getInputCompany() {
                Editable editable = editCompany.getEditableText();
                if (editable == null) return null;
                return editable.toString();
            }

            private String getInputOtherInfo() {
                Editable editable = editOtherInfo.getEditableText();
                if (editable == null) return null;
                return editable.toString();
            }

            private DayInfo getNewDayInfo(DayType type, final Day newDay, final String label) {
                if (type == DayType.CompanyHoliday) {
                    String company = getInputCompany();
                    return new CompanyHolidayInfo(type, newDay, label, company);
                }
                if (type == DayType.Other) {
                    String otherInfo = getInputOtherInfo();
                    return new OtherDayInfo(type, newDay, label, otherInfo);
                }
                return new DayInfo(type, newDay, label);
            }

            private int getCheckedRadioId() {
                int checkedId = radioGroupType.getCheckedRadioButtonId();
                if (checkedId == R.id.radio_public_holiday) {
                    checkedId = radioGroupPublicHolidayType.getCheckedRadioButtonId();
                }
                return checkedId;
            }

            @Override
            public void onPositiveClick(View rootView) {
                if (year == 0 && month == 0 && dayInMonth == 0) {
                    Utility.showInfoDialog(context, R.string.prompt_empty_date);
                    return;
                }
                final String label = getInputLabel();
                if (TextUtils.isEmpty(label)) {
                    Utility.showInfoDialog(context, R.string.prompt_empty_special_day_label);
                    return;
                }
                int checkedRadioId = getCheckedRadioId();
                DayType type = DayType.getTypeFromRadio(checkedRadioId);
                Day newDay = new Day(year, month, dayInMonth);
                DayInfo newDayInfo = getNewDayInfo(type, newDay, label);
                newDayInfo.checkDayTime(today);
                if (addSpecialDay(context, newDayInfo)) {
                    if (specialDayChangeListener != null) {
                        specialDayChangeListener.whenSpecialDayChanged(context);
                    }
                }
            }

            @Override
            public void onNegativeClick(View rootView) {
                // ...
            }
        };
        final String positiveLabel = context.getString(android.R.string.ok);
        final String negativeLabel = context.getString(android.R.string.cancel);
        Utility.showViewDialog(context, viewLayoutId, title, viewInit, positiveLabel,
                negativeLabel);
    }

    private static void showEditSpecialDay(final Context context, final Day today,
            final DayInfo dayInfo, final SpecialDayChangeListener specialDayChangeListener) {
        final int viewLayoutId = R.layout.edit_special_day;
        final String title;
        if (dayInfo == null) {
            title = context.getString(R.string.title_add_special_day);
        } else {
            title = context.getString(R.string.title_edit_special_day);
        }
        final ViewInitWithPositiveNegative viewInit = new ViewInitWithPositiveNegative() {
            private TextView textViewDate;
            private EditText editTextLabel;
            private RadioGroup radioGroupType;
            private RadioGroup radioGroupPublicHolidayType;
            private EditText editCompany;
            private EditText editOtherInfo;

            private int year, month, dayInMonth;

            @Override
            public void initViews(View rootView) {
                textViewDate = rootView.findViewById(R.id.text_date_cn);
                if (dayInfo != null) {
                    year = dayInfo.day.year;
                    month = dayInfo.day.month;
                    dayInMonth = dayInfo.day.day;

                    setDateText();
                }
                textViewDate.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Day day;
                        String title;
                        if (dayInfo == null) {
                            day = today;
                            title = context.getString(R.string.title_set_special_day_date);
                        } else {
                            day = dayInfo.day;
                            title = context.getString(R.string.title_edit_special_day_date);
                        }
                        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int n_year,
                                    int n_monthOfYear, int n_dayOfMonth) {
                                if (year != n_year || month != n_monthOfYear
                                        || dayInMonth != n_dayOfMonth) {
                                    year = n_year;
                                    month = n_monthOfYear;
                                    dayInMonth = n_dayOfMonth;
                                    setDateText();
                                }
                            }
                        };
                        Utility.showDatePickerDialog(context, day.year, day.month, day.day, title,
                                dateSetListener);
                    }
                });

                editTextLabel = rootView.findViewById(R.id.edit_special_day_label);
                radioGroupType = rootView.findViewById(R.id.radiogroup_special_day_type);
                radioGroupPublicHolidayType = rootView.findViewById(R.id.radiogroup_public_holiday_type);
                final View viewCompany = rootView.findViewById(R.id.view_company);
                editCompany = rootView.findViewById(R.id.edit_company);
                final View viewOtherInfo = rootView.findViewById(R.id.view_other_info);
                editOtherInfo = rootView.findViewById(R.id.edit_other_info);

                initViews(viewCompany, viewOtherInfo);

                checkViewsVisibility(radioGroupType.getCheckedRadioButtonId(), viewCompany,
                        viewOtherInfo);
                radioGroupType.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        checkViewsVisibility(checkedId, viewCompany, viewOtherInfo);
                    }
                });
            }

            private void initViews(final View viewCompany, final View viewOtherInfo) {
                if (dayInfo == null) {
                    radioGroupType.check(R.id.radio_other_day);
                    radioGroupPublicHolidayType.check(R.id.radio_public_holiday_3);
                } else {
                    editTextLabel.setText(dayInfo.label);
                    int radio_id = dayInfo.type.radio_id;
                    if (isPublicHoliday(radio_id)) {
                        radioGroupType.check(R.id.radio_public_holiday);
                        radioGroupPublicHolidayType.setVisibility(View.VISIBLE);
                        radioGroupPublicHolidayType.check(radio_id);
                    } else {
                        radioGroupType.check(radio_id);
                        if (dayInfo.type == DayType.CompanyHoliday) {
                            viewCompany.setVisibility(View.VISIBLE);
                            editCompany.setText(dayInfo.getOtherInfo());
                        } else if (dayInfo.type == DayType.Other) {
                            viewOtherInfo.setVisibility(View.VISIBLE);
                            editOtherInfo.setText(dayInfo.getOtherInfo());
                        }
                    }
                }
            }

            private void checkViewsVisibility(final int checkedRadioButtonId,
                    final View viewCompany, final View viewOtherInfo) {
                viewCompany.setVisibility(View.INVISIBLE);
                viewOtherInfo.setVisibility(View.INVISIBLE);
                radioGroupPublicHolidayType.setVisibility(View.INVISIBLE);
                if (checkedRadioButtonId == R.id.radio_public_holiday) {
                    radioGroupPublicHolidayType.setVisibility(View.VISIBLE);
                } else if (checkedRadioButtonId == R.id.radio_company_holiday) {
                    viewCompany.setVisibility(View.VISIBLE);
                } else if (checkedRadioButtonId == R.id.radio_other_day) {
                    viewOtherInfo.setVisibility(View.VISIBLE);
                }
            }

            private boolean isPublicHoliday(final int radio_id) {
                if (radio_id == R.id.radio_public_holiday_2) return true;
                if (radio_id == R.id.radio_public_holiday_3) return true;
                return false;
            }

            private static final String FORMAT_YMD_CN = "%4d年%02d月%02d日";

            private void setDateText() {
                String dateText = String.format(FORMAT_YMD_CN, year, month + 1, dayInMonth);
                textViewDate.setText(dateText);
            }

            private String getInputLabel() {
                Editable editable = editTextLabel.getEditableText();
                if (editable == null) return null;
                return editable.toString();
            }

            private String getInputCompany() {
                Editable editable = editCompany.getEditableText();
                if (editable == null) return null;
                return editable.toString();
            }

            private String getInputOtherInfo() {
                Editable editable = editOtherInfo.getEditableText();
                if (editable == null) return null;
                return editable.toString();
            }

            private DayInfo getNewDayInfo(DayType type, final Day newDay, final String label) {
                if (type == DayType.CompanyHoliday) {
                    String company = getInputCompany();
                    return new CompanyHolidayInfo(type, newDay, label, company);
                }
                if (type == DayType.Other) {
                    String otherInfo = getInputOtherInfo();
                    return new OtherDayInfo(type, newDay, label, otherInfo);
                }
                return new DayInfo(type, newDay, label);
            }

            private int getCheckedRadioId() {
                int checkedId = radioGroupType.getCheckedRadioButtonId();
                if (checkedId == R.id.radio_public_holiday) {
                    checkedId = radioGroupPublicHolidayType.getCheckedRadioButtonId();
                }
                return checkedId;
            }

            @Override
            public void onPositiveClick(View rootView) {
                if (year == 0 && month == 0 && dayInMonth == 0) {
                    Utility.showInfoDialog(context, R.string.prompt_empty_date);
                    return;
                }
                final String label = getInputLabel();
                if (TextUtils.isEmpty(label)) {
                    Utility.showInfoDialog(context, R.string.prompt_empty_special_day_label);
                    return;
                }
                int checkedRadioId = getCheckedRadioId();
                DayType type = DayType.getTypeFromRadio(checkedRadioId);
                Day newDay = new Day(year, month, dayInMonth);
                DayInfo newDayInfo = getNewDayInfo(type, newDay, label);
                newDayInfo.checkDayTime(today);
                if (addSpecialDay(context, newDayInfo)) {
                    if (dayInfo != null) {
                        deleteSpecialDay(context, dayInfo);
                    }
                    if (specialDayChangeListener != null) {
                        specialDayChangeListener.whenSpecialDayChanged(context);
                    }
                }
            }

            @Override
            public void onNegativeClick(View rootView) {
                // ...
            }
        };
        final String positiveLabel = context.getString(android.R.string.ok);
        final String negativeLabel = context.getString(android.R.string.cancel);
        Utility.showViewDialog(context, viewLayoutId, title, viewInit, positiveLabel,
                negativeLabel);
    }

    private static void showColorSetDialog(final Context context,
            ColorPickerDialog.OnColorSetListener colorSetListener,
            final int labelResId, final int textColor, final int backgroundColor) {
        final String text = context.getString(labelResId);
        ColorPickerDialog dialog = new ColorPickerDialog(context, colorSetListener, text,
                textColor, backgroundColor);
        dialog.setTitle(R.string.title_set_special_day_color);
        dialog.show();
    }

    private static String loadAssetFileToString(final Context context, final String assetFilename) {
        // Load file from assets.
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(assetFilename)));

            // Loop reading the asset file until end of file.
            String line;
            while ((line = reader.readLine()) != null) {
                if (sb.length() > 0) sb.append('\n');
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException ioe) {
            // log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    // log the exception
                }
            }
        }
        return null;
    }

    private interface ColorChangedListener {
        public void colorChanged();
    }

    private static void showEditSpecialDayColor(final Context context, final DayType dayType,
            final ColorChangedListener colorChangedListener) {
        final int viewLayoutId = R.layout.edit_special_day_colors;
        final String title = context.getString(R.string.title_set_special_day_color);
        final ViewInitWithPositiveNegative viewInit = new ViewInitWithPositiveNegative() {
            @Override
            public void initViews(View rootView) {
                ListView listView = rootView.findViewById(R.id.list_special_day_types);
                DayTypeColorListAdapter adapter = new DayTypeColorListAdapter(context);
                listView.setAdapter(adapter);
            }

            @Override
            public void onPositiveClick(View rootView) {
                saveSpecialDayColorsPreference(context);
                colorChangedListener.colorChanged();
            }

            @Override
            public void onNegativeClick(View rootView) {
                DayTypeColorInfo.init();
            }
        };
        final String positiveLabel = context.getString(android.R.string.ok);
        final String negativeLabel = context.getString(android.R.string.cancel);
        Utility.showViewDialog(context, viewLayoutId, title, viewInit, positiveLabel,
                negativeLabel);
    }

    private static int getCheckedId() {
        for (int[] mapping : sFirstDayMapping) {
            if (mapping[0] == sFirstDayOfWeek) return mapping[1];
        }
        return R.id.radio_sunday;
    }

    private static void setFirstDayOfWeek(final Context context, int checkedId) {
        boolean firstDayChanged = false;
        for (int[] mapping : sFirstDayMapping) {
            if (mapping[1] == checkedId) {
                sFirstDayOfWeek = mapping[0];
                firstDayChanged = true;
                break;
            }
        }
        if (!firstDayChanged) return;
        saveFirstDayOfWeekPreference(context);
        Constants.refreshWidget(context);
    }

    private void initFirstDayOfWeek() {
        final Context context = this;
        final RadioGroup radioGroupFirstDayOfWeek = (RadioGroup) findViewById(
                R.id.radiogroup_first_day_of_week);
        radioGroupFirstDayOfWeek.check(getCheckedId());
        radioGroupFirstDayOfWeek.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setFirstDayOfWeek(context, checkedId);
            }
        });
    }

    private void initShowAlmanac() {
        final Context context = this;
        final CheckBox checkBoxShowAlmanac = findViewById(R.id.checkbox_show_almanac);
        checkBoxShowAlmanac.setChecked(sShowAlmanac);

        final CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sShowAlmanac = isChecked;
                saveShowAlmanacPreference(context);
                Constants.refreshWidget(context);
            }
        };
        checkBoxShowAlmanac.setOnCheckedChangeListener(listener);
    }

    private void initShowOnlyCurMonthDays() {
        final Context context = this;
        final CheckBox checkBoxShowOnlyCurMonthDays = findViewById(
                R.id.checkbox_show_only_cur_month);
        checkBoxShowOnlyCurMonthDays.setChecked(sShowOnlyCurMonthDays);

        final CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sShowOnlyCurMonthDays = isChecked;
                saveShowOnlyCurMonthDaysPreference(context);
                Constants.refreshWidget(context);
            }
        };
        checkBoxShowOnlyCurMonthDays.setOnCheckedChangeListener(listener);
    }

    private void clearSpecialDays(final SharedPreferencesHelper prefHelper) {
        for (SpecialDayInfo specialDayInfo : sSpecialDayInfos) {
            prefHelper.remove(specialDayInfo.prefKey);
            specialDayInfo.list.clear();
        }
        mSpecialDaysAdapter.collectSpecialDays();
        updateSpecialDayViews();
    }

    private void showCalendarSearchDialog(final boolean sunar2lunar, final boolean withTime) {
        final Context context = this;

        final String title;
        final String datePrefix;
        if (sunar2lunar) {
            datePrefix = getString(R.string._sunar);
            title = getString(R.string.sunar_to_lunar);
        } else {
            datePrefix = getString(R.string._lunar);
            title = getString(R.string.lunar_to_sunar);
        }

        Utility.ViewInit viewInit = new Utility.ViewInit() {
            private TextView textViewDate, textViewTime;
            private RadioGroup radioGroupDayShow;
            private TextView textViewResult;
            private CheckBox checkboxShowAlmanac;

            private View viewAddAsSpecialDay;

            private int year, month, day;

            private int lunar_month, lunar_day;

            private int hour_of_day = -1, _minute = -1;

            private boolean mDateSet, mTimeSet;

            private void initViewDate(View rootView) {
                textViewDate = rootView.findViewById(R.id.label_input_date);
                final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int n_year,
                            int n_monthOfYear, int n_dayOfMonth) {
                        year = n_year;
                        if (sunar2lunar) {
                            month = n_monthOfYear;
                            day = n_dayOfMonth;
                        } else {
                            lunar_month = n_monthOfYear;
                            lunar_day = n_dayOfMonth;
                        }
                        setDateText();
                        calculate();
                    }
                };

                textViewDate.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String title = context.getString(R.string.prompt_input_date);
                        if (sunar2lunar) {
                            Utility.showDatePickerDialog(context, year, month, day, title,
                                    dateSetListener);
                        } else {
                            Utility.showDatePickerDialog(context, year, lunar_month, lunar_day,
                                    title, dateSetListener);
                        }
                    }
                });
            }

            private void initViewTime(View rootView) {
                textViewTime = rootView.findViewById(R.id.label_input_time);
                if (withTime) {
                    textViewTime.setVisibility(View.VISIBLE);
                } else {
                    textViewTime.setVisibility(View.GONE);
                }
                final TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        hour_of_day = hourOfDay;
                        _minute = minute;

                        setTimeText();
                        calculate();
                    }
                };

                textViewTime.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String title = context.getString(R.string.prompt_input_time);
                        Utility.showTimePickerDialog(context, title, timeSetListener, hour_of_day,
                                        _minute);
                    }
                });
            }

            @Override
            public void initViews(View rootView) {
                year = mToday.year;
                if (sunar2lunar) {
                    month = mToday.month;
                    day = mToday.day;
                } else {
                    lunar_month = mToday.lunar_month;
                    lunar_day = mToday.lunar_day;
                }

                radioGroupDayShow = rootView.findViewById(R.id.radiogroup_day_show);
                radioGroupDayShow.setVisibility(View.GONE);

                textViewResult = rootView.findViewById(R.id.text_calendar_result);
                checkboxShowAlmanac = rootView.findViewById(R.id.checkbox_show_almanac);
                checkboxShowAlmanac.setVisibility(View.GONE);

                viewAddAsSpecialDay = rootView.findViewById(R.id.view_add_as_special_day);
                viewAddAsSpecialDay.setVisibility(View.INVISIBLE);

                initViewDate(rootView);
                initViewTime(rootView);
            }

            private String getWeekInfo(final Day day) {
                return context.getString(R.string.week_format_full, day.week_of_year,
                        WeekDay.getFullLabel(context, day.weekday));
            }

            private String daysInfo(final boolean showAlmanac, final Day... days) {
                if (days == null || days.length <= 0) return null;
                final String daySeparator = "\n------------------------------------------------\n";

                StringBuilder result = new StringBuilder();
                final Day winterComingDay = WidgetMonthCalendar.getWinterComingDate(context,
                        days[0]);
                final Day summerComingDay = CalendarUtil.getSummerComingDate(context, days[0].year);
                final Day _1stGengDayAfterSummerComingDay = WidgetMonthCalendar.getLunarDayByStem(
                        summerComingDay, 6/* 庚为7，从0开始 */);
                final Day autumnDay = CalendarUtil.getAutumnDate(context, days[0].year);
                Constellation constellation;
                for (Day day : days) {
                    if (result.length() > 0) result.append(daySeparator);
                    Utility.initDay(day, sFirstDayOfWeek);

                    result.append(Utility.getDayText(context, day));
                    if (withTime) {
                        result.append(textViewTime.getText());
                    }
                    result.append(' ').append(getWeekInfo(day));
                    result.append(' ').append(Constants.getYunDay(day.day)); // 韵目代日

                    constellation = Constellation.getConstellation(day);
                    result.append('\n').append(context.getString(constellation.labelResId));
                    result.append(' ').append(context.getString(constellation.stoneResId));
                    result.append(' ').append(context.getString(constellation.flowerResId));
                    result.append('\n').append(Utility.getLunarDateText(context, day));
                    result.append('(').append(Utility.getLunarDateStemBranch(context, day)).append(')');
                    if (withTime) {
                        String chineseTime = CalendarUtil.getChineseTime2(context,
                                day.lunar_day_stem_index, hour_of_day);
                        result.append(chineseTime);
                        // 八字五行五方信息.
                        String baZiWuXing = CalendarUtil.baZiWuXing(context, day, hour_of_day);
                        result.append('\n').append(baZiWuXing);
                        // 纳音五行信息.
                        String nayinInfo = NaYinUtils.getNaYinInfo(context, day);
                        result.append('\n').append(nayinInfo);
                    }

                    String special = Utility.getSpecialDayInfo(context, day, winterComingDay,
                            _1stGengDayAfterSummerComingDay, autumnDay);
                    if (!TextUtils.isEmpty(special)) {
                        result.append('\n').append(special);
                    }
                    if (showAlmanac) {
                        result.append('\n').append(AlmanacUtils.getAlmanacText(day));
                    }
                }
                if (result.length() > 0) return result.toString();
                return null;
            }

            private boolean checkShowAlmanac() {
                if (checkboxShowAlmanac.getVisibility() == View.VISIBLE) {
                    return checkboxShowAlmanac.isChecked();
                }
                return false;
            }

            private void updateResult(final Day[] tempDays, final int dayShowConfig) {
                final boolean showAlmanac = checkShowAlmanac();
                String result = null;
                switch (dayShowConfig) {
                    case R.id.radio_day_1:
                        result = daysInfo(showAlmanac, tempDays[0]);
                        break;
                    case R.id.radio_day_2:
                        result = daysInfo(showAlmanac, tempDays[1]);
                        break;
                    case R.id.radio_day_both:
                        result = daysInfo(showAlmanac, tempDays);
                        break;
                }
                textViewResult.setText(result);
            }

            private void updateDayShow(final Day[] tempDays) {
                radioGroupDayShow.check(R.id.radio_day_both);
                if (tempDays == null || tempDays.length < 2) {
                    radioGroupDayShow.setVisibility(View.GONE);
                    return;
                }
                radioGroupDayShow.setVisibility(View.VISIBLE);

                final RadioGroup.OnCheckedChangeListener checkedChangeListener = new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        updateResult(tempDays, checkedId);
                    }
                };
                radioGroupDayShow.setOnCheckedChangeListener(checkedChangeListener);
            }

            private void updateShowAlmanac(final Day[] tempDays) {
                checkboxShowAlmanac.setVisibility(View.VISIBLE);
                final CheckBox.OnCheckedChangeListener checkedChangeListener = new CheckBox.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        updateResult(tempDays, radioGroupDayShow.getCheckedRadioButtonId());
                    }
                };
                checkboxShowAlmanac.setOnCheckedChangeListener(checkedChangeListener);
            }

            private void updateAddAsSpecialDay(final Day[] tempDays) {
                if (tempDays == null || tempDays.length <= 0) {
                    viewAddAsSpecialDay.setVisibility(View.INVISIBLE);
                    return;
                }
                viewAddAsSpecialDay.setVisibility(View.VISIBLE);
                viewAddAsSpecialDay.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Day addDay = getDayShow(tempDays);
                        showAddSpecialDay(context, mToday, addDay, mSpecialDayChangeListener);
                    }

                    private Day getDayShow(final Day[] tempDays) {
                        if (radioGroupDayShow.getVisibility() == View.VISIBLE) {
                            int checkedRadio = radioGroupDayShow.getCheckedRadioButtonId();
                            if (checkedRadio == R.id.radio_day_2) {
                                return tempDays[1]; // 使用闰月日期.
                            }
                            return tempDays[0];
                        }
                        return tempDays[0];
                    }
                });
            }

            private void calculate() {
                if (withTime) {
                    if (!mDateSet || !mTimeSet) return;
                }
                final Day[] tempDays;
                if (sunar2lunar) {
                    tempDays = new Day[] {
                            new Day(year, month, day)
                    };
                } else if (CalendarUtil.isValidLunarDay(lunar_day)) {
                    tempDays = CalendarUtil.lunarToSolar(year, lunar_month, lunar_day);
                } else {
                    textViewResult.setText(R.string.prompt_invalid_lunar_day);
                    return;
                }

                updateDayShow(tempDays);
                updateShowAlmanac(tempDays);
                updateResult(tempDays, radioGroupDayShow.getCheckedRadioButtonId());
                updateAddAsSpecialDay(tempDays);
            }

            private void setDateText() {
                mDateSet = true;

                final String strDate;
                if (sunar2lunar) {
                    strDate = context.getString(R.string.format_ymd, datePrefix, year, month + 1,
                            day);
                } else {
                    strDate = context.getString(R.string.format_ymd, datePrefix, year,
                            lunar_month + 1, lunar_day);
                }
                textViewDate.setText(strDate);
            }

            private void setTimeText() {
                mTimeSet = true;
                final String timeFormat = "%02d:%02d";
                String strTime = String.format(timeFormat, hour_of_day, _minute);
                textViewTime.setText(strTime);
            }
        };
        Utility.showViewDialog(context, title, R.layout.view_calendar_search, viewInit);
    }

    private void showRestoreDialog(final Context context) {
        final int viewLayoutId = R.layout.settings_restore_default;
        final String title = context.getString(R.string.special_days_restore_default);
        final ViewInitWithPositiveNegative viewInit = new ViewInitWithPositiveNegative() {
            private CheckBox checkboxRestoreSpecialDays;
            private CheckBox checkboxRestoreColors;

            @Override
            public void initViews(View rootView) {
                checkboxRestoreSpecialDays = rootView.findViewById(R.id.checkbox_special_days);
                checkboxRestoreColors = rootView.findViewById(R.id.checkbox_special_day_colors);
            }

            @Override
            public void onPositiveClick(View rootView) {
                boolean restoreDays = checkboxRestoreSpecialDays.isChecked();
                boolean restoreColors = checkboxRestoreColors.isChecked();
                restoreSpecialDays(context, restoreDays, restoreColors);
            }

            @Override
            public void onNegativeClick(View rootView) {
                // ...
            }
        };
        final String positiveLabel = context.getString(android.R.string.ok);
        final String negativeLabel = context.getString(android.R.string.cancel);
        Utility.showViewDialog(context, viewLayoutId, title, viewInit, positiveLabel,
                negativeLabel);
    }

    private void restoreSpecialDays(final Context context, final boolean restoreDays,
            final boolean restoreColors) {
        final SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);
        if (restoreDays) {
            clearSpecialDays(prefHelper);
            loadDaySettings(context, prefHelper);
            checkDayTime(mToday);
            mSpecialDaysAdapter.collectSpecialDays();
        }
        if (restoreColors) {
            DayTypeColorInfo.init();
            saveSpecialDayColorsPreference(prefHelper);
            mColorChangedListener.colorChanged();
        }
        updateSpecialDayViews();
    }

    private void showAlmanacWords() {
        final Context context = this;
        final String title = getString(R.string.almanac_words);
        final String almanacWords = loadAssetFileToString(context, "almanac_words.txt");
        Utility.showSelectableInfo(context, title, almanacWords);
    }

    private void show9Info(final int year) {
        final Context context = this;
        final String title = getString(R.string.format_count_9, year);
        final String count9Info = CalendarUtil.getYearCount9Info(context, year);
        Utility.showSelectableInfo(context, title, count9Info);
    }

    private void showFuInfo(final int year) {
        final Context context = this;
        final String title = getString(R.string.format_count_fu, year);
        final String countFuInfo = CalendarUtil.getYearCountFuInfo(context, year);
        Utility.showSelectableInfo(context, title, countFuInfo);
    }

    public static boolean isPublicHoliday(final Context context, final Day day) {
        final DayInfo dayInfo = getPublicHolidayInfo(context, day);
        return dayInfo != null && !TextUtils.isEmpty(dayInfo.label);
    }
    
    public static DayInfo getPublicHolidayInfo(final Context context, final Day day) {
        if (sPublicHolidays.size() <= 0) {
            loadSettings(context, day);
        }
        for (DayInfo dayInfo : sPublicHolidays) {
            if (day.isSameDay(dayInfo.day)) return dayInfo;
        }
        return null;
    }

    private static boolean isSameDay(Day day, DayInfo dayInfo) {
        switch (dayInfo.type) {
            case PublicHoliday3:
            case PublicHoliday2:
            case Work:
                return day.isSameDay(dayInfo.day);
            case Birthday:
            case Memorial:
                return day.isSameMonthDay(dayInfo.day);
            default:
                return false;
        }
    }

    public static DayInfo checkDayInfo(final Context context, final Day day, final Day today) {
        loadSettings(context, today);
        for (SpecialDayInfo specialDayInfo : sSpecialDayInfos) {
            for (DayInfo dayInfo : specialDayInfo.list) {
                if (isSameDay(day, dayInfo)) return dayInfo;
            }
        }
        return null;
    }

    public static int loadFirstDayOfWeek(final SharedPreferencesHelper prefHelper) {
        if (sFirstDayOfWeek >= 0) return sFirstDayOfWeek;

        final String key = PREF_KEY_FIRST_DAY_OF_WEEK;
        if (prefHelper.contains(key)) {
            sFirstDayOfWeek = (Integer) prefHelper.get(key, Calendar.SUNDAY);
        } else {
            sFirstDayOfWeek = Calendar.SUNDAY;
            saveFirstDayOfWeekPreference(prefHelper);
        }
        return sFirstDayOfWeek;
    }

    public static boolean loadShowAlmanac(final SharedPreferencesHelper prefHelper) {
        final String key = PREF_KEY_SHOW_ALMANAC;
        if (prefHelper.contains(key)) {
            sShowAlmanac = (Boolean) prefHelper.get(key, false);
        } else {
            sShowAlmanac = false;
            saveShowAlmanacPreference(prefHelper);
        }
        return sShowAlmanac;
    }

    public static boolean loadShowOnlyCurMonthDays(final SharedPreferencesHelper prefHelper) {
        final String key = PREF_KEY_SHOW_ONLY_CUR_MONTH_DAYS;
        if (prefHelper.contains(key)) {
            sShowOnlyCurMonthDays = (Boolean) prefHelper.get(key, false);
        } else {
            sShowOnlyCurMonthDays = false;
            saveShowOnlyCurMonthDaysPreference(prefHelper);
        }
        return sShowOnlyCurMonthDays;
    }

    private synchronized static void loadSettings(final Context context, final Day today) {
        if (sSettingsLoaded) {
            checkDayTime(today);
            return;
        }

        final SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);

        SettingsUtils.loadGeneric(prefHelper);

        loadFirstDayOfWeek(prefHelper);
        loadShowOnlyCurMonthDays(prefHelper);
        loadShowAlmanac(prefHelper);
        loadDaySettings(context, prefHelper);

        checkDayTime(today);
        sSettingsLoaded = true;
    }

    private static void loadDaySettings(final Context context,
            final SharedPreferencesHelper prefHelper) {
        String specialDaysPreference;
        String prefKey;
        ArrayList<DayInfo> specialDayList;
        DayType specialDayType;
        for (SpecialDayInfo specialDayInfo : sSpecialDayInfos) {
            prefKey = specialDayInfo.prefKey;
            specialDayList = specialDayInfo.list;
            specialDayType = DayType.getTypeFromPrefKey(prefKey);
            if (prefHelper.contains(prefKey)) {
                specialDaysPreference = (String) prefHelper.get(prefKey, null);
                if (!initSpecialDays(specialDaysPreference, specialDayType, specialDayList)) {
                    loadDefaultSpecialDays(context, specialDayType, specialDayList);
                }
            } else {
                loadDefaultSpecialDays(context, specialDayType, specialDayList);
            }
        }
        loadDayColorSettings(prefHelper);
    }

    // 获得输入日期之后的工作日.
    public static ArrayList<Day> getWorkDays(final Context context, final Day dayInput) {
        //if (sWorkDays == null || sWorkDays.size() <= 0) {
        //    final SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);
        //    final DayType dayType = DayType.Work;
        //    final String prefKey = PREF_KEY_WORK_DAYS;
        //    if (prefHelper.contains(prefKey)) {
        //        String specialDaysPreference = (String) prefHelper.get(prefKey, null);
        //        if (!initSpecialDays(specialDaysPreference, dayType, sWorkDays)) {
        //            loadDefaultSpecialDays(context, dayType, sWorkDays);
        //        }
        //    } else {
        //        loadDefaultSpecialDays(context, dayType, sWorkDays);
        //    }
        //}
        if (sWorkDays == null || sWorkDays.size() <= 0) return null;
        final ArrayList<Day> dayList = new ArrayList<Day>(sWorkDays.size());
        for (DayInfo dayInfo : sWorkDays) {
            if (dayInfo.day.laterThan(dayInput)) {
                dayList.add(dayInfo.day);
            }
        }
        if (dayList.size() <= 0) return null;
        return dayList;
    }

    private static boolean initPublicHolidays(String publicHolidays) {
        if (TextUtils.isEmpty(publicHolidays)) return false;

        sPublicHolidays.clear();
        String[] dayInfos = publicHolidays.split(SEP_HOLIDAY);
        for (String dayInfoString : dayInfos) {
            DayInfo dayInfo = DayInfo.parseFromPreference(dayInfoString);
            sPublicHolidays.add(dayInfo);
        }
        return true;
    }

    private static boolean initWorkDays(String workDays) {
        if (TextUtils.isEmpty(workDays)) return false;

        sWorkDays.clear();
        String[] dayInfos = workDays.split(SEP_HOLIDAY);
        for (String dayInfoString : dayInfos) {
            DayInfo dayInfo = DayInfo.parseFromPreference(dayInfoString);
            sWorkDays.add(dayInfo);
        }
        return true;
    }

    private static boolean initCompanyHolidays(String companyHolidays) {
        if (TextUtils.isEmpty(companyHolidays)) return false;

        sCompanyHolidays.clear();
        String[] dayInfos = companyHolidays.split(SEP_HOLIDAY);
        for (String dayInfoString : dayInfos) {
            CompanyHolidayInfo dayInfo = CompanyHolidayInfo.parseFromPreference(dayInfoString);
            sCompanyHolidays.add(dayInfo);
        }
        return true;
    }

    private static boolean initBirthdays(String birthdays) {
        if (TextUtils.isEmpty(birthdays)) return false;

        sBirthDates.clear();
        String[] dayInfos = birthdays.split(SEP_HOLIDAY);
        for (String dayInfoString : dayInfos) {
            DayInfo dayInfo = DayInfo.parseFromPreference(dayInfoString);
            sBirthDates.add(dayInfo);
        }
        return true;
    }

    private static boolean initMemorials(String memorials) {
        if (TextUtils.isEmpty(memorials)) return false;

        sMemorials.clear();
        String[] dayInfos = memorials.split(SEP_HOLIDAY);
        for (String dayInfoString : dayInfos) {
            DayInfo dayInfo = DayInfo.parseFromPreference(dayInfoString);
            sMemorials.add(dayInfo);
        }
        return true;
    }

    private static boolean initOtherDays(String otherDays) {
        if (TextUtils.isEmpty(otherDays)) return false;

        sOtherSpecialDays.clear();
        String[] dayInfos = otherDays.split(SEP_HOLIDAY);
        for (String dayInfoString : dayInfos) {
            DayInfo dayInfo = DayInfo.parseFromPreference(dayInfoString);
            sOtherSpecialDays.add(dayInfo);
        }
        return true;
    }

    private static boolean initSpecialDays(final String preferences, final DayType dayType,
            final ArrayList<? extends DayInfo> dayList) {
        switch (dayType) {
            case PublicHoliday3:
            case PublicHoliday2:
                return initPublicHolidays(preferences);
            case Work:
                return initWorkDays(preferences);
            case CompanyHoliday:
                return initCompanyHolidays(preferences);
            case Birthday:
                return initBirthdays(preferences);
            case Memorial:
                return initMemorials(preferences);
            case Other:
                return initOtherDays(preferences);
            case Normal:
            default: return false;
        }
    }

    private static void loadDefaultPublicHolidays(final Context context) {
        Resources res = context.getResources();

        sPublicHolidays.clear();
        String[] array = res.getStringArray(R.array.public_holidays_china);
        for (String holidayInfo : array) {
            String[] elements = holidayInfo.split(SEP_HOLIDAY);
            String label = elements[0];
            for (int i = 1; i < elements.length; i++) {
                String[] dayElements = elements[i].split(SEP_HOLIDYA_TYPE);
                DayType dayType = DayType.PublicHoliday2;
                if (dayElements.length > 1) {
                    dayType = DayType.getTypeFromOrdinal(Integer.parseInt(dayElements[1]));
                }
                Day day = Day.parseDateYYYYMMDD(dayElements[0]);
                DayInfo dayInfo = new DayInfo(dayType, day, label);
                sPublicHolidays.add(dayInfo);
            }
        }
        savePublicHolidaysPreferences(context);
    }

    private static void loadDefaultWorkDays(final Context context) {
        Resources res = context.getResources();
        sWorkDays.clear();
        String[] array = res.getStringArray(R.array.work_days_china);
        for (String workDayInfo : array) {
            String[] elements = workDayInfo.split(SEP_HOLIDAY);
            String label = elements[0];
            for (int i = 1; i < elements.length; i++) {
                DayType dayType = DayType.Work;
                Day day = Day.parseDateYYYYMMDD(elements[i]);
                DayInfo dayInfo = new DayInfo(dayType, day, label);
                sWorkDays.add(dayInfo);
            }
        }
        saveWorkDaysPreferences(context);
    }

    private static void loadDefaultMemorialDays(final Context context) {
        sMemorials.clear();
        // Resources res = context.getResources();
        // String[] array = res.getStringArray(R.array.memorials_default);
        // for (String memorialInfo : array) {
        //    String[] elements = memorialInfo.split(SEP_HOLIDAY);
        //    String label = elements[0];
        //    for (int i = 1; i < elements.length; i++) {
        //        DayType dayType = DayType.Memorial;
        //        Day day = Day.parseDateYYYYMMDD(elements[i]);
        //        DayInfo dayInfo = new DayInfo(dayType, day, label);
        //        sMemorials.add(dayInfo);
        //    }
        //}
        //saveMemorialsPreferences(context);
    }

    private static void loadDefaultBirthDays(final Context context) {
        sBirthDates.clear();
        // Resources res = context.getResources();
        // String[] array = res.getStringArray(R.array.birthdays_default);
        // for (String birthdayInfo : array) {
        //     String[] elements = birthdayInfo.split(SEP_HOLIDAY);
        //     String label = elements[0];
        //     for (int i = 1; i < elements.length; i++) {
        //         DayType dayType = DayType.Birthday;
        //         Day day = Day.parseDateYYYYMMDD(elements[i]);
        //         DayInfo dayInfo = new DayInfo(dayType, day, label);
        //         sBirthDates.add(dayInfo);
        //     }
        // }
        // saveBirthdaysPreferences(context);
    }

    private static void loadDefaultCompanyHolidays(final Context context) {
        Resources res = context.getResources();
        sCompanyHolidays.clear();
        String[] array = res.getStringArray(R.array.company_holidays);
        for (String companyHolidayInfo : array) {
            String[] elements = companyHolidayInfo.split(SEP_HOLIDAY);
            String label = elements[0];
            String company = elements[elements.length - 1];
            for (int i = 1; i < elements.length - 1; i++) {
                DayType dayType = DayType.CompanyHoliday;
                Day day = Day.parseDateYYYYMMDD(elements[i]);
                CompanyHolidayInfo dayInfo = new CompanyHolidayInfo(dayType, day, label, company);
                sCompanyHolidays.add(dayInfo);
            }
        }
        saveCompanyHolidaysPreferences(context);
    }

    private static void loadDefaultSpecialDays(final Context context, final DayType specialDayType,
            final ArrayList<? extends DayInfo> dayList) {
        switch (specialDayType) {
            case PublicHoliday3:
            case PublicHoliday2:
                loadDefaultPublicHolidays(context);
                break;
            case Work:
                loadDefaultWorkDays(context);
                break;
            case CompanyHoliday:
                loadDefaultCompanyHolidays(context);
                break;
            case Birthday:
                loadDefaultBirthDays(context);
                break;
            case Memorial:
                loadDefaultMemorialDays(context);
                break;
            case Other:
                break;
            case Normal:
            default:
                break;
        }
    }

    private static void loadDayColorSettings(final SharedPreferencesHelper prefHelper) {
        for (DayTypeColorInfo info : sDayTypeColorInfos) {
            if (prefHelper.contains(info.prefKey)) {
                String colorPreference = (String) prefHelper.get(info.prefKey, null);
                if (!TextUtils.isEmpty(colorPreference)) {
                    DayTypeColorInfo.parse(colorPreference);
                }
            }
        }
    }

    private static void saveSpecialDays(final Context context, final DayType specialDayType,
            final ArrayList<? extends DayInfo> dayList) {
        switch (specialDayType) {
            case PublicHoliday3:
            case PublicHoliday2:
                savePublicHolidaysPreferences(context);
                break;
            case Work:
                saveWorkDaysPreferences(context);
                break;
            case CompanyHoliday:
                saveCompanyHolidaysPreferences(context);
                break;
            case Birthday:
                saveBirthdaysPreferences(context);
                break;
            case Memorial:
                saveMemorialsPreferences(context);
                break;
            case Other:
                saveOtherDaysPreferences(context);
                break;
            case Normal:
            default:
                break;
        }
    }

    private static void savePublicHolidaysPreferences(final Context context) {
        saveSpecialDayPreferences(context, PREF_KEY_PUBLIC_HOLIDAYS, sPublicHolidays);
    }

    private static void saveWorkDaysPreferences(final Context context) {
        saveSpecialDayPreferences(context, PREF_KEY_WORK_DAYS, sWorkDays);
    }

    private static void saveSpecialDayPreferences(final Context context, final String prefKey,
                    final ArrayList<DayInfo> specialDays) {
        final SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);
        if (specialDays.size() <= 0) {
            // prefHelper.remove(prefKey); // 这里不能remove key，因为删掉key会导致下次装载的时候加载了默认日期!
            prefHelper.put(prefKey, Constants.NULL_STR);
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (DayInfo dayInfo : specialDays) {
            if (sb.length() > 0) sb.append(SEP_HOLIDAY);
            sb.append(dayInfo.formatPreference());
        }
        prefHelper.put(prefKey, sb.toString());
    }

    private static void saveCompanyHolidaysPreferences(final Context context) {
        saveSpecialDayPreferences(context, PREF_KEY_COMPANY_HOLIDAYS, sCompanyHolidays);
    }

    private static void saveBirthdaysPreferences(final Context context) {
        saveSpecialDayPreferences(context, PREF_KEY_BIRTHDAYS, sBirthDates);
    }

    private static void saveMemorialsPreferences(final Context context) {
        saveSpecialDayPreferences(context, PREF_KEY_MEMORIALS, sMemorials);
    }

    private static void saveOtherDaysPreferences(final Context context) {
        saveSpecialDayPreferences(context, PREF_KEY_OTHERS, sOtherSpecialDays);
    }

    private static void saveFirstDayOfWeekPreference(final Context context) {
        saveFirstDayOfWeekPreference(SettingsUtils.getPrefsHelper(context));
    }

    private static void saveFirstDayOfWeekPreference(SharedPreferencesHelper prefHelper) {
        prefHelper.put(PREF_KEY_FIRST_DAY_OF_WEEK, sFirstDayOfWeek);
    }

    private static void saveShowAlmanacPreference(SharedPreferencesHelper prefHelper) {
        prefHelper.put(PREF_KEY_SHOW_ALMANAC, sShowAlmanac);
    }

    private static void saveShowAlmanacPreference(final Context context) {
        saveShowAlmanacPreference(SettingsUtils.getPrefsHelper(context));
    }

    private static void saveShowOnlyCurMonthDaysPreference(SharedPreferencesHelper prefHelper) {
        prefHelper.put(PREF_KEY_SHOW_ONLY_CUR_MONTH_DAYS, sShowOnlyCurMonthDays);
    }

    private static void saveShowOnlyCurMonthDaysPreference(final Context context) {
        saveShowOnlyCurMonthDaysPreference(SettingsUtils.getPrefsHelper(context));
    }

    private static void saveSpecialDayColorsPreference(final SharedPreferencesHelper prefHelper) {
        for (DayTypeColorInfo info : sDayTypeColorInfos) {
            prefHelper.put(info.prefKey, info.colorString());
        }
    }

    private static void saveSpecialDayColorsPreference(final Context context) {
        saveSpecialDayColorsPreference(SettingsUtils.getPrefsHelper(context));
    }

    private static class DayInfoListAdapter extends BaseAdapter {
        private final Context mContext;
        private final Day mToday;
        private final SpecialDayChangeListener mSpecialDayChangeListener;
        private final ColorChangedListener mColorChangedListener;

        private final ArrayList<DayInfo> mSpecialDays = new ArrayList<DayInfo>();

        public DayInfoListAdapter(final Context context, final Day today,
                SpecialDayChangeListener specialDayChangeListener,
                ColorChangedListener colorChangedListener) {
            mContext = context;
            mToday = today;
            mSpecialDayChangeListener = specialDayChangeListener;
            mColorChangedListener = colorChangedListener;
        }

        public void collectSpecialDays() {
            mSpecialDays.clear();
            for (SpecialDayInfo specialDayInfo : sSpecialDayInfos) {
                for (DayInfo dayInfo : specialDayInfo.list) {
                    mSpecialDays.add(dayInfo);
                }
            }
            Collections.sort(mSpecialDays, new Comparator<DayInfo>() {
                @Override
                public int compare(DayInfo dayInfo1, DayInfo dayInfo2) {
                    return Day.compare(dayInfo1.day, dayInfo2.day);
                }
            });
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mSpecialDays.size();
        }

        @Override
        public Object getItem(int position) {
            return mSpecialDays.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DayInfoViewHolder viewHolder;

            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.entry_special_day, null);

                viewHolder = new DayInfoViewHolder();
                viewHolder.labelView = (TextView) convertView.findViewById(R.id.view_label);
                viewHolder.dateView = (TextView) convertView.findViewById(R.id.view_date);
                viewHolder.typeView = (TextView) convertView.findViewById(R.id.view_type);
                viewHolder.otherView = (TextView) convertView.findViewById(R.id.view_other);
                viewHolder.buttonDelete = convertView.findViewById(R.id.view_delete);
                viewHolder.buttonEdit = convertView.findViewById(R.id.view_edit);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (DayInfoViewHolder) convertView.getTag();
            }

            initViewHolder(viewHolder, position);

            return convertView;
        }

        private void setTypeColor(final DayInfo dayInfo, final TextView typeView) {
            DayTypeColorInfo colorInfo = DayTypeColorInfo.getTypeColorInfo(dayInfo.type);
            int textColor = 0, backgroundColor = 0;
            if (colorInfo == null) {
                textColor = dayInfo.type.textColor;
                backgroundColor = dayInfo.getBackgroundColor();
            } else {
                textColor = colorInfo.textColor;
                backgroundColor = colorInfo.getBackgroundColor(dayInfo.mDayTime);
            }
            if (textColor != 0) {
                typeView.setTextColor(textColor);
            }
            if (backgroundColor != 0) {
                typeView.setBackgroundColor(backgroundColor);
            }
        }

        private void initViewHolder(final DayInfoViewHolder viewHolder, final int position) {
            final DayInfo item = (DayInfo) getItem(position);
            if (item.type == DayType.Work) {
                viewHolder.labelView.setText(WeekDay.getFullLabel(item.day.weekday));
            } else {
                viewHolder.labelView.setText(item.label);
            }
            viewHolder.dateView.setText(item.day.ymdw_chinese(mContext));
            viewHolder.typeView.setText(item.type.labelResId);
            setTypeColor(item, viewHolder.typeView);
            viewHolder.typeView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditSpecialDayColor(mContext, item.type, mColorChangedListener);
                }
            });

            String otherInfo = item.getOtherInfo();
            if (TextUtils.isEmpty(otherInfo)) {
                viewHolder.otherView.setVisibility(View.GONE);
            } else {
                viewHolder.otherView.setVisibility(View.VISIBLE);
                viewHolder.otherView.setText(otherInfo);
            }

            viewHolder.buttonDelete.setVisibility(View.VISIBLE);
            viewHolder.buttonDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String confirmPrompt = mContext.getString(
                            R.string.format_prompt_delete_special_day, item.info(mContext));
                    final DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteSpecialDay(mContext, item);
                            if (mSpecialDayChangeListener == null) {
                                collectSpecialDays();
                            } else {
                                mSpecialDayChangeListener.whenSpecialDayChanged(mContext);
                            }
                        }
                    };
                    Utility.showConfirmDialog(mContext, confirmPrompt, positiveButtonListener);
                }
            });

            viewHolder.buttonEdit.setVisibility(View.VISIBLE);
            viewHolder.buttonEdit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditSpecialDay(mContext, mToday, item, mSpecialDayChangeListener);
                }
            });
        }
    }

    private static class DayInfoViewHolder {
        TextView labelView;
        TextView dateView;
        TextView typeView;
        TextView otherView;
        View buttonDelete;
        View buttonEdit;
    }

    private static class DayTypeColorListAdapter extends BaseAdapter {
        private final Context mContext;

        public DayTypeColorListAdapter(final Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return sDayTypeColorInfos.length;
        }

        @Override
        public Object getItem(int position) {
            return sDayTypeColorInfos[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DayTypeColorViewHolder viewHolder;

            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.entry_special_day_color, null);

                viewHolder = new DayTypeColorViewHolder();
                viewHolder.labelView = (TextView) convertView.findViewById(R.id.view_type_label);
                viewHolder.pastView = (TextView) convertView.findViewById(R.id.view_past);
                viewHolder.comingView = (TextView) convertView.findViewById(R.id.view_coming);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (DayTypeColorViewHolder) convertView.getTag();
            }

            initViewHolder(viewHolder, position);

            return convertView;
        }

        private void initViewHolder(final DayTypeColorViewHolder viewHolder, final int position) {
            final DayTypeColorInfo item = (DayTypeColorInfo) getItem(position);
            viewHolder.labelView.setText(item.type.toString());
            viewHolder.pastView.setText(item.type.labelResId);
            viewHolder.pastView.setTextColor(item.textColor);
            viewHolder.pastView.setBackgroundColor(item.backgroundColorPast);
            viewHolder.pastView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ColorPickerDialog.OnColorSetListener colorSetListener = new ColorPickerDialog.OnColorSetListener() {
                        @Override
                        public void onColorSet(int textColor, int backgroundColor) {
                            item.textColor = textColor;
                            item.backgroundColorPast = backgroundColor;
                            notifyDataSetChanged();
                        }
                    };
                    showColorSetDialog(mContext, colorSetListener, item.type.labelResId,
                            item.textColor, item.backgroundColorPast);
                }
            });
            viewHolder.comingView.setText(item.type.labelResId);
            viewHolder.comingView.setBackgroundColor(item.backgroundColorComing);
            viewHolder.comingView.setTextColor(item.textColor);
            viewHolder.comingView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ColorPickerDialog.OnColorSetListener colorSetListener = new ColorPickerDialog.OnColorSetListener() {
                        @Override
                        public void onColorSet(int textColor, int backgroundColor) {
                            item.textColor = textColor;
                            item.backgroundColorComing = backgroundColor;
                            notifyDataSetChanged();
                        }
                    };
                    showColorSetDialog(mContext, colorSetListener, item.type.labelResId,
                            item.textColor, item.backgroundColorComing);
                }
            });
        }
    }

    private static class DayTypeColorViewHolder {
        TextView labelView;
        TextView pastView;
        TextView comingView;
    }
}
