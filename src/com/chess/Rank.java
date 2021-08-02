package com.chess;

public enum Rank {

    RANK_1, RANK_2, RANK_3, RANK_4,
    RANK_5, RANK_6, RANK_7, RANK_8,
    NONE;


    private final long bitboard = 0xFFL << (8 * ordinal());
    public long bitboard() {
        return bitboard;
    }
    public int distance(Rank other) {
        return rankDistance[ordinal()][other.ordinal()];
    }


    private static final Rank[] allRanks = values();
    private static final int[][] rankDistance = new int[64][64];


    static {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Rank r1 = allRanks[i];
                Rank r2 = allRanks[j];
                rankDistance[i][j] = Math.abs(r2.ordinal() - r1.ordinal());
            }
        }
    }


    public static Rank rankAt(int index) {
        return allRanks[index];
    }
    public static Rank relativeRank(Side side, Square square) {
        return relativeRank(side, square.rank());
    }
    public static Rank relativeRank(Side side, Rank rank) {
        return allRanks[rank.ordinal() ^ (side.ordinal() * 7)];
    }
    public static int edgeDistance(Rank rank) {
        return Math.min(rank.ordinal(), RANK_8.ordinal() - rank.ordinal());
    }


}
