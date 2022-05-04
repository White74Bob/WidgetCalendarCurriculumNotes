
package wb.widget.utils.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.Context;
import android.text.TextUtils;
import wb.widget.R;
import wb.widget.WidgetMonthCalendar;
import wb.widget.model.Day;
import wb.widget.utils.Utility;

public class CalendarUtil {

    /* 公历每月前面的天数 */
    private final static int[] wMonthAdd = {
            0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334
    };
    /* 农历数据 */
    private final static int[] wNongliData = {
            2635, 333387, 1701, 1748, 267701, 694, 2391, 133423, 1175, 396438, 3402,
            3749, 331177, 1453, 694, 201326, 2350, 465197, 3221, 3402, 400202, 2901, 1386, 267611,
            605, 2349, 137515,
            2709, 464533, 1738, 2901, 330421, 1242, 2651, 199255, 1323, 529706, 3733, 1706, 398762,
            2741, 1206, 267438,
            2647, 1318, 204070, 3477, 461653, 1386, 2413, 330077, 1197, 2637, 268877, 3365, 531109,
            2900, 2922, 398042,
            2395, 1179, 267415, 2635, 661067, 1701, 1748, 398772, 2742, 2391, 330031, 1175, 1611,
            200010, 3749, 527717,
            1452, 2742, 332397, 2350, 3222, 268949, 3402, 3493, 133973, 1386, 464219, 605, 2349,
            334123, 2709, 2890,
            267946, 2773, 592565, 1210, 2651, 395863, 1323, 2707, 265877
    };

    //
    // 公历转农历
    //
    public static void solarToLunar4(final Day day) {
        int nTheDate = 0;
        int k = 0;
        int n = 0;

        int i = 0;
        int nBit = 0;

        /*---取当前公历年、月、日---*/
        int wCurYear = day.year;
        int wCurMonth = day.month + 1;
        int wCurDay = day.day;

        /*---计算到初始时间1921年2月8日的天数：1921-2-8(正月初一)---*/
        nTheDate = (wCurYear - 1921) * 365 + (wCurYear - 1921) / 4 + wCurDay
                + wMonthAdd[wCurMonth - 1] - 38;
        if (nTheDate < 0) {
            return;
        }
        if (((wCurYear % 4) == 0) && (wCurMonth > 2)) {
            nTheDate = nTheDate + 1;
        }

        /*--计算农历天干、地支、月、日---*/
        int nIsEnd = 0;
        int m = 0;
        while (nIsEnd != 1) {
            if (wNongliData[m] < 4095) {
                k = 11;
            } else {
                k = 12;
            }
            n = k;
            while (n >= 0) {
                // 获取wNongliData(m)的第n个二进制位的值
                nBit = wNongliData[m];
                for (i = 1; i < n + 1; i++) {
                    nBit = nBit / 2;
                }

                nBit = nBit % 2;

                if (nTheDate <= (29 + nBit)) {
                    nIsEnd = 1;
                    break;
                }

                nTheDate = nTheDate - 29 - nBit;
                n = n - 1;
            }
            if (nIsEnd != 0) {
                break;
            }
            m = m + 1;
        }

        wCurYear = 1921 + m;
        wCurMonth = k - n + 1;
        wCurDay = nTheDate;

        if (wCurDay < 0) {
            return;
        }

        if (k == 12) {
            if (wCurMonth == wNongliData[m] / 65536 + 1) {
                wCurMonth = 1 - wCurMonth;
            } else if (wCurMonth > wNongliData[m] / 65536 + 1) {
                wCurMonth = wCurMonth - 1;
            }
        }

        day.lunar_year = wCurYear;
        if (wCurMonth < 1) {
            day.lunar_month = -wCurMonth;
        } else {
            day.lunar_month = wCurMonth;
        }
        day.lunar_month--;
        day.lunar_day = wCurDay;
    }

    private final static int[] year_code = {
            0x4d, 0x4A, 0xB8, // 2001
            0x0d, 0x4A, 0x4C, // 2002
            0x0d, 0xA5, 0x41, // 2003
            0x25, 0xAA, 0xB6, // 2004
            0x05, 0x6A, 0x49, // 2005
            0x7A, 0xAd, 0xBd, // 2006
            0x02, 0x5d, 0x52, // 2007
            0x09, 0x2d, 0x47, // 2008
            0x5C, 0x95, 0xBA, // 2009
            0x0A, 0x95, 0x4e, // 2010
            0x0B, 0x4A, 0x43, // 2011
            0x4B, 0x55, 0x37, // 2012
            0x0A, 0xd5, 0x4A, // 2013
            0x95, 0x5A, 0xBf, // 2014
            0x04, 0xBA, 0x53, // 2015
            0x0A, 0x5B, 0x48, // 2016
            0x65, 0x2B, 0xBC, // 2017
            0x05, 0x2B, 0x50, // 2018
            0x0A, 0x93, 0x45, // 2019
            0x47, 0x4A, 0xB9, // 2020
            0x06, 0xAA, 0x4C, // 2021
            0x0A, 0xd5, 0x41, // 2022
            0x24, 0xdA, 0xB6, // 2023
            0x04, 0xB6, 0x4A, // 2024
            0x69, 0x57, 0x3d, // 2025
            0x0A, 0x4e, 0x51, // 2026
            0x0d, 0x26, 0x46, // 2027
            0x5e, 0x93, 0x3A, // 2028
            0x0d, 0x53, 0x4d, // 2029
            0x05, 0xAA, 0x43, // 2030
            0x36, 0xB5, 0x37, // 2031
            0x09, 0x6d, 0x4B, // 2032
            0xB4, 0xAe, 0xBf, // 2033
            0x04, 0xAd, 0x53, // 2034
            0x0A, 0x4d, 0x48, // 2035
            0x6d, 0x25, 0xBC, // 2036
            0x0d, 0x25, 0x4f, // 2037
            0x0d, 0x52, 0x44, // 2038
            0x5d, 0xAA, 0x38, // 2039
            0x0B, 0x5A, 0x4C, // 2040
            0x05, 0x6d, 0x41, // 2041
            0x24, 0xAd, 0xB6, // 2042
            0x04, 0x9B, 0x4A, // 2043
            0x7A, 0x4B, 0xBe, // 2044
            0x0A, 0x4B, 0x51, // 2045
            0x0A, 0xA5, 0x46, // 2046
            0x5B, 0x52, 0xBA, // 2047
            0x06, 0xd2, 0x4e, // 2048
            0x0A, 0xdA, 0x42, // 2049
            0x35, 0x5B, 0x37, // 2050
            0x09, 0x37, 0x4B, // 2051
            0x84, 0x97, 0xC1, // 2052
            0x04, 0x97, 0x53, // 2053
            0x06, 0x4B, 0x48, // 2054
            0x66, 0xA5, 0x3C, // 2055
            0x0e, 0xA5, 0x4f, // 2056
            0x06, 0xB2, 0x44, // 2057
            0x4A, 0xB6, 0x38, // 2058
            0x0A, 0xAe, 0x4C, // 2059
            0x09, 0x2e, 0x42, // 2060
            0x3C, 0x97, 0x35, // 2061
            0x0C, 0x96, 0x49, // 2062
            0x7d, 0x4A, 0xBd, // 2063
            0x0d, 0x4A, 0x51, // 2064
            0x0d, 0xA5, 0x45, // 2065
            0x55, 0xAA, 0xBA, // 2066
            0x05, 0x6A, 0x4e, // 2067
            0x0A, 0x6d, 0x43, // 2068
            0x45, 0x2e, 0xB7, // 2069
            0x05, 0x2d, 0x4B, // 2070
            0x8A, 0x95, 0xBf, // 2071
            0x0A, 0x95, 0x53, // 2072
            0x0B, 0x4A, 0x47, // 2073
            0x6B, 0x55, 0x3B, // 2074
            0x0A, 0xd5, 0x4f, // 2075
            0x05, 0x5A, 0x45, // 2076
            0x4A, 0x5d, 0x38, // 2077
            0x0A, 0x5B, 0x4C, // 2078
            0x05, 0x2B, 0x42, // 2079
            0x3A, 0x93, 0xB6, // 2080
            0x06, 0x93, 0x49, // 2081
            0x77, 0x29, 0xBd, // 2082
            0x06, 0xAA, 0x51, // 2083
            0x0A, 0xd5, 0x46, // 2084
            0x54, 0xdA, 0xBA, // 2085
            0x04, 0xB6, 0x4e, // 2086
            0x0A, 0x57, 0x43, // 2087
            0x45, 0x27, 0x38, // 2088
            0x0d, 0x26, 0x4A, // 2089
            0x8e, 0x93, 0x3e, // 2090
            0x0d, 0x52, 0x52, // 2091
            0x0d, 0xAA, 0x47, // 2092
            0x66, 0xB5, 0x3B, // 2093
            0x05, 0x6d, 0x4f, // 2094
            0x04, 0xAe, 0x45, // 2095
            0x4A, 0x4e, 0xB9, // 2096
            0x0A, 0x4d, 0x4C, // 2097
            0x0d, 0x15, 0x41, // 2098
            0x2d, 0x92, 0xB5, // 2099
    };

    private final static int[] day_code1 = {
            0x0, 0x1f, 0x3b, 0x5a, 0x78, 0x97, 0xb5, 0xd4, 0xf3
    };
    private final static int[] day_code2 = {
            0x111, 0x130, 0x14e
    };

