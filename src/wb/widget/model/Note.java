package wb.widget.model;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import java.util.ArrayList;

import wb.widget.R;
import wb.widget.utils.Utility;

public class Note {

    public static final int DEFAULT_COLOR_NOTE_TEXT = Color.BLACK;
    public static final int DEFAULT_COLOR_NOTE_BACKGROUND = Color.WHITE;

    public static enum FailedCause {
        SameNoteFullText("SameNoteFullText"),
        SameNoteShortText("SameNoteShortText"),
        NoteNotExist("Note does NOT exist!"),
        FailedAdd("Failed to add new note!"),
        FailedDelete("Failed to delete note!"),
        Unknown("Unknown");

        public final String cause;

        private FailedCause(final String cause) {
            this.cause = cause;
        }
    }

    public static enum Type {
        Important(R.string.note_type_important, R.id.radio_note_important, Color.RED,     Color.LTGRAY),
        Ordinary(R.string.note_type_ordinary,   R.id.radio_note_ordinary,  Color.BLACK,   Color.GREEN),
        Special(R.string.note_type_special,     R.id.radio_note_special,   Color.YELLOW,  Color.BLACK),
        Fun(R.string.note_type_fun,             R.id.radio_note_fun,       Color.MAGENTA, Color.GRAY),
        Other(R.string.note_type_other,         R.id.radio_note_other,     Color.LTGRAY,  Color.DKGRAY);

        public final int typeResId;
        public final int radioResId;
        public final int colorText, colorBackground;

        private Type(int typeTypeResId, int radioResId, int colorText, int colorBackground) {
            this.typeResId = typeTypeResId;
            this.radioResId = radioResId;
            this.colorText = colorText;
            this.colorBackground = colorBackground;
        }

        public static int typeCount() {
            return values().length;
        }

        public static Type get(final int ordinal) {
            for (Type type : values()) {
                if (type.ordinal() == ordinal) return type;
            }
            return null;
        }

        public static Type getFromRadio(final int radioResId) {
            for (Type type : values()) {
                if (type.radioResId == radioResId) return type;
            }
            return null;
        }
    }

    public final String textFull, textShort;
    public Type type;

    private Integer mColorText, mColorBackground;

    public Note(final String textFull, final String textShort, final Type type) {
        this.textFull = textFull;
        this.textShort = textShort;
        this.type = type;
    }

    public String shortText() {
        if (!TextUtils.isEmpty(textShort)) return shortText(textShort);
        return shortText(textFull);
    }

    public String briefText() {
        if (!TextUtils.isEmpty(textShort)) return textShort;
        if (TextUtils.isEmpty(textFull)) return textFull;
        return textFull.substring(0, 1);
    }

    public static String shortText(final String text) {
        final int textLen = text == null ? 0 : text.length();
        if (textLen <= 0) return EMPTY;
        if (textLen > 4) {
            return text.substring(0, 4);
        }
        return text;
    }

    public int getColorText() {
        if (mColorText != null) return mColorText;
        if (type != null) return type.colorText;
        return DEFAULT_COLOR_NOTE_TEXT;
    }

    public void setColorText(final int colorText) {
        mColorText = colorText;
    }

    public int getColorBackground() {
        if (mColorBackground != null) return mColorBackground;
        if (type != null) return type.colorBackground;
        return DEFAULT_COLOR_NOTE_BACKGROUND;
    }

    public void setColorBackground(final int colorBackground) {
        mColorBackground = colorBackground;
    }

    public String textInfo() {
        if (!TextUtils.isEmpty(textShort)) return textShort;
        return textFull;
    }

    // Format: name(type, colorText/colorBackground)
    private static final String FORMAT_TO_STRING_COLOR11 = "%s(%s,%d,%d/%d)";
    // Format: name(type, colorText)
    private static final String FORMAT_TO_STRING_COLOR10 = "%s(%s,%d,%d)";
    // Format: name(type, /colorBackground)
    private static final String FORMAT_TO_STRING_COLOR01 = "%s(%s,%d,/%d)";
    // Format: name(type)
    private static final String FORMAT_TO_STRING_COLOR00 = "%s(%s,%d)";

    private static final String EMPTY = "";

    private static final String SEP_NOTE = "#";

