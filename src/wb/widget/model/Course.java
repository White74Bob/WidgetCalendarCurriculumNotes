package wb.widget.model;

import java.util.ArrayList;

import android.graphics.Color;
import android.text.TextUtils;
import wb.widget.R;
import wb.widget.utils.Utility;

public class Course {

    public static final Course NULL = new Course(null, null);
    public static final String NULL_NAME = "NULL";

    public interface CourseChangeListener {
        public void whenCourseChanged();
        public void setCellCourse(Course course);
    }

    public static final int DEFAULT_COLOR_COURSE_TEXT = Color.BLACK;
    public static final int DEFAULT_COLOR_COURSE_BACKGROUND = Color.WHITE;

    public static enum FailedCause {
        SameCourseName(R.string.fail_cause_same_course_name),
        CourseNotExist(R.string.fail_cause_course_name_not_exist),
        Unknown(R.string.fail_cause_unknown);

        public final int causeResId;

        private FailedCause(final int causeResId) {
            this.causeResId = causeResId;
        }
    }

    public static enum Type {
        Major(R.string.course_type_major,     R.id.radio_course_major,   Color.BLACK,   Color.WHITE), // 主课
        Minor(R.string.course_type_minor,     R.id.radio_course_minor,   Color.DKGRAY,  Color.LTGRAY), // 副课
        Art(R.string.course_type_art,         R.id.radio_course_art,     Color.WHITE,   Color.MAGENTA),   // 艺术
        Self(R.string.course_type_self,       R.id.radio_course_self,    Color.WHITE,   Color.BLACK),  // 自习/活动
        Fun(R.string.course_type_fun,         R.id.radio_course_fun,     Color.BLACK,   Color.GREEN),   // 兴趣
        Other(R.string.course_type_other,     R.id.radio_course_other,   Color.LTGRAY,  Color.DKGRAY), // 班会/外出等.
        Reading(R.string.course_type_reading, R.id.radio_course_reading, Color.BLUE,    Color.GREEN); // 朗读

        public final int typeResId;
        public final int radioResId;
        public final int colorText, colorBackground;

