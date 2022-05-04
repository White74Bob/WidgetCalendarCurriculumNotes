package wb.widget.utils.calendar;

import android.graphics.Color;
import android.text.TextUtils;

import wb.widget.model.Day;

/**
 * 黄历data and knowledge
 * @author 28848745
 *
 */
public class AlmanacUtils {
    /* - 古有“太岁当头坐，无灾必有祸”。所以，太岁头上不能动土。
     * - 如马年生人不可选择子水日，虎年生人不可选择申猴日，
     * - 兔年生人不可选择酉鸡日，蛇年生人不可选择亥猪日，反之也不利；
     * - 至于龙狗牛羊则不忌，但命局中不要遇见刑伤或鬼杀，碰巧遇见者也不可以。
     * - 生日最重要，因为生日是夫妻宫，居家过日子全靠“家和万事兴”，
     * - “家”多指父母、兄弟、夫妻、子女等。搬迁新居就是要家庭温馨幸福，
     * - 所以，绝不可以刑伤冲破。
     * - 如家主男或女生日是子水日，不可选择卯日与午日和酉日，反之也不可以，
     * - 一冲一刑一破，易犯桃花煞，夫妻大忌无疑；
     * - 寅巳申亥日不可重，见则刑伤，见有三合六合要视其轻重而定；
     * - 辰戌丑未日多藏鬼煞带刑伤，也不可重见破开墓库鬼煞出入。 */

    public enum HuangDayType {
        HuangGoodDay("黄道吉日", "吉", Color.YELLOW),
        HuangNormalDay("平日", Color.GREEN),
        HeiBadDay("黑道凶日", "凶", Color.GRAY);

        public final String label;
        public final String shortLabel;
        public final int colorText;

        private HuangDayType(String label, int colorText) {
            this(label, null, colorText);
        }

        private HuangDayType(String label, String shortLabel, int colorText) {
            this.label = label;
            this.shortLabel = shortLabel;
            this.colorText = colorText;
        }
    }

    /* - 黄道、黑道的推算方法主要有两种，
     * - 一种是十二地支配上十二个天神。另一种是十二值。
     * - 十二地支配十二天神的情况是：
     * - 子日青龙 丑日明堂 寅日天刑 卯日朱雀
     * - 辰日金贵 巳日天德 午日白虎 未日玉堂
     * - 申日天牢 酉日玄武 戌日司命 亥日勾陈
     * - 凡是逢上青龙、明堂、金贵、天德、玉堂、司命六神的就是黄道日。 */

    public enum DayBranch {
        Zi("子", "青龙", HuangDayType.HuangGoodDay),
        Chou("丑", "明堂", HuangDayType.HuangGoodDay),
        Yin("寅", "天刑 "),
        Mao("卯", "朱雀"),
        Chen("辰", "金贵", HuangDayType.HuangGoodDay),
        Si("巳", "天德", HuangDayType.HuangGoodDay),
        Wu("午", "白虎"),
        Wei("未", "玉堂", HuangDayType.HuangGoodDay),
        Shen("申", "天牢"),
        You("酉", "玄武"),
        Xu("戌", "司命", HuangDayType.HuangGoodDay),
        Hai("亥", "勾陈");

        public final String branch;
        public final String label;
        public final HuangDayType dayType;

        private DayBranch(String branch, String label) {
            this(branch, label, null);
        }

        private DayBranch(String branch, String label, HuangDayType dayType) {
            this.branch = branch;
            this.label = label;
            this.dayType = dayType;
        }

        private static final String FORMAT_INFO_0 = "%s日%s";
        private static final String FORMAT_INFO_1 = "%s日%s(%s)";

        public String info() {
            if (dayType != null) {
                return String.format(FORMAT_INFO_1, branch, label, dayType.shortLabel);
            }
            return String.format(FORMAT_INFO_0, branch, label);
        }

        public static DayBranch getFromBranchIndex(final int branchIndex) {
            for (DayBranch dayBranch : values()) {
                if (dayBranch.ordinal() == branchIndex) return dayBranch;
            }
            return null;
        }
    }

