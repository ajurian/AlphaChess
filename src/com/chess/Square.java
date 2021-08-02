package com.chess;

public enum Square {

    A1, B1, C1, D1, E1, F1, G1, H1,
    A2, B2, C2, D2, E2, F2, G2, H2,
    A3, B3, C3, D3, E3, F3, G3, H3,
    A4, B4, C4, D4, E4, F4, G4, H4,
    A5, B5, C5, D5, E5, F5, G5, H5,
    A6, B6, C6, D6, E6, F6, G6, H6,
    A7, B7, C7, D7, E7, F7, G7, H7,
    A8, B8, C8, D8, E8, F8, G8, H8,
    NONE;


    private final long bitboard = 1L << ordinal();
    private final boolean lightSquare = (Bitboard.lightSquares & bitboard) != 0L;
    private final Rank rank = Rank.rankAt(ordinal() / 8);
    private final File file = File.fileAt(ordinal() % 8);


    public boolean isLightSquare() {
        return lightSquare;
    }
    public long bitboard() {
        return bitboard;
    }
    public Rank rank() {
        return rank;
    }
    public File file() {
        return file;
    }
    public int distance(Square other) {
        return squareDistance[ordinal()][other.ordinal()];
    }


    private static final Square[] allSquares = values();
    private static final int[][] squareDistance = new int[64][64];


    static {
        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 64; j++) {
                Square s1 = allSquares[i];
                Square s2 = allSquares[j];
                squareDistance[i][j] = Math.max(s1.rank.distance(s2.rank),
                                                s1.file.distance(s2.file));
            }
        }
    }


    public static Square squareAt(int index) {
        return allSquares[index];
    }
    public static Square encodeSquare(Rank rank, File file) {
        return allSquares[rank.ordinal() * 8 + file.ordinal()];
    }
    public static Square relativeSquare(Side side, Square square) {
        return encodeSquare(Rank.relativeRank(side, square), square.file);
    }


}
