package wb.widget.utils.calendar;

import android.content.Context;

import wb.widget.model.Day;
import wb.widget.utils.calendar.Constants.Branch;
import wb.widget.utils.calendar.Constants.FiveElement;
import wb.widget.utils.calendar.Constants.Stem;

// 纳音五行.
// Refer to
// https://www.d1xz.net/sx/jieshuo/art227141_2.aspx
public class NaYinUtils {
    public static class NaYinItem {
        public final Constants.Stem stem;
        public final Constants.Branch branch;
        public final String nayinLabel;
        public final String nayinAnimal;
        public final FiveElement element;

        public NaYinItem(Constants.Stem stem, Constants.Branch branch, String nayinLabel,
                String nayinAnimal, FiveElement element) {
            this.stem = stem;
            this.branch = branch;
            this.nayinLabel = nayinLabel;
            this.nayinAnimal = nayinAnimal;
            this.element = element;
        }
    }

    private static final NaYinItem[] sNaYins = {
            // 子鼠
            new NaYinItem(Stem.Jia, Branch.Zi, "海中金", "杜鼠", FiveElement.Gold), // 子
            new NaYinItem(Stem.Bing, Branch.Zi, "涧下水", "田鼠", FiveElement.Water), // 子
            new NaYinItem(Stem.Wu, Branch.Zi, "霹雳火", "粟鼠", FiveElement.Fire), // 子
            new NaYinItem(Stem.Geng, Branch.Zi, "壁上土", "白鼠", FiveElement.Earth), // 子
            new NaYinItem(Stem.Ren, Branch.Zi, "桑拓木", "狐鼠", FiveElement.Wood), // 子
            // 丑牛
            new NaYinItem(Stem.Yi, Branch.Chou, "海中金", "乳牛", FiveElement.Gold), // 丑
            new NaYinItem(Stem.Ding, Branch.Chou, "涧下水", "耕牛", FiveElement.Water), // 丑
            new NaYinItem(Stem.Ji, Branch.Chou, "霹雳火", "水牛", FiveElement.Fire), // 丑
            new NaYinItem(Stem.Xin, Branch.Chou, "壁上土", "牧牛", FiveElement.Earth), // 丑
            new NaYinItem(Stem.Gui, Branch.Chou, "桑拓木", "牵牛", FiveElement.Wood), // 丑
            // 寅虎
            new NaYinItem(Stem.Jia, Branch.Yin, "大溪水", "猛虎", FiveElement.Water), // 卯
            new NaYinItem(Stem.Bing, Branch.Yin, "炉中火", "雪虎", FiveElement.Fire), // 卯
            new NaYinItem(Stem.Wu, Branch.Yin, "城墙土", "暴虎", FiveElement.Earth), // 卯
            new NaYinItem(Stem.Geng, Branch.Yin, "松柏木", "骑虎", FiveElement.Wood), // 卯
            new NaYinItem(Stem.Ren, Branch.Yin, "金箔金", " 乳虎", FiveElement.Gold), // 卯
            // 卯兔
            new NaYinItem(Stem.Yi, Branch.Mao, "大溪水", "狡兔", FiveElement.Water), // 卯
            new NaYinItem(Stem.Ding, Branch.Mao, "炉中火", "野兔", FiveElement.Fire), // 卯
            new NaYinItem(Stem.Ji, Branch.Mao, "城墙土", "家兔", FiveElement.Earth), // 卯
            new NaYinItem(Stem.Xin, Branch.Mao, "松柏木", "月兔", FiveElement.Wood), // 卯
            new NaYinItem(Stem.Gui, Branch.Mao, "金箔金", "玉兔", FiveElement.Gold), // 卯
            // 辰龙
            new NaYinItem(Stem.Jia, Branch.Chen, "佛灯火", "降龙", FiveElement.Fire), // 辰
            new NaYinItem(Stem.Bing, Branch.Chen, "沙中土", "伏龙", FiveElement.Earth), // 辰
            new NaYinItem(Stem.Wu, Branch.Chen, "大林木", "天龙", FiveElement.Wood), // 辰
            new NaYinItem(Stem.Geng, Branch.Chen, "白腊金", "升龙", FiveElement.Gold), // 辰
            new NaYinItem(Stem.Ren, Branch.Chen, "长流水", "蛟龙", FiveElement.Water), // 辰
            // 巳蛇
            new NaYinItem(Stem.Yi, Branch.Si, "佛灯火", "长蛇", FiveElement.Fire), // 巳
            new NaYinItem(Stem.Ding, Branch.Si, "沙中土", "大蛇", FiveElement.Earth), // 巳
            new NaYinItem(Stem.Ji, Branch.Si, "大林木", "龙蛇", FiveElement.Wood), // 巳
            new NaYinItem(Stem.Xin, Branch.Si, "白腊金", "蝮蛇", FiveElement.Gold), // 巳
            new NaYinItem(Stem.Gui, Branch.Si, "长流水", "卧蛇", FiveElement.Water), // 巳
            // 午马
            new NaYinItem(Stem.Jia, Branch.Wu, "砂中金", "骑马", FiveElement.Gold), // 午
            new NaYinItem(Stem.Bing, Branch.Wu, "天河水", "天马", FiveElement.Water), // 午
            new NaYinItem(Stem.Wu, Branch.Wu, "天上火", "驮马", FiveElement.Fire), // 午
            new NaYinItem(Stem.Geng, Branch.Wu, "路旁土", "兵马", FiveElement.Earth), // 午
            new NaYinItem(Stem.Ren, Branch.Wu, "杨柳木", "快马", FiveElement.Wood), // 午
            // 未羊
            new NaYinItem(Stem.Yi, Branch.Wei, "砂中金", "白羊", FiveElement.Gold), // 未
            new NaYinItem(Stem.Ding, Branch.Wei, "天河水", "灰羊", FiveElement.Water), // 未
            new NaYinItem(Stem.Ji, Branch.Wei, "天上火", "山羊", FiveElement.Fire), // 未
            new NaYinItem(Stem.Xin, Branch.Wei, "路旁土", "野羊", FiveElement.Earth), // 未
            new NaYinItem(Stem.Gui, Branch.Wei, "杨柳木", "绵羊", FiveElement.Wood), // 未
            // 申猴
            new NaYinItem(Stem.Jia, Branch.Shen, "泉中水", "王猴", FiveElement.Water), // 申
            new NaYinItem(Stem.Bing, Branch.Shen, "山下火", "赤猴", FiveElement.Fire), // 申
            new NaYinItem(Stem.Wu, Branch.Shen, "大驿土", "山猴", FiveElement.Earth), // 申
            new NaYinItem(Stem.Geng, Branch.Shen, "石榴木", "芸猴", FiveElement.Wood), // 申
            new NaYinItem(Stem.Ren, Branch.Shen, "剑锋金", "亲猴", FiveElement.Gold), // 申
            // 酉鸡
            new NaYinItem(Stem.Yi, Branch.You, "泉中水", "乌鸡", FiveElement.Water), // 酉
            new NaYinItem(Stem.Ding, Branch.You, "山下火", "斗鸡", FiveElement.Fire), // 酉
            new NaYinItem(Stem.Ji, Branch.You, "大驿土", "野鸡", FiveElement.Earth), // 酉
            new NaYinItem(Stem.Xin, Branch.You, "石榴木", "军鸡", FiveElement.Wood), // 酉
            new NaYinItem(Stem.Gui, Branch.You, "剑锋金", "家鸡", FiveElement.Gold), // 酉
            // 戌蛇
            new NaYinItem(Stem.Jia, Branch.Xu, "山头火", "军犬", FiveElement.Fire), // 戌
            new NaYinItem(Stem.Bing, Branch.Xu, "屋上土", "猎狗", FiveElement.Earth), // 戌
            new NaYinItem(Stem.Wu, Branch.Xu, "平地木", "野犬", FiveElement.Wood), // 戌
            new NaYinItem(Stem.Geng, Branch.Xu, "钗钏金", "猛狗", FiveElement.Gold), // 戌
            new NaYinItem(Stem.Ren, Branch.Xu, "大海水", "爱狗", FiveElement.Water), // 戌
            // 亥猪
            new NaYinItem(Stem.Yi, Branch.Hai, "山头火", "山猪", FiveElement.Fire),  // 亥
            new NaYinItem(Stem.Ding, Branch.Hai, "屋上土", "河猪", FiveElement.Earth),  // 亥
            new NaYinItem(Stem.Ji, Branch.Hai, "平地木", "田猪", FiveElement.Wood),  // 亥
            new NaYinItem(Stem.Xin, Branch.Hai, "钗钏金", "家猪", FiveElement.Gold),  // 亥
            new NaYinItem(Stem.Gui, Branch.Hai, "大海水", "荒猪", FiveElement.Water),  // 亥
    };

