package com.chess;

import java.util.Arrays;
import java.util.Objects;
import static com.chess.Move.*;

public class Backup {

    private Piece capturedPiece;
    private Square capturedSquare;
    private final Side sideToMove;
    private final Square enPassant;
    private final Square enPassantTarget;
    private final long hashKey;
    private final int castlingRight;
    private final int fiftyMove;
    private final int moveCounter;
    private final int gamePly;
    private final int move;


    public Backup(Board board, int move) {
        capturedPiece = Piece.NONE;
        capturedSquare = Square.NONE;
        sideToMove = board.sideToMove();
        enPassant = board.enPassant();
        enPassantTarget = board.enPassantTarget();
        hashKey = board.hashKey();
        castlingRight = board.castlingRight();
        fiftyMove = board.fiftyMove();
        moveCounter = board.moveCounter();
        gamePly = board.gamePly();
        this.move = move;
    }


    public void setCapturedPiece(Piece capturedPiece) {
        this.capturedPiece = capturedPiece;
    }
    public Piece capturedPiece() {
        return capturedPiece;
    }


    public void setCapturedSquare(Square capturedSquare) {
        this.capturedSquare = capturedSquare;
    }
    public Square capturedSquare() {
        return capturedSquare;
    }


    public long hashKey() {
        return hashKey;
    }
    public int move() {
        return move;
    }


    public void restore(Board board) {
        board.setSideToMove(sideToMove);
        board.setEnPassant(enPassant);
        board.setEnPassantTarget(enPassantTarget);
        board.setCastlingRight(castlingRight);
        board.setFiftyMove(fiftyMove);
        board.setMoveCounter(moveCounter);
        board.setGamePly(gamePly);


        // undo move
        if (move != 0) {
            final Piece promotion = promotion(move);
            final Piece movingPiece = promotion.equals(Piece.NONE)
                                    ? movingPiece(move)
                                    : promotion;
            final Square from = from(move);
            final Square to = to(move);
            final boolean isCastling = isCastling(move);


            // undo rook castling
            if (isCastling) {
                switch (to) {
                    case G1 -> {
                        board.removePiece(Piece.WHITE_ROOK, Square.F1);
                        board.setPiece(Piece.WHITE_ROOK, Square.H1);
                    }
                    case C1 -> {
                        board.removePiece(Piece.WHITE_ROOK, Square.D1);
                        board.setPiece(Piece.WHITE_ROOK, Square.A1);
                    }
                    case G8 -> {
                        board.removePiece(Piece.BLACK_ROOK, Square.F8);
                        board.setPiece(Piece.BLACK_ROOK, Square.H8);
                    }
                    case C8 -> {
                        board.removePiece(Piece.BLACK_ROOK, Square.D8);
                        board.setPiece(Piece.BLACK_ROOK, Square.A8);
                    }
                }
            }


            board.removePiece(movingPiece, to);
            if (promotion.equals(Piece.NONE))
                board.setPiece(movingPiece, from);
            else
                board.setPiece(Piece.encodePiece(sideToMove, PieceType.PAWN), from);


            if (!capturedPiece.equals(Piece.NONE))
                board.setPiece(capturedPiece, capturedSquare);
        }


        board.setHashKey(hashKey);
    }


    @Override
    public int hashCode() {
        return (int) hashKey;
    }


}
