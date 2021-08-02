package com.chess;


import jdk.jfr.Unsigned;

import java.util.LinkedList;
import java.util.List;

import static com.chess.Move.*;
import static com.chess.Bitboard.*;
import static com.chess.Rank.*;
import static com.chess.File.*;
import static com.chess.Square.*;
import static com.chess.MoveSorter.*;

public class MoveGenerator {

    public static void addMove(List<Integer> moves, Board board, int move) {
        moves.add(move);
    }


    public static void generatePawnMoves(Board board, List<Integer> moves) {
        Side us = board.sideToMove();
        Piece ourPawn = Piece.encodePiece(us, PieceType.PAWN);
        int pawnPush = us.equals(Side.WHITE) ? 8 : -8;


        long pushBB = rankBB(relativeRank(us, RANK_2));
        long promotionBB = rankBB(relativeRank(us, RANK_7));


        long bb = board.bitboard(ourPawn);
        while (bb != 0L) {
            Square from = squareAt(lsb(bb));
            Square to = squareAt(from.ordinal() + pawnPush);
            bb = extractLsb(bb);


            // check if no piece at to square
            if ((to.bitboard() & board.bitboard()) == 0L) {
                // if one square ahead from promotion
                if ((rankBB(from) & promotionBB) != 0L) {
                    addMove(moves, board, encodeMove(from, to, ourPawn, Piece.encodePiece(us, PieceType.QUEEN), 0, 0, 0, 0));
                    addMove(moves, board, encodeMove(from, to, ourPawn, Piece.encodePiece(us, PieceType.ROOK), 0, 0, 0, 0));
                    addMove(moves, board, encodeMove(from, to, ourPawn, Piece.encodePiece(us, PieceType.BISHOP), 0, 0, 0, 0));
                    addMove(moves, board, encodeMove(from, to, ourPawn, Piece.encodePiece(us, PieceType.KNIGHT), 0, 0, 0, 0));
                } else {
                    // one push move
                    addMove(moves, board, encodeMove(from, to, ourPawn, Piece.NONE, 0, 0, 0, 0));
                    if ((rankBB(from) & pushBB) != 0L && (squareAt(to.ordinal() + pawnPush).bitboard() & board.bitboard()) == 0L)
                        // two push move
                        addMove(moves, board, encodeMove(from, squareAt(to.ordinal() + pawnPush), ourPawn, Piece.NONE, 0, 1, 0, 0));
                }
            }
        }
    }


    public static void generatePawnCaptures(Board board, List<Integer> moves) {
        Side us = board.sideToMove();
        Side them = us.flip();
        Piece ourPawn = Piece.encodePiece(us, PieceType.PAWN);


        long promotionBB = rankBB(relativeRank(us, RANK_7));
        long bb = board.bitboard(ourPawn);
        while (bb != 0L) {
            Square from = squareAt(lsb(bb));
            bb = extractLsb(bb);


            long attacks = pawnAttacks[us.ordinal()][from.ordinal()];


            if (board.enPassant() != Square.NONE && (attacks & board.enPassant().bitboard()) != 0L)
                addMove(moves, board, encodeMove(from, board.enPassant(), ourPawn, Piece.NONE, 1, 0 , 1, 0));


            attacks &= board.bitboard(them);


            while (attacks != 0L) {
                Square to = squareAt(lsb(attacks));
                attacks = extractLsb(attacks);


                // pawn capture with promotion
                if ((rankBB(from) & promotionBB) != 0L) {
                    addMove(moves, board, encodeMove(from, to, ourPawn, Piece.encodePiece(us, PieceType.QUEEN), 1, 0, 0, 0));
                    addMove(moves, board, encodeMove(from, to, ourPawn, Piece.encodePiece(us, PieceType.ROOK), 1, 0, 0, 0));
                    addMove(moves, board, encodeMove(from, to, ourPawn, Piece.encodePiece(us, PieceType.BISHOP), 1, 0, 0, 0));
                    addMove(moves, board, encodeMove(from, to, ourPawn, Piece.encodePiece(us, PieceType.KNIGHT), 1, 0, 0, 0));
                } else
                    addMove(moves, board, encodeMove(from, to, ourPawn, Piece.NONE, 1, 0, 0, 0));
            }
        }
    }