    private static int get_moon_day(final int month_p, final int table_addr) {
        switch (month_p) {
            case 1: {
                if ((year_code[table_addr] & 0x08) == 0) return 0;
                return 1;
            }
            case 2: {
                if ((year_code[table_addr] & 0x04) == 0) return 0;
                return 1;
            }
            case 3: {
                if ((year_code[table_addr] & 0x02) == 0) return 0;
                return 1;
            }
            case 4: {
                if ((year_code[table_addr] & 0x01) == 0) return 0;
                return 1;
            }
            case 5: {
                if ((year_code[table_addr + 1] & 0x80) == 0) return 0;
                return 1;
            }
            case 6: {
                if ((year_code[table_addr + 1] & 0x40) == 0) return 0;
                return 1;
            }
            case 7: {
                if ((year_code[table_addr + 1] & 0x20) == 0) return 0;
                return 1;
            }
            case 8: {
                if ((year_code[table_addr + 1] & 0x10) == 0) return 0;
                return 1;
            }
            case 9: {
                if ((year_code[table_addr + 1] & 0x08) == 0) return 0;
                return 1;
            }
            case 10: {
                if ((year_code[table_addr + 1] & 0x04) == 0) return 0;
                return 1;
            }
            case 11: {
                if ((year_code[table_addr + 1] & 0x02) == 0) return 0;
                return 1;
            }
            case 12: {
                if ((year_code[table_addr + 1] & 0x01) == 0) return 0;
                return 1;
            }
            case 13: {
                if ((year_code[table_addr + 2] & 0x80) == 0) return 0;
                return 1;
            }
            default:
                return 0;
        }
    }

    public static int CROR(final int value, final int bits) {
        return ((value >> bits) | (value << (0x20 - bits)));
    }

    public static void solarToLunar3(final Day day) {
        int year = day.year;
        int month = day.month + 1;
        int date = day.day;

        int temp1, temp2, temp3, month_p;
        int temp4, table_addr;
        boolean flag_y = false;
        table_addr = (year - 1) * 3;
        temp1 = year_code[table_addr + 2] & 0x60;
        temp1 = CROR(temp1, 5);
        temp2 = year_code[table_addr + 2] & 0x1f;
        if (temp1 == 0x1) {
            temp3 = temp2 - 1;
        } else {
            temp3 = temp2 + 0x1f - 1;
        }
        if (month < 10) {
            temp4 = day_code1[month - 1] + date - 1;
        } else {
            temp4 = day_code2[month - 10] + date - 1;
        }
        if ((month > 0x2) && (year % 0x4 == 0)) {
            temp4 += 1;
        }
        if (temp4 >= temp3) {
            temp4 -= temp3;
            month = 0x1;
            month_p = 0x1;
            flag_y = false;
            if (get_moon_day(month_p, table_addr) == 0) {
                temp1 = 0x1d;
            } else {
                temp1 = 0x1e;
            }
            temp2 = year_code[table_addr] / 16;
            while (temp4 >= temp1) {
                temp4 -= temp1;
                month_p += 1;
                if (month == temp2) {
                    flag_y = !flag_y;
                    if (!flag_y) {
                        month += 1;
                    }
                } else {
                    month += 1;
                }
                if (get_moon_day(month_p, table_addr) == 0) {
                    temp1 = 0x1d;
                } else {
                    temp1 = 0x1e;
                }
            }
            date = temp4 + 1;
        } else {
            temp3 -= temp4;
            year -= 1;
            table_addr -= 0x3;
            month = 0xc;
            temp2 = year_code[table_addr] / 16;
            if (temp2 == 0) {
                month_p = 0xc;
            } else {
                month_p = 0xd;
            }
            flag_y = false;
            if (get_moon_day(month_p, table_addr) == 0) {
                temp1 = 0x1d;
            } else {
                temp1 = 0x1e;
            }
            while (temp3 > temp1) {
                temp3 -= temp1;
                month_p -= 1;
                if (!flag_y) {
                    month -= 1;
                }
                if (month == temp2) {
                    flag_y = !flag_y;
                }
                if (get_moon_day(month_p, table_addr) == 0) {
                    temp1 = 0x1d;
                } else {
                    temp1 = 0x1e;
                }
            }
            date = temp1 - temp3 + 1;
        }

        day.lunar_year = year;
        day.lunar_month = month;
        day.lunar_day = date;
    }

    private static final int baseChineseDate = 11;
    private static final int baseChineseMonth = 11;
    private static final int baseChineseYear = 4598 - 1;
    private static final int baseDate = 1;
    private static final int baseIndex = 0;
    private static final int baseMonth = 1;

    private final static char[] chineseMonths = {
            0x00, 0x04, 0xad, 0x08, 0x5a, 0x01, 0xd5, 0x54, 0xb4, 0x09, 0x64,
            0x05, 0x59, 0x45, 0x95, 0x0a, 0xa6, 0x04, 0x55, 0x24, 0xad, 0x08, 0x5a, 0x62, 0xda,
            0x04, 0xb4, 0x05, 0xb4,
            0x55, 0x52, 0x0d, 0x94, 0x0a, 0x4a, 0x2a, 0x56, 0x02, 0x6d, 0x71, 0x6d, 0x01, 0xda,
            0x02, 0xd2, 0x52, 0xa9,
            0x05, 0x49, 0x0d, 0x2a, 0x45, 0x2b, 0x09, 0x56, 0x01, 0xb5, 0x20, 0x6d, 0x01, 0x59,
            0x69, 0xd4, 0x0a, 0xa8,
            0x05, 0xa9, 0x56, 0xa5, 0x04, 0x2b, 0x09, 0x9e, 0x38, 0xb6, 0x08, 0xec, 0x74, 0x6c,
            0x05, 0xd4, 0x0a, 0xe4,
            0x6a, 0x52, 0x05, 0x95, 0x0a, 0x5a, 0x42, 0x5b, 0x04, 0xb6, 0x04, 0xb4, 0x22, 0x6a,
            0x05, 0x52, 0x75, 0xc9,
            0x0a, 0x52, 0x05, 0x35, 0x55, 0x4d, 0x0a, 0x5a, 0x02, 0x5d, 0x31, 0xb5, 0x02, 0x6a,
            0x8a, 0x68, 0x05, 0xa9,
            0x0a, 0x8a, 0x6a, 0x2a, 0x05, 0x2d, 0x09, 0xaa, 0x48, 0x5a, 0x01, 0xb5, 0x09, 0xb0,
            0x39, 0x64, 0x05, 0x25,
            0x75, 0x95, 0x0a, 0x96, 0x04, 0x4d, 0x54, 0xad, 0x04, 0xda, 0x04, 0xd4, 0x44, 0xb4,
            0x05, 0x54, 0x85, 0x52,
            0x0d, 0x92, 0x0a, 0x56, 0x6a, 0x56, 0x02, 0x6d, 0x02, 0x6a, 0x41, 0xda, 0x02, 0xb2,
            0xa1, 0xa9, 0x05, 0x49,
            0x0d, 0x0a, 0x6d, 0x2a, 0x09, 0x56, 0x01, 0xad, 0x50, 0x6d, 0x01, 0xd9, 0x02, 0xd1,
            0x3a, 0xa8, 0x05, 0x29,
            0x85, 0xa5, 0x0c, 0x2a, 0x09, 0x96, 0x54, 0xb6, 0x08, 0x6c, 0x09, 0x64, 0x45, 0xd4,
            0x0a, 0xa4, 0x05, 0x51,
            0x25, 0x95, 0x0a, 0x2a, 0x72, 0x5b, 0x04, 0xb6, 0x04, 0xac, 0x52, 0x6a, 0x05, 0xd2,
            0x0a, 0xa2, 0x4a, 0x4a,
            0x05, 0x55, 0x94, 0x2d, 0x0a, 0x5a, 0x02, 0x75, 0x61, 0xb5, 0x02, 0x6a, 0x03, 0x61,
            0x45, 0xa9, 0x0a, 0x4a,
            0x05, 0x25, 0x25, 0x2d, 0x09, 0x9a, 0x68, 0xda, 0x08, 0xb4, 0x09, 0xa8, 0x59, 0x54,
            0x03, 0xa5, 0x0a, 0x91,
            0x3a, 0x96, 0x04, 0xad, 0xb0, 0xad, 0x04, 0xda, 0x04, 0xf4, 0x62, 0xb4, 0x05, 0x54,
            0x0b, 0x44, 0x5d, 0x52,
            0x0a, 0x95, 0x04, 0x55, 0x22, 0x6d, 0x02, 0x5a, 0x71, 0xda, 0x02, 0xaa, 0x05, 0xb2,
            0x55, 0x49, 0x0b, 0x4a,
            0x0a, 0x2d, 0x39, 0x36, 0x01, 0x6d, 0x80, 0x6d, 0x01, 0xd9, 0x02, 0xe9, 0x6a, 0xa8,
            0x05, 0x29, 0x0b, 0x9a,
            0x4c, 0xaa, 0x08, 0xb6, 0x08, 0xb4, 0x38, 0x6c, 0x09, 0x54, 0x75, 0xd4, 0x0a, 0xa4,
            0x05, 0x45, 0x55, 0x95,
            0x0a, 0x9a, 0x04, 0x55, 0x44, 0xb5, 0x04, 0x6a, 0x82, 0x6a, 0x05, 0xd2, 0x0a, 0x92,
            0x6a, 0x4a, 0x05, 0x55,
            0x0a, 0x2a, 0x4a, 0x5a, 0x02, 0xb5, 0x02, 0xb2, 0x31, 0x69, 0x03, 0x31, 0x73, 0xa9,
            0x0a, 0x4a, 0x05, 0x2d,
            0x55, 0x2d, 0x09, 0x5a, 0x01, 0xd5, 0x48, 0xb4, 0x09, 0x68, 0x89, 0x54, 0x0b, 0xa4,
            0x0a, 0xa5, 0x6a, 0x95,
            0x04, 0xad, 0x08, 0x6a, 0x44, 0xda, 0x04, 0x74, 0x05, 0xb0, 0x25, 0x54, 0x03
    };

