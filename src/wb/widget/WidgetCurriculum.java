
package wb.widget;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import wb.widget.SettingsCalendarActivity.DayInfo;
import wb.widget.SettingsCurriculumActivity.CurriculumColorScheme;
import wb.widget.SettingsCurriculumActivity.WeekDayCourse;
import wb.widget.model.Course;
import wb.widget.model.Curriculum;
import wb.widget.model.Day;
import wb.widget.model.SharedPreferencesHelper;
import wb.widget.model.TimeOffSchool;
import wb.widget.utils.Utility;

public class WidgetCurriculum {
    public static final String ACTION_PREV_CURRICULUM = "prev_curriculum";
    public static final String ACTION_NEXT_CURRICULUM = "next_curriculum";
    public static final String ACTION_SWITCH_CLASS_TIME_SHOW = "switch_class_time_show";

    public static void performUpdate(final Context context, final Day today,
                    final int firstDayOfWeek, final SharedPreferencesHelper prefsHelper,
                    final RemoteViews updateView) {
        initViews(context, updateView, firstDayOfWeek, today, prefsHelper);

        setViewClickers(context, updateView);
    }

    private static void setViewClickers(final Context appContext, final RemoteViews updateView) {
        // Click settings icon to config the widget.
        PendingIntent launchSettings = getIntentToSettings(appContext);
        if (launchSettings != null) {
            updateView.setOnClickPendingIntent(R.id.view_settings, launchSettings);
        }

        final int curriculumCount = SettingsCurriculumActivity.curriculumCount();
        // Click curriculum_prev to show the previous curriculum.
        setCurriculumClickers(appContext, updateView, curriculumCount, R.id.view_curriculum_prev,
                        ACTION_PREV_CURRICULUM);
        // Click curriculum_next to show the next curriculum.
        setCurriculumClickers(appContext, updateView, curriculumCount, R.id.view_curriculum_next,
                        ACTION_NEXT_CURRICULUM);
        // Click Monday view to show or hide class time.
        setCurriculumClicker(appContext, updateView, R.id.week_day_monday,
                        ACTION_SWITCH_CLASS_TIME_SHOW);
    }

    private static void setCurriculumClickers(final Context appContext,
                    final RemoteViews updateView, final int curriculumCount, final int viewId,
                    final String action) {
        if (curriculumCount > 1) {
            updateView.setViewVisibility(viewId, View.VISIBLE);

            Intent intentCurriculumShow = new Intent(appContext, WidgetMain.class);
            intentCurriculumShow.setAction(action);
            PendingIntent pendingIntentCurriculumShow = PendingIntent.getBroadcast(appContext, 0,
                            intentCurriculumShow, 0);
            updateView.setOnClickPendingIntent(viewId, pendingIntentCurriculumShow);
        } else {
            updateView.setViewVisibility(viewId, View.GONE);
        }
    }

    private static void setCurriculumClicker(final Context appContext, final RemoteViews updateView,
                    final int viewId, final String action) {
        Intent intentAction = new Intent(appContext, WidgetMain.class);
        intentAction.setAction(action);
        PendingIntent pendingIntentAction = PendingIntent.getBroadcast(appContext, 0,
                        intentAction, 0);
        updateView.setOnClickPendingIntent(viewId, pendingIntentAction);
    }

    private static PendingIntent getIntentToSettings(final Context context) {
        final Class<?> activityClass = SettingsCurriculumActivity.class;
        Intent startActivityIntent = new Intent(context, activityClass);
        final PendingIntent launchSettingsPendingIntent = PendingIntent.getActivity(context,
                0 /* no requestCode */, startActivityIntent, 0 /* no flags */);
        return launchSettingsPendingIntent;
    }

