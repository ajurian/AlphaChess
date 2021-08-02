package com.chess;

public enum PieceType {

    PAWN("P"),
    KNIGHT("N"),
    BISHOP("B"),
    ROOK("R"),
    QUEEN("Q"),
    KING("K"),
    NONE("NONE");


    public static final PieceType[] allPieceTypes = PieceType.values();
    private final String symbol;


    PieceType(String symbol) {
        this.symbol = symbol;
    }
    public String symbol() {
        return symbol;
    }


}
