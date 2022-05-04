
package wb.widget;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;
import wb.widget.SettingsWeekNotesActivity.WeekDayNotes;
import wb.widget.model.Day;
import wb.widget.model.Note;
import wb.widget.model.SharedPreferencesHelper;
import wb.widget.utils.Utility;

public class WidgetWeekNotes {

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
    }

    private static PendingIntent getIntentToSettings(final Context context) {
        final Class<?> activityClass = SettingsWeekNotesActivity.class;
        Intent startActivityIntent = new Intent(context, activityClass);
        final PendingIntent launchSettingsPendingIntent = PendingIntent.getActivity(context,
                0 /* no requestCode */, startActivityIntent, 0 /* no flags */);
        return launchSettingsPendingIntent;
    }

    private static void initViews(final Context context, final RemoteViews updateView,
            final int firstDayOfWeek, final Day today, final SharedPreferencesHelper prefsHelper) {
        updateView.setTextViewText(R.id.info_text, Utility.getDayText(context, today));

        // For week notes label.
        if (!initLabelView(context, updateView, prefsHelper)) {
            Note.initDefaultNotes(context);
            SettingsWeekNotesActivity.generateRandomDayNotes();
            // saveDayNotes(prefHelper);
        }

        SettingsWeekNotesActivity.initDayNotesConfig(updateView);
        SettingsWeekNotesActivity.loadColorScheme(prefsHelper);
        SettingsWeekNotesActivity.fillWeekDates(today);

        final boolean showNotesInPublicHolidays = SettingsWeekNotesActivity
                .loadShowNotesInPublicHolidays(prefsHelper);
        initDayViews(context, updateView, today.weekday, showNotesInPublicHolidays);
    }

    private static boolean initLabelView(final Context context, final RemoteViews updateView,
            final SharedPreferencesHelper prefsHelper) {
        final int labelViewId = R.id.view_week_notes_label;
        final boolean weekNotesSet = SettingsWeekNotesActivity.loadWeekDayNotes(prefsHelper);
        String weekNotesLabel;
        if (!weekNotesSet) {
            weekNotesLabel = context.getString(R.string.prompt_demo_fill_week_notes);
            updateView.setTextViewText(labelViewId, weekNotesLabel);
            return false;
        }

        weekNotesLabel = SettingsWeekNotesActivity.loadLabel(prefsHelper);
        if (TextUtils.isEmpty(weekNotesLabel)) {
            weekNotesLabel = context.getString(R.string.prompt_set_week_notes_label);
        } else {
            SettingsWeekNotesActivity.loadLabelColors(prefsHelper);
            int labelColorText = SettingsWeekNotesActivity.getLabelColorText();
            updateView.setTextColor(labelViewId, labelColorText);
            int labelColorBackground = SettingsWeekNotesActivity.getLabelColorBackground();
            WidgetMain.setViewBackgroundColor(updateView, labelViewId,
                    labelColorBackground);
        }
        updateView.setTextViewText(labelViewId, weekNotesLabel);
        return true;
    }

    private static final int[][] sDayNotesViewMapping = {
            {
                Calendar.MONDAY, R.id.weekday_monday,
                R.id.monday_morning_note, R.id.monday_evening_note,
                R.id._108, R.id._109, R.id._110, R.id._111, R.id._112, R.id._113, R.id._114,
                R.id._115, R.id._116, R.id._117,
            },
            {
                Calendar.TUESDAY, R.id.weekday_tuesday,
                R.id.tuesday_morning_note, R.id.tuesday_evening_note,
                R.id._208, R.id._209, R.id._210, R.id._211, R.id._212, R.id._213, R.id._214,
                R.id._215, R.id._216, R.id._217,
            },
            {
                Calendar.WEDNESDAY, R.id.weekday_wednesday,
                R.id.wednesday_morning_note, R.id.wednesday_evening_note,
                R.id._308, R.id._309, R.id._310, R.id._311, R.id._312, R.id._313, R.id._314,
                R.id._315, R.id._316, R.id._317,
            },
            {
                Calendar.THURSDAY, R.id.weekday_thursday,
                R.id.thursday_morning_note, R.id.thursday_evening_note,
                R.id._408, R.id._409, R.id._410, R.id._411, R.id._412, R.id._413, R.id._414,
                R.id._415, R.id._416, R.id._417,
            },
            {
                Calendar.FRIDAY, R.id.weekday_friday,
                R.id.friday_morning_note, R.id.friday_evening_note,
                R.id._508, R.id._509, R.id._510, R.id._511, R.id._512, R.id._513, R.id._514,
                R.id._515, R.id._516, R.id._517,
            },
            {
                Calendar.SATURDAY, R.id.weekday_saturday,
                R.id.saturday_morning_note, R.id.saturday_evening_note,
                R.id._608, R.id._609, R.id._610, R.id._611, R.id._612, R.id._613, R.id._614,
                R.id._615, R.id._616, R.id._617,
            },
            {
                Calendar.SUNDAY, R.id.weekday_sunday,
                R.id.sunday_morning_note, R.id.sunday_evening_note,
                R.id._708, R.id._709, R.id._710, R.id._711, R.id._712, R.id._713, R.id._714,
                R.id._715, R.id._716, R.id._717,
            },
    };

    private static int[] getDayViews(final int weekDayResId) {
        for (int[] dayViews : sDayNotesViewMapping) {
            if (dayViews[0] == weekDayResId) return dayViews;
        }
        return null;
    }

    private static final int COLOR_WEEKDAY_TODAY = Color.YELLOW;
    private static final int COLOR_WEEKDAY = Color.rgb(0xAD, 0xD8, 0xE6);

    private static void initDayViews(final Context context, final RemoteViews updateView,
            final int todayWeekDayId, final boolean showNotesInPublichHolidays) {
        final SettingsWeekNotesActivity.WeekNotesColorScheme colorScheme = SettingsWeekNotesActivity
                .getColorScheme();
        final int colorText = SettingsWeekNotesActivity.getNoteColorText();
        final int colorBackground = SettingsWeekNotesActivity.getNoteColorBackground();

        int[] dayViewIds;
        int weekdayViewId, morningViewId, eveningViewId;
        int _08, _09, _10, _11, _12, _13, _14, _15, _16, _17;
        String shortInfoMorning, shortInfoEvening;
        for (WeekDayNotes weekDayNotes : SettingsWeekNotesActivity.sWeekDayNotes) {
            dayViewIds = getDayViews(weekDayNotes.weekDayId);
            if (dayViewIds == null) continue;

            weekdayViewId = dayViewIds[1];
            morningViewId = dayViewIds[2];
            eveningViewId = dayViewIds[3];
            _08 = dayViewIds[4];
            _09 = dayViewIds[5];
            _10 = dayViewIds[6];
            _11 = dayViewIds[7];
            _12 = dayViewIds[8];
            _13 = dayViewIds[9];
            _14 = dayViewIds[10];
            _15 = dayViewIds[11];
            _16 = dayViewIds[12];
            _17 = dayViewIds[13];

            if (todayWeekDayId == weekDayNotes.weekDayId) {
                WidgetMain.setViewBackgroundColor(updateView, weekdayViewId, COLOR_WEEKDAY_TODAY);
            } else {
                WidgetMain.setViewBackgroundColor(updateView, weekdayViewId, COLOR_WEEKDAY);
            }

            if (!showNotesInPublichHolidays
                    && SettingsCalendarActivity.isPublicHoliday(context, weekDayNotes.date)) {
                noNotes(updateView, morningViewId, eveningViewId, _08, _09, _10, _11, _12, _13, _14,
                        _15, _16, _17);
                continue;
            }
            shortInfoMorning = weekDayNotes.getMorningShortInfo(context);
            updateView.setTextViewText(morningViewId, shortInfoMorning);
            if (TextUtils.isEmpty(shortInfoMorning)) {
                updateView.setViewVisibility(morningViewId, View.INVISIBLE);
            } else {
                updateView.setViewVisibility(morningViewId, View.VISIBLE);
                if (colorScheme == SettingsWeekNotesActivity.WeekNotesColorScheme.SameColor) {
                    updateView.setTextColor(morningViewId, colorText);
                    WidgetMain.setViewBackgroundColor(updateView, morningViewId,
                            colorBackground);
                } else {
                    updateView.setTextColor(morningViewId, weekDayNotes.getMorningTextColor());
                    WidgetMain.setViewBackgroundColor(updateView, morningViewId,
                            weekDayNotes.getMorningBackgroundColor());
                }
            }
            shortInfoEvening = weekDayNotes.getEveningShortInfo(context);
            updateView.setTextViewText(eveningViewId, shortInfoEvening);
            if (TextUtils.isEmpty(shortInfoEvening)) {
                updateView.setViewVisibility(eveningViewId, View.INVISIBLE);
            } else {
                updateView.setViewVisibility(eveningViewId, View.VISIBLE);
                if (colorScheme == SettingsWeekNotesActivity.WeekNotesColorScheme.SameColor) {
                    updateView.setTextColor(eveningViewId, colorText);
                    WidgetMain.setViewBackgroundColor(updateView, eveningViewId,
                            colorBackground);
                } else {
                    updateView.setTextColor(eveningViewId, weekDayNotes.getEveningTextColor());
                    WidgetMain.setViewBackgroundColor(updateView, eveningViewId,
                            weekDayNotes.getEveningBackgroundColor());
                }
            }

            initNotesInfo(updateView, weekDayNotes.getAmNotes(), _08, _09, _10, _11, _12);
            initNotesInfo(updateView, weekDayNotes.getPmNotes(), _13, _14, _15, _16, _17);
        }
    }

    private static void noNotes(final RemoteViews updateView, final int... viewIds) {
        if (viewIds == null || viewIds.length <= 0) return;
        for (int viewId : viewIds) {
            updateView.setTextViewText(viewId, null);
            updateView.setViewVisibility(viewId, View.INVISIBLE);
        }
    }

    private static class NoteInfo {
        public Note note;
        public final int noteViewId;
        public final int noteIndex;

        public NoteInfo(final int noteViewId, final int noteIndex) {
            this.noteViewId = noteViewId;
            this.noteIndex = noteIndex;
        }
    }

    private static void initNoteInfoArray(final ArrayList<Note> noteList,
            final NoteInfo[] noteInfoArray) {
        final int noteCount = noteList.size();
        for (NoteInfo noteInfo : noteInfoArray) {
            if (noteInfo.noteIndex < 0 || noteInfo.noteIndex >= noteCount) {
                noteInfo.note = null;
            } else {
                noteInfo.note = noteList.get(noteInfo.noteIndex);
            }
        }
    }

    private static void initNotesInfo(final RemoteViews updateView, final ArrayList<Note> noteList,
            final int... noteViewIds) {
        NoteInfo[] noteInfos = new NoteInfo[noteViewIds.length];
        for (int i = 0; i < noteInfos.length; i++) {
            noteInfos[i] = new NoteInfo(noteViewIds[i], i);
        }
        updateViews(updateView, noteInfos, noteList);
    }

    private static void updateViews(final RemoteViews updateView, final NoteInfo[] noteInfos,
            final ArrayList<Note> noteList) {
        final SettingsWeekNotesActivity.WeekNotesColorScheme colorScheme = SettingsWeekNotesActivity
                .getColorScheme();
        final int colorText = SettingsWeekNotesActivity.getNoteColorText();
        final int colorBackground = SettingsWeekNotesActivity.getNoteColorBackground();

        initNoteInfoArray(noteList, noteInfos);
        int viewId, colorCourseBackground;
        for (NoteInfo noteInfo : noteInfos) {
            viewId = noteInfo.noteViewId;
            if (noteInfo.note == null) {
                updateView.setViewVisibility(viewId, View.INVISIBLE);
            } else {
                updateView.setViewVisibility(viewId, View.VISIBLE);
                updateView.setTextViewText(viewId, noteInfo.note.shortText());

                if (colorScheme == SettingsWeekNotesActivity.WeekNotesColorScheme.SameColor) {
                    updateView.setTextColor(viewId, colorText);
                    WidgetMain.setViewBackgroundColor(updateView, viewId, colorBackground);
                } else {
                    updateView.setTextColor(viewId, noteInfo.note.getColorText());
                    colorCourseBackground = noteInfo.note.getColorBackground();
                    WidgetMain.setViewBackgroundColor(updateView, viewId, colorCourseBackground);
                }
            }
        }
    }
}