    private static void initViews(final Context context, final RemoteViews updateView,
            final int firstDayOfWeek, final Day today, final SharedPreferencesHelper prefsHelper) {
        updateView.setTextViewText(R.id.info_text, Utility.getDayText(context, today));

        SettingsCurriculumActivity.loadCurriculums(prefsHelper);

        final Curriculum curriculum = SettingsCurriculumActivity.getCurriculum();
        final int timeOffSchoolViewVisibility = curriculum.getTimeOffSchoolViewVisibility();
        final int eveningStudyViewVisibility = curriculum.getEveningStudyViewVisibility();
        initClassTimeView(updateView, curriculum.config, eveningStudyViewVisibility,
                        timeOffSchoolViewVisibility == View.INVISIBLE ? View.VISIBLE : View.GONE);

        // For curriculum label.
        initLabelView(context, updateView, curriculum, prefsHelper);

        SettingsCurriculumActivity.fillWeekDates(curriculum, today);
        initDayViews(context, updateView, curriculum, today.weekday, timeOffSchoolViewVisibility);
    }

    private static void initClassTimeView(final RemoteViews updateView,
                    final Curriculum.Config config,
                    final int eveningStudyViewVisibility, final int timeOffSchoolViewVisibility) {
        if (config.showClassTimeInWidget) {
            updateView.setViewVisibility(R.id.day_classes, View.VISIBLE);
            updateView.setViewVisibility(R.id.view_show_class_time_in_widget, View.INVISIBLE);
            SettingsCurriculumActivity.loadAmClassTime(updateView);
            SettingsCurriculumActivity.loadPmClassTime(updateView);
            updateView.setTextColor(R.id.day_morning_reading, Color.WHITE);

            updateView.setViewVisibility(R.id.day_evening_study, eveningStudyViewVisibility);
            updateView.setTextColor(R.id.day_evening_study, Color.LTGRAY);

            updateView.setViewVisibility(R.id.day_off_school, timeOffSchoolViewVisibility);
        } else {
            updateView.setViewVisibility(R.id.day_classes, View.GONE);
        }
    }

    private static void initLabelView(final Context context, final RemoteViews updateView,
                    final Curriculum curriculum, final SharedPreferencesHelper prefsHelper) {
        final int labelViewId = R.id.view_curriculum_label;

        final boolean curriculumSet = SettingsCurriculumActivity.loadWeekDayCourses(prefsHelper,
                        curriculum);
        String curriculumLabel;
        if (!curriculumSet) {
            curriculumLabel = context.getString(R.string.prompt_demo_fill_curriculum);
            updateView.setTextViewText(labelViewId, curriculumLabel);
            return;
        }

        SettingsCurriculumActivity.loadDateCourses(prefsHelper);
        curriculum.refreshWeekDayCourses();

        curriculumLabel = SettingsCurriculumActivity.loadLabel(prefsHelper);
        if (TextUtils.isEmpty(curriculumLabel)) {
            curriculumLabel = context.getString(R.string.prompt_set_curriculum_label);
        } else {
            int labelColorText = curriculum.getLabelColorText();
            updateView.setTextColor(labelViewId, labelColorText);
            int labelColorBackground = curriculum.getLabelColorBackground();
            WidgetMain.setViewBackgroundColor(updateView, labelViewId, labelColorBackground);
        }
        updateView.setTextViewText(labelViewId, curriculumLabel);
    }

    private static final int[] sMondayCourseViewMapping = {
            Calendar.MONDAY, R.id.week_day_monday, R.id.monday_morning, R.id.monday_evening,
            R.id._11, R.id._11_location, R.id._12, R.id._12_location, R.id._13, R.id._13_location,
            R.id._14, R.id._14_location, R.id._15, R.id._15_location, R.id._16, R.id._16_location,
            R.id._17, R.id._17_location, R.id._18, R.id._18_location,  R.id.off_school_monday
    };

