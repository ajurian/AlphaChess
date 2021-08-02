package com.chess;

public enum Piece {

    WHITE_PAWN("P"),
    WHITE_KNIGHT("N"),
    WHITE_BISHOP("B"),
    WHITE_ROOK("R"),
    WHITE_QUEEN("Q"),
    WHITE_KING("K"),
    BLACK_PAWN("p"),
    BLACK_KNIGHT("n"),
    BLACK_BISHOP("b"),
    BLACK_ROOK("r"),
    BLACK_QUEEN("q"),
    BLACK_KING("k"),
    NONE(".");


    public static final Piece[] allPieces = Piece.values();
    private final String symbol;
    private final PieceType pieceType;
    private final Side pieceSide;


    Piece(String symbol) {
        this.symbol = symbol;
        pieceType = ordinal() == 12 ? null : PieceType.allPieceTypes[ordinal() % 6];
        pieceSide = ordinal() == 12 ? null : Side.allSides[ordinal() / 6];
    }


    public String symbol() {
        return symbol;
    }
    public PieceType pieceType() {
        return pieceType;
    }
    public Side pieceSide() {
        return pieceSide;
    }


    public static Piece encodePiece(Side side, PieceType pieceType) {
        return allPieces[pieceType.ordinal() + side.ordinal() * 6];
    }
    public static Piece encodePiece(String symbol) {
        for (Piece piece : allPieces) {
            if (piece.symbol.equals(symbol))
                return piece;
        }
        return null;
    }


}
