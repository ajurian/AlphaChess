package com.chess;


import java.util.ArrayList;
import java.util.Arrays;

import static com.chess.File.*;
import static com.chess.Rank.*;
import static com.chess.Square.*;

public class Bitboard {

    private static final long[][] betweenBB = new long[64][64];
    public static final long[][] lineBB = new long[64][64];


    public static final long lightSquares = 0x55AA55AA55AA55AAL;
    public static final long darkSquares = 0xAA55AA55AA55AA55L;
    public static final long center = (fileBB(FILE_D) | fileBB(FILE_E))
                                    & (rankBB(RANK_4) | rankBB(RANK_5));
    public static final long centerRanks = rankBB(RANK_3) |
                                            rankBB(RANK_4) |
                                            rankBB(RANK_5) |
                                            rankBB(RANK_6);
    public static final long centerFiles = fileBB(FILE_C) |
                                            fileBB(FILE_D) |
                                            fileBB(FILE_E) |
                                            fileBB(FILE_F);
    public static final long queenSide = fileBB(FILE_A) |
                                        fileBB(FILE_B) |
                                        fileBB(FILE_C) |
                                        fileBB(FILE_D);
    public static final long kingSide = fileBB(FILE_E) |
                                        fileBB(FILE_F) |
                                        fileBB(FILE_G) |
                                        fileBB(FILE_H);


    public static final long[] kingFlank = {
            queenSide ^ fileBB(FILE_D), queenSide, queenSide,
            centerFiles, centerFiles,
            kingSide, kingSide, kingSide ^ fileBB(FILE_E)
    };


    private static final long[] A1H8Attacks = new long[64];
    private static final long[] H1A8Attacks = new long[64];
    private static final long[] fileAttacks = new long[64];
    private static final long[] rankAttacks = new long[64];


    public static final long[][] pawnAttacks = new long[2][64];
    public static final long[] knightAttacks = new long[64];
    public static final long[] kingAttacks = new long[64];


    static {
        for (int i = 0; i < 64; i++) {
            Square square = squareAt(i);
            A1H8Attacks[i] = maskDiagonalA1H8(square);
            H1A8Attacks[i] = maskDiagonalH1A8(square);
            fileAttacks[i] = maskFileAttacks(square);
            rankAttacks[i] = maskRankAttacks(square);
        }


        for (int i = 0; i < 64; i++) {
            Square square = squareAt(i);
            pawnAttacks[0][i] = maskPawnAttacks(Side.WHITE, square);
            pawnAttacks[1][i] = maskPawnAttacks(Side.BLACK, square);
            knightAttacks[i] = maskKnightAttacks(square);
            kingAttacks[i] = maskKingAttacks(square);


            for (PieceType pt : new PieceType[]{PieceType.BISHOP, PieceType.ROOK}) {
                long attacks = pseudoAttacks(null, pt, squareAt(i));
                for (int j = 0; j < 64; j++) {
                    if ((attacks & squareBB(j)) != 0L) {
                        long iAttacks = pseudoAttacks(null, pt, squareAt(i));
                        long jAttacks = pseudoAttacks(null, pt, squareAt(j));
                        lineBB[i][j] = (iAttacks & jAttacks) | squareBB(i) | squareBB(j);
                    }
                    betweenBB[i][j] = ((1L << j) | ((1L << j) - (1L << i)));
                }
            }
        }
    }


    private static int randomState = 1804289383;


    public static int random32() {
        int number = randomState;
        number ^= number << 13;
        number ^= number >> 17;
        number ^= number << 5;
        randomState = number;
        return number;
    }


    public static long random64() {
        long a, b, c, d;
        a = (long) (random32()) & 0xFFFF;
        b = (long) (random32()) & 0xFFFF;
        c = (long) (random32()) & 0xFFFF;
        d = (long) (random32()) & 0xFFFF;
        return a | (b << 16) | (c << 32) | (d << 48);
    }


