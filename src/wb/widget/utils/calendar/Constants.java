package wb.widget.utils.calendar;

import java.util.Calendar;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import wb.widget.R;
import wb.widget.model.Day;

public class Constants {
    public static final int INVALID_DAY_ID = -1;

    public static final String NULL_STR = "";
    public static final String SPACE_3_FULL = "　　　";

    public static enum WeekDay {
        Mon(Calendar.MONDAY,    R.string.monday,    R.string.full_monday),
        Tue(Calendar.TUESDAY,   R.string.tuesday,   R.string.full_tuesday),
        Wed(Calendar.WEDNESDAY, R.string.wednesday, R.string.full_wednesday),
        Thu(Calendar.THURSDAY,  R.string.thursday,  R.string.full_thursday),
        Fri(Calendar.FRIDAY,    R.string.friday,    R.string.full_friday),
        Sat(Calendar.SATURDAY,  R.string.saturday,  R.string.full_saturday),
        Sun(Calendar.SUNDAY,    R.string.sunday,    R.string.full_sunday);

        public final int calendarId;
        public final int labelResId;
        public final int labelFullResId;

        private WeekDay(int calendarId, int labelResId, int labelFullResId) {
            this.calendarId = calendarId;
            this.labelResId = labelResId;
            this.labelFullResId = labelFullResId;
        }

        public static int size() {
            return values().length;
        }

        public static int size(final boolean weekendIgnore) {
            if (weekendIgnore) return values().length - 2;
            return values().length;
        }

        public static String getLabel(final Context context, final int weekdayInput) {
            for (WeekDay weekDay : values()) {
                if (weekDay.calendarId == weekdayInput) {
                    return context.getString(weekDay.labelResId);
                }
            }
            return null;
        }

        public static int getLabel(final int weekdayInput) {
            for (WeekDay weekDay : values()) {
                if (weekDay.calendarId == weekdayInput) {
                    return weekDay.labelResId;
                }
            }
            return 0;
        }

        public static String getFullLabel(final Context context, final int weekdayInput) {
            for (WeekDay weekDay : values()) {
                if (weekDay.calendarId == weekdayInput) {
                    return context.getString(weekDay.labelFullResId);
                }
            }
            return null;
        }

        public static int getFullLabel(final int weekdayInput) {
            for (WeekDay weekDay : values()) {
                if (weekDay.calendarId == weekdayInput) {
                    return weekDay.labelFullResId;
                }
            }
            return 0;
        }

        public static int[][] getWeekDayCountArray() {
            WeekDay[] elements = values();
            int[][] weekDayCountArray = new int[elements.length][2];
            int i = 0;
            for (WeekDay weekday : elements) {
                weekDayCountArray[i++] = new int[] {
                        weekday.calendarId, 0
                };
            }
            return weekDayCountArray;
        }

        public static void countIncrease(final int[][] weekDayCountArray,
                final int calendarWeekDay) {
            for (int[] weekDayCountInfo : weekDayCountArray) {
                if (weekDayCountInfo[0] == calendarWeekDay) {
                    weekDayCountInfo[1] = weekDayCountInfo[1] + 1;
                }
            }
        }

        public static int getWeekDayCount(final int[][] weekDayCountArray,
                final int calendarWeekDay) {
            for (int[] weekDayCountInfo : weekDayCountArray) {
                if (weekDayCountInfo[0] == calendarWeekDay) {
                    return weekDayCountInfo[1];
                }
            }
            return 0;
        }

        public static WeekDay getWeekDayByCalendarId(final int weekDayId) {
            for (WeekDay weekDay : values()) {
                if (weekDay.calendarId == weekDayId) return weekDay;
            }
            return null;
        }

        public static WeekDay getWeekDayByOrdinal(final int ordinal) {
            for (WeekDay weekDay : values()) {
                if (weekDay.ordinal() == ordinal) return weekDay;
            }
            return null;
        }

        public static WeekDay getNextWeekDay(final int weekdayId, final boolean weekendIgnore) {
            WeekDay weekDay = getWeekDayByCalendarId(weekdayId);
            if (weekDay == null) return null;
            final int weekDayCount = WeekDay.size(weekendIgnore);
            int nextWeekdayId = (weekDay.ordinal() + 1) % weekDayCount;
            return getWeekDayByOrdinal(nextWeekdayId);
        }

        public static WeekDay getPrevWeekDay(final int weekdayId, final boolean weekendIgnore) {
            WeekDay weekDay = getWeekDayByCalendarId(weekdayId);
            if (weekDay == null) return null;
            final int weekDayCount = WeekDay.size(weekendIgnore);
            int prevWeekdayId = (weekDay.ordinal() - 1 + weekDayCount) % weekDayCount;
            return getWeekDayByOrdinal(prevWeekdayId);
        }
    }

    public static final int[] MONTH_ID_ARRAY = {
            R.string.january,
            R.string.february,
            R.string.march,
            R.string.april,
            R.string.may,
            R.string.june,
            R.string.july,
            R.string.august,
            R.string.september,
            R.string.october,
            R.string.november,
            R.string.december
    };