    private final static int[] bigLeapMonthYears = {
            6, 14, 19, 25, 33, 36, 38, 41, 44, 52, 55, 79, 117, 136, 147, 150,
            155, 158, 185, 193
    };

    private static int daysInChineseMonth(int y, int m) {
        int index = y - baseChineseYear + baseIndex;
        int v = 0;
        int l = 0;
        int d = 30;
        if (1 <= m && m <= 8) {
            v = chineseMonths[2 * index];
            l = m - 1;
            if (((v >> l) & 0x01) == 1) {
                d = 29;
            }
        } else if (9 <= m && m <= 12) {
            v = chineseMonths[2 * index + 1];
            l = m - 9;
            if (((v >> l) & 0x01) == 1) {
                d = 29;
            }
        } else {
            v = chineseMonths[2 * index + 1];
            v = (v >> 4) & 0x0F;
            if (v != Math.abs(m)) {
                d = 0;
            } else {
                d = 29;
                for (int i = 0; i < bigLeapMonthYears.length; i++) {
                    if (bigLeapMonthYears[i] == index) {
                        d = 30;
                        break;
                    }
                }
            }
        }
        return d;
    }

    // 注意：如果返回value为negative, it means lunar leap month.
    private static int nextChineseMonth(int year, int month) {
        int n = Math.abs(month) + 1;
        if (month > 0) {
            int index = year - baseChineseYear + baseIndex;
            int v = chineseMonths[2 * index + 1];
            v = (v >> 4) & 0x0F;
            if (v == month) {
                n = -month;
            }
        }
        if (n == 13) {
            n = 1;
        }
        return n;
    }

    private static int daysInGregorianMonth(int y, int m) {
        int d = Constants.MONTH_DAYS[m - 1];
        if (m == 2 && isSolarLeapYear(y)) {
            d++;
        }
        return d;
    }

    public static boolean isValidLunarMonth(final int lunar_month) {
        return ((lunar_month >= 0) && (lunar_month < Constants.LUNAR_MONTH_ID_ARRAY.length));
    }

    public static boolean isValidLunarDay(final int lunar_day) {
        return ((lunar_day > 0) && (lunar_day < 31));
    }

    public static void solarToLunar(final Day day) {
        solarToLunar2(day);
        if (!isValidLunarMonth(day.lunar_month) || !isValidLunarDay(day.lunar_day)) {
            solarToLunar4(day);
        } else {
            return;
        }
        if (!isValidLunarMonth(day.lunar_month) || !isValidLunarDay(day.lunar_day)) {
            solarToLunar1(day);
        } else {
            return;
        }
        if (!isValidLunarMonth(day.lunar_month) || !isValidLunarDay(day.lunar_day)) {
            solarToLunar3(day);
        }
    }

    public static void solarToLunar2(final Day day) {
        if (day.year < BASE_YEAR || day.year > 2100) {
            return;
        }
        int startYear = BASE_YEAR;
        int startMonth = baseMonth;
        int startDate = baseDate;
        int chineseYear = baseChineseYear;
        int chineseMonth = baseChineseMonth;
        int chineseDate = baseChineseDate;
        // 2000 1 1 对应农历 4697 11 25
        if (day.year >= 2000) {
            startYear = BASE_YEAR + 99;
            startMonth = 1;
            startDate = 1;
            chineseYear = baseChineseYear + 99;
            chineseMonth = 11;
            chineseDate = 25;
        }
        int daysDiff = 0;
        for (int i = startYear; i < day.year; i++) {
            daysDiff += 365;
            if (isSolarLeapYear(i)) {
                daysDiff += 1; // leap year
            }
        }
        for (int i = startMonth; i < day.month + 1; i++) {
            daysDiff += daysInGregorianMonth(day.year, i);
        }

        daysDiff += day.day - startDate;

        chineseDate += daysDiff;
        int lastDate = daysInChineseMonth(chineseYear, chineseMonth);
        int nextMonth = nextChineseMonth(chineseYear, chineseMonth);
        while (chineseDate > lastDate) {
            if (Math.abs(nextMonth) < Math.abs(chineseMonth)) {
                chineseYear++;
            }
            chineseMonth = nextMonth;
            chineseDate -= lastDate;
            lastDate = daysInChineseMonth(chineseYear, chineseMonth);
            nextMonth = nextChineseMonth(chineseYear, chineseMonth);
        }
        day.lunar_year = chineseYear;
        day.lunar_month = Math.abs(chineseMonth) - 1;
        if (chineseMonth < 0) {
            day.lunar_month_leap = true;
        }
        day.lunar_day = chineseDate;
    }

    /**
     * The solar calendar is turned into the lunar calendar
     */
    public static void solarToLunar1(Day day) {
        int year = day.year;
        int month = day.month + 1;
        int date = day.day;

        int iLDay, iLMonth, iLYear;
        int iOffsetDays = iGetSNewYearOffsetDays(year, month, date);
        int iLeapMonth = iGetLLeapMonth(year);

        if (iOffsetDays < iSolarLunarOffsetTable[year - 1901]) {
            iLYear = year - 1;
            iOffsetDays = iSolarLunarOffsetTable[year - 1901] - iOffsetDays;
            iLDay = iOffsetDays;

            for (iLMonth = 12; iOffsetDays > iGetLMonthDays(iLYear, iLMonth); iLMonth--) {
                iLDay = iOffsetDays;
                iOffsetDays -= iGetLMonthDays(iLYear, iLMonth);
            }
            if (0 == iLDay) {
                iLDay = 1;
            } else {
                iLDay = iGetLMonthDays(iLYear, iLMonth) - iOffsetDays + 1;
            }
        } else {
            iLYear = year;
            iOffsetDays -= iSolarLunarOffsetTable[year - BASE_YEAR];
            iLDay = iOffsetDays + 1;

            for (iLMonth = 1; iOffsetDays >= 0; iLMonth++) {
                iLDay = iOffsetDays + 1;
                iOffsetDays -= iGetLMonthDays(iLYear, iLMonth);
                if ((iLeapMonth == iLMonth) && (iOffsetDays > 0)) {
                    iLDay = iOffsetDays;
                    iOffsetDays -= iGetLMonthDays(iLYear, iLMonth + 12);
                    if (iOffsetDays <= 0) {
                        iLMonth += 12 + 1;
                        break;
                    }
                }
            }
            iLMonth--;
        }

        day.lunar_year = iLYear;
        day.lunar_month = iLMonth - 1;
        day.lunar_day = iLDay;
    }

    /**
     * The lunar calendar is turned into the Solar calendar
     */
    public static void lunarToSolar(Day day) {
        int iYear = day.lunar_year;
        int iMonth = day.lunar_month;
        int iDay = day.lunar_day;

        int iSYear, iSMonth, iSDay;
        int iOffsetDays = iGetLNewYearOffsetDays(iYear, iMonth, iDay)
                + iSolarLunarOffsetTable[iYear - 1901];
        int iYearDays = isSolarLeapYear(iYear) ? 366 : 365;

        if (iOffsetDays >= iYearDays) {
            iSYear = iYear + 1;
            iOffsetDays -= iYearDays;
        } else {
            iSYear = iYear;
        }
        iSDay = iOffsetDays + 1;
        for (iSMonth = 1; iOffsetDays >= 0; iSMonth++) {
            iSDay = iOffsetDays + 1;
            iOffsetDays -= getSYearMonthDays(iSYear, iSMonth);
        }
        iSMonth--;

        day.year = iSYear;
        day.month = iSMonth - 1;
        day.day = iSDay;
    }

    public static void lunarToSolar2(Day day) {
        int[] actualYearMonth;

        int actualYear, actualMonth;
        int monthDayNum;
        Day tempDay;
        for (int index = -1; index <= 2; index++) {
            actualYearMonth = getActualYearMonth(day.year, day.lunar_month, index);
            actualYear = actualYearMonth[0];
            actualMonth = actualYearMonth[1];
            monthDayNum = getMonthDayNum(actualYear, actualMonth);
            for (int i = 1; i <= monthDayNum; i++) {
                tempDay = new Day(actualYear, actualMonth, i);
                if (tempDay.lunar_month == day.lunar_month && tempDay.lunar_day == day.lunar_day) {
                    day.lunar_year = tempDay.lunar_year;
                    day.month = tempDay.month;
                    day.day = tempDay.day;
                    return;
                }
            }
        }
    }

    private static int[] getActualYearMonth(final int solarYear, final int lunar_month,
            final int index) {
        int actualYear, actualMonth = lunar_month + index;
        if (actualMonth < 0) {
            actualYear = solarYear - 1;
            actualMonth = actualMonth + 12;
        } else if (actualMonth >= 12) {
            actualYear = solarYear + 1;
            actualMonth = actualMonth % 12;
        } else {
            actualYear = solarYear;
        }
        return new int[] { actualYear, actualMonth };
    }