    @Override
    public String toString() {
        final String fullText = TextUtils.isEmpty(textFull) ? EMPTY : textFull;
        final String shortText = TextUtils.isEmpty(textShort) ? EMPTY : textShort;
        if (mColorText == null) {
            if (mColorBackground == null) {
                return String.format(FORMAT_TO_STRING_COLOR00, fullText, shortText, type.ordinal());
            }
            return String.format(FORMAT_TO_STRING_COLOR01, fullText, shortText, type.ordinal(),
                    mColorBackground);
        }
        if (mColorBackground == null) {
            return String.format(FORMAT_TO_STRING_COLOR10, fullText, shortText, type.ordinal(),
                    mColorText);
        }
        return String.format(FORMAT_TO_STRING_COLOR11, fullText, shortText, type.ordinal(),
                mColorText, mColorBackground);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Note)) return false;
        if (obj == this) return true;
        Note input = (Note) obj;
        if (!TextUtils.equals(textFull, input.textFull)) return false;
        return true;
    }

    // The "input" must be in format listed above.
    public static Note parse(final String input) {
        try {
            return doParse(input);
        } catch (RuntimeException re) {
            return null;
        }
    }

    private static Note doParse(final String input) {
        if (TextUtils.isEmpty(input)) return null;
        int indexLeftBracket = input.indexOf('(');
        if (indexLeftBracket < 0) return null;
        int indexRightBracket = input.indexOf(')');
        if (indexRightBracket <= indexLeftBracket) return null;

        String fullText = input.substring(0, indexLeftBracket);
        String[] elements = input.substring(indexLeftBracket + 1, indexRightBracket).split(",");
        if (elements == null || elements.length <= 0) return null;

        String shortText = elements[0];
        int typeOrdinal = Integer.parseInt(elements[1]);
        Note note = new Note(fullText, shortText, Type.get(typeOrdinal));
        if (elements.length > 2) {
            String[] colors = elements[2].split("/");
            if (colors != null && colors.length > 0) {
                note.setColorText(Integer.parseInt(colors[0]));
                if (colors.length > 1) {
                    note.setColorBackground(Integer.parseInt(colors[1]));
                }
            }
        }
        return note;
    }

    public static Note getRandomNote() {
        int randomIndex = Utility.randomInt(0, sDefaultNotes.length - 1);
        return sDefaultNotes[randomIndex];
    }

    private static final int MAX_COUNT = (1 + 5 + 5 + 1) * 7;
    private static final ArrayList<Note> sNotes = new ArrayList<Note>(MAX_COUNT);

    public static String notesString() {
        StringBuilder sb = new StringBuilder();
        for (Note note : sNotes) {
            if (sb.length() > 0) sb.append(SEP_NOTE);
            sb.append(note.toString());
        }
        return sb.toString();
    }

    public static int getNoteCount() {
        return sNotes == null ? 0 : sNotes.size();
    }

    public static int getNoteIndex(final Note note) {
        return sNotes.indexOf(note);
    }

    public static ArrayList<Note> getNotes(final Type noteType) {
        if (noteType == null) return sNotes;
        ArrayList<Note> notes = new ArrayList<Note>(sNotes.size());
        for (Note course : sNotes) {
            if (course.type == noteType) notes.add(course);
        }
        return notes;
    }

    public static Note getNote(int index) {
        int count = sNotes == null ? 0 : sNotes.size();
        if (count <= 0) return null;
        if (index < 0 || index >= count) return null;
        return sNotes.get(index);
    }

    public static Note getNoteByText(final String noteText) {
        int count = sNotes == null ? 0 : sNotes.size();
        if (count <= 0 || TextUtils.isEmpty(noteText)) return null;
        for (Note note : sNotes) {
            if (TextUtils.equals(note.textFull, noteText)) {
                return note;
            }
            if (TextUtils.equals(note.textShort, noteText)) {
                return note;
            }
        }
        return null;
    }

    private static FailedCause checkNewNote(final Note newNote) {
        for (Note note : sNotes) {
            if (note.equals(newNote)) {
                return FailedCause.SameNoteFullText;
            }
            if (TextUtils.equals(note.shortText(), newNote.shortText())) {
                return FailedCause.SameNoteShortText;
            }
        }
        return null;
    }

    public static FailedCause addNote(final Note newNote) {
        FailedCause failedCause = checkNewNote(newNote);
        if (failedCause != null) return failedCause;
        if (sNotes.add(newNote)) return null;
        return FailedCause.FailedAdd;
    }

    public static FailedCause deleteNote(final Note note) {
        if (sNotes.contains(note)) {
            boolean result = sNotes.remove(note);
            return result ? null : FailedCause.FailedDelete;
        }
        return FailedCause.NoteNotExist;
    }

    public static void parseNotes(final String notesString) {
        String[] elements = notesString.split(SEP_NOTE);
        Note note;
        for (String element : elements) {
            if (TextUtils.isEmpty(element)) continue;
            note = parse(element);
            sNotes.add(note);
        }
    }

    public static void clearNotes() {
        if (sNotes.size() > 0) {
            sNotes.clear();
        }
    }

    private static final int[] sDefaultNoteIds = {
            R.string.note_running, R.string.note_pingpang, R.string.note_book,
            R.string.note_badminton, R.string.note_movie, R.string.note_party,
            R.string.note_team_building, R.string.note_homework, R.string.note_mahjong,
    };

    private static final Note[] sDefaultNotes = new Note[sDefaultNoteIds.length];

    public static void initDefaultNotes(final Context context) {
        final String separator = ",";
        final int typeCount = Type.typeCount();
        String[] elements;
        Type type;
        for (int i = 0; i < sDefaultNotes.length; i++) {
            elements = context.getString(sDefaultNoteIds[i]).split(separator);
            type = Type.get(Utility.randomInt(0, typeCount - 1));
            sDefaultNotes[i] = new Note(elements[0], elements[1], type);
        }
    }
}
