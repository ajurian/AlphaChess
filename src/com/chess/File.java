package com.chess;

public enum File {

    FILE_A, FILE_B, FILE_C, FILE_D,
    FILE_E, FILE_F, FILE_G, FILE_H,
    NONE;


    private final long bitboard = 0x101010101010101L << ordinal();
    public long bitboard() {
        return bitboard;
    }
    public int distance(File other) {
        return fileDistance[ordinal()][other.ordinal()];
    }


    private static final File[] allFiles = values();
    private static final int[][] fileDistance = new int[64][64];


    static {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                File f1 = allFiles[i];
                File f2 = allFiles[j];
                fileDistance[i][j] = Math.abs(f2.ordinal() - f1.ordinal());
            }
        }
    }


    public static File fileAt(int index) {
        return allFiles[index];
    }
    public static File relativeFile(Side side, Square square) {
        return relativeFile(side, square.file());
    }
    public static File relativeFile(Side side, File file) {
        return allFiles[file.ordinal() ^ (side.ordinal() * 7)];
    }
    public static int edgeDistance(File file) {
        return Math.min(file.ordinal(), File.FILE_H.ordinal() - file.ordinal());
    }


}