    public static final int[] LUNAR_MONTH_ID_ARRAY = {
            R.string.lunar_january,
            R.string.lunar_february,
            R.string.lunar_march,
            R.string.lunar_april,
            R.string.lunar_may,
            R.string.lunar_june,
            R.string.lunar_july,
            R.string.lunar_august,
            R.string.lunar_september,
            R.string.lunar_october,
            R.string.lunar_november,
            R.string.lunar_december
    };

    public static final int[] MONTH_DAYS = {
            31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };

    public static final int[] LUNAR_ID_ARRAY = { 
            R.string.lunar_day1,  R.string.lunar_day2,  R.string.lunar_day3,
            R.string.lunar_day4,  R.string.lunar_day5,  R.string.lunar_day6,
            R.string.lunar_day7,  R.string.lunar_day8,  R.string.lunar_day9, 
            R.string.lunar_day10, R.string.lunar_day11, R.string.lunar_day12,
            R.string.lunar_day13, R.string.lunar_day14, R.string.lunar_day15,
            R.string.lunar_day16, R.string.lunar_day17, R.string.lunar_day18,
            R.string.lunar_day19, R.string.lunar_day20, R.string.lunar_day21,
            R.string.lunar_day22, R.string.lunar_day23, R.string.lunar_day24, 
            R.string.lunar_day25, R.string.lunar_day26, R.string.lunar_day27,
            R.string.lunar_day28, R.string.lunar_day29, R.string.lunar_day30,
    };

    // 阴阳
    public static enum YinYang {
        Yang(R.string._yang),
        Yin(R.string._yin);

        public final int labelResId;

        private YinYang(int labelResId) {
            this.labelResId = labelResId;
        }

        public String getLabel(final Context context) {
            return context.getString(labelResId);
        }
    }

    // 五方.
    public static enum Direction {
        East(R.string.dir_east),
        West(R.string.dir_west),
        South(R.string.dir_south),
        North(R.string.dir_north),
        Center(R.string.dir_center);

        public final int labelResId;

        private Direction(final int labelResId) {
            this.labelResId = labelResId;
        }

        public String getLabel(final Context context) {
            return context.getString(labelResId);
        }
    }

    // 四时.
    public static enum Season {
        Spring(R.string.season_spring),
        Summer(R.string.season_summer),
        Autumn(R.string.season_autumn),
        Winter(R.string.season_winter);

        public final int labelResId;

        private Season(final int labelResId) {
            this.labelResId = labelResId;
        }

        public String getLabel(final Context context) {
            return context.getString(labelResId);
        }
    }

    // 五音
    public static enum Sound {
        Gong(R.string.sound_gong),
        Shang(R.string.sound_shang),
        Jiao(R.string.sound_jiao),
        Zhi(R.string.sound_zhi),
        Yu(R.string.sound_yu);

        public final int labelResId;

        private Sound(int labelResId) {
            this.labelResId = labelResId;
        }

        public String getLabel(final Context context) {
            return context.getString(labelResId);
        }
    }

    // 五行
    public static enum FiveElement {
        Gold(R.string.element_gold, Direction.West, Season.Autumn, Sound.Shang),
        Wood(R.string.element_wood, Direction.East, Season.Spring, Sound.Jiao),
        Water(R.string.element_water, Direction.North, Season.Winter, Sound.Yu),
        Fire(R.string.element_fire, Direction.South, Season.Summer, Sound.Zhi),
        Earth(R.string.element_earth, Direction.Center, Season.Summer, Sound.Gong);

        public final int labelResId;
        public final Direction direction;
        public final Season season;
        public final Sound sound;

        private FiveElement(final int labelResId, Direction direction, Season season, Sound sound) {
            this.labelResId = labelResId;
            this.direction = direction;
            this.season = season;
            this.sound = sound;
        }

        public String getLabel(final Context context) {
            return context.getString(labelResId);
        }
    }

    // 天干.
    public static enum Stem {
        Jia(R.string.stem_1, FiveElement.Wood, YinYang.Yang), // 甲
        Yi(R.string.stem_2, FiveElement.Wood, YinYang.Yin),  // 乙
        Bing(R.string.stem_3, FiveElement.Fire, YinYang.Yang), // 丙
        Ding(R.string.stem_4, FiveElement.Fire, YinYang.Yin), // 丁
        Wu(R.string.stem_5, FiveElement.Earth, YinYang.Yang), // 戊
        Ji(R.string.stem_6, FiveElement.Earth, YinYang.Yin), // 己
        Geng(R.string.stem_7, FiveElement.Gold, YinYang.Yang), // 庚
        Xin(R.string.stem_8, FiveElement.Gold, YinYang.Yin), // 辛
        Ren(R.string.stem_9, FiveElement.Water, YinYang.Yang), // 壬
        Gui(R.string.stem_0, FiveElement.Water, YinYang.Yin); // 癸

        public final int labelResId;
        public final FiveElement element; // 五行
        public final YinYang yinYang; // 阴阳

        private Stem(final int labelResId, FiveElement element, YinYang yinYang) {
            this.labelResId = labelResId;
            this.element = element;
            this.yinYang = yinYang;
        }

        public String getLabel(final Context context) {
            return context.getString(labelResId);
        }

        public static Stem get(final int ordinal) {
            for (Stem stem : values()) {
                if (stem.ordinal() == ordinal) return stem;
            }
            return null;
        }
    }

