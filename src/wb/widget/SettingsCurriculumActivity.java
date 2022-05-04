package wb.widget;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.TimePicker;
import wb.widget.model.ColorPickerDialog;
import wb.widget.model.Course;
import wb.widget.model.Curriculum;
import wb.widget.model.Curriculum.DateCourses;
import wb.widget.model.Curriculum.DateCoursesChangeListener;
import wb.widget.model.Curriculum.DayCourseViewInfo;
import wb.widget.model.Curriculum.Extra;
import wb.widget.model.Curriculum.MorningEvening;
import wb.widget.model.Day;
import wb.widget.model.SharedPreferencesHelper;
import wb.widget.model.TimeOffSchool;
import wb.widget.utils.Utility;
import wb.widget.utils.calendar.CalendarUtil;
import wb.widget.utils.calendar.Constants;
import wb.widget.utils.calendar.Constants.WeekDay;

public class SettingsCurriculumActivity extends Activity {
    private static final String PREF_KEY_CURRICULUM_COUNT = "curriculum_count";
    private static final String PREF_KEY_CURRICULUM_INDEX = "curriculum_index";

    // Preferences for generic settings.
    private static final String PREF_KEY_CURRICULUM_LABEL = "curriculum_label";
    private static final String PREF_KEY_COLORS_CURRICULUM_LABEL = "colors_curriculum_label";
    private static final String PREF_KEY_COLOR_SCHEME_CURRICULUM = "curriculum_color_scheme";
    private static final String PREF_KEY_SHOW_COURSES_IN_PUBLIC_HOLIDAYS = "show_courses_in_public_holidays";
    private static final String PREF_KEY_COLORS_COURSE = "colors_course";
    private static final String PREF_KEY_SEMESTER = "semester_dates";
    private static final String PREF_KEY_COURSES = "courses";
    private static final String PREF_KEY_DATE_COURSES = "date_courses";
    private static final String PREF_KEY_SHOW_CLASS_TIME = "show_class_time";
    // Preferences for day class configuration.
    private static final String PREF_KEY_DAY_CLASS_AM_1_TIME_START = "day_am_1_time_start";
    private static final String PREF_KEY_DAY_CLASS_AM_1_TIME_END = "day_am_1_time_end";
    private static final String PREF_KEY_DAY_CLASS_AM_2_TIME_START = "day_am_2_time_start";
    private static final String PREF_KEY_DAY_CLASS_AM_2_TIME_END = "day_am_2_time_end";
    private static final String PREF_KEY_DAY_CLASS_AM_3_TIME_START = "day_am_3_time_start";
    private static final String PREF_KEY_DAY_CLASS_AM_3_TIME_END = "day_am_3_time_end";
    private static final String PREF_KEY_DAY_CLASS_AM_4_TIME_START = "day_am_4_time_start";
    private static final String PREF_KEY_DAY_CLASS_AM_4_TIME_END = "day_am_4_time_end";
    private static final String PREF_KEY_DAY_CLASS_PM_1_TIME_START = "day_pm_1_time_start";
    private static final String PREF_KEY_DAY_CLASS_PM_1_TIME_END = "day_pm_1_time_end";
    private static final String PREF_KEY_DAY_CLASS_PM_2_TIME_START = "day_pm_2_time_start";
    private static final String PREF_KEY_DAY_CLASS_PM_2_TIME_END = "day_pm_2_time_end";
    private static final String PREF_KEY_DAY_CLASS_PM_3_TIME_START = "day_pm_3_time_start";
    private static final String PREF_KEY_DAY_CLASS_PM_3_TIME_END = "day_pm_3_time_end";
    private static final String PREF_KEY_DAY_CLASS_PM_4_TIME_START = "day_pm_4_time_start";
    private static final String PREF_KEY_DAY_CLASS_PM_4_TIME_END = "day_pm_4_time_end";

    private static boolean sSettingsLoaded = false;

    private static final ArrayList<Curriculum> sCurriculums = new ArrayList<Curriculum>();
    private static int sCurrentCurriculumIndex = -1;

    public static int curriculumCount() {
        return sCurriculums.size();
    }

    public static void prevCurriculum(final Context context) {
        final int count = curriculumCount();
        sCurrentCurriculumIndex = (sCurrentCurriculumIndex - 1 + count) % count;
        saveCurriculumCountAndIndex(context);
        Constants.refreshWidget(context);
    }

    public static void nextCurriculum(final Context context) {
        final int count = curriculumCount();
        sCurrentCurriculumIndex = (sCurrentCurriculumIndex + 1) % count;
        saveCurriculumCountAndIndex(context);
        Constants.refreshWidget(context);
    }

    public static void switchClassTimeShow(final Context context) {
        Curriculum curriculum = getCurriculum();
        curriculum.config.showClassTimeInWidget = !curriculum.config.showClassTimeInWidget;
        saveShowClassTimeInWidget(SettingsUtils.getPrefsHelper(context), curriculum);
        Constants.refreshWidget(context);
    }

    public static enum CurriculumColorScheme {
        SameColor(R.id.radio_same_color),
        DifferentColors(R.id.radio_different_colors);

        public final int radioResId;

        private CurriculumColorScheme(final int radioResId) {
            this.radioResId = radioResId;
        }

        public static int count() {
            return values().length;
        }

        public static CurriculumColorScheme get(final int ordinal) {
            for (CurriculumColorScheme setting : values()) {
                if (setting.ordinal() == ordinal) return setting;
            }
            return SameColor;
        }

        public static CurriculumColorScheme getByRadio(final int radioId) {
            for (CurriculumColorScheme setting : values()) {
                if (setting.radioResId == radioId) return setting;
            }
            return SameColor;
        }
    }

    /**
     * 
     * @param prefHelper
     * @return true means first load; false means already loaded.
     */
    public static boolean loadCurriculumCountAndIndex(final SharedPreferencesHelper prefHelper) {
        if (sCurrentCurriculumIndex >= 0) return false;
        int count = (Integer) prefHelper.get(PREF_KEY_CURRICULUM_COUNT, 0);
        if (count <= 0) count = 1;
        for (int i = 0; i < count; i++) {
            sCurriculums.add(new Curriculum());
        }
        int index = (Integer) prefHelper.get(PREF_KEY_CURRICULUM_INDEX, -1);
        if (index <= 0) index = 0;
        sCurrentCurriculumIndex = index;
        return true;
    }

    public static String loadLabel(final SharedPreferencesHelper prefHelper) {
        return loadLabel(prefHelper, getCurriculum());
    }

    public static String loadLabel(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        final Curriculum.Config config = curriculum.config;
        final String prefKeyDefault = PREF_KEY_CURRICULUM_LABEL;
        final Object defaultValue = null;
        config.label = (String) loadPreference(prefHelper, curriculum, prefKeyDefault,
                        defaultValue);
        return config.label;
    }

    private static Object loadPreference(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum, final String prefKeyDefault,
                    final Object defaultValue) {
        final String prefKeyWithIndex = curriculum.getPrefKeyWithIndex(prefKeyDefault);
        if (prefHelper.contains(prefKeyWithIndex)) {
            return prefHelper.get(prefKeyWithIndex, defaultValue);
        }
        if (curriculum.curriculumIndex() > 0) return defaultValue;
        if (prefHelper.contains(prefKeyDefault)) {
            return prefHelper.get(prefKeyDefault, defaultValue);
        }
        return defaultValue;
    }

    private static void saveLabel(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        String prefKeyDefault = PREF_KEY_CURRICULUM_LABEL;
        String label = curriculum.config.label;
        savePreference(prefHelper, curriculum, prefKeyDefault, label);
    }

    public static void loadLabelColors(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        final Curriculum.Config config = curriculum.config;

        final String prefKeyDefault = PREF_KEY_COLORS_CURRICULUM_LABEL;
        final Object defaultValue = null;

        String colors = (String) loadPreference(prefHelper, curriculum, prefKeyDefault,
                        defaultValue);
        boolean colorSaved = config.parseLabelColors(colors);
        if (!colorSaved) {
            config.initLabelColors();
            saveLabelColors(prefHelper, curriculum);
        }
    }

    public static void loadColorScheme(final SharedPreferencesHelper prefHelper) {
        loadColorScheme(prefHelper, getCurriculum());
    }

    public static void loadColorScheme(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        final Curriculum.Config config = curriculum.config;

        final String prefKeyDefault = PREF_KEY_COLOR_SCHEME_CURRICULUM;
        final int defaultValue = -1;

        Integer ordinal = (Integer) loadPreference(prefHelper, curriculum, prefKeyDefault,
                        defaultValue);
        if (ordinal == defaultValue) {
            initColorSettings(prefHelper, curriculum, CurriculumColorScheme.SameColor);
        } else {
            config.colorScheme = CurriculumColorScheme.get(ordinal);
            loadCourseColors(prefHelper, curriculum);
        }
    }

    private static void initColorSettings(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum, final CurriculumColorScheme colorScheme) {
        final Curriculum.Config config = curriculum.config;
        config.colorScheme = colorScheme;
        saveColorScheme(prefHelper, curriculum);
        config.initCourseColors();
        saveCourseColors(prefHelper, curriculum);
    }

    private static final String SEP_SEMESTER_DATES = ";";

    private static Day parseDate(final String yyyymmdd) {
        Day date = Day.parseYmd(yyyymmdd);
        if (date != null) Utility.obtainWeekDay(date);
        return date;
    }

    private static String getSemesterDatesString(final Curriculum.Config config) {
        if (config.semesterStartDate == null || config.semesterEndDate == null) return null;
        StringBuilder sb = new StringBuilder();
        sb.append(config.semesterStartDate.ymd()).append(SEP_SEMESTER_DATES);
        sb.append(config.semesterEndDate.ymd());
        return sb.toString();
    }

    public static void loadSemesterDates(final SharedPreferencesHelper prefHelper) {
        loadSemesterDates(prefHelper, getCurriculum());
    }

    public static void loadSemesterDates(final SharedPreferencesHelper prefHelper, final Curriculum curriculum) {
        final Curriculum.Config config = curriculum.config;

        final String prefKeyDefault = PREF_KEY_SEMESTER;
        final String defaultValue = null;

        String datesString = (String) loadPreference(prefHelper, curriculum, prefKeyDefault,
                        defaultValue);
        if (TextUtils.isEmpty(datesString)) {
            config.semesterStartDate = null;
            config.semesterEndDate = null;
            return;
        }

        String[] elements = datesString.split(SEP_SEMESTER_DATES);
        config.semesterStartDate = parseDate(elements[0]);
        config.semesterEndDate = parseDate(elements[1]);
    }

    // 获得当前日期在本学期第几周. Format: cur/total.
    private static final String FORMAT_WEEK_SEMESTER = "%d/%d";

    public static String getWeekInfoInSemester(final SharedPreferencesHelper prefsHelper,
                    final Day today) {
        if (sCurrentCurriculumIndex < 0) {
            loadCurriculums(prefsHelper);
        }
        final Curriculum.Config config = getConfig();
        if (config.semesterStartDate == null || config.semesterEndDate == null) return null;
        if (!config.semesterEndDate.laterThan(config.semesterStartDate)) return null;
        if (today.earlierThan(config.semesterStartDate)) return null;
        if (today.laterThan(config.semesterEndDate)) return null;
        int curWeek = CalendarUtil.getWeekCountBetweenDates(config.semesterStartDate, today);
        int totalWeek = CalendarUtil.getWeekCountBetweenDates(config.semesterStartDate,
                        config.semesterEndDate);
        return String.format(FORMAT_WEEK_SEMESTER, curWeek, totalWeek);
    }