        private Type(final int typeTypeResId, final int radioResId, final int colorText,
                final int colorBackground) {
            this.typeResId = typeTypeResId;
            this.radioResId = radioResId;
            this.colorText = colorText;
            this.colorBackground = colorBackground;
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

    public final String name;
    public Type type;

    public String teacher;
    public boolean teacherVisible = true;
    public String location;
    public boolean locationVisible = true;

    private Integer mColorText, mColorBackground;

    public Course(final String name, final Type type) {
        this.name = name;
        this.type = type;
    }

    public Course(final String name, final Type type, int colorText, int colorBackground) {
        this.name = name;
        this.type = type;
        mColorText = colorText;
        mColorBackground = colorBackground;
    }

    public String shortName() {
        if (TextUtils.isEmpty(name)) return EMPTY;
        return name.substring(0, 1);
    }

    public int getColorText() {
        if (mColorText != null) return mColorText;
        if (type != null) return type.colorText;
        return DEFAULT_COLOR_COURSE_TEXT;
    }

    public void setColorText(final int colorText) {
        mColorText = colorText;
    }

    public int getColorBackground() {
        if (mColorBackground != null) return mColorBackground;
        if (type != null) return type.colorBackground;
        return DEFAULT_COLOR_COURSE_BACKGROUND;
    }

    public void setColorBackground(final int colorBackground) {
        mColorBackground = colorBackground;
    }

    private static final String FORMAT_LOCATION_INFO_1_1 = "%s @ %s";
    private static final String FORMAT_LOCATION_INFO_0_1 = "@ %s";

    public String locationInfo() {
        if (TextUtils.isEmpty(teacher) || !teacherVisible) {
            if (TextUtils.isEmpty(location) || !locationVisible) {
                return null;
            }
            return String.format(FORMAT_LOCATION_INFO_0_1, location);
        }
        if (TextUtils.isEmpty(location) || !locationVisible) {
            return teacher;
        }
        return String.format(FORMAT_LOCATION_INFO_1_1, teacher, location);
    }

    // Format: name(type_ordinal, teacher(teacherVisible), address(addrVisible), colorText/colorBackground)
    private static final String FORMAT_TO_STRING_COLOR11 = "%s(%d,%s[%d],%s[%d],%d/%d)";
    // Format: name(type_ordinal, teacher(teacherVisible), address(addrVisible), colorText, teacherVisible, addrVisible)
    private static final String FORMAT_TO_STRING_COLOR10 = "%s(%d,%s[d],%s[d],%d)";
    // Format: name(type_ordinal, teacher(teacherVisible), address(addrVisible), colorBackground, teacherVisible, addrVisible)
    private static final String FORMAT_TO_STRING_COLOR01 = "%s(%d,%s[%d],%s[%d],/%d)";
    // Format: name(type_ordinal, teacher(teacherVisible), address(addrVisible))
    private static final String FORMAT_TO_STRING_COLOR00 = "%s(%d,%s[%d],%s[%d])";

    private static final String EMPTY = "";

    public static final String SEP_COURSE = "#";

    @Override
    public String toString() {
        if (this == NULL) return NULL_NAME;

        final int teacherVisibility = teacherVisible ? 1 : 0;
        final int locationVisibility = locationVisible ? 1 : 0;
        String strTeacher = teacher, strAddress = location;
        if (TextUtils.isEmpty(teacher)) {
            strTeacher = EMPTY;
        }
        if (TextUtils.isEmpty(location)) {
            strAddress = EMPTY;
        }
        if (mColorText == null) {
            if (mColorBackground == null) {
                return String.format(FORMAT_TO_STRING_COLOR00, name, type.ordinal(), strTeacher,
                                teacherVisibility, strAddress, locationVisibility);
            }
            return String.format(FORMAT_TO_STRING_COLOR01, name, type.ordinal(), strTeacher,
                            teacherVisibility, strAddress, locationVisibility, mColorBackground);
        }
        if (mColorBackground == null) {
            return String.format(FORMAT_TO_STRING_COLOR10, name, type.ordinal(), strTeacher,
                            teacherVisibility, strAddress, locationVisibility, mColorText);
        }
        return String.format(FORMAT_TO_STRING_COLOR11, name, type.ordinal(), strTeacher,
                        teacherVisibility, strAddress, locationVisibility, mColorText,
                        mColorBackground);
    }
    
    public static boolean isNullCourse(final Course course) {
        if (course == null || course == NULL) return true;
        return TextUtils.isEmpty(course.name);
    }
    
    public static boolean equals(final Course course1, final Course course2) {
        if (course1 == course2) return true;
        if (course1 == null || course2 == null) return false;
        if (course1 == NULL || course2 == NULL) return false;
        return course1.equals(course2);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Course)) return false;
        if (obj == this) return true;
        Course input = (Course) obj;
        if (!TextUtils.equals(name, input.name)) return false;
        if (!TextUtils.equals(teacher, input.teacher)) return false;
        if (type == input.type) return false;
        return true;
    }

    private static void parseTeacherInfo(final String input, final Course course) {
        if (TextUtils.isEmpty(input)) {
            course.teacher = EMPTY;
            return;
        }
        int left = input.indexOf('[');
        int right = input.indexOf(']');
        if (left < 0 && right < 0) {
            course.teacher = input.trim();
            return;
        }
        course.teacher = input.substring(0, left).trim();
        course.teacherVisible = Integer.parseInt(input.substring(left + 1, right)) != 0;
    }

    private static void parseLocationInfo(final String input, final Course course) {
        if (TextUtils.isEmpty(input)) {
            course.location = EMPTY;
            return;
        }
        int left = input.indexOf('[');
        int right = input.indexOf(']');
        if (left < 0 && right < 0) {
            course.location = input.trim();
            return;
        }
        course.location = input.substring(0, left).trim();
        course.locationVisible = Integer.parseInt(input.substring(left + 1, right)) != 0;
    }

    // The "input" must be in format listed above.
    public static Course parse(final String input) {
        if (TextUtils.isEmpty(input)) return null;
        int indexLeftBracket = input.indexOf('(');
        if (indexLeftBracket < 0) return null;
        int indexRightBracket = input.indexOf(')');
        if (indexRightBracket <= indexLeftBracket) return null;

        String name = input.substring(0, indexLeftBracket);
        String[] elements = input.substring(indexLeftBracket + 1, indexRightBracket).split(",");
        if (elements == null || elements.length <= 0) return null;

        int typeOrdinal = Integer.parseInt(elements[0]);
        Course course = new Course(name, Type.get(typeOrdinal));

        // course.teacher = getFromArray(elements, 1);
        // course.location = getFromArray(elements, 2);
        parseTeacherInfo(getFromArray(elements, 1), course);
        parseLocationInfo(getFromArray(elements, 2), course);

        String colorString = getFromArray(elements, 3);
        if (colorString != null) {
            String[] colors = colorString.split("/");
            if (colors != null && colors.length > 0) {
                course.setColorText(Integer.parseInt(colors[0]));
                if (colors.length > 1) {
                    course.setColorBackground(Integer.parseInt(colors[1]));
                }
            }
        }
        return course;
    }