    private static int[][] getPossibleYearMonth(final int solarYear, final int lunar_month) {
        if (lunar_month >= 0 && lunar_month < 10) {
            int[][] possibleMonths = new int[5][2];
            // 一般情况下公历可能比农历大一到两个月;
            // 考虑到还有闰月的情况，所以向前找3个月.
            for (int i = -1; i <= 3; i++) {
                possibleMonths[i + 1] = getActualYearMonth(solarYear, lunar_month, i);
            }
            return possibleMonths;
        }
        if (lunar_month >= 10 && lunar_month < 12) {
            int[][] possibleMonths = new int[4][2];
            int i = 0;
            for (; i < 12 - lunar_month; i++) {
                possibleMonths[i] = getActualYearMonth(solarYear, lunar_month, i);
            }
            for (int j = 0; j < possibleMonths.length - i; j++) {
                possibleMonths[i + j] = getActualYearMonth(solarYear, j, 0);
            }
            return possibleMonths;
        }
        return null;
    }

    // 根据公历年中的农历日期，找到对应的公历日期.
    // 需要先调用getPossibleYearMonth，根据下面原则找出来需要查找的月份,然后遍历每一天比较.
    // 对于[1,10]月的农历月month，从month-1到month+2的公历月中找；
    // 对于[11-12]农历月month， 先从公历当月找；找不到的话，继续从公历1月到3月找.
    // 如果以上都找不到，就在上一年和下一年中，按照上述规则继续找。
    // 这样就可能找到4个日期. 选上一年的最后一个日期和下一年的第一个日期.
    public static Day[] lunarToSolar(final int solarYear, final int lunar_month,
            final int lunar_day) {
        final ArrayList<Day> days = new ArrayList<Day>(2);

        int actualYear;
        int actualMonth;

        int[][] possibleYearMonths = getPossibleYearMonth(solarYear, lunar_month);

        int monthDayNum;
        Day tempDay;
        for (int[] actualYearMonth : possibleYearMonths) {
            actualYear = actualYearMonth[0];
            actualMonth = actualYearMonth[1];
            monthDayNum = getMonthDayNum(actualYear, actualMonth);
            for (int i = 1; i <= monthDayNum; i++) {
                tempDay = new Day(actualYear, actualMonth, i);
                if (tempDay.lunar_month == lunar_month && tempDay.lunar_day == lunar_day) {
                    days.add(tempDay);
                }
            }
        }
        int dayNum = days.size();
        if (dayNum <= 0) {
            return lunar2SunarInNearYeas(solarYear, lunar_month, lunar_day);
        }
        return days.toArray(new Day[dayNum]);
    }

    // 因为当年没有找到对应的农历日期，就在上一年和下一年中寻找.
    // 这样最多可能找到4个日期. 选上一年的最后一个日期和下一年的第一个日期.
    private static Day[] lunar2SunarInNearYeas(final int solarYear, final int lunar_month,
            final int lunar_day) {
        Day[] daysLastYear = lunarToSolar(solarYear - 1, lunar_month, lunar_day);
        int lastCount = daysLastYear == null ? 0 : daysLastYear.length;
        Day[] daysNextYear = lunarToSolar(solarYear + 1, lunar_month, lunar_day);
        int nextCount = daysNextYear == null ? 0 : daysNextYear.length;
        if (lastCount > 0 && nextCount > 0) {
            Day[] moreDays = new Day[2];
            moreDays[0] = getLatestDay(daysLastYear);
            moreDays[1] = getEarliestDay(daysNextYear);
            return moreDays;
        }
        if (lastCount == 0) {
            return daysNextYear;
        }
        return daysLastYear;
    }

    // 从给定的日期中找到最后一天.
    private static Day getLatestDay(final Day[] days) {
        Day earliest = null;
        for (Day day : days) {
            if (earliest == null) {
                earliest = day;
                continue;
            }
            if (day.earlierThan(earliest)) {
                earliest = day;
            }
        }
        return earliest;
    }

    // 从给定的日期中找到最早一天.
    private static Day getEarliestDay(final Day[] days) {
        Day latest = null;
        for (Day day : days) {
            if (latest == null) {
                latest = day;
                continue;
            }
            if (latest.earlierThan(day)) {
                latest = day;
            }
        }
        return latest;
    }

    // Array lIntLunarDay is stored in the monthly day information in every year
    // from 1901 to 2100 of the lunar calendar,
    // The lunar calendar can only be 29 or 30 days every month, express with
    // 12(or 13) pieces of binary bit in one year,
    // it is 30 days for 1 form in the corresponding location , otherwise it is
    // 29 days
    private static final int[] iLunarMonthDaysTable = {
            0x4ae0, 0xa570, 0x5268, 0xd260, 0xd950, 0x6aa8, 0x56a0, 0x9ad0,
            0x4ae8, 0x4ae0, // 1910
            0xa4d8, 0xa4d0, 0xd250, 0xd548, 0xb550, 0x56a0, 0x96d0, 0x95b0, 0x49b8, 0x49b0, // 1920
            0xa4b0, 0xb258, 0x6a50, 0x6d40, 0xada8, 0x2b60, 0x9570, 0x4978, 0x4970, 0x64b0, // 1930
            0xd4a0, 0xea50, 0x6d48, 0x5ad0, 0x2b60, 0x9370, 0x92e0, 0xc968, 0xc950, 0xd4a0, // 1940
            0xda50, 0xb550, 0x56a0, 0xaad8, 0x25d0, 0x92d0, 0xc958, 0xa950, 0xb4a8, 0x6ca0, // 1950
            0xb550, 0x55a8, 0x4da0, 0xa5b0, 0x52b8, 0x52b0, 0xa950, 0xe950, 0x6aa0, 0xad50, // 1960
            0xab50, 0x4b60, 0xa570, 0xa570, 0x5260, 0xe930, 0xd950, 0x5aa8, 0x56a0, 0x96d0, // 1970
            0x4ae8, 0x4ad0, 0xa4d0, 0xd268, 0xd250, 0xd528, 0xb540, 0xb6a0, 0x96d0, 0x95b0, // 1980
            0x49b0, 0xa4b8, 0xa4b0, 0xb258, 0x6a50, 0x6d40, 0xada0, 0xab60, 0x9370, 0x4978, // 1990
            0x4970, 0x64b0, 0x6a50, 0xea50, 0x6b28, 0x5ac0, 0xab60, 0x9368, 0x92e0, 0xc960, // 2000
            0xd4a8, 0xd4a0, 0xda50, 0x5aa8, 0x56a0, 0xaad8, 0x25d0, 0x92d0, 0xc958, 0xa950, // 2010
            0xb4a0, 0xb550, 0xb550, 0x55a8, 0x4ba0, 0xa5b0, 0x52b8, 0x52b0, 0xa930, 0x74a8, // 2020
            0x6aa0, 0xad50, 0x4da8, 0x4b60, 0x9570, 0xa4e0, 0xd260, 0xe930, 0xd530, 0x5aa0, // 2030
            0x6b50, 0x96d0, 0x4ae8, 0x4ad0, 0xa4d0, 0xd258, 0xd250, 0xd520, 0xdaa0, 0xb5a0, // 2040
            0x56d0, 0x4ad8, 0x49b0, 0xa4b8, 0xa4b0, 0xaa50, 0xb528, 0x6d20, 0xada0, 0x55b0 // 2050
    };

    // Array iLunarLeapMonthTable preserves the lunar calendar leap month from
    // 1901 to 2050,
    // if it is 0 express not to have , every byte was stored for two years
    private static final char[] iLunarLeapMonthTable = {
            0x00, 0x50, 0x04, 0x00, 0x20, // 1910
            0x60, 0x05, 0x00, 0x20, 0x70, // 1920
            0x05, 0x00, 0x40, 0x02, 0x06, // 1930
            0x00, 0x50, 0x03, 0x07, 0x00, // 1940
            0x60, 0x04, 0x00, 0x20, 0x70, // 1950
            0x05, 0x00, 0x30, 0x80, 0x06, // 1960
            0x00, 0x40, 0x03, 0x07, 0x00, // 1970
            0x50, 0x04, 0x08, 0x00, 0x60, // 1980
            0x04, 0x0a, 0x00, 0x60, 0x05, // 1990
            0x00, 0x30, 0x80, 0x05, 0x00, // 2000
            0x40, 0x02, 0x07, 0x00, 0x50, // 2010
            0x04, 0x09, 0x00, 0x60, 0x04, // 2020
            0x00, 0x20, 0x60, 0x05, 0x00, // 2030
            0x30, 0xb0, 0x06, 0x00, 0x50, // 2040
            0x02, 0x07, 0x00, 0x50, 0x03 // 2050
    };

    // Array iSolarLunarTable stored the offset days
    // in New Year of solar calendar and lunar calendar from 1901 to 2050;
    private static final char[] iSolarLunarOffsetTable = {
            49, 38, 28, 46, 34, 24, 43, 32, 21, 40, // 1910
            29, 48, 36, 25, 44, 34, 22, 41, 31, 50, // 1920
            38, 27, 46, 35, 23, 43, 32, 22, 40, 29, // 1930
            47, 36, 25, 44, 34, 23, 41, 30, 49, 38, // 1940
            26, 45, 35, 24, 43, 32, 21, 40, 28, 47, // 1950
            36, 26, 44, 33, 23, 42, 30, 48, 38, 27, // 1960
            45, 35, 24, 43, 32, 20, 39, 29, 47, 36, // 1970
            26, 45, 33, 22, 41, 30, 48, 37, 27, 46, // 1980
            35, 24, 43, 32, 50, 39, 28, 47, 36, 26, // 1990
            45, 34, 22, 40, 30, 49, 37, 27, 46, 35, // 2000
            23, 42, 31, 21, 39, 28, 48, 37, 25, 44, // 2010
            33, 23, 41, 31, 50, 39, 28, 47, 35, 24, // 2020
            42, 30, 21, 40, 28, 47, 36, 25, 43, 33, // 2030
            22, 41, 30, 49, 37, 26, 44, 33, 23, 42, // 2040
            31, 21, 40, 29, 47, 36, 25, 44, 32, 22, // 2050
    };