    private static void saveSemesterDates(final Context context, final Curriculum curriculum) {
        SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);
        saveSemesterDates(prefHelper, curriculum);
    }

    private static void saveSemesterDates(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        final String prefKeyDefault = PREF_KEY_SEMESTER;
        final String datesString = getSemesterDatesString(curriculum.config);
        savePreference(prefHelper, curriculum, prefKeyDefault, datesString);
    }

    public static void loadCourseColors(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        final String prefKeyDefault = PREF_KEY_COLORS_COURSE;
        final Curriculum.Config config = curriculum.config;
        final String defaultValue = null;
        
        boolean colorSaved = false;
        String colors = (String)loadPreference(prefHelper, curriculum, prefKeyDefault, defaultValue);
        if (!TextUtils.isEmpty(colors)) {
            colorSaved = config.parseCourseColors(colors);
        }
        if (!colorSaved) {
            config.initCourseColors();
            saveCourseColors(prefHelper, curriculum);
        }
    }

    public static void loadCourses(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        curriculum.clearCourses();

        final String prefKeyDefault = PREF_KEY_COURSES;
        final String defaultValue = null;
        String coursesString = (String) loadPreference(prefHelper, curriculum, prefKeyDefault,
                        defaultValue);
        if (TextUtils.isEmpty(coursesString)) {
            curriculum.copyDefaultCourses();
            saveCourses(prefHelper, curriculum);
            return;
        }
        curriculum.parseCourses(coursesString);
    }

    public static void loadDateCourses(final SharedPreferencesHelper prefHelper) {
        loadDateCourses(prefHelper, getCurriculum());
    }

    public static void loadDateCourses(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        final String prefKeyDefault = PREF_KEY_DATE_COURSES;
        final String defaultValue = null;
        String dateCoursesString = (String) loadPreference(prefHelper, curriculum, prefKeyDefault,
                        defaultValue);
        curriculum.parseDateCourses(dateCoursesString);
    }

    private static void loadTimeOffSchool(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum, final int weekdayId) {
        final String prefKeyDefault = curriculum.getTimeOffSchoolPrefKey(weekdayId);
        final String defaultValue = null;
        String timeOffSchoolString = (String) loadPreference(prefHelper, curriculum, prefKeyDefault,
                        defaultValue);
        curriculum.setTimeOffSchool(weekdayId, TimeOffSchool.parse(timeOffSchoolString));
    }

    public static void saveTimeOffSchool(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum, final WeekDayCourse weekdayCourse) {
        final String prefKeyDefault = weekdayCourse.prefKeyTimeOffSchool;
        final String prefKeyWithIndex = curriculum.getPrefKeyWithIndex(prefKeyDefault);
        final TimeOffSchool timeOffSchool = weekdayCourse.getTimeOffSchool(false);
        if (timeOffSchool == null) {
            prefHelper.remove(prefKeyWithIndex);
        } else {
            prefHelper.put(prefKeyWithIndex, timeOffSchool.toString());
        }
    }

    private static void saveCurriculumCountAndIndex(final Context context) {
        SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);
        saveCurriculumCountAndIndex(prefHelper);
    }

    private static void saveCurriculumCountAndIndex(final SharedPreferencesHelper prefHelper) {
        prefHelper.put(PREF_KEY_CURRICULUM_COUNT, sCurriculums.size());
        prefHelper.put(PREF_KEY_CURRICULUM_INDEX, sCurrentCurriculumIndex);
    }
    
    public static boolean loadShowCoursesInPublicHolidays(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        final Curriculum.Config config = curriculum.config;

        final String prefKeyDefault = PREF_KEY_SHOW_COURSES_IN_PUBLIC_HOLIDAYS;
        final int defaultValue = 0;

        Integer show = (Integer) loadPreference(prefHelper, curriculum, prefKeyDefault,
                        defaultValue);
        config.showCoursesInPublicHolidays = show == 1;
        return config.showCoursesInPublicHolidays;
    }

    private static void saveShowCoursesInPublicHolidays(final SharedPreferencesHelper prefHelper,
            final Curriculum curriculum) {
        String prefKeyDefault = PREF_KEY_SHOW_COURSES_IN_PUBLIC_HOLIDAYS;
        int value = curriculum.config.showCoursesInPublicHolidays ? 1 : 0;
        savePreference(prefHelper, curriculum, prefKeyDefault, value);
    }
    
    public static boolean loadShowClassTimeInWidget(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        final Curriculum.Config config = curriculum.config;
        final String prefKeyDefault = PREF_KEY_SHOW_CLASS_TIME;
        final int defaultValue = 0;

        Integer show = (Integer) loadPreference(prefHelper, curriculum, prefKeyDefault,
                        defaultValue);
        config.showClassTimeInWidget = show == 1;
        return config.showClassTimeInWidget;
    }
    
    private static void saveShowClassTimeInWidget(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        String prefKeyDefault = PREF_KEY_SHOW_CLASS_TIME;
        int value = curriculum.config.showClassTimeInWidget ? 1 : 0;
        savePreference(prefHelper, curriculum, prefKeyDefault, value);
    }

    private static void saveColorScheme(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        String prefKeyDefault = PREF_KEY_COLOR_SCHEME_CURRICULUM;
        int colorSchemeOrdinal = curriculum.config.colorScheme.ordinal();
        savePreference(prefHelper, curriculum, prefKeyDefault, colorSchemeOrdinal);
    }

    private static void saveCourseColors(final Context context, final Curriculum curriculum) {
        SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);
        saveCourseColors(prefHelper, curriculum);
    }

    private static void saveCourseColors(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        String prefKeyDefault = PREF_KEY_COLORS_COURSE;
        String courseColorString = curriculum.config.courseColorString();
        savePreference(prefHelper, curriculum, prefKeyDefault, courseColorString);
    }

    private static void saveLabelColors(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        String prefKeyDefault = PREF_KEY_COLORS_CURRICULUM_LABEL;
        String laborColorsString = curriculum.config.labelColorsString();
        savePreference(prefHelper, curriculum, prefKeyDefault, laborColorsString);
    }

    private static void savePreference(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum, final String prefKeyDefault, final Object value) {
        String prefKeyWithIndex = curriculum.getPrefKeyWithIndex(prefKeyDefault);
        if (value == null) {
            prefHelper.remove(prefKeyWithIndex);
        } else {
            prefHelper.put(prefKeyWithIndex, value);
        }
    }

    public static void saveCourses(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        String prefKeyDefault = PREF_KEY_COURSES;
        String coursesString = curriculum.coursesString();
        savePreference(prefHelper, curriculum, prefKeyDefault, coursesString);
    }

    public static boolean loadWeekDayCourses(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        if (curriculum.getCourseCount() <= 0) {
            loadCourses(prefHelper, curriculum);
        }
        curriculum.config.curriculumSet = false;
        
        String curKeyDefault, weekdayCoursesString;
        final String defaultWeekDayCoursesString = null;
        for (WeekDayCourse weekdayCourse : curriculum.weekDayCourses) {
            // Load courses for the week day.
            curKeyDefault = weekdayCourse.prefKeyCourses;
            weekdayCoursesString = (String) loadPreference(prefHelper, curriculum, curKeyDefault,
                            defaultWeekDayCoursesString);
            if (TextUtils.isEmpty(weekdayCoursesString)) {
                continue;
            }
            weekdayCourse.parseWeekDayCourses(curriculum, weekdayCoursesString);
            if (weekdayCourse.getCourseCount() > 0) curriculum.config.curriculumSet = true;

            loadTimeOffSchool(prefHelper, curriculum, weekdayCourse.weekDayId);
        }
        if (!curriculum.config.curriculumSet) {
            if (hasDefaultCurriculum() && curriculum.curriculumIndex() == 0) {
                loadDefaultCurriculum(prefHelper, curriculum);
                saveWeekDayCourses(prefHelper, curriculum);
                curriculum.config.curriculumSet = true;
            } else {
                generateRandomDayCourses(curriculum);
            }
            // saveWeekDayCourses(prefHelper);
        }
        return curriculum.config.curriculumSet;
    }

    private static boolean hasDefaultCurriculum() {
        return sDefaultCurriculum != null && sDefaultCurriculum.length > 0;
    }

    private static void saveWeekDayCourses(final Context context, final Curriculum curriculum) {
        SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);
        saveWeekDayCourses(prefHelper, curriculum);
    }

    private static void saveWeekDayCourses(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        curriculum.config.curriculumSet = false;
        String weekdayCoursesString;
        String prefKeyWithIndex;
        for (WeekDayCourse weekdayCourse : curriculum.weekDayCourses) {
            saveTimeOffSchool(prefHelper, curriculum, weekdayCourse);
            prefKeyWithIndex = curriculum.getPrefKeyWithIndex(weekdayCourse.prefKeyCourses);
            if (weekdayCourse.getCourseCount() <= 0) {
                prefHelper.remove(prefKeyWithIndex);
            } else {
                // Save courses for the weekday.
                weekdayCoursesString = weekdayCourse.toString();
                //Log.d("WB", "saveWeekDayCourses, weekdayCourses:" + weekdayCoursesString);
                prefHelper.put(prefKeyWithIndex, weekdayCoursesString);
                curriculum.config.curriculumSet = true;
            }
        }
    }

    private static void saveDateCourses(final Context context, final Curriculum curriculum) {
        SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);
        saveDateCourses(prefHelper, curriculum);
    }

    private static void saveDateCourses(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        final String prefKeyDefault = PREF_KEY_DATE_COURSES;
        final String dateCoursesString = curriculum.getDateCoursesString();
        savePreference(prefHelper, curriculum, prefKeyDefault, dateCoursesString);
    }

    private Button mButtonSetCoursesOnDate;
    private View mViewFillCurriculumPrompt;
    private View mViewExport;

    private int mCurrentIndex;

    private View mViewCurriculumAdd;
    private View mViewCurriculumDelete;
    private View mViewCurriculumPrev;
    private View mViewCurriculumNext;

    final Curriculum.CurriculumListener mCurrirulumListener = new Curriculum.CurriculumListener() {
        @Override
        public void showSetCourseDialog(final Activity activity, final Curriculum curriculum,
                        final int weekdayId, final int courseLabelViewId,
                        final int courseLocationViewId, final int viewLabelResId) {
            switch (viewLabelResId) {
                case R.string.morning_reading:
                    final MorningEvening morningReading = curriculum.getMorningReading(weekdayId);
                    showSetMorningEveningDialog(curriculum, weekdayId, courseLabelViewId,
                                    viewLabelResId,
                                    morningReading, Extra.Morning);
                    break;
                case R.string.evening_study:
                    final MorningEvening eveningStudy = curriculum.getEveningStudy(weekdayId);
                    showSetMorningEveningDialog(curriculum, weekdayId, courseLabelViewId,
                                    viewLabelResId,
                                    eveningStudy, Extra.Evening);
                    break;
                case R.string.time_off_school:
                    showSetTimeOffSchoolDialog(activity, curriculum, weekdayId, courseLabelViewId,
                                    viewLabelResId);
                    break;
                default:
                    final Course courseInput = curriculum.getCourse(weekdayId, viewLabelResId);
                    final Course.CourseChangeListener courseChangeListener = new Course.CourseChangeListener() {
                        private void updateCourses() {
                            saveCourses(SettingsUtils.getPrefsHelper(activity), curriculum);
                        }

                        @Override
                        public void whenCourseChanged() {
                            updateCourses();

                            checkRestoreDefaultVisibility();
                            curriculum.setCourses(activity);
                            saveWeekCourses(activity, curriculum);
                        }

                        @Override
                        public void setCellCourse(Course course) {
                            setCourse(weekdayId, courseLabelViewId, courseLocationViewId, course);
                            saveWeekCourses(activity, curriculum);
                        }
                    };
                    showSetCellCourseDialog(activity, curriculum, null, weekdayId,
                                    courseLabelViewId,
                                    viewLabelResId, courseInput, courseChangeListener);
                    break;
            }
        }

        @Override
        public void showSetTimeOffSchoolDialog(final Activity activity, final Curriculum curriculum,
                        final int weekdayId, final int viewId, final int viewLabelResId) {
            final WeekDayCourse weekdayCourses = curriculum.getWeekDayCourses(weekdayId);
            final TimeOffSchool timeOffSchoolInput = curriculum.getTimeOffSchool(weekdayId);
            final TimeOffSchoolChangeListener timeOffSchoolChangeListener = new TimeOffSchoolChangeListener() {
                @Override
                public void timeOffSchoolChanged(Curriculum curriculum, int weekdayId,
                                TimeOffSchool timeOffSchool) {
                    weekdayCourses.setTimeOffSchool(timeOffSchool);
                    setTimeOffSchool(viewId, timeOffSchool);
                    saveTimeOffSchool(SettingsUtils.getPrefsHelper(activity), curriculum,
                                    weekdayCourses);
                    Constants.refreshWidget(activity);
                }

                @Override
                public void timeOffSchoolCleared(Curriculum curriculum, int weekdayId) {
                    weekdayCourses.setTimeOffSchool(null);
                    setTimeOffSchool(viewId, null);
                    saveTimeOffSchool(SettingsUtils.getPrefsHelper(activity), curriculum,
                                    weekdayCourses);
                    Constants.refreshWidget(activity);
                }
            };
            showDialogSetTimeOffSchool(activity, curriculum, null, weekdayId, viewId,
                            viewLabelResId,
                            timeOffSchoolInput, timeOffSchoolChangeListener);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_curriculum);

        loadSettings(this);

        mCurrentIndex = sCurrentCurriculumIndex;

        initViews();

        refreshViews(current().config.curriculumSet);
    }

    private Curriculum current() {
        return getCurriculum(mCurrentIndex);
    }

    private void initViews() {
        final Context context = this;
        final Curriculum current = current();

        SettingsUtils.initGeneric(this);

        initDayConfigViews(this, current);

        initSemesterDateViews(context, current);
        initLabelViews(context, current);
        initButtons(context);
        initCurriculumButtons(context);

        initColorSchemeViews(context, current);
        initCheckBox(context, current);
        initDemoViews(context, current);

        initCurriculumViews();

        refreshColorViewsShow(current);
    }

    private void initDayConfigViews(final Activity activity, final Curriculum curriculum) {
        final ClassTimeChangeListener timeChangeListener = new ClassTimeChangeListener() {
            @Override
            public void setAmClassTime(AmCourse amCourse) {
                AmCourse.saveTimePreferences(activity, curriculum);
                AmCourse.loadTimePreferences(activity, curriculum, null);
            }

            @Override
            public void setPmClassTime(PmCourse pmCourse) {
                PmCourse.saveTimePreferences(activity, curriculum);
                PmCourse.loadTimePreferences(activity, curriculum, null);
            }
        };
        AmCourse.loadTimePreferences(activity, curriculum, timeChangeListener);
        PmCourse.loadTimePreferences(activity, curriculum, timeChangeListener);
    }

    private void initCurriculumButtons(final Context context) {
        mViewCurriculumPrev = findViewById(R.id.view_curriculum_prev);
        mViewCurriculumPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final int curriculumCount = curriculumCount();
                mCurrentIndex = (mCurrentIndex - 1 + curriculumCount) % curriculumCount;
                initViews();
            }
        });

        mViewCurriculumNext = findViewById(R.id.view_curriculum_next);
        mViewCurriculumNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final int curriculumCount = curriculumCount();
                mCurrentIndex = (mCurrentIndex + 1) % curriculumCount;
                initViews();
            }
        });

        mViewCurriculumAdd = findViewById(R.id.view_add_curriculum);
        mViewCurriculumAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = getString(R.string.curriculum_add);
                String confirmPrompt = getString(R.string.prompt_add_curriculum);
                DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addCurriculum();
                    }
                };
                Utility.showConfirmDialog(context, title, confirmPrompt, positiveButtonListener);
            }
        });

        mViewCurriculumDelete = findViewById(R.id.view_delete_curriculum);
        mViewCurriculumDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title;
                if (TextUtils.isEmpty(getCurriculum(mCurrentIndex).config.label)) {
                    title = getString(R.string.curriculum_del);
                } else {
                    final String formatTitle = "%s\"%s\"";
                    title = String.format(formatTitle, getString(R.string.curriculum_del),
                                    getCurriculum(mCurrentIndex).config.label);
                }

                String confirmPrompt = getString(R.string.prompt_delete_curriculum);
                DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCurriculum();
                    }
                };
                Utility.showConfirmDialog(context, title, confirmPrompt, positiveButtonListener);
            }
        });
        refreshCurriculumViews();
    }

    private void refreshCurriculumViews() {
        final int curriculumCount = curriculumCount();
        if (mCurrentIndex > 0) {
            mViewCurriculumPrev.setVisibility(View.VISIBLE);
        } else {
            mViewCurriculumPrev.setVisibility(View.INVISIBLE);
        }

        if (mCurrentIndex < curriculumCount - 1) {
            mViewCurriculumNext.setVisibility(View.VISIBLE);
        } else {
            mViewCurriculumNext.setVisibility(View.INVISIBLE);
        }

        if (curriculumCount > 1) {
            mViewCurriculumDelete.setVisibility(View.VISIBLE);
        } else {
            mViewCurriculumDelete.setVisibility(View.INVISIBLE);
        }
    }
    
    private void initCheckBox(final Context context, final Curriculum curriculum) {
        final SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);
        final Curriculum.Config config = curriculum.config;
        final CheckBox checkBoxShowCoursesInPublicHolidays = (CheckBox) findViewById(
                R.id.checkbox_show_courses_public_holidays);
        checkBoxShowCoursesInPublicHolidays.setChecked(config.showCoursesInPublicHolidays);
        checkBoxShowCoursesInPublicHolidays.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                config.showCoursesInPublicHolidays = isChecked;
                saveShowCoursesInPublicHolidays(prefHelper, curriculum);
                Constants.refreshWidget(context);
            }
        });

        final TextView viewShowClassTimeInWidget = findViewById(
                        R.id.view_show_class_time_in_widget);
        viewShowClassTimeInWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.showClassTimeInWidget = !config.showClassTimeInWidget;
                refreshShowClassTimeInWidget(config);
                saveShowClassTimeInWidget(prefHelper, curriculum);
                Constants.refreshWidget(context);
            }
        });
        refreshShowClassTimeInWidget(config);
    }

    private void refreshShowClassTimeInWidget(final Curriculum.Config config) {
        final TextView viewShowClassTimeInWidget = findViewById(
                        R.id.view_show_class_time_in_widget);
        if (config.showClassTimeInWidget) {
            viewShowClassTimeInWidget.setText(R.string.show_class_time_in_widget);
        } else {
            viewShowClassTimeInWidget.setText(R.string.not_show_class_time_in_widget);
        }
    }

    private void addCurriculum() {
        Curriculum newOne = new Curriculum();
        newOne.setIndex(sCurriculums.size());

        sCurriculums.add(newOne);
        mCurrentIndex = newOne.curriculumIndex();
        initViews();

        saveCurriculumCountAndIndex(this);
        Constants.refreshWidget(this);
    }
    
    private static final String[] CURRICULUM_DEFAULT_PREF_KEYS = {
                    // Preferences for curriculum configuration.
                    PREF_KEY_CURRICULUM_LABEL,
                    PREF_KEY_COLORS_CURRICULUM_LABEL,
                    PREF_KEY_COLOR_SCHEME_CURRICULUM,
                    PREF_KEY_SHOW_COURSES_IN_PUBLIC_HOLIDAYS,
                    PREF_KEY_COLORS_COURSE,
                    PREF_KEY_SEMESTER,
                    // Preferences for day class configuration.
                    PREF_KEY_DAY_CLASS_AM_1_TIME_START,
                    PREF_KEY_DAY_CLASS_AM_1_TIME_END,
                    PREF_KEY_DAY_CLASS_AM_2_TIME_START,
                    PREF_KEY_DAY_CLASS_AM_2_TIME_END,
                    PREF_KEY_DAY_CLASS_AM_3_TIME_START,
                    PREF_KEY_DAY_CLASS_AM_3_TIME_END,
                    PREF_KEY_DAY_CLASS_AM_4_TIME_START,
                    PREF_KEY_DAY_CLASS_AM_4_TIME_END,
                    PREF_KEY_DAY_CLASS_PM_1_TIME_START,
                    PREF_KEY_DAY_CLASS_PM_1_TIME_END,
                    PREF_KEY_DAY_CLASS_PM_2_TIME_START,
                    PREF_KEY_DAY_CLASS_PM_2_TIME_END,
                    PREF_KEY_DAY_CLASS_PM_3_TIME_START,
                    PREF_KEY_DAY_CLASS_PM_3_TIME_END,
                    // Preference for all courses in the current curriculum.
                    PREF_KEY_COURSES,
                    // Preference for the courses in all the special dates.
                    PREF_KEY_DATE_COURSES,
                    // Preference for whether the class time is shown in widget.
                    PREF_KEY_SHOW_CLASS_TIME,
                    // Preferences for weekday courses.
                    Curriculum.PREF_KEY_COURSES_MONDAY,
                    Curriculum.PREF_KEY_COURSES_TUESDAY,
                    Curriculum.PREF_KEY_COURSES_WEDNESDAY,
                    Curriculum.PREF_KEY_COURSES_THURSDAY,
                    Curriculum.PREF_KEY_COURSES_FRIDAY,
                    Curriculum.PREF_KEY_COURSES_SATURDAY,
                    Curriculum.PREF_KEY_COURSES_SUNDAY,
                    // Preferences for time off school.
                    Curriculum.PREF_KEY_TIME_OFF_MONDAY,
                    Curriculum.PREF_KEY_TIME_OFF_TUESDAY,
                    Curriculum.PREF_KEY_TIME_OFF_WEDNESDAY,
                    Curriculum.PREF_KEY_TIME_OFF_THURSDAY,
                    Curriculum.PREF_KEY_TIME_OFF_FRIDAY,
                    Curriculum.PREF_KEY_TIME_OFF_SATURDAY,
                    Curriculum.PREF_KEY_TIME_OFF_SUNDAY,
    };
    
    private void clearCurriculumPreferences(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        final int curriculumIndex = curriculum.curriculumIndex();
        final ArrayList<String> prefKeys = new ArrayList<String>(
                        CURRICULUM_DEFAULT_PREF_KEYS.length);
        for (String prefKeyDefault : CURRICULUM_DEFAULT_PREF_KEYS) {
            prefKeys.add(curriculum.getPrefKeyWithIndex(prefKeyDefault));
        }
        prefHelper.remove(prefKeys);
        if (curriculumIndex == 0) prefHelper.remove(CURRICULUM_DEFAULT_PREF_KEYS);
    }

    private void clearCurriculumPreferences(final SharedPreferencesHelper prefHelper) {
        Map<String, ?> curriculumMap = prefHelper.getAll();
        Set<String> keySet = curriculumMap.keySet();
        String[] keys = keySet.toArray(new String[keySet.size()]);
        prefHelper.removeLike(CURRICULUM_DEFAULT_PREF_KEYS, keys);
    }

    private void saveCurriculums(final SharedPreferencesHelper prefHelper) {
        final int curriculumCount = curriculumCount();
        Curriculum curriculum;
        for (int index = 0; index < curriculumCount; index++) {
            curriculum = sCurriculums.get(index);
            curriculum.setIndex(index);
            saveCurriculum(prefHelper, curriculum);
        }
    }

    // 以前的做法保留下来，只是修改mCurrentIndex和其后的课程表。
    @SuppressWarnings("unused")
    private void deleteCurriculum(final SharedPreferencesHelper prefHelper) {
        // 1. Clear the preferences for the current and the following
        // curriculum(s).
        // Because the indexes for them have been impacted.
        Curriculum curriculum;
        for (int index = mCurrentIndex; mCurrentIndex < curriculumCount(); index++) {
            curriculum = sCurriculums.get(index);
            clearCurriculumPreferences(prefHelper, curriculum);
            curriculum.clear(prefHelper);
        }
        // 2. Clear the courses in the memory
        curriculum = current();
        curriculum.clearCourses();
        curriculum.clearDateCourses();
        // 3. remove the current curriculum from the list.
        sCurriculums.remove(mCurrentIndex);
        if (mCurrentIndex >= curriculumCount()) {
            mCurrentIndex = curriculumCount() - 1;
        }
        // The index keeps unchanged or the last one,
        // and the current curriculum has been different.
        for (int index = mCurrentIndex; index < curriculumCount(); index++) {
            curriculum = sCurriculums.get(index);
            curriculum.setIndex(index);
            saveCurriculum(prefHelper, curriculum);
        }

        sCurrentCurriculumIndex = mCurrentIndex;
        saveCurriculumCountAndIndex(prefHelper);
        Constants.refreshWidget(this);

        // Refresh the views for the current.
        initViews();
    }

    private void deleteCurriculum() {
        final SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(this);

        clearCurriculumPreferences(prefHelper);

        sCurriculums.remove(mCurrentIndex);
        if (mCurrentIndex >= curriculumCount()) {
            mCurrentIndex = curriculumCount() - 1;
        }
        saveCurriculums(prefHelper);

        if (sCurrentCurriculumIndex >= curriculumCount()) {
            sCurrentCurriculumIndex = mCurrentIndex;
        }
        saveCurriculumCountAndIndex(prefHelper);
        Constants.refreshWidget(this);

        // Refresh the views for the current.
        initViews();
    }

    private boolean refreshViews(final boolean curriculumSet) {
        if (curriculumSet) {
            mViewFillCurriculumPrompt.setVisibility(View.GONE);
            mViewExport.setVisibility(View.VISIBLE);
            mButtonSetCoursesOnDate.setVisibility(View.VISIBLE);
            return true;
        }
        mViewFillCurriculumPrompt.setVisibility(View.VISIBLE);
        mButtonSetCoursesOnDate.setVisibility(View.GONE);
        mViewExport.setVisibility(View.GONE);
        return false;
    }

    private void saveWeekCourses(final Context context, final Curriculum curriculum) {
        saveWeekDayCourses(context, curriculum);
        Constants.refreshWidget(context);
        refreshViews(curriculum.config.curriculumSet);
    }

    private void initSemesterDateViews(final Context context, final Curriculum curriculum) {
        final Curriculum.Config config = curriculum.config;

        final TextView viewSemesterStartDate = findViewById(R.id.view_start_date);
        final TextView viewSemesterEndDate = findViewById(R.id.view_end_date);

        viewSemesterStartDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Day today = Utility.getToday(Calendar.getInstance().getFirstDayOfWeek());
                final String title = context.getString(R.string.set_start_date);
                final DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                            int dayOfMonth) {
                        config.semesterStartDate = new Day(year, monthOfYear, dayOfMonth);
                        Utility.obtainWeekDay(config.semesterStartDate);
                        dateChanged(context, curriculum, viewSemesterStartDate, viewSemesterEndDate);
                    }
                };
                int year, month, day;
                if (config.semesterStartDate == null) {
                    year = today.year;
                    month = today.month;
                    day = today.day;
                } else {
                    year = config.semesterStartDate.year;
                    month = config.semesterStartDate.month;
                    day = config.semesterStartDate.day;
                }
                Utility.showDatePickerDialog(context, year, month, day, title, onDateSetListener);
            }
        });

        viewSemesterEndDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Day today = Utility.getToday(Calendar.getInstance().getFirstDayOfWeek());
                final String title = context.getString(R.string.set_end_date);
                final DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                            int dayOfMonth) {
                        config.semesterEndDate = new Day(year, monthOfYear, dayOfMonth);
                        Utility.obtainWeekDay(config.semesterEndDate);
                        dateChanged(context, curriculum, viewSemesterStartDate, viewSemesterEndDate);
                    }
                };
                int year, month, day;
                if (config.semesterEndDate == null) {
                    year = today.year;
                    month = today.month;
                    day = today.day;
                } else {
                    year = config.semesterEndDate.year;
                    month = config.semesterEndDate.month;
                    day = config.semesterEndDate.day;
                }
                Utility.showDatePickerDialog(context, year, month, day, title, onDateSetListener);
            }
        });
        dateChanged(context, curriculum, viewSemesterStartDate, viewSemesterEndDate);
    }

    private void dateChanged(final Context context, final Curriculum curriculum,
                    final TextView viewSemesterStartDate, final TextView viewSemesterEndDate) {
        final Curriculum.Config config = curriculum.config;
        final int yyyymmddFormatResId = R.string.format_yyyymmdd;
        if (config.semesterStartDate == null) {
            viewSemesterStartDate.setText(R.string.set_semester_start_date);
        } else {
            viewSemesterStartDate.setText(getString(R.string.format_semester_start,
                            config.semesterStartDate.ymd(context, yyyymmddFormatResId)));
        }
        if (config.semesterEndDate == null) {
            viewSemesterEndDate.setText(R.string.set_semester_end_date);
        } else {
            viewSemesterEndDate.setText(getString(R.string.format_semester_end,
                            config.semesterEndDate.ymd(context, yyyymmddFormatResId)));
        }
        if (config.semesterStartDate != null && config.semesterEndDate != null
                        && config.semesterEndDate.laterThan(config.semesterStartDate)) {
            saveSemesterDates(context, curriculum);
            Constants.refreshWidget(context);
        }
    }

    private TextView refreshLabelViews(final Context context, final Curriculum.Config config) {
        final TextView viewLabel = findViewById(R.id.view_curriculum_label);
        final TextView viewSetLabel = findViewById(R.id.view_set_curriculum_label);
        if (TextUtils.isEmpty(config.label)) {
            if (config.curriculumSet) {
                viewLabel.setText(R.string.prompt_set_curriculum_label);
                viewLabel.setTextColor(config.labelTextColor);
                viewLabel.setBackgroundColor(config.labelBackgroundColor);
            } else {
                viewLabel.setText(R.string.prompt_demo_fill_curriculum);
                config.labelInDefaultColors(viewLabel);
            }
            viewSetLabel.setText(R.string.set_curriculum_label);
        } else {
            viewLabel.setText(config.label);
            viewLabel.setTextColor(config.labelTextColor);
            viewLabel.setBackgroundColor(config.labelBackgroundColor);

            viewSetLabel.setText(R.string.edit_curriculum_label);
        }
        return viewSetLabel;
    }

    private void initLabelViews(final Context context, final Curriculum curriculum) {
        final TextView viewSetLabel = refreshLabelViews(context, curriculum.config);
        viewSetLabel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditLabelDialog(context, curriculum);
            }
        });
    }

    private void checkRestoreDefaultVisibility() {
        final View viewRestore = findViewById(R.id.view_restore_curriculum);
        if (current().isDefaultUsed()) {
            viewRestore.setVisibility(View.GONE);
        } else {
            viewRestore.setVisibility(View.VISIBLE);
        }
    }

    private void initButtons(final Context context) {
        final View viewReset = findViewById(R.id.view_reset_curriculum);
        viewReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetConfirmDialog(context);
            }
        });

        final View viewRestore = findViewById(R.id.view_restore_curriculum);
        viewRestore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showRestoreConfirmDialog(context);
            }
        });
        checkRestoreDefaultVisibility();

        final View viewImport = findViewById(R.id.view_import_curriculum);
        viewImport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showImportDialog(context);
            }
        });
        mViewExport = findViewById(R.id.view_export_curriculum);
        mViewExport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showExportDialog(context);
            }
        });

        mButtonSetCoursesOnDate = findViewById(R.id.button_set_date_courses);
        mButtonSetCoursesOnDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetDateCoursesDialog(context);
            }
        });

        mViewFillCurriculumPrompt = findViewById(R.id.view_prompt_fill_curriculum);
    }

    private void initColorSchemeViews(final Context context, final Curriculum curriculum) {
        final Curriculum.Config config = curriculum.config;
        final RadioGroup radioGroupColorScheme = (RadioGroup) findViewById(R.id.radiogroup_color_scheme);
        radioGroupColorScheme.setOnCheckedChangeListener(null);
        radioGroupColorScheme.check(config.colorScheme.radioResId);
        radioGroupColorScheme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);
                initColorSettings(prefHelper, curriculum, CurriculumColorScheme.getByRadio(checkedId));

                refreshColorViewsShow(curriculum);
                Constants.refreshWidget(context);
            }
        });
    }

    private void initDemoViews(final Context context, final Curriculum curriculum) {
        final Curriculum.Config config = curriculum.config;
        final View viewChangeColors = findViewById(R.id.view_change_colors);
        viewChangeColors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = context.getString(R.string.change_colors);
                final String text = context.getString(R.string.text_course);
                final ColorPickerDialog.OnColorSetListener colorSetListener = new ColorPickerDialog.OnColorSetListener() {
                    @Override
                    public void onColorSet(int textColor, int backgroundColor) {
                        config.courseColorBackground = backgroundColor;
                        config.courseColorText = textColor;
                        saveCourseColors(context, curriculum);
                        refreshColorViewsShow(curriculum);
                        Constants.refreshWidget(context);
                    }
                };
                showColorSetDialog(context, title, text, colorSetListener,
                                config.courseColorText, config.courseColorBackground);
            }
        });
    }

    private void refreshColorViewsShow(final Curriculum curriculum) {
        curriculum.refreshViews();

        final View viewColorDemos = findViewById(R.id.view_color_demos);
        switch (curriculum.config.colorScheme) {
            case SameColor:
                viewColorDemos.setVisibility(View.VISIBLE);
                break;
            case DifferentColors:
                viewColorDemos.setVisibility(View.GONE);
                return;
        }
        final TextView textViewCourse = findViewById(R.id.demo_course);
        curriculum.config.setCourseColors(textViewCourse);
    }

    private void initCurriculumViews() {
        final Activity activity = this;

        final Curriculum curriculum = current();
        curriculum.setListener(mCurrirulumListener);
        curriculum.initViews(activity);
        curriculum.setCourses(activity);
    }

    private static void showClearCourseConfirm(final Context context, final String classInfo,
            final Course course, final CourseActionListener courseActionListener) {
        final String formatCourseInfo = "%s %s";
        final String courseInfo = String.format(formatCourseInfo, classInfo, course.name);
        final String confirmPrompt = context.getString(R.string.confirm_clear_course, courseInfo);
        final DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                courseActionListener.clearCourse();
            }
        };
        Utility.showConfirmDialog(context, confirmPrompt, positiveButtonListener);
    }

    private static void showClearTimeOffSchoolConfirm(final Context context,
                    final Curriculum curriculum, final int weekdayId,
                    final TimeOffSchoolChangeListener timeOffSchoolChangeListener) {
        final String confirmPrompt = context.getString(R.string.confirm_clear_time_off_school,
                        WeekDay.getFullLabel(context, weekdayId));
        final DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                timeOffSchoolChangeListener.timeOffSchoolCleared(curriculum, weekdayId);
            }
        };
        Utility.showConfirmDialog(context, confirmPrompt, positiveButtonListener);
    }

    private void setCourse(final int weekdayId, final int courseLabelViewId,
                    final int courseLocationViewId, final Course course) {
        final TextView viewCourseLabel = findViewById(courseLabelViewId);
        final TextView viewCourseLocation;
        if (courseLocationViewId == -1) {
            viewCourseLocation = null;
        } else {
            viewCourseLocation = findViewById(courseLocationViewId);
        }
        if (Course.isNullCourse(course)) {
            noCourse(viewCourseLabel, viewCourseLocation);
            return;
        }
        viewCourseLabel.setText(course.name);
        viewCourseLabel.setTextColor(course.getColorText());
        viewCourseLabel.setBackgroundColor(course.getColorBackground());
        if (viewCourseLocation != null) {
            final String locationInfo = course.locationInfo();
            if (TextUtils.isEmpty(locationInfo)) {
                viewCourseLocation.setVisibility(View.GONE);
            } else {
                viewCourseLocation.setVisibility(View.VISIBLE);
                viewCourseLocation.setText(locationInfo);
            }
        }
    }

    public void setTimeOffSchool(final int viewId, final TimeOffSchool timeOffSchool) {
        final TextView textView = findViewById(viewId);
        if (timeOffSchool == null) {
            textView.setText(R.string.set_time_off_school);
            textView.setTextColor(Color.BLACK);
            textView.setBackgroundColor(Color.GRAY);
            return;
        }
        textView.setText(timeOffSchool.timeOffSchool);
        textView.setTextColor(timeOffSchool.colorText);
        textView.setBackgroundColor(timeOffSchool.colorBackground);
    }

    private void noCourse(final TextView viewCourseLabel, final TextView viewCourseLocation) {
        viewCourseLabel.setText(R.string.set_course);
        viewCourseLabel.setTextColor(Color.BLACK);
        viewCourseLabel.setBackgroundColor(Color.GRAY);

        if (viewCourseLocation != null) {
            viewCourseLocation.setVisibility(View.GONE);
        }
    }

    private void setMorningEvening(final Context context, final int courseLabelViewId,
            final MorningEvening morningEvening) {
        final TextView view = findViewById(courseLabelViewId);
        if (morningEvening == null || Course.isNullCourse(morningEvening.course)) {
            noCourse(view, null);
        } else {
            view.setText(morningEvening.shortInfo(context));
            view.setTextColor(morningEvening.getTextColor());
            view.setBackgroundColor(morningEvening.getBackgroundColor());
        }
    }

    private void showResetConfirmDialog(final Context context) {
        final String confirmPrompt = context.getString(R.string.confirm_clear_curriculum);
        final DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Curriculum curriculum = current();
                resetCurriculum(SettingsUtils.getPrefsHelper(context), curriculum, false);
                Constants.refreshWidget(context);
            }
        };
        Utility.showConfirmDialog(context, confirmPrompt, positiveButtonListener);
    }

    private void resetCurriculum(final SharedPreferencesHelper prefsHelper,
                    final Curriculum curriculum, final boolean isRestoreDefault) {
        curriculum.clear(prefsHelper);
        if (isRestoreDefault) {
            curriculum.copyDefaultCourses();
        }
        if (refreshViews(curriculum.config.curriculumSet)) {
            curriculum.refreshWeekDayCourses();
        }
        if (isRestoreDefault) {
            saveCourses(prefsHelper, curriculum);
        }
        saveWeekDayCourses(prefsHelper, curriculum);
    }

    private void showRestoreConfirmDialog(final Context context) {
        final Curriculum curriculum = getCurriculum(mCurrentIndex);
        final String confirmPrompt = context.getString(R.string.confirm_restore_default_courses);
        final DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferencesHelper prefsHelper = SettingsUtils.getPrefsHelper(context);

                resetCurriculum(prefsHelper, curriculum, true);
                Constants.refreshWidget(context);

                checkRestoreDefaultVisibility();
            }
        };
        Utility.showConfirmDialog(context, confirmPrompt, positiveButtonListener);
    }

    private void showImportDialog(final Context context) {
        Utility.showInfoDialog(context, "Sorry! Not implemented!");
    }

    private void showExportDialog(final Context context) {
        Utility.showInfoDialog(context, "Sorry! Not implemented!");
    }

    private void showSetMorningEveningDialog(final Curriculum curriculum, final int weekdayId,
                    final int viewId, final int viewLabelResId, final MorningEvening morningEvening,
                    final Extra extra) {
        final Context context = this;
        final int viewLayoutId, radioGroupId;
        final int noMorningEveningRadioId, noMorningEveningId, noCourseId;
        switch (extra) {
            case Morning:
                viewLayoutId = R.layout.view_curriculum_set_morning_reading;
                radioGroupId = R.id.radiogroup_morning_reading;
                noMorningEveningRadioId = R.id.radio_no_morning_reading;
                noMorningEveningId = R.string.no_morning_reading;
                noCourseId = R.string.morning_no_course;
                break;
            case Evening:
                viewLayoutId = R.layout.view_curriculum_set_evening_study;
                radioGroupId = R.id.radiogroup_evening_study;
                noMorningEveningRadioId = R.id.radio_no_evening_study;
                noMorningEveningId = R.string.no_evening_study;
                noCourseId = R.string.evening_no_course;
                break;
            default:
                return;
        }
        final String formatTitle = "%s %s";
        final String title = String.format(formatTitle, WeekDay.getFullLabel(context, weekdayId),
                        context.getString(viewLabelResId));

        final String positiveLabel = context.getString(android.R.string.ok);
        final String negativeLabel = context.getString(android.R.string.cancel);

        final Utility.ViewInitWithPositiveNegative viewInit = new Utility.ViewInitWithPositiveNegative() {
            private TextView mmTextViewInfo;
            private RadioGroup mmRadioGroupMorningEvening;

            private View mmViewHeader;
            private ListView mmListViewCourses;
            private CourseListAdapter mmCourseListAdapter;

            private Course mSelectedCourse;

            private final CourseSelectedListener mmCourseSelectedListener = new CourseSelectedListener() {
                @Override
                public void courseSelected(int index) {
                    mSelectedCourse = (Course) mmCourseListAdapter.getItem(index);
                    showShortInfo();
                    mmCourseListAdapter.setSelected(index);
                }
            };

            @Override
            public void initViews(View rootView) {
                mSelectedCourse = morningEvening == null ? null : morningEvening.course;

                mmTextViewInfo = rootView.findViewById(R.id.view_info);
                mmRadioGroupMorningEvening = rootView.findViewById(radioGroupId);
                final RadioGroup.OnCheckedChangeListener radioCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        radioChecked(checkedId);
                    }
                };
                mmRadioGroupMorningEvening.setOnCheckedChangeListener(radioCheckedChangeListener);

                mmCourseListAdapter = new CourseListAdapter(context, getCurriculum(mCurrentIndex),
                                mmCourseSelectedListener, Course.Type.Major, Course.Type.Reading);

                mmViewHeader = rootView.findViewById(R.id.view_header);
                mmListViewCourses = rootView.findViewById(R.id.list_courses);
                mmListViewCourses.setAdapter(mmCourseListAdapter);

                initViewContents();
            }

            private void initViewContents() {
                int checkedRadioId;
                if (morningEvening == null) {
                    checkedRadioId = noMorningEveningRadioId;
                } else {
                    mmTextViewInfo.setText(morningEvening.shortInfo(context));
                    if (morningEvening.course == null) {
                        checkedRadioId = R.id.radio_no_course;
                    } else {
                        checkedRadioId = R.id.radio_course_select;
                    }
                }
                mmRadioGroupMorningEvening.check(checkedRadioId);
                radioChecked(checkedRadioId);
            }

            private void radioChecked(final int checkedRadioId) {
                //mmListViewCourses.setVisibility(View.GONE);
                mmViewHeader.setVisibility(View.GONE);
                switch (checkedRadioId) {
                    case R.id.radio_no_course:
                        mmTextViewInfo.setText(noCourseId);
                        break;
                    case R.id.radio_course_select:
                        //mmListViewCourses.setVisibility(View.VISIBLE);
                        mmViewHeader.setVisibility(View.VISIBLE);
                        showShortInfo();
                        break;
                    default:
                        if (checkedRadioId == noMorningEveningRadioId) {
                            mmTextViewInfo.setText(noMorningEveningId);
                        }
                        break;
                }
            }

            private void showShortInfo() {
                String shortInfo = MorningEvening.shortInfo(context, extra, mSelectedCourse);
                mmTextViewInfo.setText(shortInfo);
            }

            @Override
            public void onPositiveClick(View rootView) {
                int checkedRadioId = mmRadioGroupMorningEvening.getCheckedRadioButtonId();
                MorningEvening mmMorningEvening;
                switch (checkedRadioId) {
                    case R.id.radio_no_course:
                        if (morningEvening == null) {
                            mmMorningEvening = new MorningEvening(extra, null);
                        } else {
                            mmMorningEvening = morningEvening;
                            mmMorningEvening.course = null;
                        }
                        break;
                    case R.id.radio_course_select:
                        if (morningEvening == null) {
                            mmMorningEvening = new MorningEvening(extra, mSelectedCourse);
                        } else {
                            mmMorningEvening = morningEvening;
                            mmMorningEvening.course = mSelectedCourse;
                        }
                        break;
                    default:
                        if (checkedRadioId == noMorningEveningRadioId) {
                            mmMorningEvening = null;
                            break;
                        }
                        return;
                }
                setValue(mmMorningEvening);
            }

            private void setValue(final MorningEvening mmMorningEvening) {
                boolean result = curriculum.setMorningEvening(weekdayId, extra, mmMorningEvening);
                if (result) {
                    setMorningEvening(context, viewId, mmMorningEvening);
                    saveWeekCourses(context, curriculum);
                }
            }

            @Override
            public void onNegativeClick(View rootView) {
                // ...
            }
        };
        Utility.showViewDialog(context, viewLayoutId, title, viewInit, positiveLabel,
                negativeLabel);
    }

    private static void showDialogClassTime(final Context context,
                    final Curriculum curriculum, final /* PmCourse */Object classAmPm,
                    final ClassTimeChangeListener timeChangeListener) {
        final String classTitle;
        final AmCourse amClass;
        final PmCourse pmClass;
        if (classAmPm instanceof AmCourse) {
            amClass = (AmCourse) classAmPm;
            pmClass = null;
            classTitle = context.getString(amClass.labelResId);
        } else if (classAmPm instanceof PmCourse) {
            amClass = null;
            pmClass = (PmCourse) classAmPm;
            classTitle = context.getString(pmClass.labelResId);
        } else {
            return;
        }
        final String title = classTitle + context.getString(R.string.class_time);
        final String positiveLabel = context.getString(android.R.string.ok);
        final String negativeLabel = context.getString(android.R.string.cancel);
        final Utility.ViewInitWithPositiveNegative viewInit = new Utility.ViewInitWithPositiveNegative() {
            private TextView mmViewClassTime;

            private View mmViewTimeStart, mmViewTimeEnd;

            private int mmTimeStartHour, mmTimeStartMinute;
            private int mmTimeEndHour, mmTimeEndMinute;

            private int hourOfTime(final String timeString) {
                String strHour = timeString.split(":")[0];
                return Integer.parseInt(strHour);
            }

            private int minuteOfTime(final String timeString) {
                String strMinute = timeString.split(":")[1];
                return Integer.parseInt(strMinute);
            }

            @Override
            public void initViews(View rootView) {
                if (amClass != null) {
                    mmTimeStartHour = hourOfTime(amClass.timeStart);
                    mmTimeStartMinute = minuteOfTime(amClass.timeStart);
                    mmTimeEndHour = hourOfTime(amClass.timeEnd);
                    mmTimeEndMinute = minuteOfTime(amClass.timeEnd);
                } else if (pmClass != null) {
                    mmTimeStartHour = hourOfTime(pmClass.timeStart);
                    mmTimeStartMinute = minuteOfTime(pmClass.timeStart);
                    mmTimeEndHour = hourOfTime(pmClass.timeEnd);
                    mmTimeEndMinute = minuteOfTime(pmClass.timeEnd);
                }
                
                mmViewClassTime = rootView.findViewById(R.id.view_class_time);

                mmViewTimeStart = rootView.findViewById(R.id.view_time_start);
                final TimePickerDialog.OnTimeSetListener timeStartListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mmTimeStartHour = hourOfDay;
                        mmTimeStartMinute = minute;
                        refreshTime();
                    }
                };
                mmViewTimeStart.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String titleTimeStart = classTitle + context.getString(
                                        R.string.class_time_start);
                        Utility.showTimePickerDialog(context, titleTimeStart, timeStartListener,
                                        mmTimeStartHour, mmTimeStartMinute);
                    }
                });

                mmViewTimeEnd = rootView.findViewById(R.id.view_time_end);
                final TimePickerDialog.OnTimeSetListener timeEndListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mmTimeEndHour = hourOfDay;
                        mmTimeEndMinute = minute;
                        refreshTime();
                    }
                };
                mmViewTimeEnd.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String titleTimeEnd = classTitle + context.getString(
                                        R.string.class_time_end);
                        Utility.showTimePickerDialog(context, titleTimeEnd, timeEndListener,
                                        mmTimeEndHour, mmTimeEndMinute);
                    }
                });

                refreshTime();
            }

            private void refreshTime() {
                final String formatTime = "%02d:%02d ~ %02d:%02d";
                final String timeString = String.format(formatTime, mmTimeStartHour,
                                mmTimeStartMinute, mmTimeEndHour, mmTimeEndMinute);
                mmViewClassTime.setText(timeString);
            }

            @Override
            public void onPositiveClick(View rootView) {
                final String formatTime = "%02d:%02d";
                final String timeStart = String.format(formatTime, mmTimeStartHour, mmTimeStartMinute);
                final String timeEnd = String.format(formatTime, mmTimeEndHour, mmTimeEndMinute);
                if (amClass != null) {
                    amClass.timeStart = timeStart;
                    amClass.timeEnd = timeEnd;
                    timeChangeListener.setAmClassTime(amClass);
                } else if (pmClass != null) {
                    pmClass.timeStart = timeStart;
                    pmClass.timeEnd = timeEnd;
                    timeChangeListener.setPmClassTime(pmClass);
                }
            }

            @Override
            public void onNegativeClick(View rootView) {
                // ...
            }
        };
        Utility.showViewDialog(context, R.layout.view_curriculum_set_class_time, title, viewInit,
                        positiveLabel, negativeLabel);
    }

    private static void showDialogSetTimeOffSchool(final Context context,
                    final Curriculum curriculum,
                    final Curriculum.DayCourseViewInfo dayCourseViewInfo, final int weekdayId,
                    final int viewId, final int viewLabelResId,
                    final TimeOffSchool timeOffSchoolInput,
                    final TimeOffSchoolChangeListener timeOffSchoolChangeListener) {
        String title = context.getString(R.string.set_time_off_school);
        if (weekdayId >= 0) {
            final String formatTitle = "%s %s";
            title = String.format(formatTitle, title, WeekDay.getFullLabel(context, weekdayId));
        }

        final String positiveLabel = context.getString(android.R.string.ok);
        final String negativeLabel = context.getString(android.R.string.cancel);
        final String neutralLabel;
        if (timeOffSchoolInput == null) {
            neutralLabel = null;
        } else {
            neutralLabel = context.getString(R.string.clear_time_off_school);
        }

        final Utility.ViewInitWith3Buttons viewInit = new Utility.ViewInitWith3Buttons() {
            private TimeOffSchool mmTimeOffSchool;

            private TextView mmTextViewTimeOffSchool;
            private View mmViewSetTime;
            private View mmViewSetColors;

            @Override
            public void initViews(View rootView) {
                if (timeOffSchoolInput == null) {
                    mmTimeOffSchool = null;
                } else {
                    mmTimeOffSchool = TimeOffSchool.copy(timeOffSchoolInput);
                    mmTimeOffSchool.parseHourMinute();
                }

                mmTextViewTimeOffSchool = rootView.findViewById(R.id.view_time_off_school);

                final TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (mmTimeOffSchool == null) {
                            mmTimeOffSchool = new TimeOffSchool();
                        }
                        mmTimeOffSchool.setTime(hourOfDay, minute);
                        refreshTimeOffSchool();
                    }
                };

                mmViewSetTime = rootView.findViewById(R.id.view_time);
                mmViewSetTime.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int hour_of_day = mmTimeOffSchool == null ? -1 : mmTimeOffSchool.getHourOfDay();
                        final int minute = mmTimeOffSchool == null ? -1 : mmTimeOffSchool.getMinute();
                        final String title = context.getString(R.string.prompt_input_time);
                        Utility.showTimePickerDialog(context, title, timeSetListener,
                                        hour_of_day, minute);
                    }
                });

                mmViewSetColors = rootView.findViewById(R.id.view_colors);
                mmViewSetColors.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String title = context.getString(R.string.change_colors);
                        final String text = mmTimeOffSchool.timeOffSchoolString();
                        final ColorPickerDialog.OnColorSetListener colorSetListener = new ColorPickerDialog.OnColorSetListener() {
                            @Override
                            public void onColorSet(int textColor, int backgroundColor) {
                                mmTimeOffSchool.colorBackground = backgroundColor;
                                mmTimeOffSchool.colorText = textColor;
                                refreshTimeOffSchool();
                            }
                        };
                        showColorSetDialog(context, title, text, colorSetListener,
                                        mmTimeOffSchool.colorText, mmTimeOffSchool.colorBackground);
                    }
                });

                refreshTimeOffSchool();
            }

            private void refreshTimeOffSchool() {
                if (mmTimeOffSchool == null) {
                    mmTextViewTimeOffSchool.setText(null);

                    mmViewSetColors.setVisibility(View.INVISIBLE);
                } else {
                    mmTextViewTimeOffSchool.setText(mmTimeOffSchool.timeOffSchoolString());
                    mmTextViewTimeOffSchool.setTextColor(mmTimeOffSchool.colorText);
                    mmTextViewTimeOffSchool.setBackgroundColor(mmTimeOffSchool.colorBackground);

                    mmViewSetColors.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPositiveClick(View rootView) {
                if (dayCourseViewInfo == null && timeOffSchoolChangeListener != null) {
                    curriculum.setTimeOffSchool(weekdayId, mmTimeOffSchool);
                    timeOffSchoolChangeListener.timeOffSchoolChanged(curriculum, weekdayId,
                                    mmTimeOffSchool);
                } else {
                    dayCourseViewInfo.setTimeOffSchool(mmTimeOffSchool);
                }
            }

            @Override
            public void onNeutralClick(View rootView) {
                showClearTimeOffSchoolConfirm(context, curriculum, weekdayId,
                                timeOffSchoolChangeListener);
            }

            @Override
            public void onNegativeClick(View rootView) {
                // Nothing now.
            }
        };
        Utility.showViewDialog(context, R.layout.view_curriculum_set_time_off_school, title,
                        viewInit, positiveLabel, neutralLabel, negativeLabel);
    }

    private static void showSetCellCourseDialog(final Context context, final Curriculum curriculum,
                    final Curriculum.DayCourseViewInfo dayCourseViewInfo, final int weekdayId,
                    final int viewId, final int viewLabelResId, final Course courseInput,
                    final Course.CourseChangeListener courseChangeListener) {
        final String title;
        if (dayCourseViewInfo == null && weekdayId >= 0) {
            final String formatTitle = "%s %s";
            title = String.format(formatTitle, WeekDay.getFullLabel(context, weekdayId),
                    context.getString(viewLabelResId));
        } else {
            title = context.getString(viewLabelResId);
        }

        final String positiveLabel = context.getString(android.R.string.ok);
        final String negativeLabel = context.getString(android.R.string.cancel);
        final String neutralLabel;
        if (Course.isNullCourse(courseInput)) {
            neutralLabel = null;
        } else {
            neutralLabel = context.getString(R.string.clear_course);
        }

        final Utility.ViewInitWith3Buttons viewInit = new Utility.ViewInitWith3Buttons() {
            private TextView mmTextViewCourseInfo;
            private TextView mmTextViewTotalInfo;

            private ListView mmCourseListViewCourses;
            private CourseListAdapter mmCourseListAdapter;

            private Course mmCourse;

            private View mmViewAddCourse;

            private final CourseActionListener mmCourseActionListener = new CourseActionListener() {
                @Override
                public void onCourseAdded() {
                    if (courseChangeListener != null) {
                        courseChangeListener.whenCourseChanged();
                    }
                    mmCourseListAdapter.dataChanged();
                }

                @Override
                public void onCourseDeleted(final Course courseDeleted) {
                    if (courseChangeListener != null) {
                        courseChangeListener.whenCourseChanged();
                    }
                    if (courseDeleted.equals(mmCourse)) {
                        mmCourse = null;
                        updateInfo();
                    }
                    mmCourseListAdapter.dataChanged();
                }

                @Override
                public void onCourseChanged() {
                    if (courseChangeListener != null) {
                        courseChangeListener.whenCourseChanged();
                    }
                    updateInfo();
                    mmCourseListAdapter.dataChanged();
                }

                @Override
                public void clearCourse() {
                    mmCourse = Course.NULL;
                    updateInfo();
                    setCellCourse();
                }

                @Override
                public void courseSelected(int index) {
                    mmCourse = (Course) mmCourseListAdapter.getItem(index);
                    mmCourseListAdapter.setSelected(index);
                    updateInfo();
                }
            };

            private void setCellCourse() {
                if (dayCourseViewInfo == null && courseChangeListener != null) {
                    curriculum.setDayCourse(weekdayId, viewLabelResId, mmCourse);
                    courseChangeListener.setCellCourse(mmCourse);
                    Constants.refreshWidget(context);
                } else {
                    final Curriculum.Config config = curriculum.config;
                    dayCourseViewInfo.setCourseByLabel(viewLabelResId, mmCourse);
                    dayCourseViewInfo.refreshViews(config.colorScheme, config.courseColorText,
                                    config.courseColorBackground);
                }
            }

            @Override
            public void initViews(View rootView) {
                mmCourse = courseInput;

                mmTextViewCourseInfo = rootView.findViewById(R.id.view_course_info);
                mmTextViewTotalInfo = rootView.findViewById(R.id.view_total_info);

                mmCourseListViewCourses = rootView.findViewById(R.id.list_courses);
                mmCourseListAdapter = new CourseListAdapter(context, curriculum,
                                mmCourseActionListener);
                mmCourseListViewCourses.setAdapter(mmCourseListAdapter);

                initButtons(rootView);

                if (courseInput != null) {
                    int selectedIndex = mmCourseListAdapter.getItemIndex(courseInput);
                    mmCourseListAdapter.setSelected(selectedIndex);
                }
                updateInfo();
            }

            private void updateInfo() {
                final String info;
                if (Course.isNullCourse(mmCourse)) {
                    info = context.getString(R.string.info_no_course_set);
                } else {
                    int courseOccurrence = curriculum.getCourseCountInWeek(mmCourse);
                    if (courseOccurrence > 0) {
                        String strOccurrence = context.getString(R.string.format_course_count_in_a_week,
                                courseOccurrence);
                        final String format_info = "%s(%s)";
                        info = String.format(format_info, mmCourse.name, strOccurrence);
                    } else {
                        info = mmCourse.name;
                    }
                }
                mmTextViewCourseInfo.setText(info);

                final int courseCount = curriculum.getCourseCount();
                final String infoCourseCount = context.getString(R.string.info_course_count,
                                courseCount);
                mmTextViewTotalInfo.setText(infoCourseCount);
            }

            private void initButtons(final View rootView) {
                mmViewAddCourse = rootView.findViewById(R.id.button_add_course);
                mmViewAddCourse.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditCourseDialog(context, curriculum, null, mmCourseActionListener);
                    }
                });
            }

            @Override
            public void onPositiveClick(View rootView) {
                if (mmCourse != null) {
                    setCellCourse();
                }
            }

            @Override
            public void onNegativeClick(View rootView) {
                // ...
            }

            @Override
            public void onNeutralClick(View rootView) {
                showClearCourseConfirm(context, title, courseInput, mmCourseActionListener);
            }
        };
        Utility.showViewDialog(context, R.layout.view_curriculum_set_course, title, viewInit,
                positiveLabel, neutralLabel, negativeLabel);
    }

    private void showSetDateCoursesDialog(final Context context) {
        final Curriculum curriculum = getCurriculum(mCurrentIndex);
        final String title = context.getString(R.string.set_course_by_date);

        final Utility.ViewInit viewInit = new Utility.ViewInit() {
            private TextView mmTextViewInfo;

            private ListView mmListViewDates;

            private View mmViewClear;
            private View mmViewAddDate;

            private DateCoursesListAdapter mmDateCoursesListAdapter;

            private DateCoursesChangeListener mmDateCoursesChangeListener = new DateCoursesChangeListener() {
                @Override
                public void onDateCoursesChanged() {
                    mmDateCoursesListAdapter.notifyDataSetChanged();
                    saveDateCourses(context, curriculum);
                    if (curriculum.refreshWeekDayCourses()) {
                        Constants.refreshWidget(context);
                    }
                    updateInfo();
                }
            };

            @Override
            public void initViews(View rootView) {
                mmDateCoursesListAdapter = new DateCoursesListAdapter(context, curriculum,
                        mmDateCoursesChangeListener);

                mmTextViewInfo = rootView.findViewById(R.id.view_info);

                mmListViewDates = rootView.findViewById(R.id.list_date_courses);
                mmListViewDates.setAdapter(mmDateCoursesListAdapter);

                mmViewClear = rootView.findViewById(R.id.button_clear_dates);
                mmViewClear.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String title = context.getString(R.string.title_delete_date_courses);
                        String confirmPrompt = context.getString(R.string.prompt_clear_dates_courses);
                        DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                curriculum.clearDateCourses();
                                mmDateCoursesChangeListener.onDateCoursesChanged();
                            }
                        };
                        Utility.showConfirmDialog(context, title, confirmPrompt,
                                positiveButtonListener);
                    }
                });

                mmViewAddDate = rootView.findViewById(R.id.button_add_date);
                mmViewAddDate.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditDateCoursesDialog(context, curriculum, null,
                                        mmDateCoursesChangeListener);
                    }
                });

                updateInfo();
            }

            private void updateInfo() {
                int dateCount = mmListViewDates.getCount();
                String info;
                if (dateCount <= 0) {
                    info = context.getString(R.string.prompt_no_date_courses_set);
                    mmViewClear.setVisibility(View.GONE);
                } else {
                    info = context.getString(R.string.format_n_date_courses_set, dateCount);
                    mmViewClear.setVisibility(View.VISIBLE);
                }
                mmTextViewInfo.setText(info);
            }
        };
        Utility.showViewDialog(context, R.layout.view_curriculum_date_courses, title, viewInit,
                null, null);
    }

    private static final Curriculum.DayCoursesListener sDayCoursesListener = new Curriculum.DayCoursesListener() {
        @Override
        public void showSetCourseDialog(final Context context, final Curriculum curriculum,
                        DayCourseViewInfo dayCourseViewInfo, int viewId, int viewLabelResId,
                        Course courseInput) {
            showSetCellCourseDialog(context, curriculum, dayCourseViewInfo, -1, viewId,
                            viewLabelResId, courseInput, null);
        }

        @Override
        public void showSetTimeOffSchoolDialog(final Context context, final Curriculum curriculum,
                        DayCourseViewInfo dayCourseViewInfo, int viewId, int viewLabelResId,
                        final TimeOffSchool timeOffSchool) {
            final TimeOffSchoolChangeListener timeOffSchoolChangeListener = null;
            showDialogSetTimeOffSchool(context, curriculum, dayCourseViewInfo, -1, viewId,
                            viewLabelResId, timeOffSchool, timeOffSchoolChangeListener);
        }
    };

    /**
     *
     * @param context
     * @param dateCoursesInput null for add new, Not null for edit existing.
     * @param dateCoursesChangeListener
     */
    private static void showEditDateCoursesDialog(final Context context,
                    final Curriculum curriculum, final DateCourses dateCoursesInput,
                    final DateCoursesChangeListener dateCoursesChangeListener) {
        final Day today = Utility.getToday(Calendar.MONDAY);
        final ArrayList<Day> workDays;
        if (dateCoursesInput == null) {
            workDays = SettingsCalendarActivity.getWorkDays(context, today);
        } else {
            workDays = null;
        }
        final String title = context.getString(R.string.set_course_by_date);
        final String positiveLabel = context.getString(android.R.string.ok);
        final String negativeLabel = context.getString(android.R.string.cancel);

        final int defaultCourseSetRadioId = R.id.radio_same_as_weekday;
        final Utility.ViewInitWithPositiveNegative viewInit = new Utility.ViewInitWithPositiveNegative() {
            private ListView mmListViewDates;

            private final OnItemClickListener mmDateItemClickListener = new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int which,
                        long arg3) {
                    daySelected(which);
                }
            };

            private TextView mmViewDateSpecified;

            private RadioGroup mmRadioGroupCoursesSet;

            private View mmViewCoursesSet;
            private ListView mmListViewWeekDays;
            private View mmViewDateCourses;

            private DateCourses mmDateCourses;

            private WeekDay[] mmWeekDayArray;

            private Curriculum.DayCourseViewInfo mmDayCourseViewInfo;

            private void initDayCourseViews(final View rootView) {
                if (dateCoursesInput == null) {
                    mmDayCourseViewInfo = new Curriculum.DayCourseViewInfo(null);
                } else {
                    mmDayCourseViewInfo = new Curriculum.DayCourseViewInfo(
                            dateCoursesInput.dayCourse);
                }

                mmDayCourseViewInfo.initViews(context, curriculum, -1, rootView,
                                sDayCoursesListener);
            }

            @Override
            public void initViews(View rootView) {
                initDayCourseViews(rootView);

                mmListViewDates = rootView.findViewById(R.id.list_dates);

                mmViewDateSpecified = rootView.findViewById(R.id.view_date_specified);
                if (dateCoursesInput == null) {
                    mmViewDateSpecified.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                        int dayOfMonth) {
                                    Day day = new Day(year, monthOfYear, dayOfMonth);
                                    Utility.obtainWeekDay(day);

                                    mmDateCourses = new DateCourses(day);
                                    otherDateSpecified();
                                }
                            };
                            Utility.showDatePickerDialog(context, today.year, today.month, today.day,
                                    title, onDateSetListener);
                        }
                    });
                } else {
                    mmDateCourses = dateCoursesInput;
                    otherDateSpecified();
                }

                mmRadioGroupCoursesSet = rootView.findViewById(R.id.radiogroup_set_date_courses);

                mmViewCoursesSet = rootView.findViewById(R.id.view_courses_set);
                mmListViewWeekDays = rootView.findViewById(R.id.list_week_days);
                mmViewDateCourses = rootView.findViewById(R.id.view_day_courses);

                getComingWorkDays();

                initRadioGroup();
            }

            private void initRadioGroup() {
                final int radioId;
                if (mmDateCourses == null || mmDateCourses.weekdayId_equals > 0
                        || mmDateCourses.dayCourse == null) {
                    radioId = defaultCourseSetRadioId;
                } else {
                    radioId = R.id.radio_specify_courses;
                }
                mmRadioGroupCoursesSet.check(radioId);
                radioChecked(radioId);

                final RadioGroup.OnCheckedChangeListener radioCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        radioChecked(checkedId);
                    }
                };
                mmRadioGroupCoursesSet.setOnCheckedChangeListener(radioCheckedChangeListener);
            }

            private void getComingWorkDays() {
                if (workDays == null || workDays.size() <= 0) {
                    mmListViewDates.setVisibility(View.GONE);
                    mmViewDateSpecified.setVisibility(View.VISIBLE);
                    daySelected(-1);
                } else {
                    mmListViewDates.setVisibility(View.VISIBLE);
                    mmViewDateSpecified.setVisibility(View.INVISIBLE);

                    final String[] workDayStrings = constructDateList(workDays);
                    mmListViewDates.setAdapter(new ArrayAdapter<String>(context,
                            android.R.layout.simple_list_item_single_choice, workDayStrings));
                    mmListViewDates.setOnItemClickListener(mmDateItemClickListener);
                    mmListViewDates.setItemChecked(0, true);
                    daySelected(0);
                }
            }

            private void daySelected(final int which) {
                final int workDayCount = workDays == null ? 0 : workDays.size();
                if (workDayCount > 0 && which >= 0 && which <= workDayCount - 1) {
                    mmDateCourses = new DateCourses(workDays.get(which));
                    mmViewDateSpecified.setVisibility(View.INVISIBLE);
                } else {
                    mmDateCourses = null;
                    mmViewDateSpecified.setVisibility(View.VISIBLE);
                }
                updateWeekDayListView(which);
            }

            private String[] constructDateList(final ArrayList<Day> workDays) {
                String[] array = new String[workDays.size() + 1];
                for (int i = 0; i < array.length - 1; i++) {
                    array[i] = workDays.get(i).ymdw_chinese(context);
                }
                array[array.length - 1] = context.getString(R.string.set_course_by_date);
                return array;
            }

            private void otherDateSpecified() {
                mmViewDateSpecified.setText(mmDateCourses.date.ymdw_chinese(context));
            }

            private void radioChecked(final int checkedRadioId) {
                switch (checkedRadioId) {
                    case R.id.radio_same_as_weekday:
                        mmListViewWeekDays.setVisibility(View.VISIBLE);
                        mmViewDateCourses.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.radio_specify_courses:
                        mmListViewWeekDays.setVisibility(View.INVISIBLE);
                        mmViewDateCourses.setVisibility(View.VISIBLE);
                        break;
                    default:
                        return;
                }
            }

            private String[] constructWeekDayList(final int excludedWeekDay) {
                ArrayList<WeekDay> weekDays = new ArrayList<WeekDay>(6);
                for (WeekDayCourse weekDayCourse : curriculum.weekDayCourses) {
                    if (weekDayCourse.weekDayId == excludedWeekDay) continue;
                    if (weekDayCourse.getCourseCount(null) <= 0) continue;
                    weekDays.add(WeekDay.getWeekDayByCalendarId(weekDayCourse.weekDayId));
                }
                mmWeekDayArray = weekDays.toArray(new WeekDay[weekDays.size()]);
                String[] array = new String[mmWeekDayArray.length];
                int weekDayId;
                for (int i = 0; i < array.length; i++) {
                    weekDayId = weekDays.get(i).calendarId;
                    array[i] = WeekDay.getFullLabel(context, weekDayId);
                }
                return array;
            }

            private void updateWeekDayListView(final int selectedDateIndex) {
                radioChecked(mmRadioGroupCoursesSet.getCheckedRadioButtonId());

                if (mmDateCourses == null) {
                    mmListViewWeekDays.setAdapter(null);
                    mmListViewWeekDays.setVisibility(View.GONE);
                    return;
                }
                mmRadioGroupCoursesSet.setVisibility(View.VISIBLE);
                mmViewCoursesSet.setVisibility(View.VISIBLE);

                final int excludedWeekDay = mmDateCourses == null ? -1 : mmDateCourses.date.weekday;
                final String[] weekDayStrings = constructWeekDayList(excludedWeekDay);
                mmListViewWeekDays.setAdapter(new ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_single_choice, weekDayStrings));
                mmListViewWeekDays.setItemChecked(getCheckedWeekId(), true);
            }

            private int getCheckedWeekId() {
                if (mmDateCourses == null) return 0;
                for (int i = 0; i < mmWeekDayArray.length; i++) {
                    if (mmWeekDayArray[i].calendarId == mmDateCourses.weekdayId_equals) {
                        return i;
                    }
                }
                return 0;
            }

            @Override
            public void onPositiveClick(View rootView) {
                if (mmDateCourses == null) {
                    Utility.showInfoDialog(context, R.string.prompt_no_date_set);
                    return;
                }
                final int checkedRadioId = mmRadioGroupCoursesSet.getCheckedRadioButtonId();
                switch (checkedRadioId) {
                    case R.id.radio_same_as_weekday:
                        int checkedWeekDayIndex = mmListViewWeekDays.getCheckedItemPosition();
                        mmDateCourses.weekdayId_equals = mmWeekDayArray[checkedWeekDayIndex].calendarId;
                        break;
                    case R.id.radio_specify_courses:
                        fillDayCourses(mmDateCourses.dayCourse);
                        break;
                    default:
                        return;
                }
                checkDateCoursesAdd(context, curriculum, mmDateCourses, dateCoursesChangeListener);
            }

            private void fillDayCourses(final DayCourses dayCourses) {
                // ...
            }

            @Override
            public void onNegativeClick(View rootView) {
                // ..
            }
        };
        Utility.showViewDialog(context, R.layout.view_curriculum_add_date_courses, title, viewInit,
                positiveLabel, negativeLabel);
    }

    private static void checkDateCoursesAdd(final Context context, final Curriculum curriculum,
                    final DateCourses dateCourses,
                    final DateCoursesChangeListener dateCoursesChangeListener) {
        if (curriculum.dateExists(dateCourses)) {
            showConfirmDateCoursesOverwritting(context, curriculum, dateCourses,
                            dateCoursesChangeListener);
        } else {
            curriculum.addDateCourses(context, dateCourses, dateCoursesChangeListener);
        }
    }

    private static void showConfirmDateCoursesOverwritting(final Context context,
                    final Curriculum curriculum, final DateCourses newDateCourses,
                    final DateCoursesChangeListener dateCoursesChangeListener) {
        String title = context.getString(R.string.title_date_overwrite);
        String confirmPrompt = context.getString(R.string.prompt_date_overwrite);
        DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                curriculum.removeDateCourses(newDateCourses.date);
                curriculum.addDateCourses(context, newDateCourses, dateCoursesChangeListener);
            }
        };

        Utility.showConfirmDialog(context, title, confirmPrompt, positiveButtonListener);
    }

    private void showEditLabelDialog(final Context context, final Curriculum curriculum) {
        final Curriculum.Config config = curriculum.config;
        final String title;
        if (TextUtils.isEmpty(config.label)) {
            title = context.getString(R.string.set_curriculum_label);
        } else {
            title = context.getString(R.string.edit_curriculum_label);
        }
        final String positiveLabel = context.getString(android.R.string.ok);
        final String negativeLabel = context.getString(android.R.string.cancel);
        final Utility.ViewInitWithPositiveNegative viewInit = new Utility.ViewInitWithPositiveNegative() {
            private EditText editTextLabel;
            private TextView viewLabel;
            private View viewChangeColors;

            private int mColorText, mColorBackground;

            @Override
            public void initViews(View rootView) {
                mColorText = config.labelTextColor;
                mColorBackground = config.labelBackgroundColor;

                editTextLabel = rootView.findViewById(R.id.edit_curriculum_label);
                editTextLabel.setText(config.label);

                viewLabel = rootView.findViewById(R.id.demo_label);
                viewLabel.setText(config.label);

                viewChangeColors = rootView.findViewById(R.id.view_change_colors);
                viewChangeColors.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = Utility.getEditText(editTextLabel);
                        final ColorPickerDialog.OnColorSetListener colorSetListener = new ColorPickerDialog.OnColorSetListener() {
                            @Override
                            public void onColorSet(int textColor, int backgroundColor) {
                                mColorBackground = backgroundColor;
                                mColorText = textColor;
                                refreshViewLabel();
                            }
                        };
                        showColorSetDialog(context, title, text, colorSetListener, mColorText,
                                mColorBackground);
                    }
                });

                refreshViewLabel();
            }

            private void refreshViewLabel() {
                viewLabel.setText(Utility.getEditText(editTextLabel));
                viewLabel.setTextColor(mColorText);
                viewLabel.setBackgroundColor(mColorBackground);
            }

            @Override
            public void onPositiveClick(View rootView) {
                final SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);
                config.label = Utility.getEditText(editTextLabel);
                saveLabel(prefHelper, curriculum);

                config.labelBackgroundColor = mColorBackground;
                config.labelTextColor = mColorText;
                saveLabelColors(prefHelper, curriculum);

                refreshLabelViews(context, config);

                Constants.refreshWidget(context);
            }

            @Override
            public void onNegativeClick(View rootView) {
                // ...
            }
        };
        Utility.showViewDialog(context, R.layout.view_curriculum_label, title, viewInit,
                positiveLabel, negativeLabel);
    }

    private static void showColorSetDialog(final Context context, final String title,
            final String text, ColorPickerDialog.OnColorSetListener colorSetListener,
            final int textColor, final int backgroundColor) {
        ColorPickerDialog dialog = new ColorPickerDialog(context, colorSetListener, text,
                textColor, backgroundColor);
        dialog.setTitle(title);
        dialog.show();
    }

    private static void showEditCourseDialog(final Context context, final Curriculum curriculum,
                    final Course course, final CourseActionListener courseActionListener) {
        final String title;
        if (course == null) {
            title = context.getString(R.string.add_course);
        } else {
            final String formatEditCourse = "%s %s";
            title = String.format(formatEditCourse, context.getString(R.string.edit_course),
                    course.name);
        }
        final Utility.ViewInitWithPositiveNegative viewInit = new Utility.ViewInitWithPositiveNegative() {
            private EditText mmEditTextName;
            private RadioGroup mmRadioGroupType;

            private EditText mmEditTextTeacher;
            private CheckBox mmCheckBoxTeacherVisible;
            private EditText mmEditTextLocation;
            private CheckBox mmCheckBoxLocationVisible;
            private TextView mmTextViewDemo;
            private View mmViewChangeColors;

            private int mmColorText, mmColorBackground;

            @Override
            public void initViews(View rootView) {
                mmEditTextName = rootView.findViewById(R.id.edit_course_name);

                mmRadioGroupType = rootView.findViewById(R.id.radiogroup_course_type);
                if (course == null) {
                    mmRadioGroupType.check(Course.Type.Other.radioResId);
                } else {
                    mmRadioGroupType.check(course.type.radioResId);
                }

                mmEditTextTeacher = rootView.findViewById(R.id.edit_course_teacher);
                mmCheckBoxTeacherVisible = rootView.findViewById(R.id.checkbox_teacher_visible);
                mmEditTextLocation = rootView.findViewById(R.id.edit_course_location);
                mmCheckBoxLocationVisible = rootView.findViewById(R.id.checkbox_location_visible);
                mmTextViewDemo = rootView.findViewById(R.id.demo_course);

                mmViewChangeColors = rootView.findViewById(R.id.view_change_colors);
                mmViewChangeColors.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String text = Utility.getTextViewText(mmTextViewDemo);
                        final ColorPickerDialog.OnColorSetListener colorSetListener = new ColorPickerDialog.OnColorSetListener() {
                            @Override
                            public void onColorSet(int textColor, int backgroundColor) {
                                mmColorText = textColor;
                                mmColorBackground = backgroundColor;
                                refreshDemoView();
                                courseActionListener.onCourseChanged();
                            }
                        };
                        showColorSetDialog(context, title, text, colorSetListener, mmColorText,
                                mmColorBackground);
                    }
                });
                initViewContent();
            }

            private void initViewContent() {
                if (course == null) {
                    mmColorText = Course.DEFAULT_COLOR_COURSE_TEXT;
                    mmColorBackground = Course.DEFAULT_COLOR_COURSE_BACKGROUND;
                    mmEditTextName.setEnabled(true);
                    mmRadioGroupType.check(R.id.radio_course_major);
                } else {
                    mmColorText = course.getColorText();
                    mmColorBackground = course.getColorBackground();

                    mmEditTextName.setText(course.name);
                    mmEditTextName.setEnabled(false);
                    mmRadioGroupType.check(course.type.radioResId);
                    mmEditTextTeacher.setText(course.teacher);
                    mmCheckBoxTeacherVisible.setChecked(course.teacherVisible);
                    mmEditTextLocation.setText(course.location);
                    mmCheckBoxLocationVisible.setChecked(course.locationVisible);
                }
                refreshDemoView();
            }

            private void refreshDemoView() {
                final String textCourse = Utility.getEditText(mmEditTextName);
                if (TextUtils.isEmpty(textCourse)) {
                    mmTextViewDemo.setText(R.string.text_course);
                } else {
                    mmTextViewDemo.setText(textCourse);
                }
                mmTextViewDemo.setTextColor(mmColorText);
                mmTextViewDemo.setBackgroundColor(mmColorBackground);
            }

            @Override
            public void onPositiveClick(View rootView) {
                String newName = Utility.getEditText(mmEditTextName);
                if (TextUtils.isEmpty(newName)) {
                    return;
                }
                Course.Type courseType = Course.Type
                        .getFromRadio(mmRadioGroupType.getCheckedRadioButtonId());
                String newTeacher = Utility.getEditText(mmEditTextTeacher);
                String newLocation = Utility.getEditText(mmEditTextLocation);

                Course newCourse;
                if (course == null) {
                    newCourse = new Course(newName, courseType);
                } else {
                    newCourse = course;
                    newCourse.type = courseType;
                }
                newCourse.teacher = newTeacher;
                newCourse.teacherVisible = mmCheckBoxTeacherVisible.isChecked();
                newCourse.location = newLocation;
                newCourse.locationVisible = mmCheckBoxLocationVisible.isChecked();
                newCourse.setColorText(mmColorText);
                newCourse.setColorBackground(mmColorBackground);

                if (course == null) {
                    Course.FailedCause failedCause = curriculum.addCourse(newCourse);
                    if (failedCause == null) {
                        courseActionListener.onCourseAdded();
                    } else {
                        final String failedCauseString = context.getString(failedCause.causeResId);
                        final String prompt = context.getString(R.string.failed_add_course,
                                        newCourse, failedCauseString);
                        Utility.showInfoDialog(context, prompt);
                    }
                } else {
                    courseActionListener.onCourseChanged();
                }
            }

            @Override
            public void onNegativeClick(View rootView) {
                // ...
            }
        };
        Utility.showViewDialog(context, title, R.layout.edit_course, viewInit);
    }

    private synchronized static void loadSettings(final Context context) {
        if (sSettingsLoaded) {
            return;
        }

        final SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);

        SettingsUtils.loadGeneric(prefHelper);

        loadCurriculums(prefHelper);

        sSettingsLoaded = true;
    }

    public synchronized static void loadCurriculums(final SharedPreferencesHelper prefHelper) {
        SettingsUtils.loadGeneric(prefHelper);

        if (!loadCurriculumCountAndIndex(prefHelper)) return;
        final int curriculumCount = curriculumCount();

        Curriculum curriculum;
        for (int index = 0; index < curriculumCount; index++) {
            curriculum = getCurriculum(index);
            curriculum.setIndex(index);

            loadLabel(prefHelper, curriculum);
            loadLabelColors(prefHelper, curriculum);
            loadColorScheme(prefHelper, curriculum);
            loadShowCoursesInPublicHolidays(prefHelper, curriculum);
            loadCourseColors(prefHelper, curriculum);
            loadSemesterDates(prefHelper, curriculum);
            loadCourses(prefHelper, curriculum);
            loadWeekDayCourses(prefHelper, curriculum);
            loadDateCourses(prefHelper, curriculum);
            loadShowClassTimeInWidget(prefHelper, curriculum);
        }
    }

    private static void saveCurriculum(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        saveLabel(prefHelper, curriculum);
        saveLabelColors(prefHelper, curriculum);
        saveColorScheme(prefHelper, curriculum);
        saveCourseColors(prefHelper, curriculum);
        saveSemesterDates(prefHelper, curriculum);
        saveCourses(prefHelper, curriculum);
        if (curriculum.config.curriculumSet) {
            saveWeekDayCourses(prefHelper, curriculum);
            saveDateCourses(prefHelper, curriculum);
        }
    }

    private static final MorningEvening sMorningReadingChinese = new MorningEvening(Extra.Morning,
            Course.sChinese);
    private static final MorningEvening sMorningReadingEnglish = new MorningEvening(Extra.Morning,
            Course.sEnglish);

    private static final String EMPTY = "";
    private static final String SPACE = " ";

    private interface ClassTimeChangeListener {
        public void setAmClassTime(AmCourse amCourse);

        public void setPmClassTime(PmCourse pmCourse);
    }

    // 上午最多4节课.
    private static enum AmCourse {
        Am1(R.id.day_am_1_label, R.string.morning_course_1, R.id.day_am_1_time,
                        PREF_KEY_DAY_CLASS_AM_1_TIME_START, "8:00",
                        PREF_KEY_DAY_CLASS_AM_1_TIME_END, "8:40"),
        Am2(R.id.day_am_2_label, R.string.morning_course_2, R.id.day_am_2_time,
                        PREF_KEY_DAY_CLASS_AM_2_TIME_START, "8:50",
                        PREF_KEY_DAY_CLASS_AM_2_TIME_END, "9:30"),
        Am3(R.id.day_am_3_label, R.string.morning_course_3, R.id.day_am_3_time,
                        PREF_KEY_DAY_CLASS_AM_3_TIME_START, "10:10",
                        PREF_KEY_DAY_CLASS_AM_3_TIME_END, "10:55"),
        Am4(R.id.day_am_4_label, R.string.morning_course_4, R.id.day_am_4_time,
                        PREF_KEY_DAY_CLASS_AM_4_TIME_START, "11:05",
                        PREF_KEY_DAY_CLASS_AM_4_TIME_END, "11:45");

        public final int labelViewId;
        public final int labelResId;
        public final int timeViewId;
        public final String prefKeyTimeStart;
        public final String prefKeyTimeEnd;

        public final String timeStartDefault, timeEndDefault;

        public String timeStart, timeEnd;

        private AmCourse(int labelViewId, int labelResId, int timeViewId, String prefKeyTimeStart,
                        String timeStart, String prefKeyTimeEnd, String timeEnd) {
            this.labelViewId = labelViewId;
            this.labelResId = labelResId;
            this.timeViewId = timeViewId;
            this.prefKeyTimeStart = prefKeyTimeStart;
            this.prefKeyTimeEnd = prefKeyTimeEnd;
            this.timeStartDefault = this.timeStart = timeStart;
            this.timeEndDefault = this.timeEnd = timeEnd;
        }

        private static final String FORMAT_TIME = "%s-%s";

        public String timeString() {
            return String.format(FORMAT_TIME, timeStart, timeEnd);
        }

        public static void loadTimePreferences(final Activity activity,
                        final Curriculum curriculum,
                        final ClassTimeChangeListener timeChangeListener) {
            final SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(activity);
            TextView timeView;
            for (final AmCourse amCourse : values()) {
                amCourse.timeStart = (String) loadPreference(prefHelper, curriculum,
                                amCourse.prefKeyTimeStart, amCourse.timeStartDefault);
                amCourse.timeEnd = (String) loadPreference(prefHelper, curriculum,
                                amCourse.prefKeyTimeEnd, amCourse.timeEndDefault);
                timeView = activity.findViewById(amCourse.timeViewId);
                timeView.setText(amCourse.timeString());
                if (timeChangeListener != null) {
                    timeView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDialogClassTime(activity, curriculum, amCourse, timeChangeListener);
                        }
                    });
                }
            }
        }

        public static void saveTimePreferences(final Activity activity,
                        final Curriculum curriculum) {
            final SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(activity);
            for (AmCourse amCourse : values()) {
                savePreference(prefHelper, curriculum, amCourse.prefKeyTimeStart,
                                amCourse.timeStart);
                savePreference(prefHelper, curriculum, amCourse.prefKeyTimeEnd,
                                amCourse.timeEnd);
            }
        }

        public static AmCourse getFromLabel(final int labelResId) {
            for (AmCourse amCourse : values()) {
                if (amCourse.labelResId == labelResId) return amCourse;
            }
            return null;
        }

        public static int count() {
            return values().length;
        }
    }

    // 下午最多三节课.
    private static enum PmCourse {
        Pm1(R.id.day_pm_1_label, R.string.afternoon_course_1, R.id.day_pm_1_time,
                        PREF_KEY_DAY_CLASS_PM_1_TIME_START, "13:25",
                        PREF_KEY_DAY_CLASS_PM_1_TIME_END, "14:10"),
        Pm2(R.id.day_pm_2_label, R.string.afternoon_course_2, R.id.day_pm_2_time,
                        PREF_KEY_DAY_CLASS_PM_2_TIME_START, "14:20",
                        PREF_KEY_DAY_CLASS_PM_2_TIME_END, "15:00"),
        Pm3(R.id.day_pm_3_label, R.string.afternoon_course_3, R.id.day_pm_3_time,
                        PREF_KEY_DAY_CLASS_PM_3_TIME_START, "15:10",
                        PREF_KEY_DAY_CLASS_PM_3_TIME_END, "15:50"),
        Pm4(R.id.day_pm_4_label, R.string.afternoon_course_4, R.id.day_pm_4_time,
                        PREF_KEY_DAY_CLASS_PM_4_TIME_START, "16:00",
                        PREF_KEY_DAY_CLASS_PM_4_TIME_END, "16:30");

        public final int labelViewId;
        public final int labelResId;
        public final int timeViewId;

        public final String prefKeyTimeStart;
        public final String prefKeyTimeEnd;

        public final String timeStartDefault, timeEndDefault;

        public String timeStart, timeEnd;

        private PmCourse(int labelViewId, int labelResId, int timeViewId, String prefKeyTimeStart,
                        String timeStart, String prefKeyTimeEnd, String timeEnd) {
            this.labelViewId = labelViewId;
            this.labelResId = labelResId;
            this.timeViewId = timeViewId;
            this.prefKeyTimeStart = prefKeyTimeStart;
            this.prefKeyTimeEnd = prefKeyTimeEnd;
            this.timeStartDefault = this.timeStart = timeStart;
            this.timeEndDefault = this.timeEnd = timeEnd;
        }

        private static final String FORMAT_TIME = "%s-%s";

        public String timeString() {
            return String.format(FORMAT_TIME, timeStart, timeEnd);
        }

        public static void loadTimePreferences(final Activity activity,
                        final Curriculum curriculum,
                        final ClassTimeChangeListener timeChangeListener) {
            final SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(activity);
            TextView timeView;
            for (final PmCourse pmCourse : values()) {
                pmCourse.timeStart = (String) loadPreference(prefHelper, curriculum,
                                pmCourse.prefKeyTimeStart, pmCourse.timeStartDefault);
                pmCourse.timeEnd = (String) loadPreference(prefHelper, curriculum,
                                pmCourse.prefKeyTimeEnd, pmCourse.timeEndDefault);
                timeView = activity.findViewById(pmCourse.timeViewId);
                timeView.setText(pmCourse.timeString());
                if (timeChangeListener != null) {
                    timeView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDialogClassTime(activity, curriculum, pmCourse, timeChangeListener);
                        }
                    });
                }
            }
        }

        public static void saveTimePreferences(final Activity activity,
                        final Curriculum curriculum) {
            final SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(activity);
            for (PmCourse pmCourse : values()) {
                savePreference(prefHelper, curriculum, pmCourse.prefKeyTimeStart,
                                pmCourse.timeStart);
                savePreference(prefHelper, curriculum, pmCourse.prefKeyTimeEnd,
                                pmCourse.timeEnd);
            }
        }

        public static PmCourse getFromLabel(final int labelResId) {
            for (PmCourse pmCourse : values()) {
                if (pmCourse.labelResId == labelResId) return pmCourse;
            }
            return null;
        }

        public static int count() {
            return values().length;
        }
    }

    public static void loadAmClassTime(final RemoteViews updateView) {
        for (final AmCourse amCourse : AmCourse.values()) {
            updateView.setTextViewText(amCourse.labelViewId, amCourse.timeStart);
            updateView.setTextColor(amCourse.labelViewId, Color.WHITE);
            updateView.setTextViewText(amCourse.timeViewId, amCourse.timeEnd);
        }
    }

    public static void loadPmClassTime(final RemoteViews updateView) {
        for (final PmCourse pmClass : PmCourse.values()) {
            updateView.setTextViewText(pmClass.labelViewId, pmClass.timeStart);
            updateView.setTextColor(pmClass.labelViewId, Color.YELLOW);
            updateView.setTextViewText(pmClass.timeViewId, pmClass.timeEnd);
        }
    }

    private static Curriculum getCurriculum(final int index) {
        return sCurriculums.get(index);
    }

    public static Curriculum getCurriculum() {
        return sCurriculums.get(sCurrentCurriculumIndex);
    }

    public static Curriculum.Config getConfig() {
        return getCurriculum().config;
    }

    public static class DayCourses {
        public MorningEvening morningReading, eveningStudy;
        public final ArrayList<Course> morningCourses;
        public final ArrayList<Course> afternoonCourses;

        public TimeOffSchool timeOffSchool;

        private static final String SEP_COURSE = "·";

        public DayCourses() {
            morningCourses = initMorningCourses(null);
            afternoonCourses = initAfternoonCourses(null);
        }

        public void init() {
            morningReading = null;
            eveningStudy = null;
            initMorningCourses(morningCourses);
            initAfternoonCourses(afternoonCourses);
            timeOffSchool = null;
        }

        public void copyDayCourses(final DayCourses dayCoursesInput) {
            eveningStudy = dayCoursesInput.eveningStudy;
            morningReading = dayCoursesInput.morningReading;

            int i = 0;
            initMorningCourses(morningCourses);
            for (Course course : dayCoursesInput.morningCourses) {
                morningCourses.set(i++, course);
            }

            i = 0;
            initAfternoonCourses(afternoonCourses);
            for (Course course : dayCoursesInput.afternoonCourses) {
                afternoonCourses.set(i++, course);
            }

            timeOffSchool = dayCoursesInput.timeOffSchool;
        }

        public int getMorningReadingTextColor() {
            if (morningReading == null) {
                return getConfig().courseColorText;
            }
            return morningReading.getTextColor();
        }

        public int getMorningReadingBackgroundColor() {
            if (morningReading == null) {
                return getConfig().courseColorBackground;
            }
            return morningReading.getBackgroundColor();
        }

        public int getEveningStudyTextColor() {
            if (eveningStudy == null) {
                return getConfig().courseColorText;
            }
            return eveningStudy.getTextColor();
        }

        public int getEveningStudyBackgroundColor() {
            if (eveningStudy == null) {
                return getConfig().courseColorBackground;
            }
            return eveningStudy.getBackgroundColor();
        }

        private static ArrayList<Course> initMorningCourses(ArrayList<Course> morningCourses) {
            AmCourse[] courses = AmCourse.values();
            if (morningCourses == null) {
                morningCourses = new ArrayList<Course>(courses.length);
                for (int i = 0; i < courses.length; i++) {
                    morningCourses.add(Course.NULL);
                }
            } else {
                for (int i = 0; i < courses.length; i++) {
                    morningCourses.set(i, Course.NULL);
                }
            }
            return morningCourses;
        }

        private static ArrayList<Course> initAfternoonCourses(ArrayList<Course> afternoonCourses) {
            PmCourse[] courses = PmCourse.values();
            if (afternoonCourses == null) {
                afternoonCourses = new ArrayList<Course>(courses.length);
                for (int i = 0; i < courses.length; i++) {
                    afternoonCourses.add(Course.NULL);
                }
            } else {
                for (int i = 0; i < courses.length; i++) {
                    afternoonCourses.set(i, Course.NULL);
                }
            }
            return afternoonCourses;
        }

        public String shortInfo() {
            StringBuilder sb = new StringBuilder();
            if (morningReading != null) {
                sb.append(morningReading.toString().substring(0, 1));
            }
            for (Course course : morningCourses) {
                if (sb.length() > 0) sb.append(SEP_COURSE);
                if (Course.isNullCourse(course)) continue;
                sb.append(course.name.substring(0, 1));
            }
            sb.append('+');
            for (Course course : afternoonCourses) {
                sb.append(SEP_COURSE);
                if (Course.isNullCourse(course)) continue;
                sb.append(course.name.substring(0, 1));
            }
            if (eveningStudy != null) {
                sb.append(SEP_COURSE);
                sb.append(eveningStudy.toString().substring(0, 1));
            }
            return sb.toString();
        }

        private static String coursesString(final ArrayList<Course> courses) {
            final int count = (courses == null ? 0 : courses.size());
            if (count <= 0) return EMPTY;

            int realCount = 0;
            Course course;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++) {
                course = courses.get(i);
                if (!Course.isNullCourse(course)) {
                    realCount++;
                    sb.append(course.name);
                }
                if (i < count - 1) sb.append(SEP_COURSE);
            }
            if (realCount > 0) {
                return sb.toString();
            }
            return EMPTY;
        }

        private static void parseCourses(final Curriculum curriculum, final String input,
                        final ArrayList<Course> courses) {
            if (TextUtils.isEmpty(input)) return;
            String[] elements = input.split(SEP_COURSE);

            for (int i = 0; i < elements.length; i++) {
                if (TextUtils.isEmpty(elements[i])) continue;
                courses.set(i, curriculum.getCourseByName(elements[i]));
            }
        }

        public boolean setCourseByLabel(final int labelResId, Course course) {
            if (course == null) course = Course.NULL;

            AmCourse amCourse = AmCourse.getFromLabel(labelResId);
            if (amCourse == null) {
                PmCourse pmCourse = PmCourse.getFromLabel(labelResId);
                if (pmCourse != null) {
                    afternoonCourses.set(pmCourse.ordinal(), course);
                    return true;
                }
                return false;
            }
            morningCourses.set(amCourse.ordinal(), course);
            return true;
        }

        public Course getCourseByLabel(final int labelResId) {
            AmCourse amCourse = AmCourse.getFromLabel(labelResId);
            if (amCourse == null) {
                PmCourse pmCourse = PmCourse.getFromLabel(labelResId);
                if (pmCourse == null) return Course.NULL;
                return afternoonCourses.get(pmCourse.ordinal());
            }
            return morningCourses.get(amCourse.ordinal());
        }

        public int getCourseCount() {
            int count = 0;
            for (Course course : morningCourses) {
                if (!Course.isNullCourse(course)) {
                    count++;
                }
            }
            for (Course course : afternoonCourses) {
                if (!Course.isNullCourse(course)) {
                    count++;
                }
            }
            return count;
        }

        public int getCourseCount(final Course courseInput) {
            if (Course.isNullCourse(courseInput)) return 0;
            int count = 0;
            for (Course course : morningCourses) {
                if (Course.equals(course, courseInput)) {
                    count++;
                }
            }
            for (Course course : afternoonCourses) {
                if (Course.equals(course, courseInput)) {
                    count++;
                }
            }
            return count;
        }

        public boolean deleteCourse(final Course course) {
            if (morningReading != null) {
                if (morningReading.isCourse(course)) {
                    morningReading.course = null;
                }
            }
            if (eveningStudy != null) {
                if (eveningStudy.isCourse(course)) {
                    eveningStudy.course = null;
                }
            }
            int amCount = morningCourses.size();
            for (int i = 0; i < amCount; i++) {
                if (course.equals(morningCourses.get(i))) {
                    morningCourses.set(i, null);
                }
            }

            int pmCount = afternoonCourses.size();
            for (int i = 0; i < pmCount; i++) {
                if (course.equals(afternoonCourses.get(i))) {
                    afternoonCourses.set(i, null);
                }
            }
            return true;
        }

        private static final String LEFT = "[", RIGHT = "]";
        private static final String SEP_ = "@";
        // Format: [morningReading,MorningCourses,AfternoonCourses,eveningStudy]
        private static final String FORMAT_TO_STRING = LEFT + "%1$s" + SEP_ + "%2$s" + SEP_ + "%3$s"
                + SEP_ + "%4$s" + RIGHT;

        @Override
        public String toString() {
            String strMorningReading = morningReading == null ? SPACE : morningReading.toString();
            String strMorningCourses = coursesString(morningCourses);
            String strAfternoonCourses = coursesString(afternoonCourses);
            String strEveningStudy = eveningStudy == null ? SPACE : eveningStudy.toString();
            return String.format(FORMAT_TO_STRING, strMorningReading, strMorningCourses,
                    strAfternoonCourses, strEveningStudy);
        }

        public static DayCourses parseDayCourses(final Curriculum curriculum,
                        final String dayCoursesString, DayCourses dayCourses) {
            if (TextUtils.isEmpty(dayCoursesString)) return null;

            int leftIndex = dayCoursesString.indexOf(LEFT);
            if (leftIndex < 0) return null;

            final int startIndex = leftIndex + 1;
            int endIndex = dayCoursesString.indexOf(RIGHT);
            if (endIndex <= startIndex) return null;

            String[] elements = dayCoursesString.substring(startIndex, endIndex).split(SEP_);
            if (dayCourses == null) {
                dayCourses = new DayCourses();
            } else {
                dayCourses.init();
            }

            if (elements.length > 0) {
                dayCourses.morningReading = MorningEvening.parse(curriculum, elements[0]);
            }
            if (elements.length > 1) {
                parseCourses(curriculum, elements[1], dayCourses.morningCourses);
            }
            if (elements.length > 2) {
                parseCourses(curriculum, elements[2], dayCourses.afternoonCourses);
            }
            if (elements.length > 3) {
                dayCourses.eveningStudy = MorningEvening.parse(curriculum, elements[3]);
            }

            return dayCourses;
        }
    }

    private interface TimeOffSchoolChangeListener {
        public void timeOffSchoolChanged(final Curriculum curriculum, final int weekdayId,
                        final TimeOffSchool timeOffSchool);

        public void timeOffSchoolCleared(final Curriculum curriculum, final int weekdayId);
    }

    public static class WeekDayCourse {
        public final int weekDayId;
        public final int viewResId;
        
        public final String prefKeyCourses;
        public final String prefKeyTimeOffSchool;

        public final DayCourses dayCourses = new DayCourses();

        public DayCourses dateCourses;

        public Day date;

        public WeekDayCourse(final int weekDayId, final int viewResId, final String prefKey,
                        final String prefKeyTimeOffSchool) {
            this.weekDayId = weekDayId;
            this.viewResId = viewResId;
            this.prefKeyCourses = prefKey;
            this.prefKeyTimeOffSchool = prefKeyTimeOffSchool;
        }
        
        public boolean hasEveningStudy() {
            if (dateCourses == null) {
                return dayCourses.eveningStudy == null ? false : true;
            }
            return dateCourses.eveningStudy == null ? false : true;
        }

        public TimeOffSchool getTimeOffSchool(final boolean forWidget) {
            if (forWidget) {
                if (dateCourses == null) return dayCourses.timeOffSchool;
                return dateCourses.timeOffSchool;
            }
            return dayCourses.timeOffSchool;
        }

        public void setTimeOffSchool(final TimeOffSchool timeOffSchool) {
            dayCourses.timeOffSchool = timeOffSchool;
        }
        
        public void setDateTimeOffSchool(final String timeOffSchool) {
            if (dateCourses == null) return;
            if (dateCourses.timeOffSchool == null) {
                dateCourses.timeOffSchool = new TimeOffSchool();
            }
            dateCourses.timeOffSchool.timeOffSchool = timeOffSchool;
        }

        public void setMorningReading(MorningEvening morningReading) {
            dayCourses.morningReading = morningReading;
        }

        public void setMorningReading(Course course) {
            if (dayCourses.morningReading == null) {
                dayCourses.morningReading = new MorningEvening(Extra.Morning, course);
            } else {
                dayCourses.morningReading.course = course;
            }
        }

        public void setEveningStudy(MorningEvening eveningStudy) {
            dayCourses.eveningStudy = eveningStudy;
        }

        public void setEveningStudy(Course course) {
            if (dayCourses.eveningStudy == null) {
                dayCourses.eveningStudy = new MorningEvening(Extra.Evening, course);
            } else {
                dayCourses.eveningStudy.course = course;
            }
        }

        // 返回值表示是否有dateCourses的change.
        public boolean setDateCourses(final DayCourses dayCoursesInput) {
            if (dayCoursesInput == null) {
                if (dateCourses == null) {
                    return false;
                }
                if (dateCourses.getCourseCount(null) > 0) {
                    dateCourses.init();
                    return true;
                }
                return false;
            }
            if (dateCourses == null) {
                dateCourses = new DayCourses();
            } else {
                dateCourses.init();
            }
            dateCourses.copyDayCourses(dayCoursesInput);
            return true;
        }

        public ArrayList<Course> getMorningCourses(final boolean forWidget) {
            if (forWidget && getConfig().curriculumSet) {
                if (dateCourses == null) {
                    return getMorningCourses(dayCourses);
                }
                return getMorningCourses(dateCourses);
            }
            return getMorningCourses(dayCourses);
        }

        private static ArrayList<Course> getMorningCourses(final DayCourses dayCoursesInput) {
            return dayCoursesInput == null ? null : dayCoursesInput.morningCourses;
        }

        public ArrayList<Course> getAfternoonCourses(final boolean forWidget) {
            if (forWidget && getConfig().curriculumSet) {
                if (dateCourses == null) {
                    return getAfternoonCourses(dayCourses);
                }
                return getAfternoonCourses(dateCourses);
            }
            return getAfternoonCourses(dayCourses);
        }

        private static ArrayList<Course> getAfternoonCourses(final DayCourses dayCoursesInput) {
            return dayCoursesInput == null ? null : dayCoursesInput.afternoonCourses;
        }

        public String getMorningReadingShortInfo(final Context context, final boolean forWidget) {
            if (forWidget && getConfig().curriculumSet) {
                if (dateCourses == null) {
                    return getMorningReadingShortInfo(context, dayCourses);
                }
                return getMorningReadingShortInfo(context, dateCourses);
            }
            return getMorningReadingShortInfo(context, dayCourses);
        }

        private static String getMorningReadingShortInfo(final Context context,
                final DayCourses dayCoursesInput) {
            if (dayCoursesInput == null || dayCoursesInput.morningReading == null) return null;
            return dayCoursesInput.morningReading.shortInfo(context);
        }

        public int getMorningReadingTextColor(final boolean forWidget) {
            if (forWidget && getConfig().curriculumSet) {
                if (dateCourses == null) {
                    return dayCourses.getMorningReadingTextColor();
                }
                return dateCourses.getMorningReadingTextColor();
            }
            return dayCourses.getMorningReadingTextColor();
        }

        public int getMorningReadingBackgroundColor(final boolean forWidget) {
            if (forWidget && getConfig().curriculumSet) {
                if (dateCourses == null) {
                    return dayCourses.getMorningReadingBackgroundColor();
                }
                return dateCourses.getMorningReadingBackgroundColor();
            }
            return dayCourses.getMorningReadingBackgroundColor();
        }

        public String getEveningStudyShortInfo(final Context context, final boolean forWidget) {
            if (forWidget && getConfig().curriculumSet) {
                if (dateCourses == null) {
                    return getEveningStudyShortInfo(context, dayCourses);
                }
                return getEveningStudyShortInfo(context, dateCourses);
            }
            return getEveningStudyShortInfo(context, dayCourses);
        }

        private static String getEveningStudyShortInfo(final Context context,
                final DayCourses dayCoursesInput) {
            if (dayCoursesInput == null || dayCoursesInput.eveningStudy == null) return null;
            return dayCoursesInput.eveningStudy.shortInfo(context);
        }

        public int getCourseCount(final Course course) {
            if (dayCourses == null) return 0;
            if (Course.isNullCourse(course)) return dayCourses.getCourseCount();
            return dayCourses.getCourseCount(course);
        }
        
        public int getCourseCount() {
            if (dayCourses == null) return 0;
            return dayCourses.getCourseCount();
        }

        public int getEveningStudyTextColor(final boolean forWidget) {
            if (forWidget && getConfig().curriculumSet) {
                if (dateCourses == null) {
                    return dayCourses.getEveningStudyTextColor();
                }
                return dateCourses.getEveningStudyTextColor();
            }
            return dayCourses.getEveningStudyTextColor();
        }

        public int getEveningStudyBackgroundColor(final boolean forWidget) {
            if (forWidget && getConfig().curriculumSet) {
                if (dateCourses == null) {
                    return dayCourses.getEveningStudyBackgroundColor();
                }
                return dateCourses.getEveningStudyBackgroundColor();
            }
            return dayCourses.getEveningStudyBackgroundColor();
        }

        public void setMorningCourse(final int index, final Course course) {
            dayCourses.morningCourses.set(index, course);
        }

        public void setAfternoonCourse(final int index, final Course course) {
            dayCourses.afternoonCourses.set(index, course);
        }

        public String timeOffSchoolString(final boolean forWidget) {
            final TimeOffSchool timeOffSchool = getTimeOffSchool(forWidget);
            return (timeOffSchool == null) ? null : timeOffSchool.timeOffSchoolString();
        }

        // Format: weekDayId[morningReading,MorningCourses,AfternoonCourses,eveningStudy]
        private static final String FORMAT_TO_STRING = "%1$d%2$s";

        @Override
        public String toString() {
            String dayCoursesString = dayCourses.toString();
            if (TextUtils.isEmpty(dayCoursesString)) {
                dayCoursesString = EMPTY;
            }
            return String.format(FORMAT_TO_STRING, weekDayId, dayCoursesString);
        }

        public int parseWeekDayCourses(final Curriculum curriculum,
                        final String weekDayCoursesString) {
            String dayCoursesString = weekDayCoursesString.substring(1);
            DayCourses.parseDayCourses(curriculum, dayCoursesString, dayCourses);
            return dayCourses.getCourseCount(null);
        }

    }

    private static void generateRandomDayCourses(final Curriculum curriculum) {
        Course course;
        for (WeekDayCourse dayCourse : curriculum.weekDayCourses) {
            if (dayCourse.weekDayId == Calendar.SATURDAY
                    || dayCourse.weekDayId == Calendar.SUNDAY) {
                continue;
            }
            if (dayCourse.weekDayId % 2 == 0) {
                dayCourse.setMorningReading(sMorningReadingChinese);
            } else {
                dayCourse.setMorningReading(sMorningReadingEnglish);
            }
            course = null;
            boolean hasEveningStudy = Utility.randomBoolean();
            if (hasEveningStudy) {
                if (Utility.randomBoolean()) {
                    course = Course.getRandomCourse0(Course.Type.Major);
                }
                dayCourse.setEveningStudy(new MorningEvening(Extra.Evening, course));
            }
            for (AmCourse amCourse : AmCourse.values()) {
                course = Course.getRandomCourse0(Course.Type.Major, Course.Type.Minor);
                dayCourse.setMorningCourse(amCourse.ordinal(), course);
            }
            for (PmCourse pmCourse : PmCourse.values()) {
                int ordinal = pmCourse.ordinal();
                course = Course.getRandomCourse();
                if (ordinal <= 1 || (ordinal > 1 && Utility.randomBoolean())) {
                    dayCourse.setAfternoonCourse(ordinal, course);
                }
            }
        }
    }

    private static final String[][] sDefaultCurriculum = {
            {"2019,09,02", "2020,01,17"}, // 学期开始日期，学期结束日期
            { "晨诵", "数学", "语文", "音乐",     "体育",     "语文",      "英语",  null,  "15:15" },
            { "英语", "语文", "数学", "科学",     "语文阅读", "英语",      "体育",  "校本", "16:15" },
            { "晨诵", "数学", "音乐", "体育",     "语文",     null,        null,   null,   "11:40" },
            { "数学", "数学", "英语", "美术",     "美术",     "道德与法治", "语文", "校本", "16:15" },
            { "语文", "语文", "舞蹈", "数学游戏", "体育",     "班会",       "校本", "校本", "16:15" },
    };

    private static void loadDefaultCurriculum(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum) {
        curriculum.config.colorScheme = CurriculumColorScheme.DifferentColors;
        curriculum.config.semesterStartDate = parseDate(sDefaultCurriculum[0][0]);
        curriculum.config.semesterEndDate = parseDate(sDefaultCurriculum[0][1]);

        final int amCourseCount = AmCourse.count();
        final int pmCourseCount = PmCourse.count();

        Course course;
        int weekId = 1;
        int courseIndex;
        String[] courses;
        for (WeekDayCourse dayCourse : curriculum.weekDayCourses) {
            if (dayCourse.weekDayId == Calendar.SATURDAY
                            || dayCourse.weekDayId == Calendar.SUNDAY) {
                continue;
            }
            courses = sDefaultCurriculum[weekId++];
            if (courses == null || courses.length <= 0) continue;
            
            courseIndex = 0;
            // 早读
            dayCourse.setMorningReading(curriculum.getCourseByName(courses[courseIndex++]));
            
            course = null;
            // 上午课程
            for (AmCourse amCourse : AmCourse.values()) {
                if (courseIndex >= courses.length) break;
                course = getCourseByName(prefHelper, curriculum, courses[courseIndex++]);
                dayCourse.setMorningCourse(amCourse.ordinal(), course);
            }
            // 下午课程.
            for (PmCourse pmCourse : PmCourse.values()) {
                if (courseIndex >= courses.length) break;
                course = getCourseByName(prefHelper, curriculum, courses[courseIndex++]);
                dayCourse.setAfternoonCourse(pmCourse.ordinal(), course);
            }
            // 放学时间
            if (courses.length >= (1 + amCourseCount + pmCourseCount)) {
                dayCourse.setTimeOffSchool(TimeOffSchool.parse(courses[courseIndex++]));
            } else {
                continue;
            }
            // 晚自习
            if (courseIndex <= courses.length - 1) {
                dayCourse.setEveningStudy(curriculum.getCourseByName(courses[courseIndex++]));
            } else {
                continue;
            }
        }
    }

    // 如果Course.getCourseByName(...)返回null，说明需要更新当前的course列表。
    private static Course getCourseByName(final SharedPreferencesHelper prefHelper,
                    final Curriculum curriculum, final String courseName) {
        if (TextUtils.isEmpty(courseName)) return Course.NULL;
        Course course = curriculum.getCourseByName(courseName);
        if (Course.isNullCourse(course)) { // 更新当前course列表.
            prefHelper.remove(curriculum.getPrefKeyWithIndex(PREF_KEY_COURSES));
            loadCourses(prefHelper, curriculum);
            // 重新根据名称找出course.
            course = curriculum.getCourseByName(courseName);
        }
        return course;
    }

    private static int fillTodayDate(final Curriculum curriculum, final Day today) {
        int index = 0;
        for (WeekDayCourse dayCourse : curriculum.weekDayCourses) {
            if (dayCourse.weekDayId == today.weekday) {
                dayCourse.date = today;
                return index;
            }
            index++;
        }
        return -1;
    }

    public static void fillWeekDates(final Curriculum curriculum, final Day today) {
        final int todayIndex = fillTodayDate(curriculum, today);
        int deltaDayCount;
        for (int i = 0; i < curriculum.weekDayCourses.length; i++) {
            if (i == todayIndex) continue;
            deltaDayCount = i - todayIndex;
            curriculum.weekDayCourses[i].date = Utility.getDate(today, deltaDayCount);
        }
    }

    private static class DateCoursesListAdapter extends BaseAdapter {
        private final Context mContext;

        private Curriculum mCurriculum;

        private final DateCoursesChangeListener mmDateCoursesChangeListener;

        public DateCoursesListAdapter(final Context context, final Curriculum curriculum,
                        final DateCoursesChangeListener dateCoursesChangeListener) {
            mContext = context;
            mCurriculum = curriculum;
            mmDateCoursesChangeListener = dateCoursesChangeListener;
        }

        @Override
        public int getCount() {
            return mCurriculum.dateCoursesSize();
        }

        @Override
        public Object getItem(int position) {
            return mCurriculum.getDateCourses(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DateCoursesViewHolder viewHolder;

            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.item_date_courses, null);

                viewHolder = new DateCoursesViewHolder();
                viewHolder.viewDate = (TextView) convertView.findViewById(R.id.view_date);
                viewHolder.viewInfo = (TextView) convertView.findViewById(R.id.view_info);
                viewHolder.viewDelete = convertView.findViewById(R.id.view_delete);
                viewHolder.viewEdit = convertView.findViewById(R.id.view_edit);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (DateCoursesViewHolder) convertView.getTag();
            }

            initViewHolder(viewHolder, position);

            return convertView;
        }

        private void initViewHolder(final DateCoursesViewHolder viewHolder, final int position) {
            final DateCourses item = (DateCourses) getItem(position);
            viewHolder.viewDate.setText(item.dateInfo(mContext));
            viewHolder.viewInfo.setText(item.coursesInfo(mContext));
            viewHolder.viewDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmDialog(item);
                }
            });
            viewHolder.viewEdit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditDateCoursesDialog(mContext, mCurriculum, item,
                                    mmDateCoursesChangeListener);
                }
            });
        }

        private void showDeleteConfirmDialog(final DateCourses item) {
            String title = mContext.getString(R.string.title_delete_date_courses);
            String confirmPrompt = mContext.getString(R.string.prompt_delete_date_courses,
                    item.date.ymd_chinese());
            DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mCurriculum.removeDateCourses(mContext, item, mmDateCoursesChangeListener);
                }
            };
            Utility.showConfirmDialog(mContext, title, confirmPrompt, positiveButtonListener);
        }
    }

    private static class DateCoursesViewHolder {
        TextView viewDate;
        TextView viewInfo;
        View viewDelete;
        View viewEdit;
    }

    private interface CourseSelectedListener {
        public void courseSelected(int index);
    }
    
    private interface CourseActionListener extends CourseSelectedListener {
        public void onCourseAdded();

        public void onCourseDeleted(Course courseDeleted);

        public void onCourseChanged();

        public void clearCourse();
    }

    private static class CourseListAdapter extends BaseAdapter {
        private final Context mContext;
        private final CourseSelectedListener mCourseActionListener;
        private final Course.Type[] mCourseTypes;

        private Curriculum mCurriculum;
        private ArrayList<Course> mCourses;

        private int mSelectedIndex = -1;

        public CourseListAdapter(final Context context, final Curriculum curriculum,
                        final CourseSelectedListener listener, final Course.Type... courseTypes) {
            mContext = context;
            mCurriculum = curriculum;
            mCourseActionListener = listener;
            mCourseTypes = courseTypes;
            collectData();
        }

        private void collectData() {
            mCourses = mCurriculum.getCourses(mCourseTypes);
        }

        public void dataChanged() {
            collectData();
            notifyDataSetChanged();
        }

        public void setSelected(final int index) {
            if (index < 0 && index >= getCount()) return;
            if (mSelectedIndex == index) return;
            mSelectedIndex = index;
            notifyDataSetInvalidated();
        }

        @Override
        public int getCount() {
            return mCourses == null ? 0 : mCourses.size();
        }

        @Override
        public Object getItem(int position) {
            int count = getCount();
            if (position >= count || position < 0) return null;
            return mCourses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public int getItemIndex(final Object obj) {
            if (mCourses == null || mCourses.size() <= 0) return -1;
            return mCourses.indexOf(obj);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CourseViewHolder viewHolder;

            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.item_course, null);

                viewHolder = new CourseViewHolder();

                viewHolder.viewCourseInfo = convertView.findViewById(R.id.view_course_info);
                viewHolder.viewSelect = (RadioButton) convertView.findViewById(R.id.radio_course);
                viewHolder.viewName = (TextView) convertView.findViewById(R.id.view_name);
                viewHolder.viewTeacher = (TextView) convertView.findViewById(R.id.view_teacher);
                viewHolder.viewLocation = (TextView) convertView.findViewById(R.id.view_location);
                viewHolder.viewType = (TextView) convertView.findViewById(R.id.view_course_type);
                viewHolder.viewDelete = convertView.findViewById(R.id.view_delete);
                viewHolder.viewEdit = convertView.findViewById(R.id.view_edit);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (CourseViewHolder) convertView.getTag();
            }

            initViewHolder(viewHolder, position);

            return convertView;
        }

        private void initViewHolder(final CourseViewHolder viewHolder, final int position) {
            final Course item = (Course) getItem(position);

            viewHolder.viewSelect.setClickable(false);
            viewHolder.viewSelect.setChecked(mSelectedIndex == position);

            viewHolder.viewCourseInfo.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCourseActionListener.courseSelected(position);
                }
            });

            viewHolder.viewName.setText(item.name);
            viewHolder.viewName.setTextColor(item.getColorText());
            viewHolder.viewName.setBackgroundColor(item.getColorBackground());

            viewHolder.viewTeacher.setText(item.teacher);

            viewHolder.viewType.setText(item.type.typeResId);
            viewHolder.viewType.setTextColor(item.type.colorText);
            viewHolder.viewType.setBackgroundColor(item.type.colorBackground);

            if (TextUtils.isEmpty(item.location)) {
                viewHolder.viewLocation.setVisibility(View.GONE);
            } else {
                viewHolder.viewLocation.setVisibility(View.VISIBLE);
                viewHolder.viewLocation.setText(item.location);
            }

            if (mCourseActionListener instanceof CourseActionListener) {
                final CourseActionListener courseActionListener = (CourseActionListener) mCourseActionListener;

                viewHolder.viewDelete.setVisibility(View.VISIBLE);
                viewHolder.viewDelete.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDeleteCourseConfirmDialog(item);
                    }
                });
                viewHolder.viewEdit.setVisibility(View.VISIBLE);
                viewHolder.viewEdit.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditCourseDialog(mContext, mCurriculum, item, courseActionListener);
                    }
                });
            } else {
                viewHolder.viewDelete.setVisibility(View.GONE);
                viewHolder.viewEdit.setVisibility(View.GONE);
            }
        }

        private void showDeleteCourseConfirmDialog(final Course course) {
            final String confirmPrompt = mContext.getString(R.string.prompt_delete_course,
                    course.name);
            final DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Course.FailedCause failedCause = mCurriculum.deleteCourse(course);
                    if (failedCause == null) {
                        if (course.equals(getItem(mSelectedIndex))) {
                            mSelectedIndex = -1;
                        }
                        ((CourseActionListener) mCourseActionListener).onCourseDeleted(course);
                    } else {
                        final String failedCauseString = mContext.getString(failedCause.causeResId); 
                        final String prompt = mContext.getString(R.string.failed_delete_course,
                                        course.toString(), failedCauseString);
                        Utility.showInfoDialog(mContext, prompt);
                    }
                }
            };
            Utility.showConfirmDialog(mContext, confirmPrompt, positiveButtonListener);
        }
    }

    private static class CourseViewHolder {
        View viewCourseInfo;
        RadioButton viewSelect;
        TextView viewName;
        TextView viewTeacher;
        TextView viewLocation;

        TextView viewType;
        View viewDelete;
        View viewEdit;
    }
}