    /* 通胜十二日建:建 除 满 平 定 执 破 危 成 收 开 闭
     * ---黄吉黑凶:黑 黄 黑 黑 黄 黄 黑 黄 黄 黑 黄 黑
     * 凡与除、定、执、危、成、开对应的日子为黄道吉日。
     * 凡与建、满、平、破、收、闭对应的日子为黑道凶日。
     * 吉日：红白二事皆宜的日子。
     * 次吉：吉日后，退而求其次的日子。
     * 凶日：诸事不宜，最好避之则吉，喜事更可免则免。
     * 平日：普通的日子。
     * ********************************
     * 建日：建基立业，从头开始，如苗初长，终获收成。
     * 除日：除旧迎新，沐浴洗礼，拜神祈福，安居乐业。
     * 满日：满载而归，圆圆满满，兴高采烈，得胜之时。
     * 平日：平平安安，平平常常，不平不忿，吉凶各半。
     * 定日：定如泰山，安定团结，事已成形，根基牢固。
     * 执日：执日多忧，顽固不化，吉凶各半，不可大用。
     * 破日：月建冲破，最喜动工，红白喜事，十用九凶。
     * 危日：危日惊险，做事小心，岌岌可危，凶煞占宫。
     * 成日：吉星高照，贵人接引，天皇地皇，金玉满堂。
     * 收日：煞集中宫，只宜回笼，收帐要债，此日最佳。
     * 开日：马到成功，旗开得胜，大吉大利，百无禁忌。
     * 闭日：闭门不出，闭塞不通，万物归仓，只宜静养。
     * *******************************
     * - 依据以下歌诀灵活变通：
     * 建满平收黑，除危定执黄；
     * 成开皆可用，闭破不吉祥。
     * 黑中平不爱，黄中危不强。
     * 建宜出行收嫁娶，定宜冠带满修仓。
     * 破除疗病执宜捕，危本安床开丈量。
     * *******************************
     * - 建日：万物生育、强健、健壮的日子。
     * -- 宜：赴任、祈福、求嗣、破土、安葬、修造、上樑、求財、
     * -----置业、入学、考试、结婚、动土、签约、交涉、出行。
     * -- 忌：动土、开仓、掘井、乘船、新船下水、新车下地、维修水电器具。
     * - 除日：扫除恶煞、去旧迎新的日子。
     * -- 宜：祭祀、祈福、婚姻、出行、入伙、搬迁、出货、动土、求医、交易。
     * -- 忌：结婚、赴任、远行、签约。
     * - 满日：丰收、美满、天帝宝库积满的日子。
     * -- 宜：嫁娶、祈福、移徙、开市、交易、求財、立契、祭祀、出行、牧养。
     * -- 忌：造葬、赴任、求医。
     * - 平日：平常、官人集合平分的日子。
     * -- 宜：修造、破土、安葬、牧养、开市、安床、动土、求嗣。
     * -- 忌：祈福、求嗣、赴任、嫁娶、开市、安葬。
     * - 定日：安定、平常、天帝众客定座的日子。
     * -- 宜：祭祀、祈福、嫁娶、造屋、装修、修路、开市、入学、上任、入伙。
     * -- 忌：诉讼、出行、交涉。
     * - 执日：破日之从神，曰小耗，天帝执行万物赐天福，较差的日子。
     * -- 宜：造屋、装修、嫁娶、收购、立契、祭祀。
     * -- 忌：开市、求財、出行、搬迁。
     * - 破日：日月相衝，曰大耗，斗柄相衝相向必破坏的日子，大事不宜。
     * -- 宜：破土、拆卸、求医。
     * -- 忌：嫁娶、签约、交涉、出行、搬迁。
     * - 危日：危机、危险，诸事不宜的日子。
     * -- 宜：祭祀、祈福、安床、拆卸、破土。
     * -- 忌：登山、乘船、出行、嫁娶、造葬、迁徙。
     * - 成日：成功、天帝纪万物成就的大吉日子，凡事皆顺。
     * -- 宜：结婚、开市、修造、动土、安床、破土、安葬、搬迁、
     * --- 交易、求財、出行、立契、竖柱、裁种、牧养。
     * -- 忌：诉讼。
     * - 收日：收成、收穫，天帝宝库收纳的日子。
     * -- 宜：祈福、求嗣、赴任、嫁娶、安床、修造、动土、
     * --- 求学、开市、交易、买卖、立契。
     * -- 忌：放债、新船下水、新车下地、破土、安葬。
     * - 开日：开始、开展的日子。
     * -- 宜：祭祀、祈福、入学、上任、修造、动土、
     * --- 开市、安床、交易、出行、竖柱。
     * -- 忌：放债、诉讼、安葬。
     * - 闭日：十二建中最后一日，关闭、收藏、天地阴阳闭寒的日子。
     * -- 宜：祭祀、祈福、筑堤、埋池、埋穴、造葬、填补、修屋。
     * -- 忌：开市、出行、求医、手术、嫁娶。 */

