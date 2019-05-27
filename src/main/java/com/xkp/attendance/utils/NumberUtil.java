package com.xkp.attendance.utils;

import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.util.StringUtil;

import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * @author yangmeiliang
 */
public final class NumberUtil {
    private static final Pattern CLEAR_PATTERN = Pattern.compile("[^0-9]");
    private static final NumberFormat DEFAULT_FLOAT_FORMAT = NumberFormat.getInstance();
    private static Random RANDOM = new Random(System.currentTimeMillis());

    static {
        DEFAULT_FLOAT_FORMAT.setMaximumFractionDigits(1);
        DEFAULT_FLOAT_FORMAT.setMinimumFractionDigits(0);
        DEFAULT_FLOAT_FORMAT.setGroupingUsed(false);
    }

    public static String format(double value) {
        return DEFAULT_FLOAT_FORMAT.format(value);
    }

    public static String format(double value, int fractionDigits) {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(fractionDigits);
        numberFormat.setMinimumFractionDigits(fractionDigits);
        numberFormat.setGroupingUsed(false);
        return numberFormat.format(value);
    }

    public static boolean between(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static boolean between(long value, long min, long max) {
        return value >= min && value <= max;
    }

    /**
     * <p>Checks whether the <code>String</code> contains only
     * digit characters.</p>
     * <p>
     * <p>判断参数value是否仅包含数字字符，null或空字符串返回false，负数(如'-3')、小数(如'3.14')返回false.</p>
     * <p>
     * <p>判断是否是合法数字，请使用isParsable{@code isParsable} 方法.</p>
     * <p>
     * <p><code>Null</code> and empty String will return
     * <code>false</code>.</p>
     *
     * @param value the <code>String</code> to check
     * @return <code>true</code> if str contains only Unicode numeric
     */
    public static boolean is(String value) {
        if (value != null && value.length() > 0) {
            char[] chars = value.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (!Character.isDigit(chars[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * <p>判断是否是合法数字. null或空字符串返回false，负数(如'-3')、小数(如'3.14')返回true.</p>
     */
    public static <T extends Number> boolean isParsable(String value, Class<T> targetClass) {
        boolean parsable = true;
        try {
            NumberUtils.parseNumber(value, targetClass);
        } catch (Exception e) {
            parsable = false;
        }
        return parsable;
    }

    public static String getPercent(int value, int total) {
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(2);
        if (value <= 0 || total <= 0) {
            nf.format(0);
        }

        return nf.format(value * 1.0 / total);
    }

    public static boolean not(String value) {
        return !is(value);
    }

    /**
     * Parse the given {@code value} into a {@link Number} instance of the given
     * target class, using the corresponding {@code decode} / {@code valueOf} method.
     * <p>Trims the input {@code String} before attempting to parse the number.
     * <p>Supports numbers in hex format (with leading "0x", "0X", or "#") as well.
     * <p>
     * <p>按给定的targetClass将value转换为对应的实例.</p>
     * <p>支持"0x", "0X", or "#"等十六进制前缀.</p>
     */
    public static <T extends Number> T parseNumber(String value, Class<T> targetClass) {
        return NumberUtils.parseNumber(value, targetClass);
    }

    public static double parseDoubleQuietly(Object value) {
        return parseDoubleQuietly(value, 0);
    }

    public static double parseDoubleQuietly(Object value, double def) {
        if (value != null) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            try {
                return parseNumber(value.toString(), Double.class);
            } catch (IllegalArgumentException e) {
                /* do nothing. */
            }
        }
        return def;
    }

    public static int parseIntQuietly(Object value) {
        return parseIntQuietly(value, 0);
    }

    public static int parseIntQuietly(Object value, int def) {
        if (value != null) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            try {
                return Integer.valueOf(value.toString());
            } catch (Throwable e) {
            }
        }

        return def;
    }

    public static int parseIntQuietlyAfterClear(Object value, int def) {
        if (value != null) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            try {
                return Integer.valueOf(CLEAR_PATTERN.matcher(value.toString()).replaceAll(""));
            } catch (Throwable e) {
            }
        }

        return def;
    }

    public static long parseLongQuietly(Object value, long def) {
        if (value != null) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            try {
                return Long.valueOf(value.toString());
            } catch (Throwable e) {
            }
        }

        return def;
    }

    public static int nextInt() {
        return RANDOM.nextInt();
    }

    public static int nextInt(int n) {
        return RANDOM.nextInt(n);
    }

    public static final int random(int length) {
        if (length > 0 && length < 10) {
            int sum = 0;
            int n = 1;
            int r = 0;
            for (int i = 1; i < length; i++) {
                r = NumberUtil.nextInt(10);
                sum += r * n;
                n = n * 10;
            }
            r = 1 + NumberUtil.nextInt(9);
            sum += r * n;
            return sum;
        }
        return 0;
    }

    public static int getIntByPosition(int value, int index) {
        if (value <= 0) {
            return 0;
        }

        if (index <= 0) {
            index = 1;
        }

        String strValue = String.valueOf(value);
        if (strValue.length() < index) {
            return 0;
        }

        return Integer.valueOf("" + strValue.charAt(strValue.length() - index));
    }

    public static int setIntByPosition(int source, int index, int value) {
        if (index <= 0) {
            index = 1;
        }

        if (value <= 9 && value >= 0) {
            StringBuilder buff = new StringBuilder(String.valueOf(source));
            if (buff.length() >= index) {
                buff.setCharAt(buff.length() - index, Integer.valueOf(value).toString().charAt(0));
            } else {
                int maxIndex = index - buff.length() - 1;
                for (int i = 0; i < maxIndex; i++) {
                    buff.insert(0, "0");
                }

                buff.insert(0, value);
            }

            return Integer.valueOf(buff.toString());
        }

        return source;
    }

    public static boolean isEQ(int source, int index, int value) {
        return getIntByPosition(source, index) == value;
    }

    public static boolean isGT(int source, int index, int value) {
        return getIntByPosition(source, index) > value;
    }

    public static boolean isGE(int source, int index, int value) {
        return getIntByPosition(source, index) >= value;
    }

    public static int[] toArray(Collection<Integer> ints) {
        if (ints == null || ints.isEmpty()) {
            return new int[0];
        }

        int[] result = new int[ints.size()];
        int idx = 0;
        for (Integer _int : ints) {
            if (_int != null) {
                result[idx++] = _int;
            }
        }
        return result;
    }

    public static byte[] toBytes(long value) {
        byte[] bytes = new byte[8];
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.putLong(value);
        return bytes;
    }

    public static byte[] toBytes(int value) {
        byte[] bytes = new byte[4];
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.putInt(value);
        return bytes;
    }

    public static long toLong(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getLong();
    }

    public static int toInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static boolean isPositive(Integer value) {
        return value != null && value > 0;
    }

    public static boolean isPositive(Long value) {
        return value != null && value > 0;
    }

    public static boolean isPositive(int value) {
        return value > 0;
    }

    public static boolean isPositive(long value) {
        return value > 0;
    }

    public static boolean isNegative(int value) {
        return value < 0;
    }

    public static boolean nullOrEqualsTo(Integer source, Integer target) {
        return source == null || source.equals(target);
    }

    public static String padding(long value, int length) {
        return padding(value, length, '0');
    }

    public static String padding(long value, int length, char padding) {
        String str = String.valueOf(value);
        int size = length - str.length();
        if (size <= 0) {
            return str;
        }

        StringBuilder buff = new StringBuilder(length);
        for (int i = 0; i < size; i++) {
            buff.append(padding);
        }
        buff.append(str);
        return buff.toString();
    }

    /**
     * hi 和lo 必须是正整数
     */
    public static long merge(int hi, int lo) {
        return (((long) hi) << 32) | (long) lo;
    }

    public static int hi(long value) {
        return (int) (value >> 32);
    }

    public static int lo(long value) {
        return (int) (value & -1);
    }

    public static boolean in(int value, int[] ints) {
        for (int i : ints) {
            if (i == value) {
                return true;
            }
        }
        return false;
    }
}

