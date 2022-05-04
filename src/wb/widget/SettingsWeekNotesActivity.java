package wb.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import wb.widget.model.ColorPickerDialog;
import wb.widget.model.Day;
import wb.widget.model.Note;
import wb.widget.model.SharedPreferencesHelper;
import wb.widget.model.WeekNotes;
import wb.widget.utils.Utility;
import wb.widget.utils.calendar.Constants;
import wb.widget.utils.calendar.Constants.WeekDay;

public class SettingsWeekNotesActivity extends Activity {
    // Preferences for generic settings.
    private static final String PREF_KEY_WEEK_NOTES_LABEL = "note_label";
    private static final String PREF_KEY_COLORS_WEEK_NOTES_LABEL = "colors_note_label";
    private static final String PREF_KEY_COLOR_SCHEME_NOTES = "week_notes_color_scheme";
    private static final String PREF_KEY_COLORS_NOTE = "colors_note";
    private static final String PREF_KEY_NOTES = "notes";
    private static final String PREF_KEY_DATE_NOTES = "date_notes";
    private static final String PREF_KEY_SHOW_NOTES_IN_PUBLIC_HOLIDAYS = "show_notes_in_public_holidays";
    // Preferences for week day notes.
    private static final String PREF_KEY_NOTES_MONDAY = "notes_monday";
    private static final String PREF_KEY_NOTES_TUESDAY = "notes_tuesday";
    private static final String PREF_KEY_NOTES_WEDNESDAY = "notes_wednesday";
    private static final String PREF_KEY_NOTES_THURSDAY = "notes_thursday";
    private static final String PREF_KEY_NOTES_FRIDAY = "notes_friday";
    private static final String PREF_KEY_NOTES_SATURDAY = "notes_saturday";
    private static final String PREF_KEY_NOTES_SUNDAY = "notes_sunday";

    private static boolean sSettingsLoaded = false;
    private static boolean sWeekNotesSet = false;
    private static WeekNotesColorScheme sColorScheme;
    private static int sNoteColorText, sNoteColorBackground;
    private static String sLabel;
    private static int sLabelTextColor, sLabelBackgroundColor;
    private static final ArrayList<DateNotes> sDateNotes = new ArrayList<DateNotes>();

    private static final int DEFAULT_COLOR_LABEL_TEXT = Color.MAGENTA;
    private static final int DEFAULT_COLOR_LABEL_BACKGROUND = Color.BLACK;

    // Format: colorText,colorBackground
    private static final String SEP_COLOR = ",";
    private static final String FORMAT_COLORS = "%d" + SEP_COLOR + "%d";

    private static boolean parseLabelColors(final String colors) {
        if (TextUtils.isEmpty(colors)) return false;
        String[] elements = colors.split(SEP_COLOR);
        sLabelTextColor = Integer.parseInt(elements[0]);
        sLabelBackgroundColor = Integer.parseInt(elements[1]);
        return true;
    }

    private static boolean parseNoteColors(final String colors) {
        if (TextUtils.isEmpty(colors)) return false;
        String[] elements = colors.split(SEP_COLOR);
        sNoteColorText = Integer.parseInt(elements[0]);
        sNoteColorBackground = Integer.parseInt(elements[1]);
        return true;
    }

    public static enum WeekNotesColorScheme {
        SameColor(R.id.radio_same_color),
        DifferentColors(R.id.radio_different_colors);

        public final int radioResId;

        private WeekNotesColorScheme(final int radioResId) {
            this.radioResId = radioResId;
        }

        public static WeekNotesColorScheme get(final int ordinal) {
            for (WeekNotesColorScheme setting : values()) {
                if (setting.ordinal() == ordinal) return setting;
            }
            return SameColor;
        }

        public static WeekNotesColorScheme getByRadio(final int radioId) {
            for (WeekNotesColorScheme setting : values()) {
                if (setting.radioResId == radioId) return setting;
            }
            return SameColor;
        }
    }

    protected static final WeekDayNotes[] sWeekDayNotes = {
            new WeekDayNotes(Calendar.MONDAY,    R.id.notes_monday,    PREF_KEY_NOTES_MONDAY),
            new WeekDayNotes(Calendar.TUESDAY,   R.id.notes_tuesday,   PREF_KEY_NOTES_TUESDAY),
            new WeekDayNotes(Calendar.WEDNESDAY, R.id.notes_wednesday, PREF_KEY_NOTES_WEDNESDAY),
            new WeekDayNotes(Calendar.THURSDAY,  R.id.notes_thursday,  PREF_KEY_NOTES_THURSDAY),
            new WeekDayNotes(Calendar.FRIDAY,    R.id.notes_friday,    PREF_KEY_NOTES_FRIDAY),
            new WeekDayNotes(Calendar.SATURDAY,  R.id.notes_saturday,  PREF_KEY_NOTES_SATURDAY),
            new WeekDayNotes(Calendar.SUNDAY,    R.id.notes_sunday,    PREF_KEY_NOTES_SUNDAY),
    };

    public static String loadLabel(final SharedPreferencesHelper prefHelper) {
        sLabel = (String) prefHelper.get(PREF_KEY_WEEK_NOTES_LABEL, null);
        return sLabel;
    }

    private static void saveLabel(final SharedPreferencesHelper prefHelper) {
        prefHelper.put(PREF_KEY_WEEK_NOTES_LABEL, sLabel);
    }

    public static void loadLabelColors(final SharedPreferencesHelper prefHelper) {
        final String key = PREF_KEY_COLORS_WEEK_NOTES_LABEL;
        boolean colorSaved = false;
        if (prefHelper.contains(key)) {
            String colors = (String) prefHelper.get(key, null);
            colorSaved = parseLabelColors(colors);
        }
        if (!colorSaved) {
            sLabelBackgroundColor = DEFAULT_COLOR_LABEL_BACKGROUND;
            sLabelTextColor = DEFAULT_COLOR_LABEL_TEXT;
            saveLabelColors(prefHelper);
        }
    }

    public static void loadColorScheme(final SharedPreferencesHelper prefHelper) {
        final String key = PREF_KEY_COLOR_SCHEME_NOTES;
        if (prefHelper.contains(key)) {
            int ordinal = (Integer) prefHelper.get(key, 0);
            sColorScheme = WeekNotesColorScheme.get(ordinal);
            loadNoteColors(prefHelper);
        } else {
            sColorScheme = WeekNotesColorScheme.SameColor;
            saveColorSetting(prefHelper);
            sNoteColorText = Note.DEFAULT_COLOR_NOTE_TEXT;
            sNoteColorBackground = Note.DEFAULT_COLOR_NOTE_BACKGROUND;
            saveNoteColors(prefHelper);
        }
    }

    public static WeekNotesColorScheme getColorScheme() {
        return sColorScheme;
    }

    public static int getNoteColorText() {
        return sNoteColorText;
    }

    public static int getNoteColorBackground() {
        return sNoteColorBackground;
    }

    public static int getLabelColorText() {
        return sLabelTextColor;
    }

    public static int getLabelColorBackground() {
        return sLabelBackgroundColor;
    }

    public static void loadNoteColors(final SharedPreferencesHelper prefHelper) {
        final String key = PREF_KEY_COLORS_NOTE;
        boolean colorSaved = false;
        if (prefHelper.contains(key)) {
            String colors = (String) prefHelper.get(key, null);
            colorSaved = parseNoteColors(colors);
        }
        if (!colorSaved) {
            sNoteColorText = Note.DEFAULT_COLOR_NOTE_TEXT;
            sNoteColorBackground = Note.DEFAULT_COLOR_NOTE_BACKGROUND;
            saveNoteColors(prefHelper);
        }
    }

    public static void loadNotes(final SharedPreferencesHelper prefHelper) {
        Note.clearNotes();
        String notesString = (String) prefHelper.get(PREF_KEY_NOTES, null);
        if (TextUtils.isEmpty(notesString)) {
            return;
        }
        Note.parseNotes(notesString);
    }

    public static void loadDateNotes(final SharedPreferencesHelper prefHelper) {
        String dateNotesString = (String) prefHelper.get(PREF_KEY_DATE_NOTES, null);
        if (TextUtils.isEmpty(dateNotesString)) {
            return;
        }
        DateNotes.parseDateNotes(dateNotesString);
    }

    public static boolean loadShowNotesInPublicHolidays(final SharedPreferencesHelper prefHelper) {
        Boolean showNotesInPublicHolidays = (Boolean) prefHelper
                .get(PREF_KEY_SHOW_NOTES_IN_PUBLIC_HOLIDAYS, false);
        return showNotesInPublicHolidays;
    }

    private static void saveShowNotesInPublicHolidays(final SharedPreferencesHelper prefHelper,
            final boolean showNotesInPublicHodliays) {
        prefHelper.put(PREF_KEY_SHOW_NOTES_IN_PUBLIC_HOLIDAYS,
                Boolean.valueOf(showNotesInPublicHodliays));
    }