    public static NaYinItem getNaYinItem(final Stem stem, final Branch branch) {
        for (NaYinItem nayin : sNaYins) {
            if (nayin.stem != stem) continue;
            if (nayin.branch != branch) continue;
            return nayin;
        }
        return null;
    }

    public static NaYinItem getNaYinItem(final Day day) {
        int yearStemIndex = CalendarUtil.getStemIndex(day.year, day.month, day.lunar_month);
        Stem yearStem = Constants.getStem(yearStemIndex);

        int yearBranchIndex = CalendarUtil.getBranchIndex(day.year, day.month, day.lunar_month);
        Branch yearBranch = Constants.getBranch(yearBranchIndex);

        return getNaYinItem(yearStem, yearBranch);
    }

    private static final String FORMAT_NA_YIN = "纳音五行: %s(%s) %s";

    public static String getNaYinInfo(final Context context, final Stem stem, final Branch branch) {
        NaYinItem nayin = getNaYinItem(stem, branch);
        return String.format(FORMAT_NA_YIN, nayin.nayinLabel, nayin.element.sound.getLabel(context),
                nayin.nayinAnimal);
    }

    public static String getNaYinInfo(final Context context, final Day day) {
        int yearStemIndex = CalendarUtil.getStemIndex(day.year, day.month, day.lunar_month);
        Stem yearStem = Constants.getStem(yearStemIndex);

        int yearBranchIndex = CalendarUtil.getBranchIndex(day.year, day.month, day.lunar_month);
        Branch yearBranch = Constants.getBranch(yearBranchIndex);

        return getNaYinInfo(context, yearStem, yearBranch);
    }
}