    private static String getFromArray(final String[] array, final int index) {
        if (index >= array.length) return null;
        return array[index];
    }

    public static final Course sChinese = new Course("语文", Type.Major, Color.BLACK, Color.WHITE);
    public static final Course sChineseReading = new Course("语文阅读", Type.Self);
    public static final Course sMath = new Course("数学", Type.Major, Color.WHITE, Color.BLACK);
    public static final Course sMathGame = new Course("数学游戏", Type.Fun, Color.BLACK, Color.YELLOW);
    public static final Course sEnglish = new Course("英语", Type.Major, Color.YELLOW, Color.GRAY);
    public static final Course sPolitics = new Course("政治", Type.Major, Color.WHITE, Color.GRAY);
    public static final Course sMoralityLaw = new Course("道德与法治", Type.Major, Color.BLACK, Color.GRAY);
    public static final Course sHistory = new Course("历史", Type.Minor, Color.GRAY, Color.WHITE);
    public static final Course sGeography = new Course("地理", Type.Minor, Color.WHITE, Color.GRAY);
    public static final Course sPhysics = new Course("物理", Type.Major, Color.GRAY, Color.YELLOW);
    public static final Course sChemistry = new Course("化学", Type.Major, Color.CYAN, Color.YELLOW);
    public static final Course sBiology = new Course("生物", Type.Major, Color.BLUE, Color.CYAN);
    public static final Course sMusic = new Course("音乐", Type.Art, Color.MAGENTA, Color.BLUE);
    public static final Course sDance = new Course("舞蹈", Type.Art, Color.MAGENTA, Color.CYAN);
    public static final Course sDrawing = new Course("美术", Type.Art, Color.MAGENTA, Color.YELLOW);
    public static final Course sPhysicalEducation = new Course("体育", Type.Minor, Color.BLACK, Color.RED);
    public static final Course sMorningReading = new Course("晨诵", Type.Reading, Color.GRAY, Color.WHITE);
    public static final Course sClass = new Course("班会", Type.Other, Color.GRAY, Color.WHITE);
    public static final Course sSchool = new Course("校本", Type.Other, Color.WHITE, Color.GRAY);
    public static final Course sSelfStudy = new Course("自习", Type.Self, Color.BLACK, Color.GREEN);
    public static final Course sSelfAct = new Course("活动", Type.Other, Color.GREEN, Color.BLACK);
    public static final Course sScience = new Course("科学", Type.Fun, Color.BLUE, Color.MAGENTA);
    public static final Course sComputer = new Course("计算机", Type.Fun, Color.BLUE, Color.MAGENTA);
    public static final Course sOut = new Course("课外", Type.Other, Color.GREEN, Color.GRAY);
    public static final Course sEveningStudy = new Course("晚自习", Type.Self, Color.WHITE, Color.BLACK);

    private static final Course[] sDefaultCourses = {
                    sChinese, sMath, sEnglish, sMoralityLaw,
                    sMusic, sDance, sDrawing, sPhysicalEducation, sScience,
                    sMorningReading, sChineseReading, sMathGame, sClass, sSchool
    };

    public static Course getRandomCourse() {
        int randomIndex = Utility.randomInt(0, sDefaultCourses.length - 1);
        return sDefaultCourses[randomIndex];
    }

    public static Course getRandomCourse0(final Course.Type... courseTypes) {
        int randomIndex;
        while (true) {
            randomIndex = Utility.randomInt(0, sDefaultCourses.length - 1);
            for (Course.Type type : courseTypes) {
                if (sDefaultCourses[randomIndex].type == type) {
                    return sDefaultCourses[randomIndex];
                }
            }
        }
    }

    public static String defaultCoursesString() {
        return Utility.array2String(sDefaultCourses, SEP_COURSE);
    }
    
    public static void copyDefaultCourses(final ArrayList<Course> courses) {
        courses.clear();
        //courses = new ArrayList<Course>(sDefaultCourses.length);
        for (Course course : sDefaultCourses) {
            courses.add(course);
        }
    }

    public static boolean isDefaultUsed(final ArrayList<Course> courses) {
        int courseCount = courses.size();
        if (courseCount != sDefaultCourses.length) return false;
        for (int i = 0; i < courseCount; i++) {
            if (!sDefaultCourses[i].equals(courses.get(i))) return false;
        }
        return true;
    }
}
