package com.chess;

import java.util.Arrays;

import static com.chess.Score.*;

public class ScoreConstants {

    public static int pawn = S(126, 208),
                    knight = S(781, 854),
                    bishop = S(825, 915),
                    rook = S(1276, 1380),
                    queen = S(2538, 2682);
    public static int[] pieceScores = {pawn, knight, bishop, rook, queen, 0};


    // piece pawn bonus/penalties
    public static int doubledEarly  = S(20,  7);
    public static int doubled = S(13, 51);
    public static int isolated = S(3, 15);
    public static int backward = S( 9, 22);
    public static int weakUnopposed = S(13, 24);
    public static int weakLever = S(4, 58);
    public static int passedFile = S(11,  8);
    public static int threatBySafePawn = S(173, 94);
    public static int threatByPawnPush = S( 48, 39);
    public static int[] blockedPawn = {S(-17, -6), S(-9, 2)};
    public static int[] connectedRank = {0, 5, 7, 11, 23, 48, 87};
    public static int[] passedRank = {
            S(0, 0),
            S(7, 27),
            S(16, 32),
            S(17, 40),
            S(64, 71),
            S(170, 174),
            S(278, 262)
    };


    // piece minor bonus
    public static int uncontestedOutpost = S(1, 10);
    public static int reachableOutpost = S(31, 22);
    public static int minorBehindPawn = S( 18,  3);
    public static int[] outpost = {S(57, 38), S(31, 24)};
    public static int[] threatByMinor = {
            S(0, 0),
            S(5, 32),
            S(55, 41),
            S(77, 56),
            S(89, 119),
            S(79, 162)
    };


    // piece bishop bonus/penalties
    public static int bishopXRayPawns = S(4, 5);
    public static int longDiagonalBishop = S( 45, 0);
    public static int bishopOnKingRing = S(24, 0);
    public static int[] bishopPawns = {
            S(3, 8),
            S(3, 9),
            S(2, 8),
            S(3, 8)
    };


    // piece rook bonus/penalties
    public static int[] openFileRook = {S(19, 6), S(47, 26)};
    public static int closedFileRook = S(10, 5);
    public static int trappedRook = S(55, 13);
    public static int rookOnKingRing = S(16, 0);
    public static int[] threatByRook = {
            S(0, 0),
            S(3, 44),
            S(37, 68),
            S(42, 60),
            S(0, 39),
            S(58, 43)
    };


    // piece queen bonus/penalties
    public static int knightOnQueen = S(16, 11);
    public static int sliderOnQueen = S(60, 18);
    public static int weakQueen = S(56, 15);
    public static int weakQueenProtection = S(14,  0);


    // piece king bonus/penalties
    public static int threatByKing = S(24, 89);
    public static int pawnlessFlank = S(17, 95);
    public static int flankAttacks = S(8, 0);
    public static int[] kingProtector = {S(8, 9), S(6, 9)};
    public static int[] kingAttackWeights = {0, 81, 52, 44, 10};
    public static int[][] safeCheck = {
            {803, 1292},
            {639, 974},
            {1087, 1878},
            {759, 1132}
    };
    public static int[][] kingOnFile = {
            {S(-21,10), S(-7, 1)},
            {S(0,-3), S( 9,-4)}
    };
    public static int[] blockedStorm = {
            S(0, 0),
            S(0, 0),
            S(75, 78),
            S(-8, 16),
            S(-6, 10),
            S(-6, 6),
            S(0, 2)
    };
    public static int[][] shelterStrength = {
            {-5, 82, 92, 54, 36, 22, 28},
            {-44, 63, 33, -50, -30, -12, -62},
            {-11, 77, 22, -6, 31, 8, -45},
            {-39, -12, -29, -50, -43, -68, -164}
    };
    public static int[][] unblockedStorm = {
            {87, -288, -168, 96, 47, 44, 46},
            {42, -25, 120, 45, 34, -9, 24},
            {-8, 51, 167, 35, -4, -16, -12},
            {-17, -13, 100, 4, 9, -16, -31}
    };