    private static final int[] sTuesdayCourseViewMapping = {
            Calendar.TUESDAY, R.id.week_day_tuesday, R.id.tuesday_morning, R.id.tuesday_evening,
            R.id._21, R.id._21_location, R.id._22, R.id._22_location, R.id._23, R.id._23_location,
            R.id._24, R.id._24_location, R.id._25, R.id._25_location, R.id._26, R.id._26_location,
            R.id._27, R.id._27_location, R.id._28, R.id._28_location, R.id.off_school_tuesday
    };
    
    private static final int[] sWednesdayCourseViewMapping = {
            Calendar.WEDNESDAY, R.id.week_day_wednesday, R.id.wednesday_morning, R.id.wednesday_evening,
            R.id._31, R.id._31_location, R.id._32, R.id._32_location, R.id._33, R.id._33_location,
            R.id._34, R.id._34_location, R.id._35, R.id._35_location, R.id._36, R.id._36_location,
            R.id._37, R.id._37_location, R.id._38, R.id._38_location, R.id.off_school_wednesday
    };
    
    private static final int[] sThursdayCourseViewMapping = {
            Calendar.THURSDAY, R.id.week_day_thursday, R.id.thursday_morning, R.id.thursday_evening,
            R.id._41, R.id._41_location, R.id._42, R.id._42_location, R.id._43, R.id._43_location,
            R.id._44, R.id._44_location, R.id._45, R.id._45_location, R.id._46, R.id._46_location,
            R.id._47, R.id._47_location, R.id._48, R.id._48_location, R.id.off_school_thursday
    };
    
    private static final int[] sFridayCourseViewMapping = {
            Calendar.FRIDAY, R.id.week_day_friday, R.id.friday_morning, R.id.friday_evening,
            R.id._51, R.id._51_location, R.id._52, R.id._52_location, R.id._53, R.id._53_location,
            R.id._54, R.id._54_location, R.id._55, R.id._55_location, R.id._56, R.id._56_location,
            R.id._57, R.id._57_location, R.id._58, R.id._58_location, R.id.off_school_friday
    };

    private static final int[] sSaturdayCourseViewMapping = {
            Calendar.SATURDAY, R.id.week_day_saturday, R.id.saturday_morning, R.id.saturday_evening,
            R.id._61, R.id._61_location, R.id._62, R.id._62_location, R.id._63, R.id._63_location,
            R.id._64, R.id._64_location, R.id._65, R.id._65_location, R.id._66, R.id._66_location,
            R.id._67, R.id._67_location, R.id._68, R.id._68_location, R.id.off_school_saturday
    };
    
    private static final int[] sSundayCourseViewMapping = {
            Calendar.SUNDAY, R.id.week_day_sunday, R.id.sunday_morning, R.id.sunday_evening,
            R.id._71, R.id._71_location, R.id._72, R.id._72_location, R.id._73, R.id._73_location,
            R.id._74, R.id._74_location, R.id._75, R.id._75_location, R.id._76, R.id._76_location,
            R.id._77, R.id._77_location, R.id._78, R.id._78_location, R.id.off_school_sunday
    };

    private static final int[][] sDayCourseViewMapping = {
                    sMondayCourseViewMapping,
                    sTuesdayCourseViewMapping,
                    sWednesdayCourseViewMapping,
                    sThursdayCourseViewMapping,
                    sFridayCourseViewMapping,
                    sSaturdayCourseViewMapping,
                    sSundayCourseViewMapping,
    };

    private static int[] getDayViews(final int weekDayResId) {
        for (int[] dayViews : sDayCourseViewMapping) {
            if (dayViews[0] == weekDayResId) return dayViews;
        }
        return null;
    }

    private static final int COLOR_WEEKDAY_TODAY = Color.YELLOW;
    private static final int COLOR_WEEKDAY = Color.rgb(0xAD, 0xD8, 0xE6);