    static boolean isSolarLeapYear(int iYear) {
        return ((iYear % 4 == 0) && (iYear % 100 != 0) || iYear % 400 == 0);
    }

    // The days in the month of solar calendar
    static int getSYearMonthDays(int iYear, int iMonth) {
        if ((iMonth <= 0) || (iMonth > 12)) {
            return 0;
        } else if ((iMonth == 2) && isSolarLeapYear(iYear)) {
            return 29;
        }
        return Constants.MONTH_DAYS[iMonth - 1];
    }

    // The offset days from New Year and the day when point out in solar
    // calendar
    static int iGetSNewYearOffsetDays(int iYear, int iMonth, int iDay) {
        int iOffsetDays = 0;

        for (int i = 1; i < iMonth; i++) {
            iOffsetDays += getSYearMonthDays(iYear, i);
        }
        iOffsetDays += iDay - 1;

        return iOffsetDays;
    }

    static int iGetLLeapMonth(int iYear) {
        char iMonth = iLunarLeapMonthTable[(iYear - 1901) / 2];

        if (iYear % 2 == 0)
            return (iMonth & 0x0f);
        else
            return (iMonth & 0xf0) >> 4;
    }

    static int iGetLMonthDays(int iYear, int iMonth) {
        int iLeapMonth = iGetLLeapMonth(iYear);
        if ((iMonth > 12) && (iMonth - 12 != iLeapMonth) || (iMonth < 0)) {
            // System.out.println("Wrong month, ^_^ , i think you are want a -1,
            // go to death!");
            return -1;
        }
        if (iMonth - 12 == iLeapMonth) {
            if ((iLunarMonthDaysTable[iYear - BASE_YEAR] & (0x8000 >> iLeapMonth)) == 0) {
                return 29;
            }
            return 30;
        }
        if ((iLeapMonth > 0) && (iMonth > iLeapMonth)) {
            iMonth++;
        }
        if ((iLunarMonthDaysTable[iYear - BASE_YEAR] & (0x8000 >> (iMonth - 1))) == 0) {
            return 29;
        }
        return 30;
    }

    // Days in this year of lunar calendar
    static int iGetLYearDays(int iYear) {
        int iYearDays = 0;
        int iLeapMonth = iGetLLeapMonth(iYear);

        for (int i = 1; i < 13; i++) {
            iYearDays += iGetLMonthDays(iYear, i);
        }
        if (iLeapMonth > 0) {
            iYearDays += iGetLMonthDays(iYear, iLeapMonth + 12);
        }
        return iYearDays;
    }

    static int iGetLNewYearOffsetDays(int iYear, int iMonth, int iDay) {
        int iOffsetDays = 0;
        int iLeapMonth = iGetLLeapMonth(iYear);

        if ((iLeapMonth > 0) && (iLeapMonth == iMonth - 12)) {
            iMonth = iLeapMonth;
            iOffsetDays += iGetLMonthDays(iYear, iMonth);
        }

        for (int i = 1; i < iMonth; i++) {
            iOffsetDays += iGetLMonthDays(iYear, i);
            if (i == iLeapMonth) {
                iOffsetDays += iGetLMonthDays(iYear, iLeapMonth + 12);
            }
        }
        iOffsetDays += iDay - 1;

        return iOffsetDays;
    }

    // 年月得到天干(甲乙丙丁.....)
    public static int getStemIndex(final int year, final int month, final int lunar_month) {
        final int stemCount = Constants.getStemCount();
        final int yearOffset = year - 1900 + 36 - 1;
        if ((lunar_month > 9) && (lunar_month > month)) {
            return yearOffset % stemCount;
        }
        return (yearOffset + 1) % stemCount;
    }

    // 年月得到地支(子丑寅卯......)
    public static int getBranchIndex(final int year, final int month, final int lunar_month) {
        final int branchCount = Constants.getBranchCount();
        final int yearOffset = year - 1900 + 36 - 1;
        if ((lunar_month > 9) && (lunar_month > month)) {
            return yearOffset % branchCount;
        }
        return (yearOffset + 1) % branchCount;
    }

    // 根据日期和时间，获得八字和阴阳五行五方四时信息.
    public static String baZiWuXing(final Context context, final Day day, final int hour_of_day) {
        final int yearStemIndex = getStemIndex(day.year, day.month, day.lunar_month);
        Constants.Stem yearStem = Constants.getStem(yearStemIndex);

        final int yearBranchIndex = getBranchIndex(day.year, day.month, day.lunar_month);
        Constants.Branch yearBranch = Constants.getBranch(yearBranchIndex);

        final Constants.Stem monthStem;
        final Constants.Branch monthBranch;
        if (day.lunar_month_leap) {
            monthStem = null;
            monthBranch = null;
        } else {
            monthStem = getLunarMonthStem(yearStem.getLabel(context), day.lunar_month);
            monthBranch = getLunarMonthBranch(day.lunar_month);
        }
        Constants.Stem dayStem = day.getDayStem();
        Constants.Branch dayBranch = day.getDayBranch();

        Constants.Branch timeBranch = Constants.getTimeBranch(hour_of_day);
        Constants.Stem timeStem = getTimeStem(dayStem.ordinal(), timeBranch.ordinal());

        StringBuilder sb = new StringBuilder();

        // 生辰八字.
        sb.append(yearStem.getLabel(context));
        sb.append(yearBranch.getLabel(context)).append(' ');

        if (monthStem == null) {
            sb.append(Constants._LEAP).append(Constants._LEAP).append(' ');
        } else {
            sb.append(monthStem.getLabel(context));
            sb.append(monthBranch.getLabel(context)).append(' ');
        }

        sb.append(dayStem.getLabel(context));
        sb.append(dayBranch.getLabel(context)).append(' ');

        sb.append(timeStem.getLabel(context));
        sb.append(timeBranch.getLabel(context));
        // 换行输出阴阳信息.
        sb.append('\n').append(yearStem.yinYang.getLabel(context));
        sb.append(yearBranch.yinYang.getLabel(context)).append(' ');

        if (monthStem == null) {
            sb.append(Constants._LEAP).append(Constants._LEAP).append(' ');
        } else {
            sb.append(monthStem.yinYang.getLabel(context));
            sb.append(monthBranch.yinYang.getLabel(context)).append(' ');
        }

        sb.append(dayStem.yinYang.getLabel(context));
        sb.append(dayBranch.yinYang.getLabel(context)).append(' ');

        sb.append(timeStem.yinYang.getLabel(context));
        sb.append(timeBranch.yinYang.getLabel(context)).append(' ');
        // 换行输出五行信息.
        sb.append('\n').append(yearStem.element.getLabel(context));
        sb.append(yearBranch.element.getLabel(context)).append(' ');

        if (monthStem == null) {
            sb.append(Constants._LEAP).append(Constants._LEAP).append(' ');
        } else {
            sb.append(monthStem.element.getLabel(context));
            sb.append(monthBranch.element.getLabel(context)).append(' ');
        }

        sb.append(dayStem.element.getLabel(context));
        sb.append(dayBranch.element.getLabel(context)).append(' ');

        sb.append(timeStem.element.getLabel(context));
        sb.append(timeBranch.element.getLabel(context)).append(' ');

        // 换行输出五方信息.
        sb.append('\n').append(yearStem.element.direction.getLabel(context));
        sb.append(yearBranch.element.direction.getLabel(context)).append(' ');

        if (monthStem == null) {
            sb.append(Constants._LEAP).append(Constants._LEAP).append(' ');
        } else {
            sb.append(monthStem.element.direction.getLabel(context));
            sb.append(monthBranch.element.direction.getLabel(context)).append(' ');
        }

        sb.append(dayStem.element.direction.getLabel(context));
        sb.append(dayBranch.element.direction.getLabel(context)).append(' ');

        sb.append(timeStem.element.direction.getLabel(context));
        sb.append(timeBranch.element.direction.getLabel(context)).append(' ');
        return sb.toString();
    }

    // 根据本月1号的星期几，可以知道上个月还需要看多少天在本月.
    public static int getRemainingDaysInLastMonth(final int year, final int month,
            final int firstDayOfWeek) {
        final GregorianCalendar gregorian_cal = new GregorianCalendar(year, month, 1);
        int remainingCount = gregorian_cal.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek/* Calendar.
         * SUNDAY */;
        if (remainingCount > 0) return remainingCount;
        return 7 + remainingCount;
    }

    public static int getMonthDayNum(final int year, final int month) {
        // Gregorian Calendar : MINUS 1, set to FIRST OF MONTH
        final GregorianCalendar gregorian_cal = new GregorianCalendar(year, month, 1);
        if (gregorian_cal.isLeapYear(year) && month == Calendar.FEBRUARY) {
            return Constants.MONTH_DAYS[month] + 1;
        }
        return Constants.MONTH_DAYS[month];
    }

    private final static int BASE_YEAR = 1901;