    // 地支.
    public static enum Branch {
        Zi(R.string.branch_01, R.string.animal_rat, FiveElement.Water, YinYang.Yang, 23, 1), // 子鼠
        Chou(R.string.branch_02, R.string.animal_ox, FiveElement.Earth, YinYang.Yin, 1, 3), // 丑牛
        Yin(R.string.branch_03, R.string.animal_tiger, FiveElement.Wood, YinYang.Yang, 3, 5), // 寅虎
        Mao(R.string.branch_04, R.string.animal_rabbit, FiveElement.Wood, YinYang.Yin, 5, 7), // 卯兔
        Chen(R.string.branch_05, R.string.animal_dragon, FiveElement.Earth, YinYang.Yang, 7, 9), // 辰龙
        Si(R.string.branch_06, R.string.animal_snake, FiveElement.Fire, YinYang.Yin, 9, 11), // 巳蛇
        Wu(R.string.branch_07, R.string.animal_horse, FiveElement.Fire, YinYang.Yang, 11, 13), // 午马
        Wei(R.string.branch_08, R.string.animal_sheep, FiveElement.Earth, YinYang.Yin, 13, 15), // 未羊
        Shen(R.string.branch_09, R.string.animal_monkey, FiveElement.Gold, YinYang.Yang, 15, 17), // 申猴
        You(R.string.branch_10, R.string.animal_rooster, FiveElement.Gold, YinYang.Yin, 17, 19), // 酉鸡
        Xu(R.string.branch_11, R.string.animal_dog, FiveElement.Earth, YinYang.Yang, 19, 21), // 戌狗
        Hai(R.string.branch_12, R.string.animal_pig, FiveElement.Water, YinYang.Yin, 21, 23); // 亥猪

        public final int branchResId;
        public final int animalResId;
        public final FiveElement element; // 五行
        public final YinYang yinYang; // 阴阳
        public final int timeStart;
        public final int timeEnd;

        private Branch(int branchResId, int animalResId, FiveElement element, YinYang yinYang,
                int timeStart, int timeEnd) {
            this.branchResId = branchResId;
            this.animalResId = animalResId;
            this.element = element;
            this.yinYang = yinYang;
            this.timeStart = timeStart;
            this.timeEnd = timeEnd;
        }

        public String getLabel(final Context context) {
            return context.getString(branchResId);
        }

        public static Branch get(final int ordinal) {
            for (Branch branch : values()) {
                if (branch.ordinal() == ordinal) return branch;
            }
            return null;
        }
    }

    public static String getBranchLabel(final Context context, final int branchOrdinal) {
        Branch branch = Branch.get(branchOrdinal);
        if (branch == null) return null;
        return context.getString(branch.branchResId);
    }

    public static Branch getTimeBranch(int hour_of_day) {
        if (hour_of_day == 0) hour_of_day = 24;
        int timeStart, timeEnd;
        for (Branch branch : Branch.values()) {
            timeStart = branch.timeStart;
            timeEnd = branch.timeEnd;
            if (timeStart > timeEnd) {
                timeEnd = 24 + timeEnd;
            }
            if (hour_of_day >= timeStart && hour_of_day < timeEnd) {
                return branch;
            }
        }
        return null;
    }

    public static int getAnimalCount() {
        return Branch.values().length;
    }

    public static int getBranchCount() {
        return Branch.values().length;
    }

    private static final int[][] LUNAR_MONTH1_IMPORTANT_DAYS = {
            // 日期,      含义,       日期含义(全),         日期含义(短)
            {  1, INVALID_DAY_ID, R.string.lunarday0101},
            { 15, INVALID_DAY_ID, R.string.lunarday0115, R.string.lunarday0115_1 }
    };
    private static final int[][] LUNAR_MONTH2_IMPORTANT_DAYS = {
            { 2, INVALID_DAY_ID, R.string.lunarday0202, R.string.lunarday0202_1 }
    };
    private static final int[][] LUNAR_MONTH4_IMPORTANT_DAYS = {
            { 8, INVALID_DAY_ID, R.string.lunarday0408, R.string.lunarday0408_1 }
    };
    private static final int[][] LUNAR_MONTH5_IMPORTANT_DAYS = {
            { 5, INVALID_DAY_ID, R.string.lunarday0505, R.string.lunarday0505_1 }
    };
    private static final int[][] LUNAR_MONTH6_IMPORTANT_DAYS = {
            { 24, INVALID_DAY_ID, R.string.lunarday0624, R.string.lunarday0624_1 }
    };
    private static final int[][] LUNAR_MONTH7_IMPORTANT_DAYS = {
            {  7, INVALID_DAY_ID, R.string.lunarday0707 },
            { 15, INVALID_DAY_ID, R.string.lunarday0715, R.string.lunarday0715_1 }
    };
    private static final int[][] LUNAR_MONTH8_IMPORTANT_DAYS = {
            { 15, INVALID_DAY_ID, R.string.lunarday0815, R.string.lunarday0815_1 }
    };
    private static final int[][] LUNAR_MONTH9_IMPORTANT_DAYS = {
            { 9, INVALID_DAY_ID, R.string.lunarday0909, R.string.lunarday0909_1 }
    };
    private static final int[][] LUNAR_MONTH10_IMPORTANT_DAYS = {
            { 1,  INVALID_DAY_ID, R.string.lunarday1001, R.string.lunarday1001_1 },
            { 15, INVALID_DAY_ID, R.string.lunarday1015, R.string.lunarday1015_1 },
    };
    private static final int[][] LUNAR_MONTH12_IMPORTANT_DAYS = {
            {  8, INVALID_DAY_ID, R.string.lunarday1208 },
            { 23, INVALID_DAY_ID, R.string.lunarday1223 },
    };
    public static final int[][][] LUNAR_IMPORTANT_DAYS = {
            LUNAR_MONTH1_IMPORTANT_DAYS,
            LUNAR_MONTH2_IMPORTANT_DAYS,
            {}/* LUNAR_MONTH3_IMPORTANT_DAYS */,
            LUNAR_MONTH4_IMPORTANT_DAYS,
            LUNAR_MONTH5_IMPORTANT_DAYS,
            LUNAR_MONTH6_IMPORTANT_DAYS,
            LUNAR_MONTH7_IMPORTANT_DAYS,
            LUNAR_MONTH8_IMPORTANT_DAYS,
            LUNAR_MONTH9_IMPORTANT_DAYS,
            LUNAR_MONTH10_IMPORTANT_DAYS,
            {}/* LUNAR_MONTH11_IMPORTANT_DAYS */,
            LUNAR_MONTH12_IMPORTANT_DAYS
    };