    public static class HuangDay {
        public final String label;
        public final String star;
        public final HuangDayType dayType;
        public final String description;
        public final String genericText;
        public final String canDo;
        public final String notDo;

        public HuangDay(String label, String star, HuangDayType dayType, String description,
                String genericText, String canDo, String notDo) {
            this.label = label;
            this.star = star;
            this.dayType = dayType;
            this.description = description;
            this.genericText = genericText;
            this.canDo = canDo;
            this.notDo = notDo;
        }

        private static final String FORMAT_INFO = "%1$s日(%2$s, %3$s星):%4$s\n%5$s\n宜: %6$s\n忌: %7$s";

        public String info() {
            return String.format(FORMAT_INFO, label, dayType.label, star, description, genericText,
                    canDo, notDo);
        }

        private static final String FORMAT_SHORT_INFO = "%1$s日:%2$s";

        public String shortInfo() {
            return String.format(FORMAT_SHORT_INFO, label, genericText);
        }
    }

    // Refer to:
    // http://www.zgtsm.com/eHtml/20161017/1448_2234.html
    private static final HuangDay[] sHuangDays = {
            // 建日：万物生育、强健、健壮的日子。
            new HuangDay(
                    "建", "太岁", HuangDayType.HeiBadDay,
                    "万物生育、强健、健壮的日子。",
                    "建基立业，从头开始，如苗初长，终获收成。",
                    "出行、上任、会友、上书、见工",
                    "动土、开仓、掘井、嫁娶、纳采、新船下水、新车下地"
                    ),
            // 除日：扫除恶煞、去旧迎新的日子。
            new HuangDay(
                    "除", "青龙", HuangDayType.HuangGoodDay,
                    "扫除恶煞、去旧迎新的日子。",
                    "除旧迎新，沐浴洗礼，拜神祈福，安居乐业。",
                    "除服、疗病、出行、拆卸、入宅、出货、动土、交易",
                    "求官、开张、探病、赴任"
                    ),
            // 满日：丰收、美满、天帝宝库积满的日子。
            new HuangDay(
                    "满", "丧门", HuangDayType.HeiBadDay,
                    "丰收、美满、天帝宝库积满的日子。",
                    "满载而归，圆圆满满，兴高采烈，得胜之时。",
                    "嫁娶、祈福、移徙、开市、交易、求財、立契、祭祀、出行、牧养",
                    "造葬、赴任、求医"
                    ),
            // 平日：平常、官人集合平分的日子。
            new HuangDay(
                    "平", "六合", HuangDayType.HeiBadDay,
                    "平常、官人集合平分的日子。",
                    "平平安安，平平常常，不平不忿，吉凶各半。",
                    "修造、修坟、牧养、开市、安床、动土、求嗣",
                    "移徙、入宅、祈福、赴任、嫁娶、开市、安葬"
                    ),
            // 定日：安定、平常、天帝众客定座的日子。
            new HuangDay(
                    "定", "官符", HuangDayType.HuangGoodDay,
                    "安定、平常、天帝众客定座的日子。",
                    "定如泰山，安定团结，事已成形，根基牢固。",
                    "祭祀、祈福、嫁娶、造屋、装修、修路、开市、入学、上任、入伙",
                    "诉讼、出行、交涉、种植、置业、卖田、掘井、造船"
                    ),
            // 执日：破日之从神，曰小耗，天帝执行万物赐天福，较差的日子。
            new HuangDay(
                    "执", "少耗", HuangDayType.HuangGoodDay,
                    "破日之从神，曰小耗，天帝执行万物赐天福，较差的日子。",
                    "执日多忧，顽固不化，吉凶各半，不可大用。",
                    "祈福、祭祀、求子、立约、造屋、装修、嫁娶、收购",
                    "开市、交易、搬家、远行"
                    ),
            // 破日：日月相衝，曰大耗，斗柄相衝相向必破坏的日子，大事不宜。
            new HuangDay(
                    "破", "大耗", HuangDayType.HeiBadDay,
                    "日月相衝，曰大耗，斗柄相衝相向必破坏的日子，大事不宜。",
                    "月建冲破，最喜动工，红白喜事，十用九凶。",
                    "破土、拆卸、求医",
                    "嫁娶、签约、交涉、出行、搬迁"
                    ),
            // 危日：危机、危险，诸事不宜的日子。
            new HuangDay(
                    "危", "朱雀", HuangDayType.HuangGoodDay,
                    "危机、危险，诸事不宜的日子。",
                    "危日惊险，做事小心，岌岌可危，凶煞占宫。",
                    "经营、交易、求官、納畜、祭祀、祈福、安床、拆卸、破土",
                    "乘船、出行、嫁娶、造葬、迁徙、登高、入宅、博彩"
                    ),
            // 成日：成功、天帝纪万物成就的大吉日子，凡事皆顺。
            new HuangDay(
                    "成", "白虎", HuangDayType.HuangGoodDay,
                    "成功、天帝纪万物成就的大吉日子，凡事皆顺。",
                    "吉星高照，贵人接引，天皇地皇，金玉满堂。",
                    "祈福、入学、开市、求医、成服",
                    "词讼、安門、移徙"
                    ),
            // 收日：收成、收穫，天帝宝库收纳的日子。
            new HuangDay(
                    "收", "贵神", HuangDayType.HeiBadDay,
                    "收成、收穫，天帝宝库收纳的日子。",
                    "煞集中宫，只宜回笼，收帐要债，此日最佳。",
                    "祭祀、签约、订盟、祈福、求嗣、赴任、嫁娶、安床、修造、动土、求学、开市、交易、买卖",
                    "放债、新船下水、新车下地、破土、安葬、开市、入宅"
                    ),
            // 开日：开始、开展的日子。
            new HuangDay(
                    "开", "吊客", HuangDayType.HuangGoodDay,
                    "开始、开展的日子。",
                    "马到成功，旗开得胜，大吉大利，百无禁忌。",
                    "祭祀、祈福、入学、上任、修造、动土、开市、安床、交易、出行、竖柱、疗病、结婚、入仓",
                    "放债、诉讼、安葬、动土、针灸"
                    ),
            // 闭日：十二建中最后一日，关闭、收藏、天地阴阳闭寒的日子。
            new HuangDay(
                    "闭", "病符", HuangDayType.HeiBadDay,
                    "关闭、收藏、天地阴阳闭寒的日子。",
                    "闭门不出，闭塞不通，万物归仓，只宜静养。",
                    "祭祀、祈福、筑堤、埋池、埋穴、造葬、填补、修屋",
                    "开市、出行、求医、手术、嫁娶、宴会、安床、移徙"
                    )
    };