    private final static char[][] sPrincipleTermMap = {
            {
                21, 21, 21, 21, 21, 20, 21, 21, 21, 20, 20, 21, 21, 20, 20, 20, 20, 20, 20, 20,
                20, 19, 20, 20, 20, 19, 19, 20
            },
            {
                20, 19, 19, 20, 20, 19, 19, 19, 19, 19, 19, 19, 19, 18, 19, 19, 19, 18, 18, 19,
                19, 18, 18, 18, 18, 18, 18, 18
            },
            {
                21, 21, 21, 22, 21, 21, 21, 21, 20, 21, 21, 21, 20, 20, 21, 21, 20, 20, 20, 21,
                20, 20, 20, 20, 19, 20, 20, 20, 20
            },
            {
                20, 21, 21, 21, 20, 20, 21, 21, 20, 20, 20, 21, 20, 20, 20, 20, 19, 20, 20, 20,
                19, 19, 20, 20, 19, 19,
                19, 20, 20
            },
            {
                21, 22, 22, 22, 21, 21, 22, 22, 21, 21, 21, 22, 21, 21, 21, 21, 20, 21, 21, 21,
                20, 20, 21, 21, 20, 20,
                20, 21, 21
            },
            {
                22, 22, 22, 22, 21, 22, 22, 22, 21, 21, 22, 22, 21, 21, 21, 22, 21, 21, 21, 21,
                20, 21, 21, 21, 20, 20,
                21, 21, 21
            },
            {
                23, 23, 24, 24, 23, 23, 23, 24, 23, 23, 23, 23, 22, 23, 23, 23, 22, 22, 23, 23,
                22, 22, 22, 23, 22, 22,
                22, 22, 23
            },
            {
                23, 24, 24, 24, 23, 23, 24, 24, 23, 23, 23, 24, 23, 23, 23, 23, 22, 23, 23, 23,
                22, 22, 23, 23, 22, 22,
                22, 23, 23
            },
            {
                23, 24, 24, 24, 23, 23, 24, 24, 23, 23, 23, 24, 23, 23, 23, 23, 22, 23, 23, 23,
                22, 22, 23, 23, 22, 22,
                22, 23, 23
            },
            {
                24, 24, 24, 24, 23, 24, 24, 24, 23, 23, 24, 24, 23, 23, 23, 24, 23, 23, 23, 23,
                22, 23, 23, 23, 22, 22,
                23, 23, 23
            },
            {
                23, 23, 23, 23, 22, 23, 23, 23, 22, 22, 23, 23, 22, 22, 22, 23, 22, 22, 22, 22,
                21, 22, 22, 22, 21, 21,
                22, 22, 22
            },
            {
                22, 22, 23, 23, 22, 22, 22, 23, 22, 22, 22, 22, 21, 22, 22, 22, 21, 21, 22, 22,
                21, 21, 21, 22, 21, 21, 21, 21, 22
            }
    };

    private final static char[][] sPrincipleTermYear = {
            {
                13, 45, 81, 113, 149, 185, 201
            },
            {
                21, 57, 93, 125, 161, 193, 201
            },
            {
                21, 56, 88, 120, 152, 188, 200, 201
            },
            {
                21, 49, 81, 116, 144, 176, 200, 201
            },
            {
                17, 49, 77, 112, 140, 168, 200, 201
            },
            {
                28, 60, 88, 116, 148, 180, 200, 201
            },
            {
                25, 53, 84, 112, 144, 172, 200, 201
            },
            {
                29, 57, 89, 120, 148, 180, 200, 201
            },
            {
                17, 45, 73, 108, 140, 168, 200, 201
            },
            {
                28, 60, 92, 124, 160, 192, 200, 201
            },
            {
                16, 44, 80, 112, 148, 180, 200, 201
            },
            {
                17, 53, 88, 120, 156, 188, 200, 201
            }
    };

    private final static char[][] sSectionalTermMap = {
            {
                7, 6, 6, 6, 6, 6, 6, 6, 6, 5, 6, 6, 6, 5, 5, 6, 6, 5, 5, 5, 5, 5, 5, 5, 5, 4, 5,
                5
            },
            {
                5, 4, 5, 5, 5, 4, 4, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4, 3, 4, 4, 4, 3, 3, 4, 4, 3, 3,
                3
            },
            {
                6, 6, 6, 7, 6, 6, 6, 6, 5, 6, 6, 6, 5, 5, 6, 6, 5, 5, 5, 6, 5, 5, 5, 5, 4, 5, 5,
                5, 5
            },
            {
                5, 5, 6, 6, 5, 5, 5, 6, 5, 5, 5, 5, 4, 5, 5, 5, 4, 4, 5, 5, 4, 4, 4, 5, 4, 4, 4,
                4, 5
            },
            {
                6, 6, 6, 7, 6, 6, 6, 6, 5, 6, 6, 6, 5, 5, 6, 6, 5, 5, 5, 6, 5, 5, 5, 5, 4, 5, 5,
                5, 5
            },
            {
                6, 6, 7, 7, 6, 6, 6, 7, 6, 6, 6, 6, 5, 6, 6, 6, 5, 5, 6, 6, 5, 5, 5, 6, 5, 5, 5,
                5, 4, 5, 5, 5, 5
            },
            {
                7, 8, 8, 8, 7, 7, 8, 8, 7, 7, 7, 8, 7, 7, 7, 7, 6, 7, 7, 7, 6, 6, 7, 7, 6, 6, 6,
                7, 7
            },
            {
                8, 8, 8, 9, 8, 8, 8, 8, 7, 8, 8, 8, 7, 7, 8, 8, 7, 7, 7, 8, 7, 7, 7, 7, 6, 7, 7,
                7, 6, 6, 7, 7, 7
            },
            {
                8, 8, 8, 9, 8, 8, 8, 8, 7, 8, 8, 8, 7, 7, 8, 8, 7, 7, 7, 8, 7, 7, 7, 7, 6, 7, 7,
                7, 7
            },
            {
                9, 9, 9, 9, 8, 9, 9, 9, 8, 8, 9, 9, 8, 8, 8, 9, 8, 8, 8, 8, 7, 8, 8, 8, 7, 7, 8,
                8, 8
            },
            {
                8, 8, 8, 8, 7, 8, 8, 8, 7, 7, 8, 8, 7, 7, 7, 8, 7, 7, 7, 7, 6, 7, 7, 7, 6, 6, 7,
                7, 7
            },
            {
                7, 8, 8, 8, 7, 7, 8, 8, 7, 7, 7, 8, 7, 7, 7, 7, 6, 7, 7, 7, 6, 6, 7, 7, 6, 6, 6,
                7, 7
            }
    };

    private final static char[][] sSectionalTermYear = {
            {
                13, 49, 85, 117, 149, 185, 201, 250, 250
            },
            {
                13, 45, 81, 117, 149, 185, 201, 250, 250
            },
            {
                13, 48, 84, 112, 148, 184, 200, 201, 250
            },
            {
                13, 45, 76, 108, 140, 172, 200, 201, 250
            },
            {
                13, 44, 72, 104, 132, 168, 200, 201, 250
            },
            {
                5, 33, 68, 96, 124, 152, 188, 200, 201
            },
            {
                29, 57, 85, 120, 148, 176, 200, 201, 250
            },
            {
                13, 48, 76, 104, 132, 168, 196, 200, 201
            },
            {
                25, 60, 88, 120, 148, 184, 200, 201, 250
            },
            {
                16, 44, 76, 108, 144, 172, 200, 201, 250
            },
            {
                28, 60, 92, 124, 160, 192, 200, 201, 250
            },
            {
                17, 53, 85, 124, 156, 188, 200, 201, 250
            }
    };

    public static final String DAY_WINTER_COMING = "冬至";
    public static final String DAY_SUMMER_COMING = "夏至";
    public static final String DAY_AUTUMN = "立秋";

    private static final String[] sPrincipleTermNames = {
            "大寒", "雨水", "春分", "谷雨", "小满", DAY_SUMMER_COMING,
            "大暑", "处暑", "秋分", "霜降", "小雪", DAY_WINTER_COMING
    };

    private static final String[] sSectionalTermNames = {
            "小寒", "立春", "惊蛰", "清明", "立夏", "芒种", "小暑", DAY_AUTUMN, "白露", "寒露", "立冬", "大雪"
    };

    /**
     * 组合条件
     *
     * @param y
     * @param m
     * @return int
     */
    private static int sectionalTerm(int y, int m) {
        if (y < BASE_YEAR || y > 2100) {
            return 0;
        }
        int index = 0;
        int ry = y - BASE_YEAR + 1;
        while (ry >= sSectionalTermYear[m - 1][index]) {
            index++;
        }
        int term = sSectionalTermMap[m - 1][4 * index + ry % 4];
        if ((ry == 121) && (m == 4)) {
            term = 5;
        } else if ((ry == 132) && (m == 4)) {
            term = 5;
        } else if ((ry == 194) && (m == 6)) {
            term = 6;
        }
        return term;
    }

    /**
     * 法则
     *
     * @param y
     * @param m
     * @return int
     */
    private static int principleTerm(int y, int m) {
        if (y < BASE_YEAR || y > 2100) {
            return 0;
        }
        int index = 0;
        int ry = y - BASE_YEAR + 1;
        while (ry >= sPrincipleTermYear[m - 1][index]) {
            index++;
        }
        int term = sPrincipleTermMap[m - 1][4 * index + ry % 4];
        if ((ry == 171) && (m == 3)) {
            term = 21;
        } else if ((ry == 181) && (m == 5)) {
            term = 21;
        }
        return term;
    }

    // 计算节气
    public static String getDayTerms(final int year, final int month, final int day) {
        if (year < BASE_YEAR || year > 2100) {
            return null;
        }
        int sectionalTerm = sectionalTerm(year, month + 1);
        int principleTerm = principleTerm(year, month + 1);
        if (day == sectionalTerm) {
            return sSectionalTermNames[month];
        }
        if (day == principleTerm) {
            return sPrincipleTermNames[month];
        }
        return null;
    }