    private static void initDayViews(final Context context, final RemoteViews updateView,
                    final Curriculum curriculum, final int todayWeekDayId,
                    final int timeOffSchoolViewHidden) {
        final SettingsCurriculumActivity.CurriculumColorScheme colorScheme = curriculum
                        .getColorScheme();
        final int colorText = curriculum.getCourseColorText();
        final int colorBackground = curriculum.getCourseColorBackground();

        int[] dayViewIds;
        int weekdayViewId, morningReadingId, timeOffSchoolId, eveningStudyId;
        int _1, _1_location, _2, _2_location, _3, _3_location, _4, _4_location;
        int _5, _5_location, _6, _6_location, _7, _7_location, _8, _8_location;
        String shortInfoMorningReading, shortInfoEveningStudy;
        for (WeekDayCourse weekdayCourse : curriculum.weekDayCourses) {
            dayViewIds = getDayViews(weekdayCourse.weekDayId);
            if (dayViewIds == null) continue;

            weekdayViewId = dayViewIds[1];
            morningReadingId = dayViewIds[2];
            eveningStudyId = dayViewIds[3];
            _1 = dayViewIds[4];
            _1_location = dayViewIds[5];
            _2 = dayViewIds[6];
            _2_location = dayViewIds[7];
            _3 = dayViewIds[8];
            _3_location = dayViewIds[9];
            _4 = dayViewIds[10];
            _4_location = dayViewIds[11];
            _5 = dayViewIds[12];
            _5_location = dayViewIds[13];
            _6 = dayViewIds[14];
            _6_location = dayViewIds[15];
            _7 = dayViewIds[16];
            _7_location = dayViewIds[17];
            _8 = dayViewIds[18];
            _8_location = dayViewIds[19];
            timeOffSchoolId = dayViewIds[20];

            if (todayWeekDayId == weekdayCourse.weekDayId) {
                WidgetMain.setViewBackgroundColor(updateView, weekdayViewId, COLOR_WEEKDAY_TODAY);
            } else {
                WidgetMain.setViewBackgroundColor(updateView, weekdayViewId, COLOR_WEEKDAY);
            }

            final DayInfo publicHolidayInfo;
            if (curriculum.config.showCoursesInPublicHolidays) {
                publicHolidayInfo = null;
            } else {
                publicHolidayInfo = SettingsCalendarActivity.getPublicHolidayInfo(
                            context, weekdayCourse.date);
            }
            if (publicHolidayInfo != null && !TextUtils.isEmpty(publicHolidayInfo.label)) {
                noCourses(updateView, morningReadingId, eveningStudyId, _1, _1_location, _2,
                                _2_location, _3, _3_location, _4, _4_location, _5, _5_location, _6,
                                _6_location, _7, _7_location, _8, _8_location);
                initTimeOffSchool(updateView, null, timeOffSchoolId, timeOffSchoolViewHidden);
                // Show public holiday info in the morning reading view.
                showPublicHolidayInfo(updateView, morningReadingId, publicHolidayInfo);
                continue;
            }
            shortInfoMorningReading = weekdayCourse.getMorningReadingShortInfo(context, true);
            updateView.setTextViewText(morningReadingId, shortInfoMorningReading);
            if (TextUtils.isEmpty(shortInfoMorningReading)) {
                updateView.setViewVisibility(morningReadingId, View.INVISIBLE);
            } else {
                updateView.setViewVisibility(morningReadingId, View.VISIBLE);
                if (colorScheme == SettingsCurriculumActivity.CurriculumColorScheme.SameColor) {
                    updateView.setTextColor(morningReadingId, colorText);
                    WidgetMain.setViewBackgroundColor(updateView, morningReadingId,
                            colorBackground);
                } else {
                    updateView.setTextColor(morningReadingId,
                            weekdayCourse.getMorningReadingTextColor(true));
                    WidgetMain.setViewBackgroundColor(updateView, morningReadingId,
                            weekdayCourse.getMorningReadingBackgroundColor(true));
                }
            }
            shortInfoEveningStudy = weekdayCourse.getEveningStudyShortInfo(context, true);
            updateView.setTextViewText(eveningStudyId, shortInfoEveningStudy);
            if (TextUtils.isEmpty(shortInfoEveningStudy)) {
                updateView.setViewVisibility(eveningStudyId, View.INVISIBLE);
            } else {
                updateView.setViewVisibility(eveningStudyId, View.VISIBLE);
                if (colorScheme == SettingsCurriculumActivity.CurriculumColorScheme.SameColor) {
                    updateView.setTextColor(eveningStudyId, colorText);
                    WidgetMain.setViewBackgroundColor(updateView, eveningStudyId, colorBackground);
                } else {
                    updateView.setTextColor(eveningStudyId,
                            weekdayCourse.getEveningStudyTextColor(true));
                    WidgetMain.setViewBackgroundColor(updateView, eveningStudyId,
                            weekdayCourse.getEveningStudyBackgroundColor(true));
                }
            }

            initCoursesInfo(updateView, curriculum, weekdayCourse.getMorningCourses(true),
                            _1, _1_location, _2, _2_location, _3, _3_location, _4, _4_location);
            initCoursesInfo(updateView, curriculum, weekdayCourse.getAfternoonCourses(true),
                            _5, _5_location, _6, _6_location, _7, _7_location, _8, _8_location);
            initTimeOffSchool(updateView, weekdayCourse, timeOffSchoolId, timeOffSchoolViewHidden);
        }
    }