    public static void generateKnightMoves(Board board, List<Integer> moves) {
        Side us = board.sideToMove();
        Side them = us.flip();
        Piece ourKnight = Piece.encodePiece(us, PieceType.KNIGHT);


        long bb = board.bitboard(ourKnight);
        while (bb != 0L) {
            Square from = squareAt(lsb(bb));
            bb = extractLsb(bb);


            long attacks = knightAttacks[from.ordinal()] & ~board.bitboard(us);
            while (attacks != 0L) {
                Square to = squareAt(lsb(attacks));
                attacks = extractLsb(attacks);


                if ((to.bitboard() & board.bitboard(them)) != 0L)
                    addMove(moves, board, encodeMove(from, to, ourKnight, Piece.NONE, 1, 0, 0, 0));
                else
                    addMove(moves, board, encodeMove(from, to, ourKnight, Piece.NONE, 0, 0, 0, 0));
            }
        }
    }


    public static void generateBishopAttacks(Board board, List<Integer> moves) {
        Side us = board.sideToMove();
        Side them = us.flip();


        Piece ourBishop = Piece.encodePiece(us, PieceType.BISHOP);
        // include queen on bishop attacks
        Piece ourQueen = Piece.encodePiece(us, PieceType.QUEEN);


        long bb = board.bitboard(ourBishop) | board.bitboard(ourQueen);
        while (bb != 0L) {
            Square from = squareAt(lsb(bb));
            Piece piece = board.pieceAt(from).pieceType().equals(PieceType.BISHOP) ? ourBishop : ourQueen;
            bb = extractLsb(bb);


            long attacks = bishopAttacks(from, board.bitboard()) & ~board.bitboard(us);
            while (attacks != 0L) {
                Square to = squareAt(lsb(attacks));
                attacks = extractLsb(attacks);


                if ((to.bitboard() & board.bitboard(them)) != 0L)
                    addMove(moves, board, encodeMove(from, to, piece, Piece.NONE, 1, 0, 0, 0));
                else
                    addMove(moves, board, encodeMove(from, to, piece, Piece.NONE, 0, 0, 0, 0));
            }
        }
    }


    public static void generateRookAttacks(Board board, List<Integer> moves) {
        Side us = board.sideToMove();
        Side them = us.flip();


        Piece ourRook = Piece.encodePiece(us, PieceType.ROOK);
        // include queen on rook attacks
        Piece ourQueen = Piece.encodePiece(us, PieceType.QUEEN);


        long bb = board.bitboard(ourRook) | board.bitboard(ourQueen);
        while (bb != 0L) {
            Square from = squareAt(lsb(bb));
            Piece piece = board.pieceAt(from).pieceType().equals(PieceType.ROOK) ? ourRook : ourQueen;
            bb = extractLsb(bb);


            long attacks = rookAttacks(from, board.bitboard()) & ~board.bitboard(us);
            while (attacks != 0L) {
                Square to = squareAt(lsb(attacks));
                attacks = extractLsb(attacks);


                if ((to.bitboard() & board.bitboard(them)) != 0L)
                    addMove(moves, board, encodeMove(from, to, piece, Piece.NONE, 1, 0, 0, 0));
                else
                    addMove(moves, board, encodeMove(from, to, piece, Piece.NONE, 0, 0, 0, 0));
            }
        }
    }