    private static long maskPawnAttacks(Side side, Square square) {
        long attacks = 0L;
        for (int i = 0; i < 64; i++) {
            Square sq = squareAt(i);
            if (side.equals(Side.WHITE) &&
                sq.rank().ordinal() - square.rank().ordinal() == 1 &&
                sq.distance(square) == 1 && !sq.file().equals(square.file()))
                attacks |= squareBB(i);
            else if (side.equals(Side.BLACK) &&
                    sq.rank().ordinal() - square.rank().ordinal() == -1 &&
                    sq.distance(square) == 1 && !sq.file().equals(square.file()))
                attacks |= squareBB(i);
        }
        return attacks;
    }


    private static long maskKnightAttacks(Square square) {
        long attacks = 0L;
        for (int i = 0; i < 64; i++) {
            Square sq = squareAt(i);
            if (square.distance(sq) == 2 &&
                !square.rank().equals(sq.rank()) &&
                !square.file().equals(sq.file()) &&
                square.rank().distance(sq.rank()) != square.file().distance(sq.file()))
                attacks |= squareBB(sq);
        }
        return attacks;
    }


    private static long maskDiagonalA1H8(Square square) {
        long attacks = 0L;
        int tr = square.rank().ordinal(), rank;
        int tf = square.file().ordinal(), file;


        for (rank = tr + 1, file = tf + 1; rank <= 7 && file <= 7; rank++, file++)
            attacks |= squareBB(encodeSquare(rankAt(rank), fileAt(file)));


        for (rank = tr - 1, file = tf + 1; rank >= 0 && file <= 7; rank--, file++)
            attacks |= squareBB(encodeSquare(rankAt(rank), fileAt(file)));


        return attacks;
    }


    private static long maskDiagonalH1A8(Square square) {
        long attacks = 0L;
        int tr = square.rank().ordinal(), rank;
        int tf = square.file().ordinal(), file;


        for (rank = tr + 1, file = tf - 1; rank <= 7 && file >= 0; rank++, file--)
            attacks |= squareBB(encodeSquare(rankAt(rank), fileAt(file)));


        for (rank = tr - 1, file = tf - 1; rank >= 0 && file >= 0; rank--, file--)
            attacks |= squareBB(encodeSquare(rankAt(rank), fileAt(file)));


        return attacks;
    }


    private static long maskRankAttacks(Square square) {
        return rankBB(square.rank()) ^ squareBB(square);
    }
    private static long maskFileAttacks(Square square) {
        return fileBB(square.file()) ^ squareBB(square);
    }


    public static long doubleAttackPawn(Side side, long occupancy) {
        return (side.equals(Side.WHITE)
                ? (occupancy & ~fileBB(FILE_A)) << 7 & (occupancy & ~fileBB(FILE_H)) << 9
                : (occupancy & ~fileBB(FILE_A)) >> 9 & (occupancy & ~fileBB(FILE_H)) >> 7);
    }


    private static long maskKingAttacks(Square square) {
        long attacks = 0L;
        for (int i = 0; i < 64; i++) {
            Square sq = squareAt(i);
            if (square.distance(sq) == 1)
                attacks |= squareBB(sq);
        }
        return attacks;
    }


    private static long sliderAttacks(Square square, long attacks, long mask) {
        long occupancy = attacks & mask;
        if (occupancy == 0L)
            return attacks;


        long m = squareBB(square) - 1L;
        long lowerMask = occupancy & m;
        long upperMask = occupancy & ~m;
        int minor = lowerMask == 0L ? 0 : msb(lowerMask);
        int major = upperMask == 0L ? 63 : lsb(upperMask);
        return bitsBetween(attacks, minor, major);
    }


    public static long bishopAttacks(Square square, long mask) {
        return sliderAttacks(square, A1H8Attacks[square.ordinal()], mask) |
                sliderAttacks(square, H1A8Attacks[square.ordinal()], mask);
    }


    public static long rookAttacks(Square square, long mask) {
        return sliderAttacks(square, rankAttacks[square.ordinal()], mask) |
                sliderAttacks(square, fileAttacks[square.ordinal()], mask);
    }


    public static long queenAttacks(Square square, long mask) {
        return bishopAttacks(square, mask) |
                rookAttacks(square, mask);
    }


    public static long pseudoAttacks(Side side, PieceType pt, Square square) {
        return switch (pt) {
            case PAWN -> pawnAttacks[side.ordinal()][square.ordinal()];
            case KNIGHT -> knightAttacks[square.ordinal()];
            case BISHOP -> bishopAttacks(square, 0L);
            case ROOK -> rookAttacks(square, 0L);
            case QUEEN -> queenAttacks(square, 0L);
            case KING -> kingAttacks[square.ordinal()];
            case NONE -> 0L;
        };
    }