    public static class DayAlmanac {
        public final HuangDay huangDay;
        public final DayBranch dayBranch;

        public DayAlmanac(final HuangDay huangDay, final DayBranch dayBranch) {
            this.huangDay = huangDay;
            this.dayBranch = dayBranch;
        }

        public String info() {
            return huangDay.info();
        }
    }

    // 寅月（农历正月）逢地支寅日为建日，其余日期按照十二建星顺序推算得出。
    // 卯月（农历二月）逢地支卯日为建日，辰月（农历三月）逢地支辰日为建日，其余月份都是如此。
    // 从建日开始的顺序：建闭开收成危破执定平满 除. 然后循环.
    //      农历月- 1  2  3   4  5  6   7  8   9  10 11 12
    // 日期地支（日支）
    //        子 - 开, 收, 成, 危, 破, 执, 定, 平, 满, 除, 建, 闭,
    //        丑 - 闭, 开, 收, 成, 危, 破, 执, 定, 平, 满, 除, 建,
    //        寅 - 建, 闭, 开, 收, 成, 危, 破, 执, 定, 平, 满, 除,
    //        卯 - 除, 建, 闭, 开, 收, 成, 危, 破, 执, 定, 平, 满,
    //        辰 - 满, 除, 建, 闭, 开, 收, 成, 危, 破, 执, 定, 平,
    //        巳 - 平, 满, 除, 建, 闭, 开, 收, 成, 危, 破, 执, 定,
    //        午 - 定, 平, 满, 除, 建, 闭, 开, 收, 成, 危, 破, 执,
    //        未 - 执, 定, 平, 满, 除, 建, 闭, 开, 收, 成, 危, 破,
    //        申 - 破, 执, 定, 平, 满, 除, 建, 闭, 开, 收, 成, 危,
    //        酉 - 危, 破, 执, 定, 平, 满, 除, 建, 闭, 开, 收, 成,
    //        戌 - 成, 危, 破, 执, 定, 平, 满, 除, 建, 闭, 开, 收,
    //        亥 - 收, 成, 危, 破, 执, 定, 平, 满, 除, 建, 闭, 开.
    private static class HuangDayData {
        public final DayBranch dayBranch;
        public final String[] huangDayLabels;

