package com.chess;

public enum Side {

    WHITE,
    BLACK;


    public static final Side[] allSides = values();
    public Side flip() { return allSides[ordinal() ^ 1]; }


}
