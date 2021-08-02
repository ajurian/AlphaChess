package com.chess;

public class SearchConstants {

    public static final int maxPly = 128;
    public static final int maxMoves = 256;
    public static final int infinity = 31000;
    public static final int mateValue = 30000;
    public static final int winValue = 10000;
    public static final int winValueInMaxPly = mateValue - 2 * maxPly;
    public static final int lossValueInMaxPly = -winValueInMaxPly;
    public static final int BOUND_EXACT = 0, BOUND_UPPER = 1, BOUND_LOWER = 2;


    public static final int[] historyPruningThreshold = {7000, 5994, 5087, 5724};
    public static final int[] futilityMarginParent = {100, 180, 260, 340, 420, 500};


}