    public static void generateQueenAttacks(Board board, List<Integer> moves) {
        Side us = board.sideToMove();
        Side them = us.flip();
        Piece ourQueen = Piece.encodePiece(us, PieceType.QUEEN);


        long bb = board.bitboard(ourQueen);
        while (bb != 0L) {
            Square from = squareAt(lsb(bb));
            bb = extractLsb(bb);


            long attacks = queenAttacks(from, board.bitboard()) & ~board.bitboard(us);
            while (attacks != 0L) {
                Square to = squareAt(lsb(attacks));
                attacks = extractLsb(attacks);


                if ((to.bitboard() & board.bitboard(them)) != 0L)
                    addMove(moves, board, encodeMove(from, to, ourQueen, Piece.NONE, 1, 0, 0, 0));
                else
                    addMove(moves, board, encodeMove(from, to, ourQueen, Piece.NONE, 0, 0, 0, 0));
            }
        }
    }


    public static void generateKingMoves(Board board, List<Integer> moves) {
        Side us = board.sideToMove();
        Side them = us.flip();
        Piece ourKing = Piece.encodePiece(us, PieceType.KING);


        long bb = board.bitboard(ourKing);
        Square from = squareAt(lsb(bb));


        /*if (from.equals(Square.NONE)) {
            System.out.println(from);
            System.out.println(Bitboard.toString(bb));


            while (board.backups().size() > 0) {
                System.out.println(Move.notation(board.undoMove()));
                System.out.println(board);
            }


            System.exit(0);
        }*/


        long attacks = kingAttacks[from.ordinal()] & ~board.bitboard(us);
        while (attacks != 0L) {
            Square to = squareAt(lsb(attacks));
            attacks = extractLsb(attacks);


            if ((to.bitboard() & board.bitboard(them)) != 0L)
                addMove(moves, board, encodeMove(from, to, ourKing, Piece.NONE, 1, 0, 0, 0));
            else
                addMove(moves, board, encodeMove(from, to, ourKing, Piece.NONE, 0, 0, 0, 0));
        }
    }


    public static void generateCastlingMoves(Board board, List<Integer> moves) {
        Side us = board.sideToMove();
        Side them = us.flip();


        Piece ourKing = Piece.encodePiece(us, PieceType.KING);
        int kingCastling = us.equals(Side.WHITE)
                            ? CastlingRight.WHITE_KING.bit()
                            : CastlingRight.BLACK_KING.bit();
        int queenCastling = us.equals(Side.WHITE)
                            ? CastlingRight.WHITE_QUEEN.bit()
                            : CastlingRight.BLACK_QUEEN.bit();


        // check if king can castle king side
        if ((board.castlingRight() & kingCastling) != 0) {
            if ((board.bitboard() & squareBB(relativeSquare(us, F1))) == 0L && (board.bitboard() & squareBB(relativeSquare(us, G1))) == 0L) {
                if (!board.isSquareAttackedBy(them, relativeSquare(us, E1)) && !board.isSquareAttackedBy(them, relativeSquare(us, F1)))
                    addMove(moves, board, encodeMove(relativeSquare(us, E1), relativeSquare(us, G1), ourKing, Piece.NONE, 0, 0, 0, 1));
            }
        }


        // check if king can castle queen side
        if ((board.castlingRight() & queenCastling) != 0) {
            if ((board.bitboard() & squareBB(relativeSquare(us, D1))) == 0L && (board.bitboard() & squareBB(relativeSquare(us, C1))) == 0L && (board.bitboard() & squareBB(relativeSquare(us, B1))) == 0L) {
                if (!board.isSquareAttackedBy(them, relativeSquare(us, E1)) && !board.isSquareAttackedBy(them, relativeSquare(us, D1)))
                    addMove(moves, board, encodeMove(relativeSquare(us, E1), relativeSquare(us, C1), ourKing, Piece.NONE, 0, 0, 0, 1));
            }
        }
    }


    public static List<Integer> generateLegalMoves(Board board) {
        List<Integer> moves = new LinkedList<>();
        generatePawnCaptures(board, moves);
        generatePawnMoves(board, moves);
        generateKnightMoves(board, moves);
        generateBishopAttacks(board, moves);
        generateRookAttacks(board, moves);
        generateKingMoves(board, moves);
        generateCastlingMoves(board, moves);
        return moves;
    }


}