    public static long pseudoAttacks(Side side, PieceType pt, long occupancy) {
        if (pt.equals(PieceType.PAWN)) {
            if (side.equals(Side.WHITE))
                return (occupancy << 7) & ~fileBB(FILE_H) | (occupancy << 9) & ~fileBB(FILE_A);
            else
                return (occupancy >> 7) & ~fileBB(FILE_A) | (occupancy >> 9) & ~fileBB(FILE_H);
        }


        long attacks = 0L;
        while (occupancy != 0L) {
            int index = lsb(occupancy);
            occupancy = extractLsb(occupancy);
            attacks |= pseudoAttacks(side, pt, squareAt(index));
        }
        return attacks;
    }


    public static long sliderAttacks(PieceType pt, Square square, long mask) {
        return switch (pt) {
            case BISHOP -> bishopAttacks(square, mask);
            case ROOK -> rookAttacks(square, mask);
            case QUEEN -> queenAttacks(square, mask);
            default -> 0L;
        };
    }


    public static long forwardRankBB(Side side, Rank rank) {
        return (side.equals(Side.WHITE)
                ? ~rankBB(RANK_1) << (8 * relativeRank(side, rank).ordinal())
                : ~rankBB(RANK_8) >> (8 * relativeRank(side, rank).ordinal()));
    }


    public static long forwardFileBB(Side side, Square square) {
        return forwardRankBB(side, square.rank()) & fileBB(square);
    }


    public static long shiftUp(Side side, long bb) {
        return (side.equals(Side.WHITE) ? bb << 8 : bb >> 8);
    }


    public static long shiftDown(Side side, long bb) {
        return (side.equals(Side.WHITE) ? bb >> 8 : bb << 8);
    }


    public static long adjacentFiles(File file) {
        return (fileBB(file) << 1 & ~fileBB(FILE_A)) |
                (fileBB(file) >> 1 & ~fileBB(FILE_H));
    }


    public static long passedMaskBB(Side side, Square square) {
        return ((adjacentFiles(square.file()) &
                forwardRankBB(side, square.rank())) |
                forwardFileBB(side, square));
    }


    public static int lsb(long bb) {
        return Long.numberOfTrailingZeros(bb);
    }
    public static int msb(long bb) {
        return 63 - Long.numberOfLeadingZeros(bb);
    }
    public static long extractLsb(long bb) {
        return bb & (bb - 1);
    }
    public static boolean moreThanOne(long bb) {
        return extractLsb(bb) != 0L;
    }
    public static long bitsBetween(long bb, int i, int j) {
        return betweenBB[i][j] & bb;
    }
    public static Square frontMostSquare(Side side, long bb) {
        return (side.equals(Side.WHITE)
                ? squareAt(msb(bb))
                : squareAt(lsb(bb)));
    }


    public static long rankBB(int index) {
        return rankBB(rankAt(index));
    }
    public static long rankBB(Square square) {
        return rankBB(square.rank());
    }
    public static long rankBB(Rank rank) {
        return rank.bitboard();
    }


    public static long fileBB(int index) {
        return fileBB(fileAt(index));
    }
    public static long fileBB(Square square) {
        return fileBB(square.file());
    }
    public static long fileBB(File file) {
        return file.bitboard();
    }


    public static long squareBB(int index) {
        return squareBB(squareAt(index));
    }
    public static long squareBB(Square square) {
        return square.bitboard();
    }


    public static String toString(long bb) {
        StringBuilder builder = new StringBuilder();


        builder.append("    a b c d e f g h\n  +-----------------+\n");
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Square square = encodeSquare(rankAt(rank), fileAt(file));


                if (file == 0)
                    builder.append(String.format("%d |", rank + 1));


                if ((bb & squareBB(square)) != 0L)
                    builder.append(" 1");
                else
                    builder.append(" 0");
            }
            builder.append(" |\n");
        }
        builder.append("  +-----------------+\n");


        return builder.toString();
    }


    public static void printBB(long bb) {
        System.out.println(toString(bb));
    }


}