    private static void saveColorSetting(final Context context) {
        SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);
        saveColorSetting(prefHelper);
        saveNoteColors(prefHelper);
    }

    private static void saveColorSetting(final SharedPreferencesHelper prefHelper) {
        prefHelper.put(PREF_KEY_COLOR_SCHEME_NOTES, sColorScheme.ordinal());
    }

    private static void saveNoteColors(final Context context) {
        SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);
        saveNoteColors(prefHelper);
    }

    private static void saveNoteColors(final SharedPreferencesHelper prefHelper) {
        String colors = String.format(FORMAT_COLORS, sNoteColorText, sNoteColorBackground);
        prefHelper.put(PREF_KEY_COLORS_NOTE, colors);
    }

    private static void saveLabelColors(final SharedPreferencesHelper prefHelper) {
        String colors = String.format(FORMAT_COLORS, sLabelTextColor, sLabelBackgroundColor);
        prefHelper.put(PREF_KEY_COLORS_WEEK_NOTES_LABEL, colors);
    }

    public static void saveNotes(final SharedPreferencesHelper prefHelper) {
        prefHelper.put(PREF_KEY_NOTES, Note.notesString());
    }

    public static boolean loadWeekDayNotes(final SharedPreferencesHelper prefHelper) {
        if (Note.getNoteCount() <= 0) {
            loadNotes(prefHelper);
        }
        boolean weekNotesSet = false;
        String curKey, weekdayNotesString;
        for (WeekDayNotes weekDayNotes : sWeekDayNotes) {
            curKey = weekDayNotes.prefKey;
            if (prefHelper.contains(curKey)) {
                weekNotesSet = true;
            } else {
                continue;
            }
            weekdayNotesString = (String) prefHelper.get(curKey, null);
            Log.d("WB", weekdayNotesString);
            weekDayNotes.parseWeekDayNotes(weekdayNotesString);
        }
        return sWeekNotesSet = weekNotesSet;
    }

    private static void saveDayNotes(final SharedPreferencesHelper prefHelper) {
        String weekdayNotesString;
        boolean weekNotesSet = false;
        for (WeekDayNotes weekdayNotes : sWeekDayNotes) {
            weekdayNotesString = weekdayNotes.toString();
            if (TextUtils.isEmpty(weekdayNotesString)) {
                prefHelper.remove(weekdayNotes.prefKey);
            } else {
                weekNotesSet = true;
                prefHelper.put(weekdayNotes.prefKey, weekdayNotesString);
            }
        }
        sWeekNotesSet = weekNotesSet;
    }

    private static void saveDateNotes(final Context context) {
        SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);
        final String key = PREF_KEY_DATE_NOTES;
        StringBuilder sb = new StringBuilder();
        for (DateNotes dateNotes : sDateNotes) {
            if (sb.length() > 0) sb.append(SEP_DATE);
            sb.append(dateNotes.toString());
        }
        prefHelper.put(key, sb.toString());
    }

    private static void removeWeekNotesPreferences(final Context context) {
        SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);
        for (WeekDayNotes weekdayCourse : sWeekDayNotes) {
            prefHelper.remove(weekdayCourse.prefKey);
        }
        sWeekNotesSet = false;
    }

    private Button mButtonSetNotesOnDate;
    private View mViewFillWeekNotesPrompt;
    private View mViewExport;

    private WeekNotes mWeekNotes;

    private interface CellNoteChangeListener {
        public void whenNoteChanged();
        public void setCellNote(final int weekdayId, final int viewId, final Note note);
    }

    private final WeekNotes.WeekNotesListener mWeekNotesListener = new WeekNotes.WeekNotesListener() {
        @Override
        public void showSetNoteDialog(final Context context, final int weekdayId, final int viewId,
                final int viewLabelResId) {
            final Note noteInput = WeekDayNotes.getNote(weekdayId, viewLabelResId);
            Log.d("WB", "mWeekNotesListener.showSetNoteDialog, note:" + noteInput);
            final CellNoteChangeListener cellNoteChangeListener = new CellNoteChangeListener() {
                @Override
                public void setCellNote(final int weekdayId, final int viewId, final Note note) {
                    setNote(weekdayId, viewId, note);
                    saveWeekDayNotes(context);
                }

                @Override
                public void whenNoteChanged() {
                    setWeekNotes();
                    saveWeekDayNotes(context);
                }
            };
            showSetCellNoteDialog(context, null, weekdayId, viewId, viewLabelResId, noteInput,
                    cellNoteChangeListener);
        }
    };

    private static final WeekNotes.DayNotesListener sDayNotesListener = new WeekNotes.DayNotesListener() {
        @Override
        public void showSetNoteDialog(Context context, WeekNotes.DayNoteViewInfo dayNoteViewInfo,
                int viewId, int viewLabelResId, Note noteInput) {
            showSetCellNoteDialog(context, dayNoteViewInfo, -1, viewId, viewLabelResId, noteInput,
                    null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.settings_week_notes);

        loadSettings(this);

        initViews();

        checkWeekNotesSet();

        super.onCreate(savedInstanceState);
    }

    private void initViews() {
        final Context context = this;
        SettingsUtils.initGeneric(this);

        initDayNotesConfig(this);

        initCheckBox(context);

        initLabelViews(context);
        initButtons(context);

        initColorSchemeViews(context);
        initDemoViews(context);

        initWeekNoteViews();

        refreshColorViewsShow();
    }

    private void initDayNotesConfig(final Activity activity) {
        AmNote.initViews(activity);
        PmNote.initViews(activity);
    }

    private void initCheckBox(final Context context) {
        final SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);

        final CheckBox checkBoxShowNotesInPublicHolidays = (CheckBox) findViewById(
                R.id.checkbox_show_notes_public_holidays);
        checkBoxShowNotesInPublicHolidays.setChecked(loadShowNotesInPublicHolidays(prefHelper));
        checkBoxShowNotesInPublicHolidays.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveShowNotesInPublicHolidays(prefHelper, isChecked);
                Constants.refreshWidget(context);
            }
        });
    }

    private TextView refreshLabelViews(final Context context) {
        final TextView viewLabel = findViewById(R.id.view_week_notes_label);
        final TextView viewSetLabel = findViewById(R.id.view_set_week_notes_label);
        if (TextUtils.isEmpty(sLabel)) {
            if (sWeekNotesSet) {
                viewLabel.setText(R.string.prompt_set_week_notes_label);
                viewLabel.setTextColor(sLabelTextColor);
                viewLabel.setBackgroundColor(sLabelBackgroundColor);
            } else {
                viewLabel.setText(R.string.prompt_demo_fill_week_notes);
                viewLabel.setTextColor(DEFAULT_COLOR_LABEL_TEXT);
                viewLabel.setBackgroundColor(DEFAULT_COLOR_LABEL_BACKGROUND);
            }
            viewSetLabel.setText(R.string.set_week_notes_label);
        } else {
            viewLabel.setText(sLabel);
            viewLabel.setTextColor(sLabelTextColor);
            viewLabel.setBackgroundColor(sLabelBackgroundColor);

            viewSetLabel.setText(R.string.edit_week_notes_label);
        }
        return viewSetLabel;
    }

    private void initLabelViews(final Context context) {
        final TextView viewSetLabel = refreshLabelViews(context);
        viewSetLabel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditLabelDialog(context);
            }
        });
    }

    private void initButtons(final Context context) {
        final View viewReset = findViewById(R.id.view_reset_week_notes);
        viewReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetConfirmDialog(context);
            }
        });

        final View viewImport = findViewById(R.id.view_import_week_notes);
        viewImport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showImportDialog(context);
            }
        });
        mViewExport = findViewById(R.id.view_export_week_notes);
        mViewExport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showExportDialog(context);
            }
        });

        mButtonSetNotesOnDate = findViewById(R.id.button_set_date_notes);
        mButtonSetNotesOnDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetDateNotesDialog(context);
            }
        });

        mViewFillWeekNotesPrompt = findViewById(R.id.view_prompt_fill_week_notes);
    }

    private void initColorSchemeViews(final Context context) {
        final RadioGroup radioGroupColorScheme = (RadioGroup) findViewById(
                R.id.radiogroup_color_scheme);
        radioGroupColorScheme.check(sColorScheme.radioResId);
        radioGroupColorScheme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                sColorScheme = WeekNotesColorScheme.getByRadio(checkedId);
                saveColorSetting(context);
                refreshColorViewsShow();
                Constants.refreshWidget(context);
            }
        });
    }

    private void initDemoViews(final Context context) {
        final View viewChangeColors = findViewById(R.id.view_change_colors);
        viewChangeColors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = context.getString(R.string.change_colors);
                final String text = context.getString(R.string.text_note);
                final ColorPickerDialog.OnColorSetListener colorSetListener = new ColorPickerDialog.OnColorSetListener() {
                    @Override
                    public void onColorSet(int textColor, int backgroundColor) {
                        sNoteColorBackground = backgroundColor;
                        sNoteColorText = textColor;
                        saveNoteColors(context);
                        refreshColorViewsShow();
                        Constants.refreshWidget(context);
                    }
                };
                showColorSetDialog(context, title, text, colorSetListener,
                        sNoteColorText, sNoteColorBackground);
            }
        });
    }

    private void refreshColorViewsShow() {
        mWeekNotes.setColorScheme(sColorScheme, sNoteColorText, sNoteColorBackground);

        final View viewColorDemos = findViewById(R.id.view_color_demos);
        switch (sColorScheme) {
            case SameColor:
                viewColorDemos.setVisibility(View.VISIBLE);
                break;
            case DifferentColors:
                viewColorDemos.setVisibility(View.GONE);
                return;
        }
        final TextView textViewCourse = findViewById(R.id.demo_note);
        textViewCourse.setTextColor(sNoteColorText);
        textViewCourse.setBackgroundColor(sNoteColorBackground);
    }

    private void initWeekNoteViews() {
        final Activity activity = this;
        mWeekNotes = new WeekNotes(mWeekNotesListener);
        mWeekNotes.initViews(activity);
        setWeekNotes();
    }

    private void checkWeekNotesSet() {
        if (sWeekNotesSet) {
            mViewFillWeekNotesPrompt.setVisibility(View.GONE);
            mViewExport.setVisibility(View.VISIBLE);
            mButtonSetNotesOnDate.setVisibility(View.VISIBLE);

            refreshWeekDayNotes(this);
        } else {
            mViewFillWeekNotesPrompt.setVisibility(View.VISIBLE);
            mButtonSetNotesOnDate.setVisibility(View.GONE);
            mViewExport.setVisibility(View.GONE);
        }
    }

    private void saveWeekDayNotes(final Context context) {
        SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);
        saveNotes(prefHelper);
        saveDayNotes(prefHelper);
        Constants.refreshWidget(context);
        checkWeekNotesSet();
    }

    private static void showClearNoteConfirm(final Context context, final String noteTime,
            final Note note, final NoteActionListener noteActionListener) {
        final String formatNoteInfo = "%s:\n%s\n%s";
        final String noteInfo = String.format(formatNoteInfo, noteTime, note.textFull,
                note.textShort);
        final String confirmPrompt = context.getString(R.string.confirm_clear_note, noteInfo);
        final DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                noteActionListener.clearNote();
            }
        };
        Utility.showConfirmDialog(context, confirmPrompt, positiveButtonListener);
    }

    private void setWeekNotes() {
        final Context context = this;
        mWeekNotes.setWeekNotes(context, sWeekDayNotes);
    }

    private void setNote(final int weekdayId, final int viewId, final Note note) {
        final TextView view = findViewById(viewId);
        if (note == null) {
            noNote(view);
            return;
        }
        view.setText(note.shortText());
        view.setTextColor(note.getColorText());
        view.setBackgroundColor(note.getColorBackground());
    }

    private static void noNote(final TextView textView) {
        textView.setText(R.string.set_note);
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundColor(Color.GRAY);
    }

    private void showResetConfirmDialog(final Context context) {
        final String confirmPrompt = context.getString(R.string.confirm_clear_week_notes);
        final DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetWeekNotes(context);
            }
        };
        Utility.showConfirmDialog(context, confirmPrompt, positiveButtonListener);
    }

    private void clearWeekNotes(final Context context) {
        for (WeekDayNotes weekdayCourse : sWeekDayNotes) {
            weekdayCourse.dayNotes.init();
        }
        mWeekNotes.clear();
    }

    private void resetWeekNotes(final Context context) {
        clearWeekNotes(context);
        removeWeekNotesPreferences(context);
        Constants.refreshWidget(context);
        checkWeekNotesSet();
    }

    private void showImportDialog(final Context context) {
        Utility.showInfoDialog(context, "Sorry! Not implemented!");
    }

    private void showExportDialog(final Context context) {
        Utility.showInfoDialog(context, "Sorry! Not implemented!");
    }

    private static void showSetCellNoteDialog(final Context context,
            final WeekNotes.DayNoteViewInfo dayNoteViewInfo, final int weekdayId,
            final int viewId, final int viewLabelResId, final Note noteInput,
            final CellNoteChangeListener cellNoteChangeListener) {
        final String title;
        if (dayNoteViewInfo == null && weekdayId > 0) {
            final String formatTitle = "%s %s";
            title = String.format(formatTitle, WeekDay.getFullLabel(context, weekdayId),
                    context.getString(viewLabelResId));
        } else {
            title = context.getString(viewLabelResId);
        }

        final String positiveLabel = context.getString(android.R.string.ok);
        final String negativeLabel = context.getString(android.R.string.cancel);
        final String neutralLabel;
        if (noteInput == null) {
            neutralLabel = null;
        } else {
            neutralLabel = context.getString(R.string.clear_note);
        }

        final Utility.ViewInitWith3Buttons viewInit = new Utility.ViewInitWith3Buttons() {
            private TextView mmTextViewNoteInfo;
            private TextView mmTextViewTotalInfo;

            private ListView mmListViewNotes;
            private NoteListAdapter mmNoteListAdapter;
            private Note mmNote;

            private View mmViewAddNote;

            private final NoteActionListener mmNoteActionListener = new NoteActionListener() {
                @Override
                public void onNoteAdded() {
                    mmNoteListAdapter.dataChanged();
                    if (cellNoteChangeListener != null) {
                        cellNoteChangeListener.whenNoteChanged();
                    }
                }

                @Override
                public void onNoteDeleted(final Note noteDeleted) {
                    if (noteDeleted.equals(mmNote)) {
                        mmNote = null;
                    }
                    updateInfo();
                    mmNoteListAdapter.dataChanged();

                    if (cellNoteChangeListener != null) {
                        cellNoteChangeListener.whenNoteChanged();
                    }
                }

                @Override
                public void onNoteChanged() {
                    updateInfo();
                    mmNoteListAdapter.dataChanged();
                    if (cellNoteChangeListener != null) {
                        cellNoteChangeListener.whenNoteChanged();
                    }
                }

                @Override
                public void clearNote() {
                    mmNote = null;
                    updateInfo();
                    setCellNote();
                }

                @Override
                public void select(int position) {
                    mmNote = (Note) mmNoteListAdapter.getItem(position);
                    mmNoteListAdapter.setSelected(position);
                    updateInfo();
                }
            };

            private void setCellNote() {
                if (cellNoteChangeListener != null) {
                    WeekDayNotes.setDayNote(weekdayId, viewLabelResId, mmNote);
                    cellNoteChangeListener.setCellNote(weekdayId, viewId, mmNote);
                } else if (dayNoteViewInfo != null) {
                    dayNoteViewInfo.setNoteByLabel(viewLabelResId, mmNote);
                    dayNoteViewInfo.refreshViews(sColorScheme, sNoteColorText,
                            sNoteColorBackground);
                }
            }

            @Override
            public void initViews(View rootView) {
                mmNote = noteInput;

                mmTextViewNoteInfo = rootView.findViewById(R.id.view_note_info);
                mmTextViewTotalInfo = rootView.findViewById(R.id.view_total_info);

                mmListViewNotes = rootView.findViewById(R.id.list_notes);
                mmNoteListAdapter = new NoteListAdapter(context, mmNoteActionListener, null);
                mmListViewNotes.setAdapter(mmNoteListAdapter);

                initButtons(rootView);

                if (mmNote != null) {
                    int index = mmNoteListAdapter.getItemIndex(mmNote);
                    mmNoteListAdapter.setSelected(index);
                }
                updateInfo();
            }

            private void updateInfo() {
                final String info;
                if (mmNote == null) {
                    info = context.getString(R.string.info_no_note_set);
                } else {
                    int noteOccurrence = WeekDayNotes.getNoteCountInWeek(mmNote);
                    if (noteOccurrence > 0) {
                        String strOccurrence = context.getString(
                                R.string.format_note_count_in_a_week, noteOccurrence);
                        final String format_info = "%s(%s)";
                        info = String.format(format_info, mmNote.textFull, strOccurrence);
                    } else {
                        info = mmNote.textFull;
                    }
                }
                mmTextViewNoteInfo.setText(info);

                final String infoNoteCount = context
                        .getString(R.string.info_note_count, Note.getNoteCount());
                mmTextViewTotalInfo.setText(infoNoteCount);
            }

            private void initButtons(final View rootView) {
                mmViewAddNote = rootView.findViewById(R.id.button_add_note);
                mmViewAddNote.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditNoteDialog(context, null, mmNoteActionListener);
                    }
                });
            }

            @Override
            public void onPositiveClick(View rootView) {
                if (mmNote != null) {
                    setCellNote();
                }
            }

            @Override
            public void onNegativeClick(View rootView) {
                // ...
            }

            @Override
            public void onNeutralClick(View rootView) {
                showClearNoteConfirm(context, title, noteInput, mmNoteActionListener);
            }
        };
        Utility.showViewDialog(context, R.layout.view_week_set_note, title, viewInit,
                positiveLabel, neutralLabel, negativeLabel);
    }

    private static DateNotes getSpecifiedDateNotes(final Day date) {
        for (DateNotes dateNotes : sDateNotes) {
            if (dateNotes.date.equals(date)) return dateNotes;
        }
        return null;
    }

    // 根据DateNotes设置，对于在本周的日期，更新notes。
    public static boolean refreshWeekDayNotes(final Context context) {
        boolean weekDayNotesChanged = false;
        DateNotes foundDateNotes;
        for (WeekDayNotes weekdayNotes : sWeekDayNotes) {
            foundDateNotes = getSpecifiedDateNotes(weekdayNotes.date);
            DayNotes curDayNotes = null;
            if (foundDateNotes != null) {
                final int weekdayIdEquals = foundDateNotes.weekdayId_equals;
                if (weekdayIdEquals > 0) {
                    curDayNotes = WeekDayNotes.getWeekDayNotes(weekdayIdEquals);
                } else {
                    curDayNotes = foundDateNotes.dayNotes;
                }
            }
            weekDayNotesChanged = weekDayNotesChanged || weekdayNotes.setDateNotes(curDayNotes);
        }
        return weekDayNotesChanged;
    }

    private interface DateNotesChangeListener {
        public void onDateNotesChanged();
    }

    private void showSetDateNotesDialog(final Context context) {
        final String title = context.getString(R.string.set_note_for_date);
        final Utility.ViewInit viewInit = new Utility.ViewInit() {
            private TextView mmTextViewInfo;

            private ListView mmListViewDates;

            private View mmViewAddDate;
            private View mmViewClear;

            private DateNotesListAdapter mmDateNotesListAdapter;

            private final DateNotesChangeListener mmDateNotesChangeListener = new DateNotesChangeListener() {
                @Override
                public void onDateNotesChanged() {
                    mmDateNotesListAdapter.notifyDataSetChanged();
                    saveDateNotes(context);
                    if (refreshWeekDayNotes(context)) {
                        Constants.refreshWidget(context);
                    }
                    updateInfo();
                }
            };

            @Override
            public void initViews(View rootView) {
                mmTextViewInfo = rootView.findViewById(R.id.view_info);

                mmDateNotesListAdapter = new DateNotesListAdapter(context, mmDateNotesChangeListener);

                mmListViewDates = rootView.findViewById(R.id.list_date_notes);
                mmListViewDates.setAdapter(mmDateNotesListAdapter);

                mmViewClear = rootView.findViewById(R.id.button_clear_dates);
                mmViewClear.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String title = context.getString(R.string.title_delete_date_notes);
                        String confirmPrompt = context.getString(R.string.prompt_clear_dates_notes);
                        DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sDateNotes.clear();
                                mmDateNotesChangeListener.onDateNotesChanged();
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
                        showEditDateNotesDialog(context, null, mmDateNotesChangeListener);
                    }
                });
                updateInfo();
            }

            private void updateInfo() {
                int dateCount = mmListViewDates.getCount();
                String info;
                if (dateCount <= 0) {
                    info = context.getString(R.string.prompt_no_date_notes_set);
                    mmViewClear.setVisibility(View.GONE);
                } else {
                    info = context.getString(R.string.format_n_date_notes_set, dateCount);
                    mmViewClear.setVisibility(View.VISIBLE);
                }
                mmTextViewInfo.setText(info);
            }
        };
        Utility.showViewDialog(context, R.layout.view_date_notes, title, viewInit, null, null);
    }

    /**
     *
     * @param context
     * @param dateNotesInput null for add new, Not null for edit existing.
     * @param dateNotesChangeListener
     */
    private static void showEditDateNotesDialog(final Context context,
            final DateNotes dateNotesInput, final DateNotesChangeListener dateNotesChangeListener) {
        final Day today = Utility.getToday(Calendar.MONDAY);
        final ArrayList<Day> workDays;
        final String title;
        if (dateNotesInput == null) {
            title = context.getString(R.string.set_note_for_date);
            workDays = SettingsCalendarActivity.getWorkDays(context, today);
        } else {
            workDays = null;
            title = context.getString(R.string.edit_note_for_date);
        }
        final String positiveLabel = context.getString(android.R.string.ok);
        final String negativeLabel = context.getString(android.R.string.cancel);

        final int defaultCheckedRadioId = R.id.radio_same_as_weekday;
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

            private RadioGroup mmRadioGroupNotesSet;

            private View mmViewNotesSet;
            private ListView mmListViewWeekDays;
            private View mmViewDateNotes;

            private DateNotes mmDateNotes;

            private WeekDay[] mmWeekDayArray;

            private WeekNotes.DayNoteViewInfo mmDayNoteViewInfo;

            private void initDayNoteViews(final View rootView) {
                if (dateNotesInput == null) {
                    mmDayNoteViewInfo = new WeekNotes.DayNoteViewInfo(null);
                } else {
                    mmDayNoteViewInfo = new WeekNotes.DayNoteViewInfo(dateNotesInput.dayNotes);
                }

                mmDayNoteViewInfo.initViews(context, rootView, sDayNotesListener);
            }

            @Override
            public void initViews(View rootView) {
                initDayNoteViews(rootView);

                mmListViewDates = rootView.findViewById(R.id.list_dates);

                mmViewDateSpecified = rootView.findViewById(R.id.view_date_specified);
                if (dateNotesInput == null) {
                    mmViewDateSpecified.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                        int dayOfMonth) {
                                    Day day = new Day(year, monthOfYear, dayOfMonth);
                                    Utility.obtainWeekDay(day);

                                    mmDateNotes = new DateNotes(day);
                                    otherDateSpecified();
                                }
                            };
                            Utility.showDatePickerDialog(context, today.year, today.month, today.day,
                                    title, onDateSetListener);
                        }
                    });
                } else {
                    mmDateNotes = dateNotesInput;
                    setDateText();
                }

                mmRadioGroupNotesSet = rootView.findViewById(R.id.radiogroup_set_date_notes);
                final RadioGroup.OnCheckedChangeListener radioCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        radioChecked(checkedId);
                    }
                };
                mmRadioGroupNotesSet.setOnCheckedChangeListener(radioCheckedChangeListener);

                mmViewNotesSet = rootView.findViewById(R.id.view_notes_set);
                mmListViewWeekDays = rootView.findViewById(R.id.list_week_days);
                mmViewDateNotes = rootView.findViewById(R.id.view_day_notes);

                if (mmDateNotes == null || mmDateNotes.weekdayId_equals > 0
                        || mmDateNotes.dayNotes == null) {
                    mmRadioGroupNotesSet.check(defaultCheckedRadioId);
                } else {
                    mmRadioGroupNotesSet.check(R.id.radio_specify_notes);
                }
                getComingWorkDays();
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
                    mmDateNotes = new DateNotes(workDays.get(which));
                    mmViewDateSpecified.setVisibility(View.INVISIBLE);
                } else {
                    mmViewDateSpecified.setVisibility(View.VISIBLE);
                }
                checkViews();
            }

            private void checkViews() {
                if (mmDateNotes == null) {
                    mmViewNotesSet.setVisibility(View.GONE);
                    mmRadioGroupNotesSet.setVisibility(View.GONE);
                } else {
                    mmViewNotesSet.setVisibility(View.VISIBLE);
                    mmRadioGroupNotesSet.setVisibility(View.VISIBLE);
                }
                updateWeekDayListView();
            }

            private String[] constructDateList(final ArrayList<Day> workDays) {
                String[] array = new String[workDays.size() + 1];
                for (int i = 0; i < array.length - 1; i++) {
                    array[i] = workDays.get(i).ymdw_chinese(context);
                }
                array[array.length - 1] = context.getString(R.string.set_note_for_date);
                return array;
            }

            private void otherDateSpecified() {
                setDateText();
                checkViews();
            }

            private void setDateText() {
                if (mmDateNotes != null) {
                    mmViewDateSpecified.setText(mmDateNotes.date.ymdw_chinese(context));
                }
            }

            private void radioChecked(final int checkedRadioId) {
                switch (checkedRadioId) {
                    case R.id.radio_same_as_weekday:
                        mmListViewWeekDays.setVisibility(View.VISIBLE);
                        mmViewDateNotes.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.radio_specify_notes:
                        mmListViewWeekDays.setVisibility(View.INVISIBLE);
                        mmViewDateNotes.setVisibility(View.VISIBLE);
                        break;
                    default:
                        return;
                }
            }

            private String[] constructWeekDayList(final int excludedWeekDay) {
                ArrayList<WeekDay> weekDays = new ArrayList<WeekDay>(6);
                for (WeekDayNotes weekDayNotes : sWeekDayNotes) {
                    if (weekDayNotes.weekDayId == excludedWeekDay) continue;
                    if (weekDayNotes.getNoteCount(null) <= 0) continue;
                    weekDays.add(WeekDay.getWeekDayByCalendarId(weekDayNotes.weekDayId));
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

            private void updateWeekDayListView() {
                radioChecked(mmRadioGroupNotesSet.getCheckedRadioButtonId());

                if (mmDateNotes == null) {
                    mmListViewWeekDays.setAdapter(null);
                    mmListViewWeekDays.setVisibility(View.GONE);
                    return;
                }
                final int excludedWeekDay = mmDateNotes.date.weekday;
                final String[] weekDayStrings = constructWeekDayList(excludedWeekDay);
                mmListViewWeekDays.setAdapter(new ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_single_choice, weekDayStrings));
                mmListViewWeekDays.setItemChecked(getCheckedWeekId(), true);
            }

            private int getCheckedWeekId() {
                if (mmDateNotes == null) return 0;
                for (int i = 0; i < mmWeekDayArray.length; i++) {
                    if (mmWeekDayArray[i].calendarId == mmDateNotes.weekdayId_equals) {
                        return i;
                    }
                }
                return 0;
            }

            @Override
            public void onPositiveClick(View rootView) {
                if (mmDateNotes == null) {
                    Utility.showInfoDialog(context, R.string.prompt_no_date_set);
                    return;
                }
                final int checkedRadioId = mmRadioGroupNotesSet.getCheckedRadioButtonId();
                switch (checkedRadioId) {
                    case R.id.radio_same_as_weekday:
                        int checkedWeekDayIndex = mmListViewWeekDays.getCheckedItemPosition();
                        mmDateNotes.weekdayId_equals = mmWeekDayArray[checkedWeekDayIndex].calendarId;
                        break;
                    case R.id.radio_specify_notes:
                        fillDayNotes(mmDateNotes.dayNotes);
                        break;
                    default:
                        return;
                }
                checkDateNotesAdd(context, mmDateNotes, dateNotesChangeListener);
            }

            private void fillDayNotes(final DayNotes dayNotes) {
                // ...
            }

            @Override
            public void onNegativeClick(View rootView) {
                // ..
            }
        };
        Utility.showViewDialog(context, R.layout.view_add_date_notes, title, viewInit,
                positiveLabel, negativeLabel);
    }

    private static void checkDateNotesAdd(final Context context, final DateNotes dateNotes,
            final DateNotesChangeListener dateNotesChangeListener) {
        if (dateExists(dateNotes)) {
            showConfirmDateNotesOverwritting(context, dateNotes, dateNotesChangeListener);
        } else {
            addDateNotes(context, dateNotes, dateNotesChangeListener);
        }
    }

    private static void showConfirmDateNotesOverwritting(final Context context,
            final DateNotes newDateNotes, final DateNotesChangeListener dateNotesChangeListener) {
        String title = context.getString(R.string.title_date_overwrite);
        String confirmPrompt = context.getString(R.string.prompt_date_overwrite);
        DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDateNotes(newDateNotes.date);
                addDateNotes(context, newDateNotes, dateNotesChangeListener);
            }
        };

        Utility.showConfirmDialog(context, title, confirmPrompt, positiveButtonListener);
    }

    private static boolean dateExists(final DateNotes dateNotesInput) {
        for (DateNotes dateNotes : sDateNotes) {
            if (dateNotes.isSameDate(dateNotesInput)) return true;
        }
        return false;
    }

    private static void removeDateNotes(final Day date) {
        ArrayList<DateNotes> sameDates = new ArrayList<DateNotes>(sDateNotes.size());
        for (DateNotes dateNotes : sDateNotes) {
            if (dateNotes.date.isSameDay(date)) {
                sameDates.add(dateNotes);
            }
        }
        for (DateNotes dateNotes : sameDates) {
            sDateNotes.remove(dateNotes);
        }
    }

    private static void removeDateNotes(final Context context, final DateNotes dateNotes,
            final DateNotesChangeListener dateNotesChangeListener) {
        if (sDateNotes.remove(dateNotes)) {
            dateNotesChangeListener.onDateNotesChanged();
        }
    }

    private static void addDateNotes(final Context context, final DateNotes dateNotes,
            final DateNotesChangeListener dateNotesChangeListener) {
        sDateNotes.add(dateNotes);
        DateNotes.sort();
        dateNotesChangeListener.onDateNotesChanged();
    }

    private void showEditLabelDialog(final Context context) {
        final String title;
        if (TextUtils.isEmpty(sLabel)) {
            title = context.getString(R.string.set_week_notes_label);
        } else {
            title = context.getString(R.string.edit_week_notes_label);
        }
        final String positiveLabel = context.getString(android.R.string.ok);
        final String negativeLabel = context.getString(android.R.string.cancel);
        final Utility.ViewInitWithPositiveNegative viewInit = new Utility.ViewInitWithPositiveNegative() {
            private EditText mmEditTextLabel;
            private TextView mmViewLabel;
            private View mmViewChangeColors;

            private int mmColorText, mmColorBackground;

            @Override
            public void initViews(View rootView) {
                mmColorText = sLabelTextColor;
                mmColorBackground = sLabelBackgroundColor;

                mmEditTextLabel = rootView.findViewById(R.id.edit_week_notes_label);
                mmEditTextLabel.setText(sLabel);

                mmViewLabel = rootView.findViewById(R.id.demo_label);
                mmViewLabel.setText(sLabel);

                mmViewChangeColors = rootView.findViewById(R.id.view_change_colors);
                mmViewChangeColors.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = Utility.getEditText(mmEditTextLabel);
                        final ColorPickerDialog.OnColorSetListener colorSetListener = new ColorPickerDialog.OnColorSetListener() {
                            @Override
                            public void onColorSet(int textColor, int backgroundColor) {
                                mmColorBackground = backgroundColor;
                                mmColorText = textColor;
                                refreshViewLabel();
                            }
                        };
                        showColorSetDialog(context, title, text, colorSetListener, mmColorText,
                                mmColorBackground);
                    }
                });

                refreshViewLabel();
            }

            private void refreshViewLabel() {
                mmViewLabel.setText(Utility.getEditText(mmEditTextLabel));
                mmViewLabel.setTextColor(mmColorText);
                mmViewLabel.setBackgroundColor(mmColorBackground);
            }

            @Override
            public void onPositiveClick(View rootView) {
                final SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);
                sLabel = Utility.getEditText(mmEditTextLabel);
                saveLabel(prefHelper);

                sLabelBackgroundColor = mmColorBackground;
                sLabelTextColor = mmColorText;
                saveLabelColors(prefHelper);

                refreshLabelViews(context);

                Constants.refreshWidget(context);
            }

            @Override
            public void onNegativeClick(View rootView) {
                // ...
            }
        };
        Utility.showViewDialog(context, R.layout.view_week_notes_label, title, viewInit,
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

    private static void showEditNoteDialog(final Context context, final Note note,
            final NoteActionListener noteActionListener) {
        final String title;
        if (note == null) {
            title = context.getString(R.string.add_note);
        } else {
            final String formatEditNote = "%s %s";
            title = String.format(formatEditNote, context.getString(R.string.edit_note),
                    note.textFull);
        }
        final Utility.ViewInitWithPositiveNegative viewInit = new Utility.ViewInitWithPositiveNegative() {
            private EditText mmEditTextFull;
            private EditText mmEditTextShort;

            private RadioGroup mmRadioGroupType;

            private TextView mmTextViewDemo;
            private View mmViewChangeColors;

            private int mmColorText, mmColorBackground;

            @Override
            public void initViews(View rootView) {
                mmEditTextFull = rootView.findViewById(R.id.edit_note_full);
                mmEditTextShort = rootView.findViewById(R.id.edit_note_short);
                mmRadioGroupType = rootView.findViewById(R.id.radiogroup_note_type);
                if (note == null) {
                    mmRadioGroupType.check(Note.Type.Ordinary.radioResId);
                } else {
                    mmRadioGroupType.check(note.type.radioResId);
                }

                mmTextViewDemo = rootView.findViewById(R.id.demo_note);

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
                                noteActionListener.onNoteChanged();
                            }
                        };
                        showColorSetDialog(context, title, text, colorSetListener, mmColorText,
                                mmColorBackground);
                    }
                });
                initViewContent();
            }

            private void initViewContent() {
                if (note == null) {
                    mmColorText = Note.DEFAULT_COLOR_NOTE_TEXT;
                    mmColorBackground = Note.DEFAULT_COLOR_NOTE_BACKGROUND;
                    mmEditTextFull.setEnabled(true);
                    mmEditTextShort.setEnabled(true);
                } else {
                    mmColorText = note.getColorText();
                    mmColorBackground = note.getColorBackground();

                    mmEditTextFull.setText(note.textFull);
                    mmEditTextShort.setText(note.textShort);
                    mmEditTextFull.setEnabled(false);
                    mmEditTextShort.setEnabled(false);
                }
                refreshDemoView();
            }

            private void refreshDemoView() {
                final String textFull = Utility.getEditText(mmEditTextFull);

                final String textShort = Utility.getEditText(mmEditTextShort);
                if (TextUtils.isEmpty(textShort)) {
                    if (TextUtils.isEmpty(textFull)) {
                        mmTextViewDemo.setText(R.string.text_note);
                    } else {
                        mmTextViewDemo.setText(Note.shortText(textFull));
                    }
                } else {
                    mmTextViewDemo.setText(textShort);
                }
                mmTextViewDemo.setTextColor(mmColorText);
                mmTextViewDemo.setBackgroundColor(mmColorBackground);
            }

            @Override
            public void onPositiveClick(View rootView) {
                String newFull = Utility.getEditText(mmEditTextFull);
                String newShort = Utility.getEditText(mmEditTextShort);
                if (TextUtils.isEmpty(newFull)) {
                    Utility.showInfoDialog(context, R.string.prompt_note_full_text_not_empty);
                    return;
                }
                if (TextUtils.isEmpty(newShort)) {
                    newShort = Note.shortText(newFull);
                }
                Note.Type noteType = Note.Type
                        .getFromRadio(mmRadioGroupType.getCheckedRadioButtonId());
                Note newNote;
                if (note == null) {
                    newNote = new Note(newFull, newShort, noteType);
                } else {
                    newNote = note;
                    newNote.type = noteType;
                }
                newNote.setColorText(mmColorText);
                newNote.setColorBackground(mmColorBackground);

                if (note == null) {
                    Note.FailedCause failedCause = Note.addNote(newNote);
                    if (failedCause == null) {
                        noteActionListener.onNoteAdded();
                    } else {
                        Utility.showInfoDialog(context,
                                "Failed to add note:" + newNote + "\n" + failedCause.cause);
                    }
                } else {
                    noteActionListener.onNoteChanged();
                }
            }

            @Override
            public void onNegativeClick(View rootView) {
                // ...
            }
        };
        Utility.showViewDialog(context, title, R.layout.edit_note, viewInit);
    }

    private synchronized static void loadSettings(final Context context) {
        if (sSettingsLoaded) {
            return;
        }

        final SharedPreferencesHelper prefHelper = SettingsUtils.getPrefsHelper(context);

        SettingsUtils.loadGeneric(prefHelper);

        loadLabel(prefHelper);
        loadLabelColors(prefHelper);
        loadColorScheme(prefHelper);
        loadNotes(prefHelper);
        loadDateNotes(prefHelper);
        loadWeekDayNotes(prefHelper);

        sSettingsLoaded = true;
    }

    private static final String EMPTY = "";
    private static final String SPACE = " ";

    // 上午从8点到12点，每小时一个记事.
    private static enum AmNote {
        Am08(R.string.morning_08,  R.id.day_08,  8,  9),
        Am09(R.string.morning_09,  R.id.day_09,  9, 10),
        Am10(R.string.morning_10,  R.id.day_10, 10, 11),
        Am11(R.string.morning_11,  R.id.day_11, 11, 12),
        Am12(R.string.morning_12,  R.id.day_12, 12, 13);

        public final int labelResId;
        public final int timeViewId;
        public final int clockStart, clockEnd;

        private AmNote(int labelResId, int timeViewId, int clockStart, int clockEnd) {
            this.labelResId = labelResId;
            this.timeViewId = timeViewId;
            this.clockStart = clockStart;
            this.clockEnd = clockEnd;
        }

        private static final String FORMAT_TIME = "%d:00-%d:00";

        public String timeString() {
            return String.format(FORMAT_TIME, clockStart, clockEnd);
        }

        public static void initViews(final Activity activity) {
            TextView timeView;
            for (AmNote amNote : values()) {
                timeView = activity.findViewById(amNote.timeViewId);
                timeView.setText(amNote.timeString());
            }
        }

        public static AmNote getFromLabel(final int labelResId) {
            for (AmNote amNote : values()) {
                if (amNote.labelResId == labelResId) return amNote;
            }
            return null;
        }

        public static AmNote getFromClock(final int curClock) {
            for (AmNote amNote : values()) {
                if (curClock >= amNote.clockStart && curClock < amNote.clockEnd) {
                    return amNote;
                }
            }
            return null;
        }
    }

    public static void initDayNotesConfig(final RemoteViews updateView) {
        for (AmNote amNote : AmNote.values()) {
            updateView.setTextViewText(amNote.timeViewId, amNote.timeString());
        }
        for (PmNote pmNote : PmNote.values()) {
            updateView.setTextViewText(pmNote.timeViewId, pmNote.timeString());
        }
    }

    // 下午从1点到5点，每小时一个记事.
    private static enum PmNote {
        Pm1(R.string.afternoon_13, R.id.day_13, 13, 14),
        Pm2(R.string.afternoon_14, R.id.day_14, 14, 15),
        Pm3(R.string.afternoon_15, R.id.day_15, 15, 16),
        Pm4(R.string.afternoon_16, R.id.day_16, 16, 17),
        Pm5(R.string.afternoon_17, R.id.day_17, 17, 18);

        public final int labelResId;
        public final int timeViewId;
        public final int clockStart, clockEnd;

        private PmNote(int labelResId, int timeViewId, int clockStart, int clockEnd) {
            this.labelResId = labelResId;
            this.timeViewId = timeViewId;
            this.clockStart = clockStart;
            this.clockEnd = clockEnd;
        }
        
        private static final String FORMAT_TIME = "%d:00-%d:00";

        public String timeString() {
            return String.format(FORMAT_TIME, clockStart, clockEnd);
        }

        public static void initViews(final Activity activity) {
            TextView timeView;
            for (PmNote amNote : values()) {
                timeView = activity.findViewById(amNote.timeViewId);
                timeView.setText(amNote.timeString());
            }
        }

        public static PmNote getFromLabel(final int labelResId) {
            for (PmNote pmNote : values()) {
                if (pmNote.labelResId == labelResId) return pmNote;
            }
            return null;
        }

        public static PmNote getFromClock(final int curClock) {
            for (PmNote pmNote : values()) {
                if (curClock >= pmNote.clockStart && curClock < pmNote.clockEnd) {
                    return pmNote;
                }
            }
            return null;
        }
    }

    public static class DayNotes {
        public Note morningNote, eveningNote;
        public final ArrayList<Note> amNotes;
        public final ArrayList<Note> pmNotes;

        private static final String SEP_NOTE = "·";

        public DayNotes() {
            amNotes = initAmNotes(null);
            pmNotes = initPmNotes(null);
        }

        public void init() {
            morningNote = null;
            eveningNote = null;
            initAmNotes(amNotes);
            initPmNotes(pmNotes);
        }

        public void copyDayNotes(final DayNotes dayNotesInput) {
            eveningNote = dayNotesInput.eveningNote;
            morningNote = dayNotesInput.morningNote;
            amNotes.clear();
            for (Note note : dayNotesInput.amNotes) {
                amNotes.add(note);
            }
            pmNotes.clear();
            for (Note note : dayNotesInput.pmNotes) {
                pmNotes.add(note);
            }
        }

        private static ArrayList<Note> initAmNotes(ArrayList<Note> morningNotes) {
            AmNote[] amNotes = AmNote.values();
            if (morningNotes == null) {
                morningNotes = new ArrayList<Note>(amNotes.length);
            } else {
                morningNotes.clear();
            }
            for (int i = 0; i < amNotes.length; i++) {
                morningNotes.add(null);
            }
            return morningNotes;
        }

        private static ArrayList<Note> initPmNotes(ArrayList<Note> afternoonNotes) {
            PmNote[] pmNotes = PmNote.values();
            if (afternoonNotes == null) {
                afternoonNotes = new ArrayList<Note>(pmNotes.length);
            } else {
                afternoonNotes.clear();
            }
            for (int i = 0; i < pmNotes.length; i++) {
                afternoonNotes.add(null);
            }
            return afternoonNotes;
        }

        public String briefInfo() {
            StringBuilder sb = new StringBuilder();
            if (morningNote != null) {
                sb.append(morningNote.briefText());
            }
            sb.append('+');
            for (Note note : amNotes) {
                if (note != null) {
                    sb.append(note.briefText());
                }
                sb.append(SEP_NOTE);
            }
            sb.append('+');
            for (Note note : pmNotes) {
                if (note != null) {
                    sb.append(note.briefText());
                }
                sb.append(SEP_NOTE);
            }
            sb.append('+');
            if (eveningNote != null) {
                sb.append(eveningNote.briefText());
            }
            return sb.toString();
        }

        private static String notesString(final ArrayList<Note> notes) {
            final int noteCount = notes == null ? 0 : notes.size();
            if (noteCount <= 0) return EMPTY;

            StringBuilder sb = new StringBuilder();
            for (Note note : notes) {
                if (sb.length() > 0) sb.append(SEP_NOTE);
                if (note == null) continue;
                sb.append(note.textInfo());
            }
            return sb.toString();
        }

        private static void parseNotes(final String input, final ArrayList<Note> noteList) {
            if (TextUtils.isEmpty(input)) return;
            String[] elements = input.split(SEP_NOTE);

            for (int i = 0; i < elements.length; i++) {
                if (TextUtils.isEmpty(elements[i])) continue;
                noteList.set(i, Note.getNoteByText(elements[i]));
            }
        }

        public void setNoteByLabel(final int labelResId, final Note note) {
            if (labelResId == R.string.morning_note) {
                morningNote = note;
                return;
            }
            if (labelResId == R.string.evening_note) {
                eveningNote = note;
                return;
            }
            AmNote amNote = AmNote.getFromLabel(labelResId);
            if (amNote == null) {
                PmNote pmNote = PmNote.getFromLabel(labelResId);
                if (pmNote != null) {
                    pmNotes.set(pmNote.ordinal(), note);
                }
            } else {
                amNotes.set(amNote.ordinal(), note);
            }
        }

        public Note getNoteByLabel(final int labelResId) {
            if (labelResId == R.string.morning_note) {
                return morningNote;
            }
            if (labelResId == R.string.evening_note) {
                return eveningNote;
            }
            AmNote amNote = AmNote.getFromLabel(labelResId);
            if (amNote == null) {
                PmNote pmNote = PmNote.getFromLabel(labelResId);
                if (pmNote == null) return null;
                return pmNotes.get(pmNote.ordinal());
            }
            return amNotes.get(amNote.ordinal());
        }

        public int getNoteCount(final Note noteInput) {
            int count = 0;
            for (Note note : amNotes) {
                if (note == null) continue;
                if (noteInput == null || note.equals(noteInput)) {
                    count++;
                }
            }
            for (Note note : pmNotes) {
                if (note == null) continue;
                if (noteInput == null || note.equals(noteInput)) {
                    count++;
                }
            }
            return count;
        }

        public boolean deleteNote(final Note note) {
            if (morningNote != null && morningNote.equals(note)) {
                morningNote = null;
            }
            if (eveningNote != null && eveningNote.equals(note)) {
                eveningNote = null;
            }
            int amCount = amNotes.size();
            for (int i = 0; i < amCount; i++) {
                if (note.equals(amNotes.get(i))) {
                    amNotes.set(i, null);
                }
            }

            int pmCount = pmNotes.size();
            for (int i = 0; i < pmCount; i++) {
                if (note.equals(pmNotes.get(i))) {
                    pmNotes.set(i, null);
                }
            }
            return true;
        }

        private static final String LEFT = "[", RIGHT = "]";
        private static final String SEP_ = "@";
        // Format: [morningNote,AM notes,PM notes,eveningNote]
        private static final String FORMAT_TO_STRING = LEFT + "%1$s" + SEP_ + "%2$s" + SEP_ + "%3$s"
                + SEP_ + "%4$s" + RIGHT;

        @Override
        public String toString() {
            String strMorningNote = morningNote == null ? SPACE : morningNote.textInfo();
            String strMorningNotes = notesString(amNotes);
            String strAfternoonNotes = notesString(pmNotes);
            String strEveningNote = eveningNote == null ? SPACE : eveningNote.textInfo();
            return String.format(FORMAT_TO_STRING, strMorningNote, strMorningNotes,
                    strAfternoonNotes, strEveningNote);
        }

        public static DayNotes parseDayNotes(final String dayNotesString,
                DayNotes dayNotes) {
            if (TextUtils.isEmpty(dayNotesString)) return null;
            int leftIndex = dayNotesString.indexOf(LEFT);
            if (leftIndex < 0) return null;
            final int startIndex = leftIndex + 1;
            int endIndex = dayNotesString.indexOf(RIGHT);
            if (endIndex <= startIndex) return null;

            String[] elements = dayNotesString.substring(startIndex, endIndex).split(SEP_);
            if (dayNotes == null) {
                dayNotes = new DayNotes();
            }

            if (elements.length > 0) {
                dayNotes.morningNote = Note.getNoteByText(elements[0]);
                if (elements.length > 1) {
                    parseNotes(elements[1], dayNotes.amNotes);
                }
                if (elements.length > 2) {
                    parseNotes(elements[2], dayNotes.pmNotes);
                }
                if (elements.length > 3) {
                    dayNotes.eveningNote = Note.getNoteByText(elements[3]);
                }
            }

            return dayNotes;
        }
    }

    public static class WeekDayNotes {
        public final int weekDayId;
        public final int viewResId;
        public final String prefKey;

        public final DayNotes dayNotes = new DayNotes();

        public DayNotes dateNotes;

        public Day date;

        public WeekDayNotes(final int weekDayId, final int viewResId, final String prefKey) {
            this.weekDayId = weekDayId;
            this.viewResId = viewResId;
            this.prefKey = prefKey;
        }

        public void init() {
            dayNotes.init();
            dateNotes = null;
            date = null;
        }

        public void setMorningNote(Note note) {
            dayNotes.morningNote = note;
        }

        public void setEveningNote(Note note) {
            dayNotes.eveningNote = note;
        }

        // 返回值表示是否有dateNotes的change.
        public boolean setDateNotes(final DayNotes dayNotesInput) {
            if (dayNotesInput == null) {
                if (dateNotes == null) {
                    return false;
                }
                if (dateNotes.getNoteCount(null) > 0) {
                    dateNotes.init();
                    return true;
                }
                return false;
            }
            if (dateNotes == null) {
                dateNotes = new DayNotes();
            } else {
                dateNotes.init();
            }
            dateNotes.copyDayNotes(dayNotesInput);
            return true;
        }

        public ArrayList<Note> getAmNotes() {
            return dayNotes == null ? null : dayNotes.amNotes;
        }

        public ArrayList<Note> getPmNotes() {
            return dayNotes == null ? null : dayNotes.pmNotes;
        }

        public String getMorningShortInfo(final Context context) {
            if (dayNotes == null || dayNotes.morningNote == null) return null;
            return dayNotes.morningNote.shortText();
        }

        public int getMorningTextColor() {
            if (dayNotes == null || dayNotes.morningNote == null) {
                return Note.DEFAULT_COLOR_NOTE_TEXT;
            }
            return dayNotes.morningNote.getColorText();
        }

        public int getMorningBackgroundColor() {
            if (dayNotes == null || dayNotes.morningNote == null) {
                return Note.DEFAULT_COLOR_NOTE_BACKGROUND;
            }
            return dayNotes.morningNote.getColorBackground();
        }

        public String getEveningShortInfo(final Context context) {
            if (dayNotes == null || dayNotes.eveningNote == null) return null;
            return dayNotes.eveningNote.shortText();
        }

        public int getNoteCount(final Note note) {
            if (dayNotes == null) return 0;
            return dayNotes.getNoteCount(note);
        }

        public int getEveningTextColor() {
            if (dayNotes == null || dayNotes.eveningNote == null) {
                return Note.DEFAULT_COLOR_NOTE_TEXT;
            }
            return dayNotes.eveningNote.getColorText();
        }

        public int getEveningBackgroundColor() {
            if (dayNotes == null || dayNotes.eveningNote == null) {
                return Note.DEFAULT_COLOR_NOTE_BACKGROUND;
            }
            return dayNotes.eveningNote.getColorBackground();
        }

        public void setAmNote(final int index, final Note note) {
            dayNotes.amNotes.set(index, note);
        }

        public void setPmNote(final int index, final Note note) {
            dayNotes.pmNotes.set(index, note);
        }

        // Format: weekDayId[morningReading,MorningNotes,AfternoonNotes,eveningNotes]
        private static final String FORMAT_TO_STRING = "%1$d%2$s";

        @Override
        public String toString() {
            String dayNotesString = dayNotes.toString();
            if (TextUtils.isEmpty(dayNotesString)) {
                dayNotesString = EMPTY;
            }
            return String.format(FORMAT_TO_STRING, weekDayId, dayNotesString);
        }

        public void parseWeekDayNotes(final String weekDayNotesString) {
            String dayNotesString = weekDayNotesString.substring(1);
            DayNotes.parseDayNotes(dayNotesString, dayNotes);
        }

        public static DayNotes getWeekDayNotes(final int weekdayId) {
            for (WeekDayNotes weekdayNotes : sWeekDayNotes) {
                if (weekdayNotes.weekDayId == weekdayId) {
                    return weekdayNotes.dayNotes;
                }
            }
            return null;
        }

        private static void setDayNote(final int weekdayId, final int labelResId,
                final Note note) {
            for (WeekDayNotes weekDayNotes : sWeekDayNotes) {
                if (weekDayNotes.weekDayId == weekdayId) {
                    weekDayNotes.dayNotes.setNoteByLabel(labelResId, note);
                    return;
                }
            }
        }

        private static Note getNote(final int weekdayId, final int labelResId) {
            for (WeekDayNotes weekDayNotes : sWeekDayNotes) {
                if (weekDayNotes.weekDayId == weekdayId) {
                    return weekDayNotes.dayNotes.getNoteByLabel(labelResId);
                }
            }
            return null;
        }

        public static Note getMorningNote(final int weekdayId) {
            for (WeekDayNotes weekDayNotes : sWeekDayNotes) {
                if (weekDayNotes.weekDayId == weekdayId) {
                    return weekDayNotes.dayNotes.morningNote;
                }
            }
            return null;
        }

        public static Note getEveningNote(final int weekdayId) {
            for (WeekDayNotes weekDayNotes : sWeekDayNotes) {
                if (weekDayNotes.weekDayId == weekdayId) {
                    return weekDayNotes.dayNotes.eveningNote;
                }
            }
            return null;
        }

        public static Note.FailedCause deleteNote(final Note note) {
            // Remove the note from week days.
            for (WeekDayNotes weekDayNotes : sWeekDayNotes) {
                weekDayNotes.dayNotes.deleteNote(note);
            }
            return Note.deleteNote(note);
        }

        public static int getNoteCountInWeek(final Note note) {
            int count = 0;
            for (WeekDayNotes weekDayNotes : sWeekDayNotes) {
                count += weekDayNotes.getNoteCount(note);
            }
            return count;
        }
    }

    private static final String SEP_DATE = "#";

    public static class DateNotes {
        public final Day date;

        public int weekdayId_equals = -1;
        public final DayNotes dayNotes = new DayNotes();

        public DateNotes(final Day date) {
            this.date = date;
            Utility.obtainWeekDay(date);
        }

        public boolean isSameDate(final DateNotes dateNotesInput) {
            if (dateNotesInput == null) return false;
            return date.isSameDay(dateNotesInput.date);
        }

        public void copyDayNotes(final DayNotes dayNotesInput) {
            dayNotes.eveningNote = dayNotesInput.eveningNote;
            dayNotes.morningNote = dayNotesInput.morningNote;
            dayNotes.amNotes.clear();
            for (Note note : dayNotesInput.amNotes) {
                dayNotes.amNotes.add(note);
            }
            dayNotes.pmNotes.clear();
            for (Note note : dayNotesInput.pmNotes) {
                dayNotes.pmNotes.add(note);
            }
        }

        public String dateInfo(final Context context) {
            return date.ymdw_chinese(context);
        }

        public String notesInfo(final Context context) {
            if (weekdayId_equals <= 0) {
                if (dayNotes == null) return null;
                return dayNotes.briefInfo();
            }
            String weekDayLabel = WeekDay.getFullLabel(context, weekdayId_equals);
            return context.getString(R.string.take_weekday_notes, weekDayLabel);
        }

        private static final String SEP_WEEKDAY = "→";
        private static final String SEP_DAY = "＋";
        // Format: yyyymmdd-weekdayId
        private static final String FORMAT_TO_STRING_WEEKDAY_EQUALS = "%4d%02d%02d" + SEP_WEEKDAY
                + "%d";
        // Format: yyyymmdd-dayCourseString
        private static final String FORMAT_TO_STRING_SPECIFIED = "%4d%02d%02d" + SEP_DAY + "%s";

        @Override
        public String toString() {
            if (weekdayId_equals >= 0) {
                return String.format(FORMAT_TO_STRING_WEEKDAY_EQUALS, date.year, date.month,
                        date.day, weekdayId_equals);
            }
            return String.format(FORMAT_TO_STRING_SPECIFIED, date.year, date.month, date.day,
                    dayNotes.toString());
        }

        private static DateNotes parseDateByYMD(final String yyyymmdd) {
            int year = Integer.parseInt(yyyymmdd.substring(0, 4));
            int month = Integer.parseInt(yyyymmdd.substring(4, 6));
            int day = Integer.parseInt(yyyymmdd.substring(6));
            return new DateNotes(new Day(year, month, day));
        }

        private static DateNotes parseDate(final String dateNotesString) {
            if (TextUtils.isEmpty(dateNotesString)) return null;
            DateNotes dateNotes = null;
            if (dateNotesString.contains(SEP_WEEKDAY)) {
                String[] elements = dateNotesString.split(SEP_WEEKDAY);
                dateNotes = parseDateByYMD(elements[0]);
                dateNotes.weekdayId_equals = Integer.parseInt(elements[1]);
            }
            if (dateNotesString.contains(SEP_DAY)) {
                String[] elements = dateNotesString.split(SEP_DAY);
                dateNotes = parseDateByYMD(elements[0]);
                dateNotes.copyDayNotes(DayNotes.parseDayNotes(elements[1], null));
            }
            return dateNotes;
        }

        public static DateNotes[] parseDateNotes(final String datesNotesString) {
            if (TextUtils.isEmpty(datesNotesString)) return null;
            String[] elements = datesNotesString.split(SEP_DATE);
            ArrayList<DateNotes> list = new ArrayList<DateNotes>(elements.length);
            DateNotes dateNotes;
            for (String element : elements) {
                dateNotes = parseDate(element);
                if (dateNotes == null) continue;
                list.add(dateNotes);
            }
            return list.toArray(new DateNotes[list.size()]);
        }

        private static void sort() {
            Collections.sort(sDateNotes, new Comparator<DateNotes>() {
                @Override
                public int compare(DateNotes dateNotes1, DateNotes dateNotes2) {
                    return Day.compare(dateNotes1.date, dateNotes2.date);
                }
            });
        }
    }

    public static void generateRandomDayNotes() {
        Note note;
        for (WeekDayNotes weekdayNotes : sWeekDayNotes) {
            weekdayNotes.init();
            if (!Utility.randomBoolean()) continue;

            boolean hasMorningNote = Utility.randomBoolean();
            if (hasMorningNote) {
                note = Note.getRandomNote();
                weekdayNotes.setMorningNote(note);
                Note.addNote(note);
            }
            note = null;
            boolean hasEveningNote = Utility.randomBoolean();
            if (hasEveningNote) {
                note = Note.getRandomNote();
                weekdayNotes.setEveningNote(note);
                Note.addNote(note);
            }
            for (AmNote amNote : AmNote.values()) {
                if (Utility.randomBoolean()) {
                    note = Note.getRandomNote();
                    Note.addNote(note);
                    weekdayNotes.setAmNote(amNote.ordinal(), note);
                }
            }
            for (PmNote pmNote : PmNote.values()) {
                if (Utility.randomBoolean()) {
                    note = Note.getRandomNote();
                    Note.addNote(note);
                    weekdayNotes.setPmNote(pmNote.ordinal(), note);
                }
            }
        }
    }

    private static int fillTodayDate(final Day today) {
        int index = 0;
        for (WeekDayNotes weekdayNotes : sWeekDayNotes) {
            if (weekdayNotes.weekDayId == today.weekday) {
                weekdayNotes.date = today;
                return index;
            }
            index++;
        }
        return -1;
    }

    public static void fillWeekDates(final Day today) {
        final int todayIndex = fillTodayDate(today);
        int deltaDayCount;
        for (int i = 0; i < sWeekDayNotes.length; i++) {
            if (i == todayIndex) continue;
            deltaDayCount = i - todayIndex;
            sWeekDayNotes[i].date = Utility.getDate(today, deltaDayCount);
        }
    }

    private static class DateNotesListAdapter extends BaseAdapter {
        private final Context mmContext;

        private final DateNotesChangeListener mmDateNotesChangeListener;

        public DateNotesListAdapter(final Context context,
                final DateNotesChangeListener dateNotesChangeListener) {
            mmContext = context;
            mmDateNotesChangeListener = dateNotesChangeListener;
        }

        @Override
        public int getCount() {
            return sDateNotes.size();
        }

        @Override
        public Object getItem(int position) {
            return sDateNotes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DateNotesViewHolder viewHolder;

            if (convertView == null) {
                convertView = View.inflate(mmContext, R.layout.item_date_notes, null);

                viewHolder = new DateNotesViewHolder();
                viewHolder.viewDate = (TextView) convertView.findViewById(R.id.view_date);
                viewHolder.viewInfo = (TextView) convertView.findViewById(R.id.view_info);
                viewHolder.viewDelete = convertView.findViewById(R.id.view_delete);
                viewHolder.viewEdit = convertView.findViewById(R.id.view_edit);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (DateNotesViewHolder) convertView.getTag();
            }

            initViewHolder(viewHolder, position);

            return convertView;
        }

        private void initViewHolder(final DateNotesViewHolder viewHolder, final int position) {
            final DateNotes item = (DateNotes) getItem(position);
            viewHolder.viewDate.setText(item.dateInfo(mmContext));
            viewHolder.viewInfo.setText(item.notesInfo(mmContext));
            viewHolder.viewDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmDialog(item);
                }
            });
            viewHolder.viewEdit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditDateNotesDialog(mmContext, item, mmDateNotesChangeListener);
                }
            });
        }

        private void showDeleteConfirmDialog(final DateNotes item) {
            String title = mmContext.getString(R.string.title_delete_date_notes);
            String confirmPrompt = mmContext.getString(R.string.prompt_delete_date_notes,
                    item.date.ymd_chinese());
            DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    removeDateNotes(mmContext, item, mmDateNotesChangeListener);
                }
            };
            Utility.showConfirmDialog(mmContext, title, confirmPrompt, positiveButtonListener);
        }
    }

    private static class DateNotesViewHolder {
        TextView viewDate;
        TextView viewInfo;
        View viewDelete;
        View viewEdit;
    }

    private interface NoteActionListener {
        public void onNoteAdded();

        public void onNoteDeleted(Note noteDeleted);

        public void onNoteChanged();

        public void clearNote();

        public void select(int position);
    }

    private static class NoteListAdapter extends BaseAdapter {
        private final Context mContext;
        private final NoteActionListener mNoteActionListener;
        private final Note.Type mNoteType;

        private ArrayList<Note> mNotes;

        private int mSelectedIndex = -1;

        public NoteListAdapter(final Context context, final NoteActionListener listener,
                final Note.Type noteType) {
            mContext = context;
            mNoteActionListener = listener;
            mNoteType = noteType;
            refreshData();
        }

        private void refreshData() {
            mNotes = Note.getNotes(mNoteType);
        }

        private void dataChanged() {
            refreshData();
            notifyDataSetChanged();
        }

        public void setSelected(final int index) {
            if (index >= 0 && index < mNotes.size() && mSelectedIndex != index) {
                mSelectedIndex = index;
                notifyDataSetInvalidated();
            }
        }

        public Note getSelected() {
            if (mSelectedIndex < 0 || mNotes == null) return null;
            if (mSelectedIndex >= mNotes.size()) return null;
            return mNotes.get(mSelectedIndex);
        }

        public int getItemIndex(final Note note) {
            return mNotes.indexOf(note);
        }

        @Override
        public int getCount() {
            return mNotes == null ? 0 : mNotes.size();
        }

        @Override
        public Object getItem(int position) {
            int count = getCount();
            if (position >= count || position < 0) return null;
            return mNotes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NoteViewHolder viewHolder;

            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.item_note, null);

                viewHolder = new NoteViewHolder();
                viewHolder.viewNoteInfo = convertView.findViewById(R.id.view_note_info);
                viewHolder.viewSelected = (RadioButton) convertView.findViewById(R.id.radio_note);
                viewHolder.viewTextFull = (TextView) convertView.findViewById(R.id.view_text_full);
                viewHolder.viewTextShort = (TextView)convertView.findViewById(R.id.view_text_short);
                viewHolder.viewType = (TextView) convertView.findViewById(R.id.view_note_type);
                viewHolder.viewDelete = convertView.findViewById(R.id.view_delete);
                viewHolder.viewEdit = convertView.findViewById(R.id.view_edit);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (NoteViewHolder) convertView.getTag();
            }

            initViewHolder(viewHolder, position);

            return convertView;
        }

        private void initViewHolder(final NoteViewHolder viewHolder, final int position) {
            final Note item = (Note) getItem(position);

            viewHolder.viewNoteInfo.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNoteActionListener.select(position);
                }
            });
            viewHolder.viewSelected.setChecked(mSelectedIndex == position);

            viewHolder.viewTextFull.setText(item.textFull);
            viewHolder.viewTextShort.setText(item.textShort);

            viewHolder.viewTextShort.setTextColor(item.getColorText());
            viewHolder.viewTextShort.setBackgroundColor(item.getColorBackground());

            viewHolder.viewType.setText(item.type.typeResId);
            viewHolder.viewType.setTextColor(item.type.colorText);
            viewHolder.viewType.setBackgroundColor(item.type.colorBackground);

            if (mNoteActionListener == null) {
                viewHolder.viewDelete.setVisibility(View.GONE);
                viewHolder.viewEdit.setVisibility(View.GONE);
            } else {
                viewHolder.viewDelete.setVisibility(View.VISIBLE);
                viewHolder.viewDelete.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDeleteNoteConfirmDialog(item);
                    }
                });
                viewHolder.viewEdit.setVisibility(View.VISIBLE);
                viewHolder.viewEdit.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditNoteDialog(mContext, item, mNoteActionListener);
                    }
                });
            }
        }

        private void showDeleteNoteConfirmDialog(final Note note) {
            final String confirmPrompt = mContext.getString(R.string.prompt_delete_note,
                    note.textFull);
            final DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Note.FailedCause failedCause = WeekDayNotes.deleteNote(note);
                    if (failedCause == null) {
                        if (note.equals(getItem(mSelectedIndex))) {
                            mSelectedIndex = -1;
                        }
                        mNoteActionListener.onNoteDeleted(note);
                    } else {
                        Utility.showInfoDialog(mContext,
                                "Failed to delete note:" + note + "\n" + failedCause.cause);
                    }
                }
            };
            Utility.showConfirmDialog(mContext, confirmPrompt, positiveButtonListener);
        }
    }

    private static class NoteViewHolder {
        View viewNoteInfo;
        RadioButton viewSelected;
        TextView viewTextFull;
        TextView viewTextShort;

        TextView viewType;
        View viewDelete;
        View viewEdit;
    }
}