    private static final int[][] MONTH1_SPECIAL_DAYS = {
           // 日期,  日期含义
            { 1, R.string.day0101 }
    };
    private static final int[][] MONTH2_SPECIAL_DAYS = {
            {  2, INVALID_DAY_ID, R.string.day0202, R.string.day0202_1 },
            { 10, INVALID_DAY_ID, R.string.day0210, R.string.day0210_1 },
            { 14, INVALID_DAY_ID, R.string.day0214 }
    };
    private static final int[][] MONTH3_SPECIAL_DAYS = {
            {  1, INVALID_DAY_ID, R.string.day0301, R.string.day0301_1 },
            // 日期,    含义,        日期含义(全),        日期含义(短)
            {  3, INVALID_DAY_ID, R.string.day0303, R.string.day0303_1 },
            {  5, INVALID_DAY_ID, R.string.day0305, R.string.day0305_1 },
            {  8, INVALID_DAY_ID, R.string.day0308, R.string.day0308_1 },
            {  9, INVALID_DAY_ID, R.string.day0309, R.string.day0309_1 },
            { 12, INVALID_DAY_ID, R.string.day0312, R.string.day0312_1 },
            { 14, INVALID_DAY_ID, R.string.day0314, R.string.day0314_1 },
            { 15, INVALID_DAY_ID, R.string.day0315, R.string.day0315_1 },
            { 17, INVALID_DAY_ID, R.string.day0317, R.string.day0317_1 },
            { 21, INVALID_DAY_ID, R.string.day0321, R.string.day0321_1 },
            { 22, INVALID_DAY_ID, R.string.day0322, R.string.day0322_1 },
            { 23, INVALID_DAY_ID, R.string.day0323, R.string.day0323_1 },
            { 24, INVALID_DAY_ID, R.string.day0324, R.string.day0324_1 },
            { 25, INVALID_DAY_ID, R.string.day0325, R.string.day0325_1 },
            { 30, INVALID_DAY_ID, R.string.day0330, R.string.day0330_1 } };
    private static final int[][] MONTH4_SPECIAL_DAYS = {
            {  1, INVALID_DAY_ID, R.string.day0401, R.string.day0401_1 },
            {  7, INVALID_DAY_ID, R.string.day0407, R.string.day0407_1 },
            { 15, INVALID_DAY_ID, R.string.day0415, R.string.day0415_1 },
            { 22, INVALID_DAY_ID, R.string.day0422, R.string.day0422_1 },
            { 23, INVALID_DAY_ID, R.string.day0423, R.string.day0423_1 },
            { 24, INVALID_DAY_ID, R.string.day0424, R.string.day0424_1 },
            { 26, INVALID_DAY_ID, R.string.day0426, R.string.day0426_1 } };
    private static final int[][] MONTH5_SPECIAL_DAYS = {
            {  1, R.string.labor_day },
            {  3, INVALID_DAY_ID, R.string.day0503, R.string.day0503_1 },
            {  4, R.string.youth_day },
            {  5, INVALID_DAY_ID, R.string.day0505, R.string.day0505_1 },
            {  8, INVALID_DAY_ID, R.string.day0508, R.string.day0508_1 },
            {  9, INVALID_DAY_ID, R.string.day0509, R.string.day0509_1 },
            { 12, INVALID_DAY_ID, R.string.day0512, R.string.day0512_1 },
            { 15, INVALID_DAY_ID, R.string.day0515, R.string.day0515_1 },
            { 17, INVALID_DAY_ID, R.string.day0517, R.string.day0517_1 },
            { 18, INVALID_DAY_ID, R.string.day0518, R.string.day0518_1 },
            { 20, INVALID_DAY_ID, R.string.day0520, R.string.day0520_1 },
            { 23, INVALID_DAY_ID, R.string.day0523, R.string.day0523_1 },
            { 31, INVALID_DAY_ID, R.string.day0531, R.string.day0531_1 } };
    private static final int[][] MONTH6_SPECIAL_DAYS = {
            {  1, INVALID_DAY_ID, R.string.day0601, R.string.day0601_1 },
            {  5, INVALID_DAY_ID, R.string.day0605, R.string.day0605_1 },
            {  6, INVALID_DAY_ID, R.string.day0606, R.string.day0606_1 },
            { 17, INVALID_DAY_ID, R.string.day0617, R.string.day0617_1 },
            { 23, INVALID_DAY_ID, R.string.day0623, R.string.day0623_1 },
            { 25, INVALID_DAY_ID, R.string.day0625, R.string.day0625_1 },
            { 26, INVALID_DAY_ID, R.string.day0626, R.string.day0626_1 } };
    private static final int[][] MONTH7_SPECIAL_DAYS = {
            {  1, INVALID_DAY_ID, R.string.day0701, R.string.day0701_1 },
            {  2, INVALID_DAY_ID, R.string.day0702, R.string.day0702_1 },
            {  7, INVALID_DAY_ID, R.string.day0707, R.string.day0707_1 },
            { 11, INVALID_DAY_ID, R.string.day0711, R.string.day0711_1 },
            { 27, INVALID_DAY_ID, R.string.day0727, R.string.day0727_1 },
            { 30, INVALID_DAY_ID, R.string.day0730, R.string.day0730_1 } };
    private static final int[][] MONTH8_SPECIAL_DAYS = {
            {  1, R.string.pla_birthday },
            {  8, INVALID_DAY_ID, R.string.day0808, R.string.day0808_1 },
            { 12, INVALID_DAY_ID, R.string.day0812, R.string.day0812_1 },
            { 15, INVALID_DAY_ID, R.string.day0815, R.string.day0815_1 },
            { 19, INVALID_DAY_ID, R.string.day0819, R.string.day0819_1 }, };
    private static final int[][] MONTH9_SPECIAL_DAYS = {
            {  1, INVALID_DAY_ID, R.string.day0901, R.string.day0901_1 },
            {  2, INVALID_DAY_ID, R.string.day0902, R.string.day0902_1 },
            {  8, INVALID_DAY_ID, R.string.day0908, R.string.day0908_1 },
            {  9, INVALID_DAY_ID, R.string.day0909, R.string.day0909_1 },
            { 10, INVALID_DAY_ID, R.string.day0910, R.string.day0910_1 },
            { 14, INVALID_DAY_ID, R.string.day0914, R.string.day0914_1 },
            { 16, INVALID_DAY_ID, R.string.day0916, R.string.day0916_1 },
            { 18, INVALID_DAY_ID, R.string.day0918, R.string.day0918_1 },
            { 20, INVALID_DAY_ID, R.string.day0920, R.string.day0920_1 },
            { 21, INVALID_DAY_ID, R.string.day0921, R.string.day0921_1 },
            { 27, INVALID_DAY_ID, R.string.day0927, R.string.day0927_1 },
            { 28, INVALID_DAY_ID, R.string.day0928, R.string.day0928_1 }
    };
    private static final int[][] MONTH10_SPECIAL_DAYS = {
            {  1, R.string.nation_birthday, R.string.day1001, R.string.day1001_1 },
            {  4, R.string.animal_day, R.string.day1004, R.string.day1004_1 },
            {  5, INVALID_DAY_ID, R.string.day1005, R.string.day1005_1 },
            {  6, R.string.elder_day },
            {  8, INVALID_DAY_ID, R.string.day1008, R.string.day1008_1 },
            {  9, INVALID_DAY_ID, R.string.day1009, R.string.day1009_1 },
            { 10, INVALID_DAY_ID, R.string.day1010, R.string.day1010_1 },
            { 13, INVALID_DAY_ID, R.string.day1013, R.string.day1013_1 },
            { 14, INVALID_DAY_ID, R.string.day1014, R.string.day1014_1 },
            { 15, INVALID_DAY_ID, R.string.day1015, R.string.day1015_1 },
            { 16, INVALID_DAY_ID, R.string.day1016, R.string.day1016_1 },
            { 17, INVALID_DAY_ID, R.string.day1017, R.string.day1017_1 },
            { 22, INVALID_DAY_ID, R.string.day1022, R.string.day1022_1 },
            { 24, INVALID_DAY_ID, R.string.day1024, R.string.day1024_1 },
            { 28, INVALID_DAY_ID, R.string.day1028, R.string.day1028_1 },
            { 29, INVALID_DAY_ID, R.string.day1029, R.string.day1029_1 },
            { 31, R.string.economy_day, R.string.day1031, R.string.day1031_1 }
    };
    private static final int[][] MONTH11_SPECIAL_DAYS = {
            {  7, INVALID_DAY_ID, R.string.day1107, R.string.day1107_1 },
            {  8, INVALID_DAY_ID, R.string.day1108, R.string.day1108_1 },
            {  9, INVALID_DAY_ID, R.string.day1109, R.string.day1109_1 },
            { 10, INVALID_DAY_ID, R.string.day1110, R.string.day1110_1 },
            { 11, INVALID_DAY_ID, R.string.day1111, R.string.day1111_1 },
            { 12, INVALID_DAY_ID, R.string.day1112, R.string.day1112_1 },
            { 14, INVALID_DAY_ID, R.string.day1114, R.string.day1114_1 },
            { 17, INVALID_DAY_ID, R.string.day1117, R.string.day1117_1 },
            { 20, INVALID_DAY_ID, R.string.day1120, R.string.day1120_1 },
            { 21, INVALID_DAY_ID, R.string.day1121, R.string.day1121_1 },
            { 22, INVALID_DAY_ID, R.string.day1122, R.string.day1122_1 },
            { 25, INVALID_DAY_ID, R.string.day1125, R.string.day1125_1 },
            { 29, INVALID_DAY_ID, R.string.day1129, R.string.day1129_1 } };
    private static final int[][] MONTH12_SPECIAL_DAYS = {
            {  1, INVALID_DAY_ID, R.string.day1201, R.string.day1201_1 },
            {  3, INVALID_DAY_ID, R.string.day1203, R.string.day1203_1 },
            {  4, INVALID_DAY_ID, R.string.day1204, R.string.day1204_1 },
            {  5, INVALID_DAY_ID, R.string.day1205, R.string.day1205_1 },
            {  7, INVALID_DAY_ID, R.string.day1207, R.string.day1207_1 },
            {  8, INVALID_DAY_ID, R.string.day1208, R.string.day1208_1 },
            {  9, INVALID_DAY_ID, R.string.day1209, R.string.day1209_1 },
            { 10, INVALID_DAY_ID, R.string.day1210, R.string.day1210_1 },
            { 11, INVALID_DAY_ID, R.string.day1211, R.string.day1211_1 },
            { 12, INVALID_DAY_ID, R.string.day1212, R.string.day1212_1 },
            { 13, INVALID_DAY_ID, R.string.day1213, R.string.day1213_1 },
            { 20, INVALID_DAY_ID, R.string.day1220, R.string.day1220_1 },
            { 21, INVALID_DAY_ID, R.string.day1221, R.string.day1221_1 },
            { 24, INVALID_DAY_ID, R.string.day1224, R.string.day1224_1 },
            { 25, R.string.christmas},
            { 26, INVALID_DAY_ID, R.string.day1226, R.string.day1226_1 },
            { 29, INVALID_DAY_ID, R.string.day1229, R.string.day1229_1 }
    };