    // piece global bonus/penalties
    public static int hanging = S(69, 36);
    public static int restrictedPiece = S(7,  7);
    public static int[][] mobilityBonus = {
            {
                    S(-62, -79), S(-53, -57), S(-12, -31), S(-3, -17), S(3, 7), S(12, 13),
                    S(21, 16), S(28, 21), S(37, 26)
            },
            {
                    S(-47, -59), S(-20, -25), S(14, -8), S(29, 12), S(39, 21), S(53, 40),
                    S(53, 56), S(60, 58), S(62, 65), S(69, 72), S(78, 78), S(83, 87),
                    S(91, 88), S(96, 98)
            },
            {
                    S(-60, -82), S(-24, -15), S(0, 17), S(3, 43), S(4, 72), S(14, 100),
                    S(20, 102), S(30, 122), S(41, 133), S(41, 139), S(41, 153), S(45, 160),
                    S(57, 165), S(58, 170), S(67, 175)
            },
            {
                    S(-29, -49), S(-16, -29), S(-8, -8), S(-8, 17), S(18, 39), S(25, 54),
                    S(23, 59), S(37, 73), S(41, 76), S(54, 95), S(65, 95), S(68, 101),
                    S(69, 124), S(70, 128), S(70, 132), S(70, 133), S(71, 136), S(72, 140),
                    S(74, 147), S(76, 149), S(90, 153), S(104, 169), S(105, 171), S(106, 171),
                    S(112, 178), S(114, 185), S(114, 187), S(119, 221)
            }
    };
    public static int[][] pstBonus = {
            new int[]{
                    0, 0, 0, 0, 0, 0, 0, 0,
                    S(2, -8), S(4, -6), S(11, 9), S(18, 5), S(16, 16), S(21, 6), S(9, -6), S(-3, -18),
                    S(-9, -9), S(-15, -7), S(11, -10), S(15, 5), S(31, 2), S(23, 3), S(6, -8), S(-20, -5),
                    S(-3, 7), S(-20, 1), S(8, -8), S(19, -2), S(39, -14), S(17, -13), S(2, -11), S(-5, -6),
                    S(11, 12), S(-4, 6), S(-11, 2), S(2, -6), S(11, -5), S(0, -4), S(-12, 14), S(5, 9),
                    S(3, 27), S(-11, 18), S(-6, 19), S(22, 29), S(-8, 30), S(-5, 9), S(-14, 8), S(-11, 14),
                    S(-7, -1), S(6, -14), S(-2, 13), S(-11, 22), S(4, 24), S(-14, 17), S(10, 7), S(-9, 7),
                    0, 0, 0, 0, 0, 0, 0, 0
            },
            fillSymmetrically(new int[]{
                    S(-175, -96), S(-92, -65), S(-74, -49), S(-73, -21), 0, 0, 0, 0,
                    S(-77, -67), S(-41, -54), S(-27, -18), S(-15, 8), 0, 0, 0, 0,
                    S(-61, -40), S(-17, -27), S(6, -8), S(12, 29), 0, 0, 0, 0,
                    S(-35, -35), S(8, -2), S(40, 13), S(49, 28), 0, 0, 0, 0,
                    S(-34, -45), S(13, -16), S(44, 9), S(51, 39), 0, 0, 0, 0,
                    S(-9, -51), S(22, -44), S(58, -16), S(53, 17), 0, 0, 0, 0,
                    S(-67, -69), S(-27, -50), S(4, -51), S(37, 12), 0, 0, 0, 0,
                    S(-201, -100), S(-83, -88), S(-56, -56), S(-26, -17), 0, 0, 0, 0
            }),
            fillSymmetrically(new int[]{
                    S(-37, -40), S(-4, -21), S(-6, -26), S(-16, -8), 0, 0, 0, 0,
                    S(-11, -26), S(6, -9), S(13, -12), S(3, 1), 0, 0, 0, 0,
                    S(-5, -11), S(15, -1), S(-4, -1), S(12, 7), 0, 0, 0, 0,
                    S(-4, -14), S(8, -4), S(18, 0), S(27, 12), 0, 0, 0, 0,
                    S(-8, -12), S(20, -1), S(15, -10), S(22, 11), 0, 0, 0, 0,
                    S(-11, -21), S(4, 4), S(1, 3), S(8, 4), 0, 0, 0, 0,
                    S(-12, -22), S(-10, -14), S(4, -1), S(0, 1), 0, 0, 0, 0,
                    S(-34, -32), S(1, -29), S(-10, -26), S(-16, -17), 0, 0, 0, 0
            }),
            fillSymmetrically(new int[]{
                    S(-31, -9), S(-20, -13), S(-14, -10), S(-5, -9), 0, 0, 0, 0,
                    S(-21, -12), S(-13, -9), S(-8, -1), S(6, -2), 0, 0, 0, 0,
                    S(-25, 6), S(-11, -8), S(-1, -2), S(3, -6), 0, 0, 0, 0,
                    S(-13, -6), S(-5, 1), S(-4, -9), S(-6, 7), 0, 0, 0, 0,
                    S(-27, -5), S(-15, 8), S(-4, 7), S(3, -6), 0, 0, 0, 0,
                    S(-22, 6), S(-2, 1), S(6, -7), S(12, 10), 0, 0, 0, 0,
                    S(-2, 4), S(12, 5), S(16, 20), S(18, -5), 0, 0, 0, 0,
                    S(-17, 18), S(-19, 0), S(-1, 19), S(9, 13), 0, 0, 0, 0
            }),
            fillSymmetrically(new int[]{
                    S(3, -69), S(-5, -57), S(-5, -47), S(4, -26), 0, 0, 0, 0,
                    S(-3, -54), S(5, -31), S(8, -22), S(12, -4), 0, 0, 0, 0,
                    S(-3, -39), S(6, -18), S(13, -9), S(7, 3), 0, 0, 0, 0,
                    S(4, -23), S(5, -3), S(9, 13), S(8, 24), 0, 0, 0, 0,
                    S(0, -29), S(14, -6), S(12, 9), S(5, 21), 0, 0, 0, 0,
                    S(-4, -38), S(10, -18), S(6, -11), S(8, 1), 0, 0, 0, 0,
                    S(-5, -50), S(6, -27), S(10, -24), S(8, -8), 0, 0, 0, 0,
                    S(-2, -74), S(-2, -52), S(1, -43), S(-2, -34), 0, 0, 0, 0
            }),
            fillSymmetrically(new int[]{
                    S(271, 1), S(327, 45), S(271, 85), S(198, 76), 0, 0, 0, 0,
                    S(278, 53), S(303, 100), S(234, 133), S(179, 135), 0, 0, 0, 0,
                    S(195, 88), S(258, 130), S(169, 169), S(120, 175), 0, 0, 0, 0,
                    S(164, 103), S(190, 156), S(138, 172), S(98, 172), 0, 0, 0, 0,
                    S(154, 96), S(179, 166), S(105, 199), S(70, 199), 0, 0, 0, 0,
                    S(123, 92), S(145, 172), S(81, 184), S(31, 191), 0, 0, 0, 0,
                    S(88, 47), S(120, 121), S(65, 116), S(33, 131), 0, 0, 0, 0,
                    S(59, 11), S(89, 59), S(45, 73), S(-1, 78), 0, 0, 0, 0
            })
    };


    // e.g. [21, 36, 0, 0] -> [21, 36, 36, 21]
    private static int[] fillSymmetrically(int[] scores) {
        int[] copy = new int[scores.length];
        System.arraycopy(scores, 0, copy, 0, scores.length);
        int count = 0;
        int extra = 0;


        for (int i = 0; i < (scores.length / 2) / 4; i++) {
            count++;
            int[] part = Arrays.copyOfRange(scores, 4 * (count - 1) + extra, 4 * count + extra);
            part = reverse(part);


            for (int j = 0; j < part.length; j++) {
                try { copy[4 * count  + extra + j] = part[j]; }
                catch (Exception e) { e.printStackTrace(); }
            }


            extra += 4;
        }


        return copy;
    }


    private static int[] reverse(int[] scores) {
        int[] copy = new int[scores.length];
        for (int i = scores.length - 1; i >= 0; i--)
            copy[scores.length - i - 1] = scores[i];
        return copy;
    }


    private static int S(int mg, int eg) {
        return makeScore(mg, eg);
    }


}