        public HuangDayData(final DayBranch dayBranch, final String[] huangDayLabels) {
            this.dayBranch = dayBranch;
            this.huangDayLabels = huangDayLabels;
        }
    }

    // 子日在12个农历月中对应的黄历日.
    private static final String[] sZiDays = {
            "开", "收", "成", "危", "破", "执", "定", "平", "满", "除", "建", "闭"
    };
    // 丑日.
    private static final String[] sChouDays = new String[sZiDays.length];
    // 寅日
    private static final String[] sYinDays = new String[sZiDays.length];
    // 卯日
    private static final String[] sMaoDays = new String[sZiDays.length];
    // 辰日
    private static final String[] sChenDays = new String[sZiDays.length];
    // 巳日
    private static final String[] sSiDays = new String[sZiDays.length];
    // 午日
    private static final String[] sWuDays = new String[sZiDays.length];
    // 未日
    private static final String[] sWeiDays = new String[sZiDays.length];
    // 申日
    private static final String[] sShenDays = new String[sZiDays.length];
    // 酉日
    private static final String[] sYouDays = new String[sZiDays.length];
    // 戌日
    private static final String[] sXuDays = new String[sZiDays.length];
    // 亥日
    private static final String[] sHaiDays = new String[sZiDays.length];

    // 根据子日的黄历数据，生成其它地支的黄历数据.
    // 两行log都打开可以看到数据输出，对照上面注释中的数据可以检查是否正确生成.
    private static void initHuangDayData() {
        // Log.d("WB", Utility.array2String(sZiDays));
        String[] dayLabels;
        for (int i = 1; i < sHuangDayData.length; i++) {
            dayLabels = sHuangDayData[i].huangDayLabels;
            for (int j = 0; j < dayLabels.length; j++) {
                dayLabels[j] = sZiDays[(12 - i + j) % sZiDays.length];
            }
            // Log.d("WB", Utility.array2String(dayLabels));
        }
    }

    private static final HuangDayData[] sHuangDayData = {
            new HuangDayData(DayBranch.Zi,   sZiDays),
            new HuangDayData(DayBranch.Chou, sChouDays),
            new HuangDayData(DayBranch.Yin,  sYinDays),
            new HuangDayData(DayBranch.Mao,  sMaoDays),
            new HuangDayData(DayBranch.Chen, sChenDays),
            new HuangDayData(DayBranch.Si,   sSiDays),
            new HuangDayData(DayBranch.Wu,   sWuDays),
            new HuangDayData(DayBranch.Wei,  sWeiDays),
            new HuangDayData(DayBranch.Shen, sShenDays),
            new HuangDayData(DayBranch.You,  sYouDays),
            new HuangDayData(DayBranch.Xu,   sXuDays),
            new HuangDayData(DayBranch.Hai,  sHaiDays),
    };

    static {
        initHuangDayData();
    }

    private static String[] getHuangDayLabels(final DayBranch dayBranch) {
        for (HuangDayData dayData : sHuangDayData) {
            if (dayData.dayBranch == dayBranch) return dayData.huangDayLabels;
        }
        return null;
    }

    private static HuangDay getHuangDayByLabel(final String huangDayLabel) {
        for (HuangDay huangDay : sHuangDays) {
            if (TextUtils.equals(huangDay.label, huangDayLabel)) {
                return huangDay;
            }
        }
        return null;
    }

    private static HuangDay findHuangDay(final int lunar_month, final int lunar_day_branch_index) {
        final DayBranch dayBranch = DayBranch.getFromBranchIndex(lunar_day_branch_index);
        final String[] huangDayLabels = getHuangDayLabels(dayBranch);
        return getHuangDayByLabel(huangDayLabels[lunar_month]);
    }

    // 根据日期的地支和该农历月份，获得对应农历日期的黄历数据.
    public static DayAlmanac getAlmanacData(final Day day) {
        final HuangDay huangDay = findHuangDay(day.lunar_month, day.lunar_day_branch_index);
        final DayBranch dayBranch = DayBranch.getFromBranchIndex(day.lunar_day_branch_index);
        return new DayAlmanac(huangDay, dayBranch);
    }

    public static String getAlmanacText(final Day day) {
        DayAlmanac dayAlmanac = getAlmanacData(day);
        StringBuilder sb = new StringBuilder();
        if (dayAlmanac.dayBranch != null) {
            sb.append(dayAlmanac.dayBranch.info()).append('\n');
        }
        sb.append(dayAlmanac.huangDay.info());
        return sb.toString();
    }
}
