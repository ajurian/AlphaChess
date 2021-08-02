package com.chess;

import java.util.List;
import java.util.stream.Collectors;

import static com.chess.IntegerUtil.*;

public class Move {

    public static Square from(int move) {
        return Square.squareAt(move & 0x7F);
    }
    public static Square to(int move) {
        return Square.squareAt((move & 0x3F80) >> 7);
    }
    public static Piece movingPiece(int move) {
        return Piece.allPieces[(move & 0x3C000) >> 14];
    }
    public static Piece promotion(int move) {
        return Piece.allPieces[(move & 0x3C0000) >> 18];
    }
    public static boolean isCapture(int move) {
        return (move & 0x400000) != 0;
    }
    public static boolean isPush(int move) {
        return (move & 0x800000) != 0;
    }
    public static boolean isEnPassant(int move) {
        return (move & 0x1000000) != 0;
    }
    public static boolean isCastling(int move) {
        return (move & 0x2000000) != 0;
    }
    public static boolean isTactical(int move) {
        return isCapture(move) || !promotion(move).equals(Piece.NONE);
    }


    public static int encodeMove(Square from, Square to, Piece piece, Piece promotion, int capture, int push, int enPassant, int castling) {
        return (from.ordinal()) |
                (to.ordinal() << 7) |
                (piece.ordinal() << 14) |
                (promotion.ordinal() << 18) |
                (capture << 22) |
                (push << 23) |
                (enPassant << 24) |
                (castling << 25);
    }


    /*
              ! Move BIT representation !
                                      | from  |   (max square index)
       0000  0000  0000  0000  0000  0100  0000
                           |   to    |            (max square index)
       0000  0000  0000  0010  0000  0000  0000
                     | pc |                       (max pc index)
       0000  0000  0011  0000  0000  0000  0000
               | pr |                             (max pc index)
       0000  0011  0000  0000  0000  0000  0000
              c                                   (capture flag)
       0000  0100  0000  0000  0000  0000  0000
             p                                    (push flag)
       0000  1000  0000  0000  0000  0000  0000
          e                                       (enPassant flag)
       0001  0000  0000  0000  0000  0000  0000
         o                                        (castling flag)
       0010  0000  0000  0000  0000  0000  0000

     -- expected max move value to be encoded  --
       0011  1111  0011  0010  0000  0100  0000
                                                                     */


    public static int fromString(Board board, String move) {
        Square from = Square.valueOf(move.substring(0, 2).toUpperCase());
        Square to = Square.valueOf(move.substring(2, 4).toUpperCase());
        Piece promotion = Piece.NONE;
        Piece piece = board.pieceAt(from);


        if (from.equals(Square.NONE) || to.equals(Square.NONE))
            return 0;
        if (piece.equals(Piece.NONE))
            return 0;


        if (move.length() > 4 && piece.pieceType().equals(PieceType.PAWN) && to.rank().equals(Rank.relativeRank(piece.pieceSide(), Rank.RANK_8)))
            promotion = Piece.encodePiece(piece.pieceSide(), Piece.encodePiece((move.charAt(4) + "").toLowerCase()).pieceType());


        int push = _int(from.rank().equals(Rank.relativeRank(piece.pieceSide(), Rank.RANK_2)) && from.rank().distance(to.rank()) == 2 && piece.pieceType().equals(PieceType.PAWN));
        int enPassant = _int(to.equals(board.enPassant()) && piece.pieceType().equals(PieceType.PAWN));
        int capture = _int(enPassant == 1 || !board.pieceAt(to).equals(Piece.NONE));
        int castling = 0;


        if (piece.pieceType().equals(PieceType.KING) && from.file().distance(to.file()) == 2)
            castling = 1;
        return encodeMove(from, to, piece, promotion, capture, push, enPassant, castling);
    }


    public static String notation(int move) {
        StringBuilder builder = new StringBuilder();
        builder.append(from(move).toString().toLowerCase());
        builder.append(to(move).toString().toLowerCase());
        if (promotion(move) != Piece.NONE)
            builder.append(promotion(move).pieceType().symbol().toLowerCase());
        return builder.toString();
    }


    public static void printMove(int move) {
        System.out.print(notation(move));
    }


}