    // Date from 0 to 30 for 1 to 31.
    // Month from 0 to 11 for 1 to 12.
    public static Day getNextDay(final Day day) {
        int dayInMonth = day.day + 1;
        int month = day.month;
        int year = day.year;
        if (dayInMonth >= 29) {
            if (month == 2) { // 2月最后一天
                if (isLeapYear(year)) {
                    dayInMonth = 0;
                    month++;
                } else if (isBiggerMonth(month)) {
                    if (dayInMonth == 31) {
                        dayInMonth = 0;
                        if (month == 11) {// 12月最后一天
                            month = 0;
                            year++;
                        } else { // 其他大月最后一天
                            month++;
                        }
                    }
                } else if (dayInMonth == 30) {// 小月最后一天
                    dayInMonth = 0;
                    month++;
                }
            }
        }
        Day nextDay = new Day(year, month, dayInMonth);
        CalendarUtil.solarToLunar(nextDay);
        return nextDay;
    }

    /**
     * Return true if the given year is a leap year.
     *
     * @param year
     *            Gregorian year, with 0 == 1 BCE, -1 == 2 BCE, etc.
     * @return true if the year is a leap year
     */
    public static final boolean isLeapYear(int year) {
        // year&0x3 == year%4
        return ((year & 0x3) == 0) && ((year % 100 != 0) || (year % 400 == 0));
    }

    // month from 0 to 11 for 1 to 12.
    public static final boolean isBiggerMonth(int month) {
        if (month <= 6) return month % 2 == 0;
        return month % 2 > 0;
    }

    // 判断是否除夕: 春节前一天晚上
    // 需要阴历日期已经计算好.
    public static final boolean isSpringFestivalEve(Day day) {
        if (day.lunar_month != 11/* 阴历12月 */) return false;
        if (day.lunar_day < 26) return false; // 除夕只可能是12月28/29/30
        if (day.lunar_day == 30) return true; // 阴历12月30一定是除夕.
        Day nextDay = getNextDay(day);
        return (nextDay.lunar_month == 0/* 阴历1月 */ && nextDay.lunar_day == 1);
    }

    public static String getCount9Info(final Context context, final int _9DayNum) {
        switch (_9DayNum) {
            case 1: return context.getString(R.string._1_nine);
            case 2: return context.getString(R.string._2_nine);
            case 3: return context.getString(R.string._3_nine);
            case 4: return context.getString(R.string._4_nine);
            case 5: return context.getString(R.string._5_nine);
            case 6: return context.getString(R.string._6_nine);
            case 7: return context.getString(R.string._7_nine);
            case 8: return context.getString(R.string._8_nine);
            case 9: return context.getString(R.string._9_nine);
            default: return null;
        }
    }

    private static final String FORMAT_WRONG_DATE = "%d,%d";

    public static String getLunarDayText(final Context context, final Day day) {
        String dayTerm = getDayTerms(day.year, day.month, day.day);

        if (dayTerm == null) {
            try {
                int dayTextId = Utility.getLunarDayTextId(day.lunar_month, day.lunar_day);
                return context.getString(dayTextId);
            } catch (RuntimeException re) {
                return String.format(FORMAT_WRONG_DATE, day.lunar_month, day.lunar_day);
            }
        }
        return dayTerm;
    }

    // 获得该年冬至的日期
    public static Day getWinterComingDate(final Context context, final int year) {
        final int month = 11; // 冬至总在公历12月21/22/23中间.
        Day day;
        String dayTerm;
        for (int date = 19; date < 23; date++) {
            day = new Day(year, month, date);
            dayTerm = getLunarDayText(context, day);
            if (dayTerm.contains(DAY_WINTER_COMING)) {
                return day;
            }
        }
        return null;
    }

    // 数九.
    private static String getCount9DayInfo(final Context context, final Day today,
            final Day winterComingDate) {
        if (winterComingDate == null) return null;
        if (today.month > 2 && today.month < 11) return null;
        int dayCount = Utility.getDayCount(winterComingDate, today);
        if (dayCount < 0) return null;
        int nineDayNum = (dayCount / 9);
        if (nineDayNum >= 0 && nineDayNum < 9) {
            int dayIndex = dayCount % 9 + 1;
            return context.getString(R.string.format_x_9_fu_y,
                    getCount9Info(context, nineDayNum + 1), dayIndex);
        }
        return null;
    }

    // Format: label: startDate ～ endDate
    private static final String FORMAT_COUNT_INFO = "%s: %s ~ %s";

    private static String count9Info(final Context context, final int nineLabelResId,
            final Day startDate, final Day endDate) {
        return String.format(FORMAT_COUNT_INFO, context.getString(nineLabelResId),
                startDate.monthDayInfo(context), endDate.monthDayInfo(context));
    }

    public static String getYearCount9Info(final Context context, final int year) {
        final Day winterComingDate = getWinterComingDate(context, year);
        final Day _1_9_start = winterComingDate;
        final Day _1_9_end = Utility.getDate(_1_9_start, 8);
        final Day _2_9_start = Utility.getDate(winterComingDate, 9);
        final Day _2_9_end = Utility.getDate(_2_9_start, 8);
        final Day _3_9_start = Utility.getDate(winterComingDate, 18);
        final Day _3_9_end = Utility.getDate(_3_9_start, 8);
        final Day _4_9_start = Utility.getDate(winterComingDate, 27);
        final Day _4_9_end = Utility.getDate(_4_9_start, 8);
        final Day _5_9_start = Utility.getDate(winterComingDate, 36);
        final Day _5_9_end = Utility.getDate(_5_9_start, 8);
        final Day _6_9_start = Utility.getDate(winterComingDate, 45);
        final Day _6_9_end = Utility.getDate(_6_9_start, 8);
        final Day _7_9_start = Utility.getDate(winterComingDate, 54);
        final Day _7_9_end = Utility.getDate(_7_9_start, 8);
        final Day _8_9_start = Utility.getDate(winterComingDate, 63);
        final Day _8_9_end = Utility.getDate(_8_9_start, 8);
        final Day _9_9_start = Utility.getDate(winterComingDate, 72);
        final Day _9_9_end = Utility.getDate(_9_9_start, 8);

        StringBuilder sb = new StringBuilder();
        sb.append(count9Info(context, R.string._1_nine, _1_9_start, _1_9_end)).append('\n');
        sb.append(count9Info(context, R.string._2_nine, _2_9_start, _2_9_end)).append('\n');
        sb.append(count9Info(context, R.string._3_nine, _3_9_start, _3_9_end)).append('\n');
        sb.append(count9Info(context, R.string._4_nine, _4_9_start, _4_9_end)).append('\n');
        sb.append(count9Info(context, R.string._5_nine, _5_9_start, _5_9_end)).append('\n');
        sb.append(count9Info(context, R.string._6_nine, _6_9_start, _6_9_end)).append('\n');
        sb.append(count9Info(context, R.string._7_nine, _7_9_start, _7_9_end)).append('\n');
        sb.append(count9Info(context, R.string._8_nine, _8_9_start, _8_9_end)).append('\n');
        sb.append(count9Info(context, R.string._9_nine, _9_9_start, _9_9_end)).append('\n');
        return sb.toString();
    }

    // Format: label: Date
    private static final String FORMAT_SPECIAL_DAY = "%s: %s";

