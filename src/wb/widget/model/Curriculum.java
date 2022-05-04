package wb.widget.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import wb.widget.R;
import wb.widget.SettingsCurriculumActivity.CurriculumColorScheme;
import wb.widget.SettingsCurriculumActivity.DayCourses;
import wb.widget.SettingsCurriculumActivity.WeekDayCourse;
import wb.widget.model.Course.FailedCause;
import wb.widget.model.Course.Type;
import wb.widget.utils.Utility;
import wb.widget.utils.calendar.Constants.WeekDay;

public class Curriculum {
    // Preferences for day courses.
    public static final String PREF_KEY_COURSES_MONDAY = "courses_monday";
    public static final String PREF_KEY_COURSES_TUESDAY = "courses_tuesday";
    public static final String PREF_KEY_COURSES_WEDNESDAY = "courses_wednesday";
    public static final String PREF_KEY_COURSES_THURSDAY = "courses_thursday";
    public static final String PREF_KEY_COURSES_FRIDAY = "courses_friday";
    public static final String PREF_KEY_COURSES_SATURDAY = "courses_saturday";
    public static final String PREF_KEY_COURSES_SUNDAY = "courses_sunday";
    // Preferences for time off school.
    public static final String PREF_KEY_TIME_OFF_MONDAY = "time_off_school_monday";
    public static final String PREF_KEY_TIME_OFF_TUESDAY = "time_off_school_tuesday";
    public static final String PREF_KEY_TIME_OFF_WEDNESDAY = "time_off_school_wednesday";
    public static final String PREF_KEY_TIME_OFF_THURSDAY = "time_off_school_thursday";
    public static final String PREF_KEY_TIME_OFF_FRIDAY = "time_off_school_friday";
    public static final String PREF_KEY_TIME_OFF_SATURDAY = "time_off_school_saturday";
    public static final String PREF_KEY_TIME_OFF_SUNDAY = "time_off_school_sunday";

    public final WeekDayCourse[] weekDayCourses = {
            new WeekDayCourse(Calendar.MONDAY,    R.id.day_monday,    PREF_KEY_COURSES_MONDAY,    PREF_KEY_TIME_OFF_MONDAY),
            new WeekDayCourse(Calendar.TUESDAY,   R.id.day_tuesday,   PREF_KEY_COURSES_TUESDAY,   PREF_KEY_TIME_OFF_TUESDAY),
            new WeekDayCourse(Calendar.WEDNESDAY, R.id.day_wednesday, PREF_KEY_COURSES_WEDNESDAY, PREF_KEY_TIME_OFF_WEDNESDAY),
            new WeekDayCourse(Calendar.THURSDAY,  R.id.day_thursday,  PREF_KEY_COURSES_THURSDAY,  PREF_KEY_TIME_OFF_THURSDAY),
            new WeekDayCourse(Calendar.FRIDAY,    R.id.day_friday,    PREF_KEY_COURSES_FRIDAY,    PREF_KEY_TIME_OFF_FRIDAY),
            new WeekDayCourse(Calendar.SATURDAY,  R.id.day_saturday,  PREF_KEY_COURSES_SATURDAY,  PREF_KEY_TIME_OFF_SATURDAY),
            new WeekDayCourse(Calendar.SUNDAY,    R.id.day_sunday,    PREF_KEY_COURSES_SUNDAY,    PREF_KEY_TIME_OFF_SUNDAY),
    };

    private final ArrayList<DateCourses> mDateCourses = new ArrayList<DateCourses>();

    public static class Config {
        public boolean curriculumSet = false;

        public CurriculumColorScheme colorScheme;

        public boolean showCoursesInPublicHolidays;

        public boolean showClassTimeInWidget;

        public int courseColorText, courseColorBackground;

        public String label;
        public int labelTextColor, labelBackgroundColor;

        public Day semesterStartDate, semesterEndDate;

        private static final int DEFAULT_COLOR_LABEL_TEXT = Color.MAGENTA;
        private static final int DEFAULT_COLOR_LABEL_BACKGROUND = Color.BLACK;

        // Format: colorText,colorBackground
        private static final String SEP_COLOR = ",";
        private static final String FORMAT_COLORS = "%d" + SEP_COLOR + "%d";
        
        public Config() {
            init();
        }
        
        private void init() {
            final int colorSchemeOrdinal = Utility.randomInt(0, CurriculumColorScheme.count() - 1);
            colorScheme = CurriculumColorScheme.get(colorSchemeOrdinal);

            initCourseColors();
            initLabelColors();
        }

        public boolean parseLabelColors(final String colors) {
            if (TextUtils.isEmpty(colors)) return false;
            String[] elements = colors.split(SEP_COLOR);
            labelTextColor = Integer.parseInt(elements[0]);
            labelBackgroundColor = Integer.parseInt(elements[1]);
            return true;
        }

        public boolean parseCourseColors(final String colors) {
            if (TextUtils.isEmpty(colors)) return false;
            String[] elements = colors.split(SEP_COLOR);
            courseColorText = Integer.parseInt(elements[0]);
            courseColorBackground = Integer.parseInt(elements[1]);
            return true;
        }

        public void initLabelColors() {
            labelBackgroundColor = DEFAULT_COLOR_LABEL_BACKGROUND;
            labelTextColor = DEFAULT_COLOR_LABEL_TEXT;
        }

        public void initCourseColors() {
            courseColorText = Course.DEFAULT_COLOR_COURSE_TEXT;
            courseColorBackground = Course.DEFAULT_COLOR_COURSE_BACKGROUND;
        }

        public String labelColorsString() {
            return String.format(FORMAT_COLORS, labelTextColor, labelBackgroundColor);
        }

        public String courseColorString() {
            return String.format(FORMAT_COLORS, courseColorText, courseColorBackground);
        }

