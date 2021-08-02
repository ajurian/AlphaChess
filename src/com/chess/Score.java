package com.chess;

public class Score {

    public static int makeScore(int mg, int eg) {
        return (int) ((long) eg << 16) + mg;
    }
    public static int mgValue(int score) {
        return (short) score;
    }
    public static int egValue(int score) {
        return (short) ((long) (score + 0x8000) >> 16);
    }
    public static void printScore(int score) {
        System.out.println("S(" + mgValue(score) + ", " + egValue(score) + ")");
    }


    public static int max(int a, int b) {
        return (mgValue(a) > mgValue(b) ? a : b);
    }


}