    public static final int[][][] PUBLIC_IMPORTANT_DAYS = {
            MONTH1_SPECIAL_DAYS,  MONTH2_SPECIAL_DAYS,  MONTH3_SPECIAL_DAYS,
            MONTH4_SPECIAL_DAYS,  MONTH5_SPECIAL_DAYS,  MONTH6_SPECIAL_DAYS,
            MONTH7_SPECIAL_DAYS,  MONTH8_SPECIAL_DAYS,  MONTH9_SPECIAL_DAYS,
            MONTH10_SPECIAL_DAYS, MONTH11_SPECIAL_DAYS, MONTH12_SPECIAL_DAYS,
    };

    public static final int[][] SPECIAL_DAY_IN_MONTH_WEEK = {
         /* month No     weekday            label */
           /* 月  第几个  星期几                              日期含义 */
            { 1,  5, Calendar.SUNDAY,    R.string.week_1_5_0,  R.string.week_1_5_0_1  },
            { 3,  4, Calendar.MONDAY,    R.string.week_3_4_1,  R.string.week_3_4_1_1  },
            { 5,  2, Calendar.SUNDAY,    R.string.week_5_2_0,  R.string.week_5_2_0    },
            { 5,  3, Calendar.SUNDAY,    R.string.week_5_3_0,  R.string.week_5_3_0_1  },
            { 6,  3, Calendar.SUNDAY,    R.string.week_6_3_0,  R.string.week_6_3_0  },
            { 9,  3, Calendar.TUESDAY,   R.string.week_9_3_2,  R.string.week_9_3_2_1  },
            { 9,  3, Calendar.SATURDAY,  R.string.week_9_3_6,  R.string.week_9_3_6_1  },
            { 9,  4, Calendar.SUNDAY,    R.string.week_9_4_0,  R.string.week_9_4_0_1  },
            { 10, 1, Calendar.MONDAY,    R.string.week_10_1_1, R.string.week_10_1_1_1 },
            { 10, 2, Calendar.MONDAY,    R.string.week_10_2_1, R.string.week_10_2_1_1 },
            { 10, 2, Calendar.WEDNESDAY, R.string.week_10_2_3, R.string.week_10_2_3_1 },
            { 10, 2, Calendar.THURSDAY,  R.string.week_10_2_4, R.string.week_10_2_4_1 },
            { 11, 4, Calendar.THURSDAY,  R.string.week_11_4_4, R.string.week_11_4_4_1 }
    };

