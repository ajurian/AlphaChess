package com.chess;

public enum CastlingRight {

    NONE(0),
    WHITE_KING(1),
    WHITE_QUEEN(2),
    BLACK_KING(4),
    BLACK_QUEEN(8);


    private final int bit;
    CastlingRight(int bit) {
        this.bit = bit;
    }
    public int bit() {
        return bit;
    }


}
