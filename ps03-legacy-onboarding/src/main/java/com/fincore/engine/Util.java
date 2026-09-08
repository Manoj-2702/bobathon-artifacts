package com.fincore.engine;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Util {

    public static String f1(long v) {
        String s = Long.toString(v);
        StringBuffer b = new StringBuffer();
        int x = s.length() % 3;
        for (int i = 0; i < s.length(); i++) {
            if (i != 0 && (i - x) % 3 == 0) b.append(',');
            b.append(s.charAt(i));
        }
        return b.toString();
    }

    public static String f2(double d, int sc) {
        long n = (long)(d * Math.pow(10, sc));
        String s = Long.toString(n);
        if (s.length() <= sc) {
            while (s.length() <= sc) s = "0" + s;
        }
        return s.substring(0, s.length() - sc) + "." + s.substring(s.length() - sc);
    }

    public static long d2l(String s) {
        try {
            SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
            Date d = f.parse(s);
            return d.getTime();
        } catch (Exception e) {
            return -1;
        }
    }

    public static String l2d(long t) {
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
        return f.format(new Date(t));
    }

    public static int daysBetween(long a, long b) {
        long d = b - a;
        if (d < 0) d = -d;
        return (int)(d / 86400000L);
    }

    public static int monthsBetween(long a, long b) {
        Calendar ca = Calendar.getInstance();
        Calendar cb = Calendar.getInstance();
        ca.setTimeInMillis(a);
        cb.setTimeInMillis(b);
        int y = cb.get(Calendar.YEAR) - ca.get(Calendar.YEAR);
        int m = cb.get(Calendar.MONTH) - ca.get(Calendar.MONTH);
        return y * 12 + m;
    }

    public static long addMonths(long t, int m) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(t);
        c.add(Calendar.MONTH, m);
        return c.getTimeInMillis();
    }

    public static String b64e(String s) {
        byte[] b = s.getBytes();
        char[] t = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
        StringBuffer r = new StringBuffer();
        int i = 0;
        while (i < b.length) {
            int v = (b[i] & 0xff) << 16;
            if (i + 1 < b.length) v |= (b[i + 1] & 0xff) << 8;
            if (i + 2 < b.length) v |= (b[i + 2] & 0xff);
            r.append(t[(v >> 18) & 0x3f]);
            r.append(t[(v >> 12) & 0x3f]);
            r.append(i + 1 < b.length ? t[(v >> 6) & 0x3f] : '=');
            r.append(i + 2 < b.length ? t[v & 0x3f] : '=');
            i += 3;
        }
        return r.toString();
    }

    public static String b64d(String s) {
        String a = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        byte[] out = new byte[(s.length() * 3) / 4];
        int p = 0;
        for (int i = 0; i < s.length(); i += 4) {
            int v = 0;
            for (int j = 0; j < 4; j++) {
                char c = s.charAt(i + j);
                int x = (c == '=') ? 0 : a.indexOf(c);
                v = (v << 6) | x;
            }
            out[p++] = (byte)((v >> 16) & 0xff);
            if (s.charAt(i + 2) != '=') out[p++] = (byte)((v >> 8) & 0xff);
            if (s.charAt(i + 3) != '=') out[p++] = (byte)(v & 0xff);
        }
        return new String(out, 0, p);
    }

    public static String padL(String s, int w, char c) {
        if (s == null) s = "";
        StringBuffer b = new StringBuffer();
        for (int i = s.length(); i < w; i++) b.append(c);
        b.append(s);
        return b.toString();
    }

    public static String padR(String s, int w, char c) {
        if (s == null) s = "";
        StringBuffer b = new StringBuffer(s);
        while (b.length() < w) b.append(c);
        return b.toString();
    }

    public static String trim2(String s) {
        if (s == null) return "";
        int a = 0;
        int z = s.length() - 1;
        while (a <= z && (s.charAt(a) == ' ' || s.charAt(a) == '\t'
                || s.charAt(a) == '\r' || s.charAt(a) == '\n')) a++;
        while (z >= a && (s.charAt(z) == ' ' || s.charAt(z) == '\t'
                || s.charAt(z) == '\r' || s.charAt(z) == '\n')) z--;
        return s.substring(a, z + 1);
    }

    public static boolean isNum(String s) {
        if (s == null || s.length() == 0) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                if (i == 0 && c == '-') continue;
                if (c == '.') continue;
                return false;
            }
        }
        return true;
    }

    public static double safeD(String s) {
        try {
            return Double.parseDouble(trim2(s));
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static int safeI(String s) {
        try {
            return Integer.parseInt(trim2(s));
        } catch (Exception e) {
            return 0;
        }
    }

    public static String fmt0(String v, String dflt) {
        String r = trim2(v);
        if (r.length() == 0) return dflt;
        return r;
    }

    public static String concatArr(String[] a, String sep) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < a.length; i++) {
            if (i > 0) b.append(sep);
            b.append(a[i] == null ? "" : a[i]);
        }
        return b.toString();
    }

    public static String[] splitF(String s, char d) {
        int cnt = 1;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == d) cnt++;
        }
        String[] r = new String[cnt];
        int p = 0;
        int last = 0;
        for (int i = 0; i <= s.length(); i++) {
            if (i == s.length() || s.charAt(i) == d) {
                r[p++] = s.substring(last, i);
                last = i + 1;
            }
        }
        return r;
    }

    public static String fmtAcct(long id) {
        String s = padL(Long.toString(id), 12, '0');
        return s.substring(0, 4) + "-" + s.substring(4, 8) + "-" + s.substring(8, 12);
    }

    public static int crc8(byte[] data) {
        int c = 0;
        for (int i = 0; i < data.length; i++) {
            c ^= (data[i] & 0xff);
            for (int j = 0; j < 8; j++) {
                if ((c & 0x80) != 0) {
                    c = (c << 1) ^ 0x07;
                } else {
                    c <<= 1;
                }
                c &= 0xff;
            }
        }
        return c;
    }
}