    // 星座信息
    public enum Constellation {
        // 水瓶座, 紫水晶, 水仙花, 1/20 - 2/18
        Aquarius(R.string.constellation_aquarius, R.string.stone_aquarius, R.string.flower_aquarius,1, 20, 2, 18),
        // 双鱼座, 月长石 血石, 蔷薇花, 2/19 - 3/20
        Pisces(R.string.constellation_pisces, R.string.stone_pisces, R.string.flower_pisces, 2, 19, 3, 20),
        // 白羊座, 钻石, 彼岸花, 3/21-4/19
        Aries(R.string.constellation_aries, R.string.stone_aries, R.string.flower_aries, 3, 21, 4, 19),
        // 金牛座, 蓝宝石, 茉莉花, 4/20 - 5/20
        Taurus(R.string.constellation_taurus, R.string.stone_taurus, R.string.flower_taurus, 4, 20, 5, 20),
        // 双子座, 玛瑙, 曼陀罗, 5/21 - 6/21
        Gemini(R.string.constellation_gemini, R.string.stone_gemini, R.string.flower_gemini, 5, 21, 6, 21),
        // 巨蟹座, 珍珠, 山茶花, 6/22 - 7/22
        Cancer(R.string.constellation_cancer, R.string.stone_cancer, R.string.flower_cancer, 6, 22, 7, 22),
        // 狮子座, 红宝石, 风信子, 7/23 - 8/22
        Leo(R.string.constellation_leo, R.string.stone_leo, R.string.flower_leo, 7, 23, 8, 22),
        // 处女座, 红条纹玛瑙, 向日葵, 8/23 - 9/22
        Virgo(R.string.constellation_virgo, R.string.stone_virgo, R.string.flower_virgo, 8, 23, 9, 22),
        // 天秤座, 蓝宝石, 郁金香, 9/23 - 10/23
        Libra(R.string.constellation_libra, R.string.stone_libra, R.string.flower_libra, 9, 23, 10, 23),
        // 天蝎座, 猫眼石, 紫薇花, 10/24 - 11/22
        Scorpio(R.string.constellation_scorpio, R.string.stone_scorpio, R.string.flower_scorpio, 10, 24, 11, 22),
        // 射手座, 黄宝石, 罂粟花, 11/23-12/21
        Sagittarius(R.string.constellation_sagittarius, R.string.stone_sagittarius, R.string.flower_sagittarius, 11, 23, 12, 21),
        // 摩羯座, 土耳其玉, 樱花, 12/22- 1/19
        Capricorn(R.string.constellation_capricorn, R.string.stone_capricorn, R.string.flower_capricorn, 12, 22, 1, 19);