    public static String getYearCountFuInfo(final Context context, final int year) {
        final Day summerComingDate = getSummerComingDate(context, year);
        final Day _1stGengDayAfterSummerComingDay = WidgetMonthCalendar.getLunarDayByStem(
                summerComingDate, 6/* 庚为7，从0开始 */);
        final Day autumnDate = getAutumnDate(context, year);
        final Day _1_fuDate = Utility.get1FuDate(_1stGengDayAfterSummerComingDay);
        final Day _2_fuDate = Utility.get2FuDate(_1stGengDayAfterSummerComingDay);
        final Day _1_fuLastDate = Utility.getDate(_2_fuDate, -1);
        final Day _3_fuDate = Utility.get3FuDate(_1stGengDayAfterSummerComingDay, autumnDate);
        final Day _2_fuLastDate = Utility.getDate(_3_fuDate, -1);
        final Day _3_fuLastDate = Utility.getDate(_3_fuDate, 9);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(FORMAT_SPECIAL_DAY, context.getString(R.string.day_summer_comes),
                summerComingDate.md_stem_branch(context))).append('\n');
        sb.append(String.format(FORMAT_COUNT_INFO, context.getString(R.string._1_fu),
                _1_fuDate.getMonthDayLunarInfo(context), _1_fuLastDate.getMonthDayLunarInfo(context)))
        .append('\n');
        sb.append(String.format(FORMAT_COUNT_INFO, context.getString(R.string._2_fu),
                _2_fuDate.getMonthDayLunarInfo(context), _2_fuLastDate.getMonthDayLunarInfo(context)))
        .append('\n');
        sb.append(String.format(FORMAT_SPECIAL_DAY, context.getString(R.string.day_autumn),
                autumnDate.md_stem_branch(context))).append('\n');
        sb.append(String.format(FORMAT_COUNT_INFO, context.getString(R.string._3_fu),
                _3_fuDate.getMonthDayLunarInfo(context), _3_fuLastDate.getMonthDayLunarInfo(context)));
        return sb.toString();
    }

    // 数伏.
    private static String getCountFuDayInfo(final Context context, final Day today,
            final Day _1_gengDayAfterSummerComingDay, final Day autumnDate) {
        if (_1_gengDayAfterSummerComingDay == null) return null;
        if (today.month > 9 || today.month < 6) return null;
        Day _1_fu = Utility.get1FuDate(_1_gengDayAfterSummerComingDay);
        if (today.earlierThan(_1_fu)) return null;
        int dayCount = Utility.getDayCount(_1_fu, today);
        if (dayCount < 10) {
            return context.getString(R.string.format_x_9_fu_y, context.getString(R.string._1_fu),
                    dayCount + 1);
        }
        Day _3_fu = Utility.get3FuDate(_1_gengDayAfterSummerComingDay, autumnDate);
        if (today.greaterEquals(_3_fu)) {
            dayCount = Utility.getDayCount(_3_fu, today);
            if (dayCount >= 10) return null;
            return context.getString(R.string.format_x_9_fu_y, context.getString(R.string._3_fu),
                    dayCount + 1);
        }
        Day _2_fu = Utility.get2FuDate(_1_gengDayAfterSummerComingDay);
        dayCount = Utility.getDayCount(_2_fu, today);
        return context.getString(R.string.format_x_9_fu_y, context.getString(R.string._2_fu),
                dayCount + 1);
    }

    // 获得该年夏至的日期
    public static Day getSummerComingDate(final Context context, final int year) {
        Day day;
        String dayTerm;
        int dayCount;
        // 夏至总在公历6-7月.
        for (int month = 5; month < 7; month++) {
            dayCount = CalendarUtil.getMonthDayNum(year, month);
            for (int i = 0; i < dayCount; i++) {
                day = new Day(year, month, i + 1);
                dayTerm = getLunarDayText(context, day);
                if (dayTerm.contains(CalendarUtil.DAY_SUMMER_COMING)) {
                    return day;
                }
            }
        }
        return null;
    }

    // 获得该年立秋的日期
    public static Day getAutumnDate(final Context context, final int year) {
        Day day;
        String dayTerm;
        int dayCount;
        // 立秋总在公历7-8-9月.
        for (int month = 6; month < 9; month++) {
            dayCount = getMonthDayNum(year, month);
            for (int i = 0; i < dayCount; i++) {
                day = new Day(year, month, i + 1);
                dayTerm = getLunarDayText(context, day);
                if (dayTerm.contains(CalendarUtil.DAY_AUTUMN)) {
                    return day;
                }
            }
        }
        return null;
    }

    // 数九数伏.
    public static String getCountDayInfo(final Context context, final Day today,
            final Day winterComingDate, final Day _1stGengDayAfterSummerComingDate,
            final Day autumnDate) {
        String count9DayInfo = getCount9DayInfo(context, today, winterComingDate);
        if (!TextUtils.isEmpty(count9DayInfo)) return count9DayInfo;

        String countFuDayInfo = getCountFuDayInfo(context, today, _1stGengDayAfterSummerComingDate,
                autumnDate);
        return countFuDayInfo;
    }

    private static final String[][][] sMappingLunarMonthStems = {
            {{"甲", "己"}, {"丙"}},
            {{"乙", "庚"}, {"戊"}},
            {{"丙", "辛"}, {"庚"}},
            {{"丁", "壬"}, {"壬"}},
            {{"戊", "癸"}, {"甲"}},
    };

    public static final String[] sStems = {
            "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸",
    };

    public static final String[] sBranches = {
            "子"/* 11 */, "丑"/* 12 */, "寅"/* 1 */, "卯",
            "辰", "巳", "午", "未", "申", "酉", "戌", "亥"/* 10 */,
    };

    private static int findStemIndex(final String stem) {
        for (int i = 0; i < sStems.length; i++) {
            if (TextUtils.equals(stem, sStems[i])) return i;
        }
        return -1;
    }

    private static int getLunarMonth1StemIndex(final String yearStem, final int lunar_month) {
        for (String[][] stemMapping : sMappingLunarMonthStems) {
            for (String stem : stemMapping[0]) {
                if (TextUtils.equals(stem, yearStem)) {
                    return findStemIndex(stemMapping[1][0]);
                }
            }
        }
        return -1;
    }

    private static Constants.Stem getLunarMonthStem(final String yearStem, final int lunar_month) {
        final int _1stLunarMonthStemIndex = getLunarMonth1StemIndex(yearStem, lunar_month);
        if (_1stLunarMonthStemIndex < 0) return null;
        return Constants.getStem((_1stLunarMonthStemIndex + lunar_month) % sStems.length);
    }

    private static Constants.Branch getLunarMonthBranch(final int lunar_month) {
        // The first lunar month branch is always "寅" which has index 2 in the constant array.
        return Constants.getBranch((lunar_month + 2) % sBranches.length);
    }

    // 干支纪月
    public static String getLunarMonthStemBranch(final String yearStem, final int lunar_month) {
        Constants.Stem lunarMonthStem = getLunarMonthStem(yearStem, lunar_month);
        if (lunarMonthStem == null) return null;
        Constants.Branch lunarMonthBranch = getLunarMonthBranch(lunar_month);
        StringBuilder sb = new StringBuilder();
        sb.append(sStems[lunarMonthStem.ordinal()]);
        sb.append(sBranches[lunarMonthBranch.ordinal()]);
        return sb.toString();
    }

    // 干支纪时
    // 每日时间干支表
    // 时间地支-子--丑--寅--卯--辰--巳--午--未--申--酉--戌--亥
    // 日期天干
    // 甲己----甲--乙--丙--丁--戊--己--庚--辛--壬--癸--甲--乙
    // 乙庚----丙--丁--戊--己--庚--辛--壬--癸--甲--乙--丙--丁
    // 丙辛----戊--己--庚--辛--壬--癸--甲--乙--丙--丁--戊--己
    // 丁壬----庚--辛--壬--癸--甲--乙--丙--丁--戊--己--庚--辛
    // 戊癸----壬--癸--甲--乙--丙--丁--戊--己--庚--辛--壬--癸
    // 如甲日亥时，查上表得时干为乙，即乙亥时。乙亥时过后即丙子时，进入乙日。

    private static enum LunarDayTimeStemBranch {
        JiaJi(0, 5, new int[12]),
        YiGeng(1, 6, new int[12]),
        BingXin(2, 7, new int[12]),
        DingRen(3, 8, new int[12]),
        WuGui(4, 9, new int[12]);

        public final int stem0, stem1;
        public final int[] stemArray;

        private LunarDayTimeStemBranch(int stem0, int stem1, int[] stemArray) {
            this.stem0 = stem0;
            this.stem1 = stem1;
            this.stemArray = stemArray;
        }

        private static final String FORMAT_TO_STRING = "%s%s  %s";

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int stemIndex : stemArray) {
                sb.append(sStems[stemIndex]);
            }
            return String.format(FORMAT_TO_STRING, sStems[stem0], sStems[stem1], sb.toString());
        }
    }

    private static void initLunarDayTimeStemBranch() {
        int offset = 0;
        for (LunarDayTimeStemBranch mapping : LunarDayTimeStemBranch.values()) {
            for (int i = 0; i < mapping.stemArray.length; i++) {
                mapping.stemArray[i] = (offset++) % sStems.length;
            }
            // Log.d("WB", mapping.toString());
        }
    }

    static {
        initLunarDayTimeStemBranch();
    }

    private static String getDayTimeStem(final int dayStemIndex, final int timeBranchIndex) {
        for (LunarDayTimeStemBranch mapping : LunarDayTimeStemBranch.values()) {
            if (dayStemIndex == mapping.stem0 || dayStemIndex == mapping.stem1) {
                return sStems[mapping.stemArray[timeBranchIndex]];
            }
        }
        return null;
    }

    public static Constants.Stem getTimeStem(final int dayStemIndex, final int timeBranchIndex) {
        for (LunarDayTimeStemBranch mapping : LunarDayTimeStemBranch.values()) {
            if (dayStemIndex == mapping.stem0 || dayStemIndex == mapping.stem1) {
                return Constants.getStem(mapping.stemArray[timeBranchIndex]);
            }
        }
        return null;
    }

    // 干支纪时
    public static String getChineseTime(final Context context, final int dayStemIndex) {
        final Calendar cal = Calendar.getInstance();
        final int hour_of_day = cal.get(Calendar.HOUR_OF_DAY);

        return getChineseTime(context, dayStemIndex, hour_of_day);
    }

    public static String getChineseTime(final Context context, final int dayStemIndex,
            final int hour_of_day) {
        String formatChineseTime = context.getString(R.string.format_cn_time);
        if (TextUtils.isEmpty(formatChineseTime)) return null;

        return getChineseTime(context, dayStemIndex, hour_of_day, formatChineseTime);
    }

    // 以下方法是为了在locale非CN时也可以获得干支时间.
    private static final String FORMAT_CHINESE_TIME = "%s%s时";
    public static String getChineseTime2(final Context context, final int dayStemIndex,
            final int hour_of_day) {
        return getChineseTime(context, dayStemIndex, hour_of_day, FORMAT_CHINESE_TIME);
    }

    private static String getChineseTime(final Context context, final int dayStemIndex,
            final int hour_of_day, final String timeFormat) {
        final Constants.Branch time_branch = Constants.getTimeBranch(hour_of_day);
        final String timeBranch = time_branch.getLabel(context);
        final String timeStem = getDayTimeStem(dayStemIndex, time_branch.ordinal());

        return String.format(timeFormat, timeStem, timeBranch);
    }

    public static int getWeekCountInYear(final int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.setMinimalDaysInFirstWeek(4);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return cal.getActualMaximum(Calendar.WEEK_OF_YEAR);
    }

    public static int getWeekCountBetweenDates(final Day startDay, final Day endDay) {
        int dayCount = Utility.getDayCount(startDay, endDay);
        int weekCount = dayCount / 7;
        return weekCount + 1;
    }
}