        public void labelInDefaultColors(final TextView viewLabel) {
            viewLabel.setTextColor(DEFAULT_COLOR_LABEL_TEXT);
            viewLabel.setBackgroundColor(DEFAULT_COLOR_LABEL_BACKGROUND);
        }

        public void setCourseColors(final TextView viewCourse) {
            viewCourse.setTextColor(courseColorText);
            viewCourse.setBackgroundColor(courseColorBackground);
        }
    }

    public final Config config = new Config();

    public interface CurriculumListener {
        public void showSetCourseDialog(final Activity activity, final Curriculum curriculum,
                        final int weekdayId, final int viewId, final int courseLocationViewId,
                        final int viewLabelResId);

        public void showSetTimeOffSchoolDialog(final Activity activity, final Curriculum curriculum,
                        final int weekdayId, final int viewId, final int viewLabelResId);
    }

    public interface DayCoursesListener {
        public void showSetCourseDialog(final Context context, final Curriculum curriculum,
                final DayCourseViewInfo dayCourseViewInfo, final int viewId,
                final int viewLabelResId, final Course courseInput);

        public void showSetTimeOffSchoolDialog(final Context context, final Curriculum curriculum,
                final DayCourseViewInfo dayCourseViewInfo, int viewId,
                final int viewLabelResId, final TimeOffSchool timeOffSchool);
    }

    private static class CourseViewInfo {
        public final int labelViewId;
        public final int locationViewId;
        public final int viewLabelResId;

        private TextView mTextViewCourseName;
        private TextView mTextViewCourseLocation;

        private int mColorText, mColorBackground;

        private boolean hasCourse;

        public CourseViewInfo(int viewId, int viewLabelResId) {
            this.labelViewId = viewId;
            this.locationViewId = -1;
            this.viewLabelResId = viewLabelResId;
        }

        public CourseViewInfo(int viewId, int locationViewId, int viewLabelResId) {
            this.labelViewId = viewId;
            this.locationViewId = locationViewId;
            this.viewLabelResId = viewLabelResId;
        }