        public final int labelResId;
        public final int stoneResId;
        public final int flowerResId;
        public final int startMonth, startDay;
        public final int endMonth, endDay;

        private Constellation(final int labelResId, final int stoneResId, final int flowerResId,
                final int startMonth, final int startDay, final int endMonth, final int endDay) {
            this.labelResId = labelResId;
            this.stoneResId = stoneResId;
            this.flowerResId = flowerResId;
            this.startMonth = startMonth;
            this.startDay = startDay;
            this.endMonth = endMonth;
            this.endDay = endDay;
        }

        private static final String FORMAT_INFO = "%s(%s, %s), %02d%02d ~ %02d%02d";

        public String info(final Context context) {
            return String.format(FORMAT_INFO, context.getString(labelResId),
                    context.getString(stoneResId), context.getString(flowerResId),
                    startMonth, startDay, endMonth, endDay);

        }

        public static String constellationInfo(final Context context) {
            StringBuilder sb = new StringBuilder();
            for (Constellation constellation : values()) {
                if (sb.length() > 0) sb.append('\n');
                sb.append(constellation.info(context));
            }
            return sb.toString();
        }

        public static Constellation getConstellation(final Day day) {
            final int m = day.month + 1;
            final int d = day.day;

            int beginMonth, beginDay;
            int endMonth, endDay;
            for (Constellation constellation : values()) {
                beginMonth = constellation.startMonth;
                beginDay = constellation.startDay;
                endMonth = constellation.endMonth;
                endDay = constellation.endDay;
                if (m == beginMonth && d >= beginDay) {
                    return constellation;
                }
                if (m == endMonth && d <= endDay) {
                    return constellation;
                }
            }
            return null;
        }
    }

    public static final int[] SETTING_MENU = {
            R.string.menuitem_setting_colors, R.string.setting_color_week,
            R.string.setting_color_day, R.string.setting_color_lunar
    };

    public static final int DEFAULT_HEADER_BG_COLOR = 0;
    public static final int DEFAULT_INFO_TEXT_BG_COLOR = 0;
    public static final int DEFAULT_INFO_TEXT_COLOR = 0XFFFFFFFF;
    public static final int DEFAULT_LUNAR_TEXT_BG_COLOR = 0;
    public static final int DEFAULT_LUNAR_TEXT_COLOR = 0XFFABCDEF;
    public static final int DEFAULT_SPECIAL_DAY_TEXT_BG_COLOR = 0;
    public static final int DEFAULT_SPECIAL_DAY_TEXT_COLOR = 0XFFCDABFE;

    public static final int DEFAULT_WEEK_PROMPT_BG_COLOR = 0XFF0000FF;
    public static final int DEFAULT_WEEK_PROMPT_COLOR = 0XFFFFFFFF;

