package wb.widget.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import wb.widget.R;
import wb.widget.SettingsWeekNotesActivity.DayNotes;
import wb.widget.SettingsWeekNotesActivity.WeekDayNotes;
import wb.widget.SettingsWeekNotesActivity.WeekNotesColorScheme;

public class WeekNotes {
    public interface WeekNotesListener {
        public void showSetNoteDialog(final Context context, final int weekdayId, final int viewId,
                final int viewLabelResId);
    }

    public interface DayNotesListener {
        public void showSetNoteDialog(final Context context, final DayNoteViewInfo dayNoteViewInfo,
                final int viewId, final int viewLabelResId, final Note note);
    }

    private static class NoteViewInfo {
        public final int viewId;
        public final int viewLabelResId;

        private TextView mTextView;

        private int mColorText, mColorBackground;

        private boolean hasNotes;

        public NoteViewInfo(int viewId, int viewLabelResId) {
            this.viewId = viewId;
            this.viewLabelResId = viewLabelResId;
        }

        public void initView(final Activity activity, final int weekdayId,
                final WeekNotesListener listener) {
            mTextView = activity.findViewById(viewId);
            mTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.showSetNoteDialog(activity, weekdayId, viewId, viewLabelResId);
                }
            });
        }

        public void initView(final View rootView, final DayNoteViewInfo dayNoteViewInfo,
                final Note note, final DayNotesListener listener) {
            mTextView = rootView.findViewById(viewId);
            mTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.showSetNoteDialog(rootView.getContext(), dayNoteViewInfo, viewId,
                            viewLabelResId, note);
                }
            });
            if (note != null) {
                mTextView.setText(note.shortText());
                mTextView.setTextColor(note.getColorText());
                mTextView.setBackgroundColor(note.getColorBackground());
            }
        }

        public void setText(final String text) {
            if (TextUtils.isEmpty(text)) {
                hasNotes = false;
                mTextView.setText(R.string.set_note);
            } else {
                mTextView.setText(text);
                hasNotes = true;
            }
        }

        public void noNote() {
            hasNotes = false;
            mTextView.setText(R.string.set_note);
            setViewColors(Color.DKGRAY, Color.LTGRAY);
        }

        public void setViewColors(int colorText, int colorBackground) {
            mColorText = colorText;
            mColorBackground = colorBackground;
        }

        public void refreshView(WeekNotesColorScheme colorScheme, int colorText, int colorBackground) {
            if (!hasNotes) {
                mTextView.setTextColor(mColorText);
                mTextView.setBackgroundColor(mColorBackground);
                return;
            }
            if (colorScheme == WeekNotesColorScheme.SameColor) {
                mTextView.setTextColor(colorText);
                mTextView.setBackgroundColor(colorBackground);
            } else {
                mTextView.setTextColor(mColorText);
                mTextView.setBackgroundColor(mColorBackground);
            }
        }

        public void setNote(final Note note) {
            if (note == null) {
                noNote();
                return;
            }
            setText(note.shortText());
            setViewColors(note.getColorText(), note.getColorBackground());
        }
    }

    private static final NoteViewInfo[] sMondayViews = {
            new NoteViewInfo(R.id.monday_morning_note, R.string.morning_note),
            new NoteViewInfo(R.id.monday_evening_note, R.string.evening_note),
            new NoteViewInfo(R.id._108, R.string.morning_08),
            new NoteViewInfo(R.id._109, R.string.morning_09),
            new NoteViewInfo(R.id._110, R.string.morning_10),
            new NoteViewInfo(R.id._111, R.string.morning_11),
            new NoteViewInfo(R.id._112, R.string.morning_12),
            new NoteViewInfo(R.id._113, R.string.afternoon_13),
            new NoteViewInfo(R.id._114, R.string.afternoon_14),
            new NoteViewInfo(R.id._115, R.string.afternoon_15),
            new NoteViewInfo(R.id._116, R.string.afternoon_16),
            new NoteViewInfo(R.id._117, R.string.afternoon_17),
    };

    private static final NoteViewInfo[] sTuesdayViews = {
            new NoteViewInfo(R.id.tuesday_morning_note, R.string.morning_note),
            new NoteViewInfo(R.id.tuesday_evening_note, R.string.evening_note),
            new NoteViewInfo(R.id._208, R.string.morning_08),
            new NoteViewInfo(R.id._209, R.string.morning_09),
            new NoteViewInfo(R.id._210, R.string.morning_10),
            new NoteViewInfo(R.id._211, R.string.morning_11),
            new NoteViewInfo(R.id._212, R.string.morning_12),
            new NoteViewInfo(R.id._213, R.string.afternoon_13),
            new NoteViewInfo(R.id._214, R.string.afternoon_14),
            new NoteViewInfo(R.id._215, R.string.afternoon_15),
            new NoteViewInfo(R.id._216, R.string.afternoon_16),
            new NoteViewInfo(R.id._217, R.string.afternoon_17),
    };

    private static final NoteViewInfo[] sWednesdayViews = {
            new NoteViewInfo(R.id.wednesday_morning_note, R.string.morning_note),
            new NoteViewInfo(R.id.wednesday_evening_note, R.string.evening_note),
            new NoteViewInfo(R.id._308, R.string.morning_08),
            new NoteViewInfo(R.id._309, R.string.morning_09),
            new NoteViewInfo(R.id._310, R.string.morning_10),
            new NoteViewInfo(R.id._311, R.string.morning_11),
            new NoteViewInfo(R.id._312, R.string.morning_12),
            new NoteViewInfo(R.id._313, R.string.afternoon_13),
            new NoteViewInfo(R.id._314, R.string.afternoon_14),
            new NoteViewInfo(R.id._315, R.string.afternoon_15),
            new NoteViewInfo(R.id._316, R.string.afternoon_16),
            new NoteViewInfo(R.id._317, R.string.afternoon_17),
    };

    private static final NoteViewInfo[] sThursdayViews = {
            new NoteViewInfo(R.id.thursday_morning_note, R.string.morning_note),
            new NoteViewInfo(R.id.thursday_evening_note, R.string.evening_note),
            new NoteViewInfo(R.id._408, R.string.morning_08),
            new NoteViewInfo(R.id._409, R.string.morning_09),
            new NoteViewInfo(R.id._410, R.string.morning_10),
            new NoteViewInfo(R.id._411, R.string.morning_11),
            new NoteViewInfo(R.id._412, R.string.morning_12),
            new NoteViewInfo(R.id._413, R.string.afternoon_13),
            new NoteViewInfo(R.id._414, R.string.afternoon_14),
            new NoteViewInfo(R.id._415, R.string.afternoon_15),
            new NoteViewInfo(R.id._416, R.string.afternoon_16),
            new NoteViewInfo(R.id._417, R.string.afternoon_17),
    };

    private static final NoteViewInfo[] sFridayViews = {
            new NoteViewInfo(R.id.friday_morning_note, R.string.morning_note),
            new NoteViewInfo(R.id.friday_evening_note, R.string.evening_note),
            new NoteViewInfo(R.id._508, R.string.morning_08),
            new NoteViewInfo(R.id._509, R.string.morning_09),
            new NoteViewInfo(R.id._510, R.string.morning_10),
            new NoteViewInfo(R.id._511, R.string.morning_11),
            new NoteViewInfo(R.id._512, R.string.morning_12),
            new NoteViewInfo(R.id._513, R.string.afternoon_13),
            new NoteViewInfo(R.id._514, R.string.afternoon_14),
            new NoteViewInfo(R.id._515, R.string.afternoon_15),
            new NoteViewInfo(R.id._516, R.string.afternoon_16),
            new NoteViewInfo(R.id._517, R.string.afternoon_17),
    };

    private static final NoteViewInfo[] sSaturdayViews = {
            new NoteViewInfo(R.id.saturday_morning_note, R.string.morning_note),
            new NoteViewInfo(R.id.saturday_evening_note, R.string.evening_note),
            new NoteViewInfo(R.id._608, R.string.morning_08),
            new NoteViewInfo(R.id._609, R.string.morning_09),
            new NoteViewInfo(R.id._610, R.string.morning_10),
            new NoteViewInfo(R.id._611, R.string.morning_11),
            new NoteViewInfo(R.id._612, R.string.morning_12),
            new NoteViewInfo(R.id._613, R.string.afternoon_13),
            new NoteViewInfo(R.id._614, R.string.afternoon_14),
            new NoteViewInfo(R.id._615, R.string.afternoon_15),
            new NoteViewInfo(R.id._616, R.string.afternoon_16),
            new NoteViewInfo(R.id._617, R.string.afternoon_17),
    };

    private static final NoteViewInfo[] sSundayViews = {
            new NoteViewInfo(R.id.sunday_morning_note, R.string.morning_note),
            new NoteViewInfo(R.id.sunday_evening_note, R.string.evening_note),
            new NoteViewInfo(R.id._708, R.string.morning_08),
            new NoteViewInfo(R.id._709, R.string.morning_09),
            new NoteViewInfo(R.id._710, R.string.morning_10),
            new NoteViewInfo(R.id._711, R.string.morning_11),
            new NoteViewInfo(R.id._712, R.string.morning_12),
            new NoteViewInfo(R.id._713, R.string.afternoon_13),
            new NoteViewInfo(R.id._714, R.string.afternoon_14),
            new NoteViewInfo(R.id._715, R.string.afternoon_15),
            new NoteViewInfo(R.id._716, R.string.afternoon_16),
            new NoteViewInfo(R.id._717, R.string.afternoon_17),
    };

    private static class WeekDayNoteInfo {
        public final int weekdayId;
        public final NoteViewInfo[] weekdayViews;

        public WeekDayNoteInfo(final int weekdayId, final NoteViewInfo[] weekdayViews) {
            this.weekdayId = weekdayId;
            this.weekdayViews = weekdayViews;
        }

        public void initViews(final Activity activity, final WeekNotesListener listener) {
            for (final NoteViewInfo viewInfo : weekdayViews) {
                viewInfo.initView(activity, weekdayId, listener);
            }
        }

        public void initViews() {
            for (final NoteViewInfo viewInfo : weekdayViews) {
                viewInfo.noNote();
                viewInfo.refreshView(null, 0, 0);
            }
        }
    }

    private static final WeekDayNoteInfo[] sWeekDayNotes = {
            new WeekDayNoteInfo(Calendar.MONDAY,    sMondayViews),
            new WeekDayNoteInfo(Calendar.TUESDAY,   sTuesdayViews),
            new WeekDayNoteInfo(Calendar.WEDNESDAY, sWednesdayViews),
            new WeekDayNoteInfo(Calendar.THURSDAY,  sThursdayViews),
            new WeekDayNoteInfo(Calendar.FRIDAY,    sFridayViews),
            new WeekDayNoteInfo(Calendar.SATURDAY,  sSaturdayViews),
            new WeekDayNoteInfo(Calendar.SUNDAY,    sSundayViews),
    };

    private final WeekNotesListener mListener;

    private WeekNotesColorScheme mColorScheme;
    private int mNoteColorText, mNoteColorBackground;

    public WeekNotes(final WeekNotesListener listener) {
        mListener = listener;
    }

    public void setColorScheme(WeekNotesColorScheme colorScheme, int colorText, int colorBackground) {
        mColorScheme = colorScheme;
        mNoteColorText = colorText;
        mNoteColorBackground = colorBackground;

        refreshViews();
    }

    public void clear() {
        for (WeekDayNoteInfo weekdayInfo : sWeekDayNotes) {
            weekdayInfo.initViews();
        }
    }

    public void initViews(final Activity activity) {
        for (WeekDayNoteInfo weekdayInfo : sWeekDayNotes) {
            weekdayInfo.initViews(activity, mListener);
        }
    }

    private void refreshViews() {
        for (WeekDayNoteInfo weekdayInfo : sWeekDayNotes) {
            for (NoteViewInfo viewInfo : weekdayInfo.weekdayViews) {
                viewInfo.refreshView(mColorScheme, mNoteColorText, mNoteColorBackground);
            }
        }
    }

    public void setWeekNotes(final Context context, final WeekDayNotes[] weekDayNotes) {
        WeekDayNotes weekDayNoteFound;
        for (WeekDayNoteInfo weekdayInfo : sWeekDayNotes) {
            weekDayNoteFound = null;
            for (WeekDayNotes weekDayNote : weekDayNotes) {
                if (weekDayNote.weekDayId == weekdayInfo.weekdayId) {
                    weekDayNoteFound = weekDayNote;
                    break;
                }
            }
            for (NoteViewInfo viewInfo : weekdayInfo.weekdayViews) {
                viewInfo.refreshView(mColorScheme, mNoteColorText, mNoteColorBackground);
            }
            if (weekDayNoteFound == null) continue;
            setNotes(weekdayInfo.weekdayViews, weekDayNoteFound.dayNotes);
        }
    }

    private void setNotes(final NoteViewInfo[] weekdayViewInfoArray, final DayNotes dayNotes) {
        // Set morning note.
        setViewNote(weekdayViewInfoArray[0], dayNotes.morningNote);
        // Set evening note.
        setViewNote(weekdayViewInfoArray[1], dayNotes.eveningNote);
        // Set notes.
        Note note;
        for (int i = 2; i < weekdayViewInfoArray.length; i++) {
            note = dayNotes.getNoteByLabel(weekdayViewInfoArray[i].viewLabelResId);
            setViewNote(weekdayViewInfoArray[i], note);
        }
    }

    private void setViewNote(final NoteViewInfo viewInfo, final Note note) {
        if (note == null) {
            viewInfo.noNote();
            return;
        }
        viewInfo.setText(note.shortText());
        viewInfo.setViewColors(note.getColorText(), note.getColorBackground());
    }

    public static class DayNoteViewInfo {
        private final NoteViewInfo[] mDayNoteViewInfoArray = {
                new NoteViewInfo(R.id.morning_note, R.string.morning_note),
                new NoteViewInfo(R.id.evening_note, R.string.evening_note),
                new NoteViewInfo(R.id._08, R.string.morning_08),
                new NoteViewInfo(R.id._09, R.string.morning_09),
                new NoteViewInfo(R.id._10, R.string.morning_10),
                new NoteViewInfo(R.id._11, R.string.morning_11),
                new NoteViewInfo(R.id._12, R.string.morning_12),
                new NoteViewInfo(R.id._13, R.string.afternoon_13),
                new NoteViewInfo(R.id._14, R.string.afternoon_14),
                new NoteViewInfo(R.id._15, R.string.afternoon_15),
                new NoteViewInfo(R.id._16, R.string.afternoon_16),
                new NoteViewInfo(R.id._17, R.string.afternoon_17),
        };

        public final DayNotes oldDayNotes;
        public final DayNotes newDayNotes = new DayNotes();

        public DayNoteViewInfo(final DayNotes dayNotes) {
            oldDayNotes = dayNotes;
            if (dayNotes == null) {
                newDayNotes.init();
            } else {
                newDayNotes.copyDayNotes(dayNotes);
            }
        }

        public void initViews(final Context context, final View rootView,
                final DayNotesListener listener) {
            final DayNoteViewInfo dayNoteViewInfo = this;
            for (NoteViewInfo noteViewInfo : mDayNoteViewInfoArray) {
                final Note note = newDayNotes.getNoteByLabel(noteViewInfo.viewLabelResId);
                noteViewInfo.initView(rootView, dayNoteViewInfo, note, listener);
            }
        }

        public void setNoteByLabel(final int labelResId, final Note note) {
            newDayNotes.setNoteByLabel(labelResId, note);

            for (NoteViewInfo noteViewInfo : mDayNoteViewInfoArray) {
                if (noteViewInfo.viewLabelResId == labelResId) {
                    noteViewInfo.setNote(note);
                    break;
                }
            }
        }

        public void refreshViews(final WeekNotesColorScheme colorScheme, final int colorText,
                final int colorBackground) {
            for (NoteViewInfo noteViewInfo : mDayNoteViewInfoArray) {
                noteViewInfo.refreshView(colorScheme, colorText, colorBackground);
            }
        }
    }
}