        public void initView(final Activity activity, final Curriculum curriculum,
                        final int weekdayId, final CurriculumListener listener) {
            mTextViewCourseName = activity.findViewById(labelViewId);
            if (locationViewId > 0) {
                mTextViewCourseLocation = activity.findViewById(locationViewId);
            } else {
                mTextViewCourseLocation = null;
            }

            final OnClickListener viewClickListener;
            switch (viewLabelResId) {
                case R.string.time_off_school:
                    viewClickListener = new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.showSetTimeOffSchoolDialog(activity, curriculum, weekdayId,
                                            labelViewId, viewLabelResId);
                        }
                    };
                    break;
                default:
                    viewClickListener = new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.showSetCourseDialog(activity, curriculum, weekdayId,
                                            labelViewId, locationViewId, viewLabelResId);
                        }
                    };
                    break;
            }
            mTextViewCourseName.setOnClickListener(viewClickListener);
        }

        public void initTimeOffSchoolView(final Curriculum curriculum, final View rootView,
                        final DayCourseViewInfo viewInfo, final DayCoursesListener listener,
                        final TimeOffSchool timeOffSchool) {
            mTextViewCourseName = rootView.findViewById(labelViewId);
            if (locationViewId > 0) {
                mTextViewCourseLocation = rootView.findViewById(locationViewId);
            } else {
                mTextViewCourseLocation = null;
            }
            if (viewLabelResId != R.string.time_off_school) {
                throw new RuntimeException("How can you reach here?! - 2");
            }
            final Context context = rootView.getContext();
            final OnClickListener viewClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.showSetTimeOffSchoolDialog(context, curriculum, viewInfo, labelViewId,
                                    viewLabelResId, timeOffSchool);
                }
            };
            mTextViewCourseName.setOnClickListener(viewClickListener);
        }

        public void initCourseView(final Curriculum curriculum, final View rootView,
                        final DayCourseViewInfo viewInfo, final DayCoursesListener listener,
                        final Course courseInput) {
            mTextViewCourseName = rootView.findViewById(labelViewId);
            if (locationViewId > 0) {
                mTextViewCourseLocation = rootView.findViewById(locationViewId);
            } else {
                mTextViewCourseLocation = null;
            }

            if (viewLabelResId == R.string.time_off_school) {
                throw new RuntimeException("How can you reach here?! - 1");
            }

            final Context context = rootView.getContext();
            final OnClickListener viewClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.showSetCourseDialog(context, curriculum, viewInfo, labelViewId,
                                    viewLabelResId, courseInput);
                }
            };
            mTextViewCourseName.setOnClickListener(viewClickListener);
        }

        private boolean isCourseView() {
            return viewLabelResId != R.string.time_off_school;
        }

        public void setText(final String courseName, final String courseLocationInfo) {
            if (TextUtils.isEmpty(courseName)) {
                noCourse();
            } else {
                mTextViewCourseName.setText(courseName);
                refreshLocationView(courseLocationInfo);
                hasCourse = true;
            }
        }
        
        private void refreshLocationView(final String locationInfo) {
            if (mTextViewCourseLocation == null) return;
            if (TextUtils.isEmpty(locationInfo)) {
                mTextViewCourseLocation.setVisibility(View.GONE);
            } else {
                mTextViewCourseLocation.setVisibility(View.VISIBLE);
                mTextViewCourseLocation.setText(locationInfo);
            }
        }

        public void setText(final String text) {
            if (TextUtils.isEmpty(text)) {
                noCourse();
            } else {
                mTextViewCourseName.setText(text);
                refreshLocationView(null);
                hasCourse = true;
            }
        }

        public void noCourse() {
            hasCourse = false;
            if (isCourseView()) {
                mTextViewCourseName.setText(R.string.set_course);
            } else {
                mTextViewCourseName.setText(R.string.set_time_off_school);
            }
            setLabelViewColors(Color.DKGRAY, Color.LTGRAY);
            refreshLocationView(null);
        }

        public void setCourse(final Course course) {
            if (Course.isNullCourse(course)) {
                noCourse();
                return;
            }
            mTextViewCourseName.setText(course.name);
            refreshLocationView(course.locationInfo());
            setLabelViewColors(course.getColorText(), course.getColorBackground());
        }

        public void setTimeOffSchool(final TimeOffSchool timeOffSchool) {
            if (timeOffSchool == null) {
                noCourse();
                return;
            }
            mTextViewCourseName.setText(timeOffSchool.timeOffSchool);
            setLabelViewColors(timeOffSchool.colorText, timeOffSchool.colorBackground);
        }

        public void setLabelViewColors(int colorText, int colorBackground) {
            mColorText = colorText;
            mColorBackground = colorBackground;
        }

        public void refreshView(CurriculumColorScheme colorScheme, int colorText, int colorBackground) {
            if (!hasCourse) {
                mTextViewCourseName.setTextColor(mColorText);
                mTextViewCourseName.setBackgroundColor(mColorBackground);
                return;
            }
            if (colorScheme == CurriculumColorScheme.SameColor) {
                mTextViewCourseName.setTextColor(colorText);
                mTextViewCourseName.setBackgroundColor(colorBackground);
            } else {
                mTextViewCourseName.setTextColor(mColorText);
                mTextViewCourseName.setBackgroundColor(mColorBackground);
            }
        }
    }

    private static final CourseViewInfo[] sMondayViews = {
            new CourseViewInfo(R.id.monday_morning, R.string.morning_reading),
            new CourseViewInfo(R.id.monday_evening, R.string.evening_study),
            new CourseViewInfo(R.id._11, R.id._11_location, R.string.morning_course_1),
            new CourseViewInfo(R.id._12, R.id._12_location, R.string.morning_course_2),
            new CourseViewInfo(R.id._13, R.id._13_location, R.string.morning_course_3),
            new CourseViewInfo(R.id._14, R.id._14_location, R.string.morning_course_4),
            new CourseViewInfo(R.id._15, R.id._15_location, R.string.afternoon_course_1),
            new CourseViewInfo(R.id._16, R.id._16_location, R.string.afternoon_course_2),
            new CourseViewInfo(R.id._17, R.id._17_location, R.string.afternoon_course_3),
            new CourseViewInfo(R.id.off_school_monday, R.string.time_off_school),
    };

    private static final CourseViewInfo[] sTuesdayViews = {
            new CourseViewInfo(R.id.tuesday_morning, R.string.morning_reading),
            new CourseViewInfo(R.id.tuesday_evening, R.string.evening_study),
            new CourseViewInfo(R.id._21, R.id._21_location, R.string.morning_course_1),
            new CourseViewInfo(R.id._22, R.id._22_location, R.string.morning_course_2),
            new CourseViewInfo(R.id._23, R.id._23_location, R.string.morning_course_3),
            new CourseViewInfo(R.id._24, R.id._24_location, R.string.morning_course_4),
            new CourseViewInfo(R.id._25, R.id._25_location, R.string.afternoon_course_1),
            new CourseViewInfo(R.id._26, R.id._26_location, R.string.afternoon_course_2),
            new CourseViewInfo(R.id._27, R.id._27_location, R.string.afternoon_course_3),
            new CourseViewInfo(R.id.off_school_tuesday, R.string.time_off_school),
    };

    private static final CourseViewInfo[] sWednesdayViews = {
            new CourseViewInfo(R.id.wednesday_morning, R.string.morning_reading),
            new CourseViewInfo(R.id.wednesday_evening, R.string.evening_study),
            new CourseViewInfo(R.id._31, R.id._31_location, R.string.morning_course_1),
            new CourseViewInfo(R.id._32, R.id._32_location, R.string.morning_course_2),
            new CourseViewInfo(R.id._33, R.id._33_location, R.string.morning_course_3),
            new CourseViewInfo(R.id._34, R.id._34_location, R.string.morning_course_4),
            new CourseViewInfo(R.id._35, R.id._35_location, R.string.afternoon_course_1),
            new CourseViewInfo(R.id._36, R.id._36_location, R.string.afternoon_course_2),
            new CourseViewInfo(R.id._37, R.id._37_location, R.string.afternoon_course_3),
            new CourseViewInfo(R.id.off_school_wednesday, R.string.time_off_school),
    };

    private static final CourseViewInfo[] sThursdayViews = {
            new CourseViewInfo(R.id.thursday_morning, R.string.morning_reading),
            new CourseViewInfo(R.id.thursday_evening, R.string.evening_study),
            new CourseViewInfo(R.id._41, R.id._41_location, R.string.morning_course_1),
            new CourseViewInfo(R.id._42, R.id._42_location, R.string.morning_course_2),
            new CourseViewInfo(R.id._43, R.id._43_location, R.string.morning_course_3),
            new CourseViewInfo(R.id._44, R.id._44_location, R.string.morning_course_4),
            new CourseViewInfo(R.id._45, R.id._45_location, R.string.afternoon_course_1),
            new CourseViewInfo(R.id._46, R.id._46_location, R.string.afternoon_course_2),
            new CourseViewInfo(R.id._47, R.id._47_location, R.string.afternoon_course_3),
            new CourseViewInfo(R.id.off_school_thursday, R.string.time_off_school),
    };

    private static final CourseViewInfo[] sFridayViews = {
            new CourseViewInfo(R.id.friday_morning, R.string.morning_reading),
            new CourseViewInfo(R.id.friday_evening, R.string.evening_study),
            new CourseViewInfo(R.id._51, R.id._51_location, R.string.morning_course_1),
            new CourseViewInfo(R.id._52, R.id._52_location, R.string.morning_course_2),
            new CourseViewInfo(R.id._53, R.id._53_location, R.string.morning_course_3),
            new CourseViewInfo(R.id._54, R.id._54_location, R.string.morning_course_4),
            new CourseViewInfo(R.id._55, R.id._55_location, R.string.afternoon_course_1),
            new CourseViewInfo(R.id._56, R.id._56_location, R.string.afternoon_course_2),
            new CourseViewInfo(R.id._57, R.id._57_location, R.string.afternoon_course_3),
            new CourseViewInfo(R.id.off_school_friday, R.string.time_off_school),
    };

    private static final CourseViewInfo[] sSaturdayViews = {
            new CourseViewInfo(R.id.saturday_morning, R.string.morning_reading),
            new CourseViewInfo(R.id.saturday_evening, R.string.evening_study),
            new CourseViewInfo(R.id._61, R.id._61_location, R.string.morning_course_1),
            new CourseViewInfo(R.id._62, R.id._62_location, R.string.morning_course_2),
            new CourseViewInfo(R.id._63, R.id._63_location, R.string.morning_course_3),
            new CourseViewInfo(R.id._64, R.id._64_location, R.string.morning_course_4),
            new CourseViewInfo(R.id._65, R.id._65_location, R.string.afternoon_course_1),
            new CourseViewInfo(R.id._66, R.id._66_location, R.string.afternoon_course_2),
            new CourseViewInfo(R.id._67, R.id._67_location, R.string.afternoon_course_3),
            new CourseViewInfo(R.id.off_school_saturday, R.string.time_off_school),
    };

    private static final CourseViewInfo[] sSundayViews = {
            new CourseViewInfo(R.id.sunday_morning, R.string.morning_reading),
            new CourseViewInfo(R.id.sunday_evening, R.string.evening_study),
            new CourseViewInfo(R.id._71, R.id._71_location, R.string.morning_course_1),
            new CourseViewInfo(R.id._72, R.id._72_location, R.string.morning_course_2),
            new CourseViewInfo(R.id._73, R.id._73_location, R.string.morning_course_3),
            new CourseViewInfo(R.id._74, R.id._74_location, R.string.morning_course_4),
            new CourseViewInfo(R.id._75, R.id._75_location, R.string.afternoon_course_1),
            new CourseViewInfo(R.id._76, R.id._76_location, R.string.afternoon_course_2),
            new CourseViewInfo(R.id._77, R.id._77_location, R.string.afternoon_course_3),
            new CourseViewInfo(R.id.off_school_sunday, R.string.time_off_school),
    };

    private static class WeekDayCourseInfo {
        public final int weekdayId;
        public final CourseViewInfo[] weekdayCourseViews;

        public WeekDayCourseInfo(final int weekdayId, final CourseViewInfo[] weekdayViews) {
            this.weekdayId = weekdayId;
            this.weekdayCourseViews = weekdayViews;
        }

        public void initViews(final Activity activity, final Curriculum curriculum,
                        final CurriculumListener listener) {
            for (final CourseViewInfo viewInfo : weekdayCourseViews) {
                viewInfo.initView(activity, curriculum, weekdayId, listener);
            }
        }

        public void initViews() {
            for (final CourseViewInfo viewInfo : weekdayCourseViews) {
                viewInfo.noCourse();
                viewInfo.refreshView(null, 0, 0);
            }
        }
    }

    private static final WeekDayCourseInfo[] sWeekDayCoursesInfoArray = {
            new WeekDayCourseInfo(Calendar.MONDAY,    sMondayViews),
            new WeekDayCourseInfo(Calendar.TUESDAY,   sTuesdayViews),
            new WeekDayCourseInfo(Calendar.WEDNESDAY, sWednesdayViews),
            new WeekDayCourseInfo(Calendar.THURSDAY,  sThursdayViews),
            new WeekDayCourseInfo(Calendar.FRIDAY,    sFridayViews),
            new WeekDayCourseInfo(Calendar.SATURDAY,  sSaturdayViews),
            new WeekDayCourseInfo(Calendar.SUNDAY,    sSundayViews),
    };

    public static enum Extra {
        Morning(R.string.morning_reading, Color.WHITE, Color.BLACK),
        Evening(R.string.evening_study,   Color.BLACK, Color.LTGRAY);

        public final int labelResId;
        public final int colorBackground, colorText;

        private Extra(final int labelResId, final int colorBackground, final int colorText) {
            this.labelResId = labelResId;
            this.colorBackground = colorBackground;
            this.colorText = colorText;
        }

        public static Extra get(final int ordinal) {
            for (Extra extra : values()) {
                if (extra.ordinal() == ordinal)
                    return extra;
            }
            return null;
        }
    }

    public static class MorningEvening {
        public final Extra extraType;

        public Course course;

        public MorningEvening(final Extra extra, final Course course) {
            this.course = course;
            extraType = extra;
        }

        private static final String FORMAT_TO_STRING = "%d(%s)";

        @Override
        public String toString() {
            if (Course.isNullCourse(course)) return Integer.toString(extraType.ordinal());
            return String.format(FORMAT_TO_STRING, extraType.ordinal(), course.name);
        }

        public static MorningEvening parse(final Curriculum curriculum, final String input) {
            int leftBracketIndex = input.indexOf("(");
            String courseName = null;
            String strExtraOrdinal;

            if (leftBracketIndex < 0) {
                strExtraOrdinal = input;
            } else {
                strExtraOrdinal = input.substring(0, leftBracketIndex);
                int rightBracketIndex = input.indexOf(")");
                if (rightBracketIndex > leftBracketIndex) {
                    courseName = input.substring(leftBracketIndex + 1, rightBracketIndex);
                }
            }
            try {
                Extra extra = Extra.get(Integer.parseInt(strExtraOrdinal));
                if (extra == null)
                    return null;
                Course course = curriculum.getCourseByName(courseName);
                return new MorningEvening(extra, course);
            } catch (Exception e) {
                return null;
            }
        }

        private static final String FORMAT_INFO = "%s(%s)";

        public String shortInfo(final Context context) {
            return shortInfo(context, extraType, course);
        }

        public static String shortInfo(final Context context, final Extra extra,
                        final Course course) {
            if (Course.isNullCourse(course)) {
                return context.getString(extra.labelResId);
            }
            switch (course.type) {
                case Reading:
                    return course.name;
                default:
                    String extraLabel = context.getString(extra.labelResId);
                    String courseShortName = course.shortName();
                    return String.format(FORMAT_INFO, extraLabel, courseShortName);
            }
        }

        public int getTextColor() {
            if (Course.isNullCourse(course)) return extraType.colorText;
            return course.getColorText();
        }

        public int getBackgroundColor() {
            if (Course.isNullCourse(course)) return extraType.colorBackground;
            return course.getColorBackground();
        }

        public boolean isCourse(final Course courseInput) {
            return Course.equals(course, courseInput);
        }
    }

    private int mCurriculumIndex;

    private CurriculumListener mListener;

    private final ArrayList<Course> mCourses = new ArrayList<Course>();

    public void setListener(final CurriculumListener listener) {
        mListener = listener;
    }

    public void setIndex(final int newIndex) {
        mCurriculumIndex = newIndex;
    }

    public int curriculumIndex() {
        return mCurriculumIndex;
    }

    private static final String FORMAT_KEY_WITH_INDEX = "%s_%d";

    public String getPrefKeyWithIndex(final String prefKey) {
        return String.format(FORMAT_KEY_WITH_INDEX, prefKey, mCurriculumIndex);
    }
    
    public String getTimeOffSchoolPrefKey(final int weekdayId) {
        for (WeekDayCourse weekdayCourse : weekDayCourses) {
            if (weekdayCourse.weekDayId == weekdayId) return weekdayCourse.prefKeyTimeOffSchool;
        }
        return null;
    }

    // 获得放学时间不显示的visibility.
    // 如果都没有设置放学时间，则返回View.GONE; 否则返回View.INVISIBLE;
    // 这么做是因为现在的view显示是根据比例分配的。
    // 如果某个view不显示，就会影响其他view的高度，从而影响对齐。
    public int getTimeOffSchoolViewVisibility() {
        for (WeekDayCourse weekdayCourse : weekDayCourses) {
            if (weekdayCourse.getTimeOffSchool(true) != null) {
                return View.INVISIBLE;
            }
        }
        return View.GONE;
    }

    public int getEveningStudyViewVisibility() {
        for (WeekDayCourse weekdayCourse : weekDayCourses) {
            if (weekdayCourse.hasEveningStudy()) {
                return View.VISIBLE;
            }
        }
        return View.INVISIBLE;
    }

    public void clear(final SharedPreferencesHelper prefHelper) {
        String[] prefKeys = new String[weekDayCourses.length];
        String[] prefKeyTimeOffSchool = new String[weekDayCourses.length];
        for (int i = 0; i < weekDayCourses.length; i++) {
            weekDayCourses[i].dayCourses.init();
            prefKeys[i] = getPrefKeyWithIndex(weekDayCourses[i].prefKeyCourses);
            prefKeyTimeOffSchool[i] = getPrefKeyWithIndex(weekDayCourses[i].prefKeyTimeOffSchool);
        }
        prefHelper.remove(prefKeys);
        prefHelper.remove(prefKeyTimeOffSchool);

        for (WeekDayCourseInfo weekdayInfo : sWeekDayCoursesInfoArray) {
            weekdayInfo.initViews();
        }

        config.curriculumSet = false;
    }

    public void initViews(final Activity activity) {
        for (WeekDayCourseInfo weekdayInfo : sWeekDayCoursesInfoArray) {
            weekdayInfo.initViews(activity, this, mListener);
        }
    }

    public void refreshViews() {
        final CurriculumColorScheme colorScheme = config.colorScheme;
        final int courseColorText = config.courseColorText;
        final int courseColorBackground = config.courseColorBackground;
        for (WeekDayCourseInfo weekdayInfo : sWeekDayCoursesInfoArray) {
            for (CourseViewInfo viewInfo : weekdayInfo.weekdayCourseViews) {
                viewInfo.refreshView(colorScheme, courseColorText, courseColorBackground);
            }
        }
    }

    public void setCourses(final Context context) {
        WeekDayCourse weekDayCourseFound;
        DayCourses dayCourses;
        for (WeekDayCourseInfo weekdayInfo : sWeekDayCoursesInfoArray) {
            weekDayCourseFound = null;
            for (WeekDayCourse weekDayCourse : weekDayCourses) {
                if (weekDayCourse.weekDayId == weekdayInfo.weekdayId) {
                    weekDayCourseFound = weekDayCourse;
                    break;
                }
            }
            for (CourseViewInfo viewInfo : weekdayInfo.weekdayCourseViews) {
                viewInfo.refreshView(config.colorScheme, config.courseColorText,
                                config.courseColorBackground);
            }
            if (weekDayCourseFound == null) continue;
            dayCourses = weekDayCourseFound.dayCourses;
            setCourses(context, weekdayInfo.weekdayCourseViews, dayCourses);
        }
    }

    private void setCourses(final Context context, final CourseViewInfo[] weekdayViewInfoArray,
            final DayCourses dayCourses) {
        int index = 0;
        CourseViewInfo courseViewInfo = weekdayViewInfoArray[index++];
        // Set MorningReading.
        setViewCourse(context, courseViewInfo, dayCourses.morningReading);

        courseViewInfo = weekdayViewInfoArray[index++];
        // Set EveningStudy.
        setViewCourse(context, courseViewInfo, dayCourses.eveningStudy);

        // Set courses.
        Course course;
        while (index < weekdayViewInfoArray.length - 1) {
            courseViewInfo = weekdayViewInfoArray[index++];
            course = dayCourses.getCourseByLabel(courseViewInfo.viewLabelResId);
            setViewCourse(courseViewInfo, course);
        }

        courseViewInfo = weekdayViewInfoArray[index++];
        // Set timeOffSchool.
        setViewTimeOffSchool(courseViewInfo, dayCourses.timeOffSchool);
    }

    private void setViewCourse(final Context context, final CourseViewInfo viewInfo,
            final MorningEvening morningEvening) {
        if (morningEvening == null) {
            viewInfo.noCourse();
            return;
        }
        viewInfo.setText(morningEvening.shortInfo(context));
        viewInfo.setLabelViewColors(morningEvening.getTextColor(), morningEvening.getBackgroundColor());
    }

    private void setViewCourse(final CourseViewInfo viewInfo, final Course course) {
        if (Course.isNullCourse(course)) {
            viewInfo.noCourse();
            return;
        }
        viewInfo.setText(course.name, course.locationInfo());
        viewInfo.setLabelViewColors(course.getColorText(), course.getColorBackground());
    }

    private void setViewTimeOffSchool(final CourseViewInfo viewInfo,
                    final TimeOffSchool timeOffSchool) {
        if (timeOffSchool == null) {
            viewInfo.noCourse();
            return;
        }
        viewInfo.setText(timeOffSchool.timeOffSchool);
        viewInfo.setLabelViewColors(timeOffSchool.colorText, timeOffSchool.colorBackground);
    }

    public static class DayCourseViewInfo {
        private final CourseViewInfo[] mDayCourseViewInfoArray = {
                new CourseViewInfo(R.id.date_morning, R.string.morning_reading),
                new CourseViewInfo(R.id.date_evening, R.string.evening_study),
                new CourseViewInfo(R.id.date_am_1, R.string.morning_course_1),
                new CourseViewInfo(R.id.date_am_2, R.string.morning_course_2),
                new CourseViewInfo(R.id.date_am_3, R.string.morning_course_3),
                new CourseViewInfo(R.id.date_am_4, R.string.morning_course_4),
                new CourseViewInfo(R.id.date_pm_1, R.string.afternoon_course_1),
                new CourseViewInfo(R.id.date_pm_2, R.string.afternoon_course_2),
                new CourseViewInfo(R.id.date_pm_3, R.string.afternoon_course_3),
                new CourseViewInfo(R.id.date_time_off_school, R.string.time_off_school),
        };

        public final DayCourses oldDayCourses;
        public final DayCourses newDayCourses = new DayCourses();

        public DayCourseViewInfo(final DayCourses dayCourses) {
            oldDayCourses = dayCourses;
            if (dayCourses == null) {
                newDayCourses.init();
            } else {
                newDayCourses.copyDayCourses(dayCourses);
            }
        }

        public void initViews(final Context context, final Curriculum curriculum,
                        final int weekdayId, final View rootView,
                        final DayCoursesListener listener) {
            final DayCourseViewInfo dayCourseViewInfo = this;
            for (CourseViewInfo courseViewInfo : mDayCourseViewInfoArray) {
                switch (courseViewInfo.viewLabelResId) {
                    case R.string.time_off_school:
                        TimeOffSchool timeOffSchool = TimeOffSchool.copy(newDayCourses.timeOffSchool);
                        courseViewInfo.initTimeOffSchoolView(curriculum, rootView,
                                        dayCourseViewInfo, listener, timeOffSchool);
                        break;
                    default:
                        Course course = newDayCourses.getCourseByLabel(courseViewInfo.viewLabelResId);
                        courseViewInfo.initCourseView(curriculum, rootView, dayCourseViewInfo,
                                        listener, course);
                        break;
                }
            }
        }

        public void setCourseByLabel(final int labelResId, final Course course) {
            newDayCourses.setCourseByLabel(labelResId, course);

            for (CourseViewInfo courseViewInfo : mDayCourseViewInfoArray) {
                if (courseViewInfo.viewLabelResId == labelResId) {
                    courseViewInfo.setCourse(course);
                    break;
                }
            }
        }

        public void setTimeOffSchool(final TimeOffSchool timeOffSchool) {
            for (CourseViewInfo courseViewInfo : mDayCourseViewInfoArray) {
                if (courseViewInfo.viewLabelResId == R.string.time_off_school) {
                    courseViewInfo.setTimeOffSchool(timeOffSchool);
                    break;
                }
            }
        }

        public void refreshViews(final CurriculumColorScheme colorScheme, final int colorText,
                final int colorBackground) {
            for (CourseViewInfo courseViewInfo : mDayCourseViewInfoArray) {
                courseViewInfo.refreshView(colorScheme, colorText, colorBackground);
            }
        }
    }
    
    public String getDateCoursesString() {
        StringBuilder sb = new StringBuilder();
        for (DateCourses dateCourses : mDateCourses) {
            if (sb.length() > 0) sb.append(SEP_DATE);
            sb.append(dateCourses.toString());
        }
        return sb.toString();
    }

    public CurriculumColorScheme getColorScheme() {
        return config.colorScheme;
    }

    public int getCourseColorText() {
        return config.courseColorText;
    }

    public int getCourseColorBackground() {
        return config.courseColorBackground;
    }

    public int getLabelColorText() {
        return config.labelTextColor;
    }

    public int getLabelColorBackground() {
        return config.labelBackgroundColor;
    }
    
    public DateCourses getSpecifiedDateCourses(final Day date) {
        for (DateCourses dateCourses : mDateCourses) {
            if (dateCourses.date.equals(date)) return dateCourses;
        }
        return null;
    }

    public void clearDateCourses() {
        mDateCourses.clear();
    }

    public boolean dateExists(final DateCourses dateCoursesInput) {
        for (DateCourses dateCourses : mDateCourses) {
            if (dateCourses.isSameDate(dateCoursesInput)) return true;
        }
        return false;
    }

    public void removeDateCourses(final Day date) {
        ArrayList<DateCourses> sameDates = new ArrayList<DateCourses>(mDateCourses.size());
        for (DateCourses dateCourses : mDateCourses) {
            if (dateCourses.date.isSameDay(date)) {
                sameDates.add(dateCourses);
            }
        }
        for (DateCourses dateCourses : sameDates) {
            mDateCourses.remove(dateCourses);
        }
    }

    public void removeDateCourses(final Context context, final DateCourses dateCourses,
                    final DateCoursesChangeListener dateCoursesChangeListener) {
        if (mDateCourses.remove(dateCourses)) {
            dateCoursesChangeListener.onDateCoursesChanged();
        }
    }

    public void addDateCourses(final Context context, final DateCourses dateCourses,
                    final DateCoursesChangeListener dateCoursesChangeListener) {
        mDateCourses.add(dateCourses);
        sort(mDateCourses);
        dateCoursesChangeListener.onDateCoursesChanged();
    }

    public int dateCoursesSize() {
        return mDateCourses.size();
    }

    public DateCourses getDateCourses(int index) {
        return mDateCourses.get(index);
    }

    public interface DateCoursesChangeListener {
        public void onDateCoursesChanged();
    }

    private static final String SEP_DATE = "#";

    public static class DateCourses {
        public final Day date;

        public int weekdayId_equals = -1;
        public final DayCourses dayCourse = new DayCourses();

        public DateCourses(final Day date) {
            this.date = date;
            Utility.obtainWeekDay(date);
        }

        public boolean isSameDate(final DateCourses dateCoursesInput) {
            if (dateCoursesInput == null) return false;
            return date.isSameDay(dateCoursesInput.date);
        }

        public void copyDayCourses(final DayCourses dayCoursesInput) {
            dayCourse.init();
            dayCourse.copyDayCourses(dayCoursesInput);
        }

        public String dateInfo(final Context context) {
            return date.ymdw_chinese(context);
        }

        public String coursesInfo(final Context context) {
            if (weekdayId_equals <= 0) {
                if (dayCourse == null) return null;
                return dayCourse.shortInfo();
            }
            String weekDayLabel = WeekDay.getFullLabel(context, weekdayId_equals);
            return context.getString(R.string.take_weekday_courses, weekDayLabel);
        }

        private static final String SEP_DATE_WEEKDAY = "→";
        private static final String SEP_DATE_COURSES = "＋";

        private static final String YYYYMMDD = "%4d%02d%02d";
        // Format: yyyymmdd-weekdayId
        private static final String FORMAT_TO_STRING_WEEKDAY_EQUALS = YYYYMMDD + SEP_DATE_WEEKDAY
                        + "%d";
        // Format: yyyymmdd+dayCourseString
        private static final String FORMAT_TO_STRING_SPECIFIED = YYYYMMDD + SEP_DATE_COURSES + "%s";

        @Override
        public String toString() {
            if (weekdayId_equals >= 0) {
                return String.format(FORMAT_TO_STRING_WEEKDAY_EQUALS, date.year, date.month,
                                date.day, weekdayId_equals);
            }
            return String.format(FORMAT_TO_STRING_SPECIFIED, date.year, date.month, date.day,
                            dayCourse.toString());
        }

        private static DateCourses parseDateByYMD(final String yyyymmdd) {
            int year = Integer.parseInt(yyyymmdd.substring(0, 4));
            int month = Integer.parseInt(yyyymmdd.substring(4, 6));
            int day = Integer.parseInt(yyyymmdd.substring(6));
            return new DateCourses(new Day(year, month, day));
        }

        private static DateCourses parseDate(final Curriculum curriculum,
                        final String dateCoursesString) {
            if (TextUtils.isEmpty(dateCoursesString)) return null;
            DateCourses dateCourses = null;
            if (dateCoursesString.contains(SEP_DATE_WEEKDAY)) {
                String[] elements = dateCoursesString.split(SEP_DATE_WEEKDAY);
                dateCourses = parseDateByYMD(elements[0]);
                dateCourses.weekdayId_equals = Integer.parseInt(elements[1]);
            }
            if (dateCoursesString.contains(SEP_DATE_COURSES)) {
                String[] elements = dateCoursesString.split(SEP_DATE_COURSES);
                dateCourses = parseDateByYMD(elements[0]);
                dateCourses.copyDayCourses(DayCourses.parseDayCourses(curriculum, elements[1],
                                null));
            }
            return dateCourses;
        }
    }

    public void parseDateCourses(final String datesCoursesString) {
        mDateCourses.clear();
        if (TextUtils.isEmpty(datesCoursesString)) return;
        String[] elements = datesCoursesString.split(SEP_DATE);
        DateCourses dateCourses;
        for (String element : elements) {
            dateCourses = DateCourses.parseDate(this, element);
            if (dateCourses == null) continue;
            mDateCourses.add(dateCourses);
        }
        sort(mDateCourses);
    }

    private static void sort(final ArrayList<DateCourses> dateCoursesList) {
        Collections.sort(dateCoursesList, new Comparator<DateCourses>() {
            @Override
            public int compare(DateCourses dateCourses1, DateCourses dateCourses2) {
                return Day.compare(dateCourses1.date, dateCourses2.date);
            }
        });
    }

    // 以下从Course.java搬过来，用于操作所有course列表.
    public String coursesString() {
        return coursesString(mCourses);
    }

    public int getCourseCount() {
        return mCourses.size();
    }

    public int getCourseIndex(final Course course) {
        return mCourses.indexOf(course);
    }

    public ArrayList<Course> getCourses(final Type... courseTypes) {
        if (courseTypes == null || courseTypes.length <= 0) return mCourses;
        ArrayList<Course> courses = new ArrayList<Course>(mCourses.size());
        for (Course course : mCourses) {
            for (Type courseType : courseTypes) {
                if (course.type == courseType) courses.add(course);
            }
        }
        return courses;
    }

    public Course getCourse(int index) {
        int count = mCourses.size();
        if (count <= 0) return null;
        if (index < 0 || index >= count) return null;
        return mCourses.get(index);
    }

    public Course getCourseByName(final String courseName) {
        int count = mCourses.size();
        if (count <= 0 || TextUtils.isEmpty(courseName)) return null;
        for (Course course : mCourses) {
            if (TextUtils.equals(course.name, courseName)) {
                return course;
            }
        }
        return null;
    }

    private static String coursesString(final ArrayList<Course> courses) {
        StringBuilder sb = new StringBuilder();
        for (Course course : courses) {
            if (sb.length() > 0) sb.append(Course.SEP_COURSE);
            sb.append(course.toString());
        }
        return sb.toString();
    }

    public FailedCause addCourse(final Course newCourse) {
        if (mCourses.contains(newCourse)) {
            return FailedCause.SameCourseName;
        }
        mCourses.add(newCourse);
        return null;
    }

    public FailedCause deleteCourse(final Course course) {
        // Remove the course from week days.
        for (WeekDayCourse weekDayCourse : weekDayCourses) {
            weekDayCourse.dayCourses.deleteCourse(course);
        }
        if (mCourses.contains(course)) {
            boolean result = mCourses.remove(course);
            return result ? null : FailedCause.Unknown;
        }
        return FailedCause.CourseNotExist;
    }

    public void parseCourses(final String coursesString) {
        mCourses.clear();
        if (TextUtils.isEmpty(coursesString)) return;
        String[] elements = coursesString.split(Course.SEP_COURSE);
        Course course;
        for (String element : elements) {
            if (TextUtils.isEmpty(element)) continue;
            course = Course.parse(element);
            mCourses.add(course);
        }
    }

    public void clearCourses() {
        mCourses.clear();
    }

    public void copyDefaultCourses() {
        Course.copyDefaultCourses(mCourses);
    }

    public boolean isDefaultUsed() {
        return Course.isDefaultUsed(mCourses);
    }

    // 根据DateCourses设置，对于在本周的日期，更新courses。
    public boolean refreshWeekDayCourses() {
        boolean curriculumChanged = false;
        DateCourses foundDateCourses;
        for (WeekDayCourse weekdayCourse : weekDayCourses) {
            foundDateCourses = getSpecifiedDateCourses(weekdayCourse.date);
            DayCourses curDayCourses = null;
            if (foundDateCourses != null) {
                final int weekdayIdEquals = foundDateCourses.weekdayId_equals;
                if (weekdayIdEquals > 0) {
                    curDayCourses = getDayCourses(weekdayIdEquals);
                } else {
                    curDayCourses = foundDateCourses.dayCourse;
                }
            }
            curriculumChanged = curriculumChanged || weekdayCourse.setDateCourses(curDayCourses);
        }
        return curriculumChanged;
    }

    public WeekDayCourse getWeekDayCourses(final int weekdayId) {
        for (WeekDayCourse weekDayCourse : weekDayCourses) {
            if (weekDayCourse.weekDayId == weekdayId) {
                return weekDayCourse;
            }
        }
        return null;
    }

    public DayCourses getDayCourses(final int weekdayId) {
        for (WeekDayCourse weekDayCourse : weekDayCourses) {
            if (weekDayCourse.weekDayId == weekdayId) {
                return weekDayCourse.dayCourses;
            }
        }
        return null;
    }

    public void setDayCourse(final int weekdayId, final int labelResId, final Course course) {
        for (WeekDayCourse weekDayCourse : weekDayCourses) {
            if (weekDayCourse.weekDayId == weekdayId) {
                weekDayCourse.dayCourses.setCourseByLabel(labelResId, course);
                return;
            }
        }
    }

    public void setTimeOffSchool(final int weekdayId, final TimeOffSchool timeOffSchool) {
        for (WeekDayCourse weekdayCourse : weekDayCourses) {
            if (weekdayCourse.weekDayId == weekdayId) {
                weekdayCourse.setTimeOffSchool(timeOffSchool);
                return;
            }
        }
    }

    public TimeOffSchool getTimeOffSchool(final int weekdayId) {
        for (WeekDayCourse weekDayCourse : weekDayCourses) {
            if (weekDayCourse.weekDayId == weekdayId) {
                return weekDayCourse.getTimeOffSchool(false);
            }
        }
        return null;
    }

    public Course getCourse(final int weekdayId, final int labelResId) {
        for (WeekDayCourse weekDayCourse : weekDayCourses) {
            if (weekDayCourse.weekDayId == weekdayId) {
                return weekDayCourse.dayCourses.getCourseByLabel(labelResId);
            }
        }
        return Course.NULL;
    }

    public MorningEvening getMorningReading(final int weekdayId) {
        for (WeekDayCourse weekDayCourse : weekDayCourses) {
            if (weekDayCourse.weekDayId == weekdayId) {
                return weekDayCourse.dayCourses.morningReading;
            }
        }
        return null;
    }

    public boolean setMorningEvening(final int weekdayId, final Extra extra,
                    final MorningEvening morningEvening) {
        for (WeekDayCourse weekDayCourse : weekDayCourses) {
            if (weekDayCourse.weekDayId == weekdayId) {
                switch (extra) {
                    case Morning:
                        weekDayCourse.setMorningReading(morningEvening);
                        break;
                    case Evening:
                        weekDayCourse.setEveningStudy(morningEvening);
                        break;
                }
                return true;
            }
        }
        return false;
    }

    public MorningEvening getEveningStudy(final int weekdayId) {
        for (WeekDayCourse weekDayCourse : weekDayCourses) {
            if (weekDayCourse.weekDayId == weekdayId) {
                return weekDayCourse.dayCourses.eveningStudy;
            }
        }
        return null;
    }

    public int getCourseCountInWeek(final Course course) {
        int count = 0;
        for (WeekDayCourse weekDayCourse : weekDayCourses) {
            count += weekDayCourse.getCourseCount(course);
        }
        return count;
    }
}