    public static final int DEFAULT_DAY_GRIDCELL_BG_COLOR = 0XFF0F0F0F;
    public static final int DEFAULT_DAY_TEXT_COLOR = 0XFFFFFFFF;
    public static final int DEFAULT_WEEK_NO_TEXT_COLOR = 0XFFABCDEF;
    public static final int DEFAULT_COLOR_DAY_UNAVAILABLE = Color.GRAY;
    public static final int DEFAULT_COLOR_DAY_TODAY = Color.YELLOW;
    public static final int DEFAULT_COLOR_LUNAR_DAY = 0XFF008F00;
    public static final int DEFAULT_COLOR_LUNAR_TERM = 0XFFFF8FF8;
    public static final int DEFAULT_COLOR_SPECIAL_DAY = Color.BLUE;

    public static final String FIELD_INFO_TEXT_COLOR = "info_text_color";
    public static final String FIELD_LUNAR_TEXT_COLOR = "lunar_text_color";
    public static final String FIELD_SPECIAL_DAY_TEXT_COLOR = "special_day_text_color";

    public static final String STR_1_9 = "一九";
    public static final String STR_2_9 = "二九";
    public static final String STR_3_9 = "三九";
    public static final String STR_4_9 = "四九";
    public static final String STR_5_9 = "五九";
    public static final String STR_6_9 = "六九";
    public static final String STR_7_9 = "七九";
    public static final String STR_8_9 = "八九";
    public static final String STR_9_9 = "九九";

    public static final String _LEAP = "闰";
    public static final String FORMAT_LEAP_LUNAR_MONTH = _LEAP + "%s";

    public static class TextViewInfo {
        public final int textColor;
        public final int backgroundColor;
        public final String text;

        public TextViewInfo(String text, int textColor, int backgroundColor) {
            this.text = text;
            this.textColor = textColor;
            this.backgroundColor = backgroundColor;
        }

        public TextViewInfo(int textColor) {
            this(null, textColor, 0);
        }

        public TextViewInfo(String text) {
            this(text, 0, 0);
        }

        public TextViewInfo(String text, int textColor) {
            this(text, textColor, 0);
        }
    }

    public static String sameLengthString(String dayShortInfo) {
        int inputLen = dayShortInfo.length();
        StringBuilder sb = new StringBuilder(dayShortInfo);
        int normalLen = SPACE_3_FULL.length();
        // Add space in left and right for a full SPACE char.
        while (normalLen - inputLen > 0) {
            sb.append(' ');
            sb.insert(0, ' ');
            inputLen++;
        }
        return sb.toString();
    }

    // 韵目代日 (概念请百度)
    private static class YunDay {
        public final int dayIndex;
        public final String[] words;

        public YunDay(final int dayIndex, final String... words) {
            this.dayIndex = dayIndex;
            this.words = words;
        }
    }

    private static final YunDay[] sYunDays = {
            new YunDay(1, "东", "先", "董", "送", "屋"),
            new YunDay(2, "冬", "萧", "肿", "宋", "沃"),
            new YunDay(3, "江", "肴", "讲", "绛", "觉"),
            new YunDay(4, "支", "豪", "纸", "寘", "质"),
            new YunDay(5, "微", "歌", "尾", "未", "物"),
            new YunDay(6, "鱼", "麻", "语", "御", "月"),
            new YunDay(7, "虞", "阳", "麌", "遇", "曷"),
            new YunDay(8, "齐", "庚", "荠", "霁", "黠"),
            new YunDay(9, "佳", "青", "蟹", "泰", "屑"),
            new YunDay(10, "灰", "蒸", "贿", "卦", "药"),
            new YunDay(11, "真", "尤", "轸", "队", "陌"),
            new YunDay(12, "文", "侵", "吻", "震", "锡"),
            new YunDay(13, "元", "覃", "阮", "问", "职"),
            new YunDay(14, "寒", "盐", "旱", "愿", "缉"),
            new YunDay(15, "删", "咸", "潸", "翰", "合"),
            new YunDay(16, "铣", "谏", "叶"),
            new YunDay(17, "篠", "霰", "洽"),
            new YunDay(18, "巧", "啸"),
            new YunDay(19, "皓", "效"),
            new YunDay(20, "哿", "号"),
            new YunDay(21, "马", "箇"),
            new YunDay(22, "养", "祃"),
            new YunDay(23, "梗", "漾"),
            new YunDay(24, "迥", "敬"),
            new YunDay(25, "有", "径"),
            new YunDay(26, "寝", "宥"),
            new YunDay(27, "感", "沁"),
            new YunDay(28, "俭", "勘"),
            new YunDay(29, "艳", "豏"),
            new YunDay(30, "卅"),
            new YunDay(31, "世", "引")
    };

    private static final String FORMAT_YUN_DAY = "%s日";

    public static String getYunDay(final int dayIndex) {
        for (YunDay yunDay : sYunDays) {
            if (yunDay.dayIndex == dayIndex) {
                return String.format(FORMAT_YUN_DAY, yunDay.words[0]);
            }
        }
        return null;
    }

    public static int getStemCount() {
        return Stem.values().length;
    }

    public static String getStemLabel(final Context context, final int stemOrdinal) {
        Stem stem = Stem.get(stemOrdinal);
        if (stem == null) return null;
        return context.getString(stem.labelResId);
    }

    public static Stem getStem(final int stemOrdinal) {
        return Stem.get(stemOrdinal);
    }

    public static Branch getBranch(final int branchOrdinal) {
        return Branch.get(branchOrdinal);
    }

    public static void refreshWidget(final Context context) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        context.sendBroadcast(intent);
    }
}