    private static void showPublicHolidayInfo(final RemoteViews updateView, final int viewId,
                    final DayInfo publicHolidayInfo) {
        final int viewBackgroundColor = publicHolidayInfo.getBackgroundColor();

        final int textRed = 255 - Color.red(viewBackgroundColor);
        final int textGreen = 255 - Color.green(viewBackgroundColor);
        final int textBlue = 255 - Color.blue(viewBackgroundColor);
        final int colorText;
        // 自己随便想了一个颜色，避免背景色和文字颜色对比不强烈. :)
        if (textRed + textGreen + textBlue < 381) {
            colorText = Color.BLACK;
        } else {
            colorText = Color.rgb(textRed, textGreen, textBlue);
        }

        updateView.setTextViewText(viewId, publicHolidayInfo.label);
        updateView.setTextColor(viewId, colorText);
        WidgetMain.setViewBackgroundColor(updateView, viewId, viewBackgroundColor);
        updateView.setViewVisibility(viewId, View.VISIBLE);
    }

    private static void noCourses(final RemoteViews updateView, final int... viewIds) {
        if (viewIds == null || viewIds.length <= 0) return;
        for (int viewId : viewIds) {
            updateView.setTextViewText(viewId, null);
            updateView.setViewVisibility(viewId, View.INVISIBLE);
        }
    }

    private static class CourseInfo {
        public Course course;

        public final int courseViewId;
        public final int courseLocationViewId;
        public final int courseIndex;

        public CourseInfo(final int courseViewId, final int courseLocationViewId,
                        final int courseIndex) {
            this.courseViewId = courseViewId;
            this.courseLocationViewId = courseLocationViewId;
            this.courseIndex = courseIndex;
        }
    }

    private static void initCourseInfoArray(final ArrayList<Course> courses,
            final CourseInfo[] courseInfoArray) {
        final int courseCount = courses.size();
        for (CourseInfo courseInfo : courseInfoArray) {
            if (courseInfo.courseIndex < 0 || courseInfo.courseIndex >= courseCount) {
                courseInfo.course = Course.NULL;
            } else {
                courseInfo.course = courses.get(courseInfo.courseIndex);
            }
        }
    }

    private static void initTimeOffSchool(final RemoteViews updateView,
                    final WeekDayCourse weekdayCourse, final int timeOffSchoolViewId,
                    final int viewHidden) {
        final TimeOffSchool timeOffSchool = weekdayCourse == null ? null
                        : weekdayCourse.getTimeOffSchool(true);
        if (timeOffSchool == null) {
            updateView.setViewVisibility(timeOffSchoolViewId, viewHidden);
        } else {
            updateView.setViewVisibility(timeOffSchoolViewId, View.VISIBLE);
            updateView.setTextViewText(timeOffSchoolViewId, timeOffSchool.timeOffSchoolString());
            updateView.setTextColor(timeOffSchoolViewId, timeOffSchool.colorText);
            WidgetMain.setViewBackgroundColor(updateView, timeOffSchoolViewId,
                            timeOffSchool.colorBackground);
        }
    }

    private static void initCoursesInfo(final RemoteViews updateView, final Curriculum curriculum,
                    final ArrayList<Course> courses, final int... viewIds) {
        CourseInfo[] courseInfos = new CourseInfo[viewIds.length / 2];
        for (int i = 0; i < courseInfos.length; i++) {
            courseInfos[i] = new CourseInfo(viewIds[2 * i], viewIds[2 * i + 1], i);
        }
        updateViews(updateView, curriculum, courseInfos, courses);
    }

    private static void updateViews(final RemoteViews updateView, final Curriculum curriculum,
                    final CourseInfo[] courseInfos, final ArrayList<Course> courses) {
        final CurriculumColorScheme colorScheme = curriculum.getColorScheme();
        final int colorText = curriculum.getCourseColorText();
        final int colorBackground = curriculum.getCourseColorBackground();

        initCourseInfoArray(courses, courseInfos);

        final boolean showClassTime = curriculum.config.showClassTimeInWidget;
        final int courseLabelMaxLength = showClassTime ? 3 : 4;
        final int courseLabelDefaultFontSizeSp = 12;

        int courseViewId, courseLocationViewId;
        int colorCourseBackground;
        String courseName, courseLocationInfo;
        for (CourseInfo courseInfo : courseInfos) {
            courseViewId = courseInfo.courseViewId;
            courseLocationViewId = courseInfo.courseLocationViewId;
            if (Course.isNullCourse(courseInfo.course)) {
                updateView.setViewVisibility(courseViewId, View.INVISIBLE);
                updateView.setViewVisibility(courseLocationViewId, View.INVISIBLE);
                continue;
            }
            courseName = courseInfo.course.name;
            updateView.setViewVisibility(courseViewId, View.VISIBLE);
            updateView.setTextViewText(courseViewId, courseName);
            setTextViewMaxLength(updateView, courseViewId, courseName, courseLabelMaxLength,
                            courseLabelDefaultFontSizeSp);

            courseLocationInfo = courseInfo.course.locationInfo();
            if (TextUtils.isEmpty(courseLocationInfo)) {
                updateView.setViewVisibility(courseLocationViewId, View.GONE);
            } else {
                updateView.setViewVisibility(courseLocationViewId, View.VISIBLE);
                updateView.setTextViewText(courseLocationViewId, courseLocationInfo);
            }

            switch (colorScheme) {
                case SameColor:
                    updateView.setTextColor(courseViewId, colorText);
                    WidgetMain.setViewBackgroundColor(updateView, courseViewId, colorBackground);
                    break;
                case DifferentColors:
                default:
                    updateView.setTextColor(courseViewId, courseInfo.course.getColorText());
                    colorCourseBackground = courseInfo.course.getColorBackground();
                    WidgetMain.setViewBackgroundColor(updateView, courseViewId,
                                    colorCourseBackground);
                    break;
            }
        }
    }

    // 如果TextView中的字数超长，适当 减小字体大小.( 当前策略很简单... :) )
    private static void setTextViewMaxLength(final RemoteViews updateView, final int viewId,
                    final String viewText, final int maxLength, final int defaultTextSizeSP) {
        int textLength = viewText.length();
        int textSizeSP = defaultTextSizeSP;
        if (textLength > maxLength) {
            textSizeSP = defaultTextSizeSP - (textLength - maxLength) - 2;
        }
        updateView.setTextViewTextSize(viewId, TypedValue.COMPLEX_UNIT_SP, textSizeSP);
    }
}
