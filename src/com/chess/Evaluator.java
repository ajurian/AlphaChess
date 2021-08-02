package com.chess;

import java.util.Arrays;

import static com.chess.Score.*;
import static com.chess.ScoreConstants.*;
import static com.chess.IntegerUtil.*;
import static com.chess.Bitboard.*;
import static com.chess.Rank.*;
import static com.chess.File.*;
import static com.chess.Square.*;
import static com.chess.Trace.*;

public class Evaluator {

    private static Board board = null;
    private static final long[] pawns = new long[2];
    private static final long[] knights = new long[2];
    private static final long[] bishops = new long[2];
    private static final long[] rooks = new long[2];
    private static final long[] queens = new long[2];
    private static final long[] kings = new long[2];


    public static void main(String[] args) {
        Board board = new Board();
//        board.setFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
        board.setFen("4r3/3r2k1/1bp2p2/2Np1bp1/3P3p/P1R4P/1P1R1PPB/6K1 w - - 12 34");
//        board.doMove("d2d4");
//        board.doMove("d7d5");
        System.out.println(board);
        trace(board);
    }


    private static int S(int mg, int eg) {
        return makeScore(mg, eg);
    }


    private static void update() {
        Arrays.fill(passedPawns, 0L);
        Arrays.fill(pawnAttackSpan, 0L);
        passedPawnCount = 0;
        blockedPawnCount = 0;


        pawns[0] = board.bitboard(Piece.WHITE_PAWN);
        pawns[1] = board.bitboard(Piece.BLACK_PAWN);


        knights[0] = board.bitboard(Piece.WHITE_KNIGHT);
        knights[1] = board.bitboard(Piece.BLACK_KNIGHT);


        bishops[0] = board.bitboard(Piece.WHITE_BISHOP);
        bishops[1] = board.bitboard(Piece.BLACK_BISHOP);


        rooks[0] = board.bitboard(Piece.WHITE_ROOK);
        rooks[1] = board.bitboard(Piece.BLACK_ROOK);


        queens[0] = board.bitboard(Piece.WHITE_QUEEN);
        queens[1] = board.bitboard(Piece.BLACK_QUEEN);


        kings[0] = board.bitboard(Piece.WHITE_KING);
        kings[1] = board.bitboard(Piece.BLACK_KING);


        pieceCount = new int[][]{
                {
                    _int(board.pieceCount(Piece.WHITE_BISHOP) > 1), board.pieceCount(Piece.WHITE_PAWN), board.pieceCount(Piece.WHITE_KNIGHT),
                    board.pieceCount(Piece.WHITE_BISHOP), board.pieceCount(Piece.WHITE_ROOK), board.pieceCount(Piece.WHITE_QUEEN),
                },
                {
                    _int(board.pieceCount(Piece.BLACK_BISHOP) > 1), board.pieceCount(Piece.BLACK_PAWN), board.pieceCount(Piece.BLACK_KNIGHT),
                    board.pieceCount(Piece.BLACK_BISHOP), board.pieceCount(Piece.BLACK_ROOK), board.pieceCount(Piece.BLACK_QUEEN)
                }
        };
    }



    private static int[][] pieceCount = new int[2][6];


    private static final int[][] quadraticOurs = {
            {S(1419, 1455)},
            {S(101, 28), S(37, 39)},
            {S(57, 64), S(249, 187), S(-49, -62)},
            {S(0, 0), S(118, 137), S(10, 27), S(0, 0)},
            {S(-63, -68), S(-5, 3), S(100, 81), S(132, 118), S(-246, -244)},
            {S(-210, -211), S(37, 14), S(147, 141), S(161, 105), S(-158, -174), S(-9, -31)}
    };


    private static final int[][] quadraticTheirs = {
            {},
            {S(33, 30)},
            {S(46, 18), S(106, 84)},
            {S(75, 35), S(59, 44), S(60, 15)},
            {S(26, 35), S(6, 22), S(38, 39), S(-12, -2)},
            {S(97, 93), S(100, 163), S(-58, -91), S(112, 192), S(276, 225)}
    };


    private static int imbalance(Side us) {
        Side them = us.flip();
        int bonus = 0;


        for (int pt1 = 0; pt1 <= 5; pt1++) {
            if (pieceCount[us.ordinal()][pt1] == 0)
                continue;
            int v = quadraticOurs[pt1][pt1] * pieceCount[us.ordinal()][pt1];
            for (int pt2 = 0; pt2 < pt1; ++pt2)
                v +=  quadraticOurs[pt1][pt2] * pieceCount[us.ordinal()][pt2]
                    + quadraticTheirs[pt1][pt2] * pieceCount[them.ordinal()][pt2];
            bonus += pieceCount[us.ordinal()][pt1] * v;
        }


        return bonus;
    }


    private static int imbalance() {
        int imbalance = (short) (imbalance(Side.WHITE) - imbalance(Side.BLACK)) / 16;
        if (isTracing())
            set("Imbalance", 2, imbalance);
        return imbalance;
    }


    private static final long[] passedPawns = new long[2];
    private static final long[] pawnAttackSpan = new long[2];
    private static int passedPawnCount = 0;
    private static int blockedPawnCount = 0;


    private static int pawnEvaluation(Side us) {
        Side them = us.flip();
        int score = 0;


        long dblAttackThem = doubleAttackPawn(them, pawns[them.ordinal()]);
        long materials = pawns[us.ordinal()];
        long bb = 0L;


        blockedPawnCount += Long.bitCount(shiftUp(us, pawns[us.ordinal()]) &
                (pawns[them.ordinal()] | dblAttackThem));
        pawnAttackSpan[us.ordinal()] = pseudoAttacks(us, PieceType.PAWN, pawns[us.ordinal()]);


        while (materials != 0L) {
            int index = lsb(materials);
            Square square = squareAt(index);
            materials = extractLsb(materials);


            int rank = relativeRank(us, square.rank()).ordinal();
            long opposed = pawns[them.ordinal()] & forwardFileBB(us, square);
            long blocked = pawns[them.ordinal()] & shiftUp(us, squareBB(index));
            long stoppers = pawns[them.ordinal()] & passedMaskBB(us, square);
            long lever = pawns[them.ordinal()] & pawnAttacks[us.ordinal()][index];
            long leverPush = pawns[them.ordinal()] & pawnAttacks[us.ordinal()][(us.equals(Side.WHITE) ? index + 8 : index - 8)];
            long doubled = pawns[us.ordinal()] & shiftDown(us, squareBB(index));
            long neighbors = pawns[us.ordinal()] & adjacentFiles(square.file());
            long phalanx = neighbors & rankBB(square);
            long support = neighbors & shiftDown(us, rankBB(square));


            if (doubled != 0L) {
                bb = pawns[them.ordinal()] | pseudoAttacks(them, PieceType.PAWN, pawns[them.ordinal()]);
                if ((pawns[us.ordinal()] & shiftDown(us, bb)) == 0L)
                    score -= doubledEarly;
            }


            boolean passed = (stoppers ^ lever) == 0L ||
                    ((stoppers ^ leverPush) == 0L &&
                            Long.bitCount(phalanx) >= Long.bitCount(leverPush)) ||
                    (stoppers == blocked &&
                            rank >= RANK_5.ordinal() &&
                            (shiftUp(us, support) & ~(pawns[them.ordinal()] | dblAttackThem)) != 0L);
            passed &= (forwardFileBB(us, square) & pawns[us.ordinal()]) == 0L;


            if (passed) {
                passedPawns[us.ordinal()] |= squareBB(index);
                passedPawnCount++;
            }


            boolean backward = (neighbors & forwardRankBB(them, squareAt((us.equals(Side.WHITE) ? index + 8 : index - 8)).rank())) == 0L &&
                    (leverPush | blocked) != 0L;
            if (!backward && blocked == 0L)
                pawnAttackSpan[us.ordinal()] |= forwardRankBB(us, square.rank()) &
                        adjacentFiles(square.file());


            if ((support | phalanx) != 0L) {
                int v = connectedRank[rank] * (2 + _int(phalanx != 0L) - _int(opposed != 0L)) + 22 * Long.bitCount(support);
                score += S(v, v * (rank - 2) / 4);
            } else if (neighbors == 0L) {
                if (opposed != 0L &&
                        (pawns[us.ordinal()] & forwardFileBB(them, square)) != 0L &&
                        (pawns[them.ordinal()] & adjacentFiles(square.file())) == 0L)
                    score -= ScoreConstants.doubled;
                else
                    score -= isolated + weakUnopposed * _int(opposed == 0L);
            } else if (backward)
                score -= ScoreConstants.backward + weakUnopposed * _int(opposed == 0L) *
                        _int((~(fileBB(FILE_A) | fileBB(FILE_H)) & squareBB(square)) != 0L);


            if (support == 0L)
                score -= ScoreConstants.doubled * _int(doubled != 0L)
                        + weakLever * _int(moreThanOne(lever));


            if (blocked != 0L && rank >= RANK_5.ordinal())
                score += blockedPawn[rank - RANK_5.ordinal()];
        }


        if (isTracing())
            set("Pawns", us.ordinal(), score);


        return score;
    }


    private static final int lazyThreshold1 = 1565;
    private static final int lazyThreshold2 = 1102;
    private static final int spaceThreshold = 11551;
    private static boolean lazySkip(int score, int threshold) {
        return Math.abs(mgValue(score) + egValue(score)) <= threshold + board.nonPawnMaterial() / 32;
    }


    private static final long[][] attackedBy = new long[2][7];
    private static final long[] attackedBy2 = new long[2];
    private static final long[] mobilityArea = new long[2];
    private static final int[] mobilities = new int[2];
    private static final long[] kingRing = new long[2];
    private static final int[] kingAttackersCount = new int[2];
    private static final int[] kingAttackersWeight = new int[2];
    private static final int[] kingAttacksCount = new int[2];


    private static void initialize(Side us) {
        Side them = us.flip();
        Square ksq = board.kingSquare(us);


        long lowRanks = (us.equals(Side.WHITE)
                       ? rankBB(RANK_2) | rankBB(RANK_3)
                       : rankBB(RANK_7) | rankBB(RANK_6));
        long dblAttackPawn = doubleAttackPawn(us, pawns[us.ordinal()]);
        long bb = pawns[us.ordinal()] & (shiftDown(us, board.bitboard()) | lowRanks);


        mobilityArea[us.ordinal()] = ~(bb | (kings[us.ordinal()] | queens[us.ordinal()])
                                    | board.sliderBlockers(ksq, board.bitboard(them))
                                    | pseudoAttacks(them, PieceType.PAWN, pawns[them.ordinal()]));
        attackedBy[us.ordinal()][5] = kingAttacks[ksq.ordinal()];
        attackedBy[us.ordinal()][0] = pseudoAttacks(us, PieceType.PAWN, pawns[us.ordinal()]);
        attackedBy[us.ordinal()][6] = attackedBy[us.ordinal()][5] | attackedBy[us.ordinal()][0];
        attackedBy2[us.ordinal()] = dblAttackPawn | (attackedBy[us.ordinal()][5] & attackedBy[us.ordinal()][0]);
        mobilities[us.ordinal()] = 0;


        Square square = encodeSquare(rankAt(clamp(ksq.rank().ordinal(), RANK_2.ordinal(), RANK_7.ordinal())),
                                     fileAt(clamp(ksq.file().ordinal(), FILE_B.ordinal(), FILE_G.ordinal())));
        kingRing[us.ordinal()] = kingAttacks[ksq.ordinal()] | squareBB(square);
        kingAttackersCount[them.ordinal()] = Long.bitCount(kingRing[us.ordinal()] & pseudoAttacks(them, PieceType.PAWN, pawns[them.ordinal()]));
        kingAttackersWeight[them.ordinal()] = 0;
        kingAttacksCount[them.ordinal()] = 0;
        kingRing[us.ordinal()] &= ~dblAttackPawn;
    }


    private static int pieces(Side us, PieceType pt) {
        Side them = us.flip();
        Square ksq = board.kingSquare(us);
        int score = 0;


        long materials = board.bitboard(Piece.encodePiece(us, pt));
        attackedBy[us.ordinal()][pt.ordinal()] = 0L;


        while (materials != 0L) {
            int index = lsb(materials);
            Square square = squareAt(index);
            materials = extractLsb(materials);


            long attacks = pt.equals(PieceType.BISHOP) ? bishopAttacks(square, board.bitboard() ^ (queens[0] | queens[1]))
                    : pt.equals(PieceType.ROOK) ? rookAttacks(square, board.bitboard() ^ rooks[us.ordinal()] ^ (queens[0] | queens[1]))
                    : pt.equals(PieceType.QUEEN) ? queenAttacks(square, board.bitboard())
                    : pseudoAttacks(us, pt, square);


            // piece is pinned, the only attacks are the ones that covers the king
            if ((board.sliderBlockers(ksq, board.bitboard(them)) & squareBB(square)) != 0L)
                attacks &= lineBB[ksq.ordinal()][square.ordinal()];


            attackedBy2[us.ordinal()] |= attackedBy[us.ordinal()][6] & attacks;
            attackedBy[us.ordinal()][pt.ordinal()] |= attacks;
            attackedBy[us.ordinal()][6] |= attacks;


            if ((attacks & kingRing[them.ordinal()]) != 0L) {
                kingAttackersCount[us.ordinal()]++;
                kingAttackersWeight[us.ordinal()] += kingAttackWeights[pt.ordinal()];
                kingAttacksCount[us.ordinal()] += Long.bitCount(attacks & attackedBy[them.ordinal()][5]);
            }
            else if (pt.equals(PieceType.ROOK) && (fileBB(square) & kingRing[them.ordinal()]) != 0L)
                score += rookOnKingRing;
            else if (pt.equals(PieceType.BISHOP) && (bishopAttacks(square, pawns[0] | pawns[1]) & kingRing[them.ordinal()]) != 0L)
                score += bishopOnKingRing;


            int mobility = Long.bitCount(attacks & mobilityArea[us.ordinal()]);
            mobilities[us.ordinal()] += mobilityBonus[pt.ordinal() - 1][mobility];


            if (pt.equals(PieceType.KNIGHT) || pt.equals(PieceType.BISHOP)) {
                long outpostRanks = (us.equals(Side.WHITE) ? (rankBB(RANK_4) | rankBB(RANK_5) | rankBB(RANK_6))
                        : (rankBB(RANK_5) | rankBB(RANK_4) | rankBB(RANK_3)));
                long bb = outpostRanks & (attackedBy[us.ordinal()][0] | shiftDown(us, pawns[0] | pawns[1])) & ~pawnAttackSpan[them.ordinal()];
                long targets = board.bitboard(them) & ~pawns[them.ordinal()];


                if (pt.equals(PieceType.KNIGHT) &&
                        (bb & squareBB(square) & ~centerFiles) != 0L &&
                        (attacks & targets) == 0L &&
                        !moreThanOne(targets & ((squareBB(square) & queenSide) != 0L ? queenSide : kingSide)))
                    score += uncontestedOutpost * Long.bitCount((pawns[0] | pawns[1]) & ((squareBB(square) & queenSide) != 0L ? queenSide : kingSide));
                else if ((bb & squareBB(square)) != 0L)
                    score += outpost[_int(pt.equals(PieceType.BISHOP))];
                else if (pt.equals(PieceType.KNIGHT) && (bb & attacks & ~board.bitboard(us)) != 0L)
                    score += reachableOutpost;


                if ((shiftDown(us, pawns[0] | pawns[1]) & squareBB(square)) != 0L)
                    score += minorBehindPawn;
                score -= kingProtector[_int(pt.equals(PieceType.BISHOP))] * ksq.distance(square);


                if (pt.equals(PieceType.BISHOP)) {
                    long blocked = pawns[us.ordinal()] & shiftDown(us, board.bitboard());
                    score -= bishopPawns[edgeDistance(square.file())] * board.piecesOnSameSquareColor(square, pawns[us.ordinal()])
                            * (_int((attackedBy[us.ordinal()][0] & squareBB(square)) == 0L) + Long.bitCount(blocked & centerFiles));
                    score -= bishopXRayPawns * Long.bitCount(bishopAttacks(square, 0L) & pawns[them.ordinal()]);


                    if (moreThanOne(bishopAttacks(square, pawns[0] | pawns[1]) & center))
                        score += longDiagonalBishop;
                }
            } else if (pt.equals(PieceType.ROOK)) {
                if (board.semiOpenFile(us, square))
                    score += openFileRook[_int(board.semiOpenFile(them, square))];
                else {
                    if ((pawns[us.ordinal()] & shiftDown(us, board.bitboard()) & fileBB(square)) != 0L)
                        score -= closedFileRook;


                    if (mobility <= 3) {
                        int kf = ksq.file().ordinal();
                        if ((kf < FILE_E.ordinal()) == (square.file().ordinal() < kf))
                            score -= trappedRook * (1 + _int(!board.hasCastlingRight(us)));
                    }
                }
            } else if (pt.equals(PieceType.QUEEN)) {
                if (board.sliderBlockers(square, bishops[them.ordinal()] | rooks[them.ordinal()]) != 0L)
                    score -= weakQueen;
            }
        }


        if (isTracing()) {
            String str = pt.toString().toLowerCase();
            set(str.substring(0, 1).toUpperCase() + str.substring(1) + "s", us.ordinal(), score);
        }


        return score;
    }


    private static int kingShelter(Side us, Square ksq) {
        Side them = us.flip();
        long bb = (pawns[0] | pawns[1]) & ~forwardRankBB(them, ksq.rank());
        long ourPawns = bb & board.bitboard(us) & ~pseudoAttacks(them, PieceType.PAWN, pawns[them.ordinal()]);
        long theirPawns = bb & board.bitboard(them);


        int bonus = S(5, 5);
        int center = clamp(ksq.file().ordinal(), File.FILE_B.ordinal(), File.FILE_G.ordinal());


        for (int file = center - 1; file <= center + 1; file++) {
            bb = ourPawns & fileBB(File.fileAt(file));
            int ourRank = bb == 0L ? 0 : relativeRank(us, frontMostSquare(them, bb).rank()).ordinal();


            bb = theirPawns & fileBB(File.fileAt(file));
            int theirRank = bb == 0L ? 0 : relativeRank(us, frontMostSquare(them, bb).rank()).ordinal();


            int distance = edgeDistance(File.fileAt(file));
            bonus += S(shelterStrength[distance][ourRank], 0);


            if (ourRank != 0 && (ourRank == theirRank - 1))
                bonus -= blockedStorm[theirRank];
            else
                bonus -= S(unblockedStorm[distance][theirRank], 0);
        }


        bonus -= kingOnFile[_int(board.semiOpenFile(us, ksq))][_int(board.semiOpenFile(them, ksq))];
        return bonus;
    }


    private static int kingSafety(Side us) {
        Square ksq = board.kingSquare(us);
        int shelter = kingShelter(us, ksq);
        long bb = 0L;


        if (board.hasKingCastlingRight(us))
            shelter = max(shelter, kingShelter(us, relativeSquare(us, G1)));
        if (board.hasQueenCastlingRight(us))
            shelter = max(shelter, kingShelter(us, relativeSquare(us, C1)));


        int minPawnDistance = 6;
        if ((pawns[us.ordinal()] & kingAttacks[ksq.ordinal()]) != 0L)
            minPawnDistance = 1;
        else {
            bb = pawns[us.ordinal()];
            while (bb != 0L) {
                Square square = Square.squareAt(lsb(bb));
                bb = extractLsb(bb);
                minPawnDistance = Math.min(minPawnDistance, ksq.distance(square));
            }
        }


        return shelter - S(0, 16 * minPawnDistance);
    }


    private static int king(Side us) {
        Side them = us.flip();
        Square ksq = board.kingSquare(us);
        int score = kingSafety(us);
        int kingDanger = 0;


        long unsafeChecks = 0L;
        long weak = attackedBy[them.ordinal()][6] & ~attackedBy2[us.ordinal()]
                & (~attackedBy[us.ordinal()][6] | attackedBy[us.ordinal()][5] | attackedBy[us.ordinal()][4]);
        long safe = ~board.bitboard(them);
        safe &= ~attackedBy[us.ordinal()][6] | (weak & attackedBy2[them.ordinal()]);


        long rookAttacks = rookAttacks(ksq, board.bitboard() ^ queens[us.ordinal()]);
        long bishopAttacks = bishopAttacks(ksq, board.bitboard() ^ queens[us.ordinal()]);


        long rookChecks = rookAttacks & attackedBy[them.ordinal()][3] & safe;
        if (rookChecks != 0L)
            kingDanger += safeCheck[2][_int(moreThanOne(rookChecks))];
        else
            unsafeChecks |= rookAttacks & attackedBy[them.ordinal()][3];


        long queenChecks = (rookAttacks | bishopAttacks) & attackedBy[them.ordinal()][4] & safe
                        & ~(attackedBy[us.ordinal()][4] | rookChecks);
        if (queenChecks != 0L)
            kingDanger += safeCheck[3][_int(moreThanOne(queenChecks))];


        long bishopChecks = bishopAttacks & attackedBy[them.ordinal()][2] & safe & ~queenChecks;
        if (bishopChecks != 0L)
            kingDanger += safeCheck[1][_int(moreThanOne(bishopChecks))];
        else
            unsafeChecks |= bishopAttacks & attackedBy[them.ordinal()][2];


        long knightChecks = knightAttacks[ksq.ordinal()] & attackedBy[them.ordinal()][1];
        if ((knightChecks & safe) != 0L)
            kingDanger += safeCheck[0][_int(moreThanOne(knightChecks & safe))];
        else
            unsafeChecks |= knightChecks;


        long camp = (us.equals(Side.WHITE) ? ~0L ^ rankBB(RANK_6) ^ rankBB(RANK_7) ^ rankBB(RANK_8)
                                           : ~0L ^ rankBB(RANK_3) ^ rankBB(RANK_2) ^ rankBB(RANK_1));
        long a = attackedBy[them.ordinal()][6] & kingFlank[ksq.file().ordinal()] & camp;
        long b = a & attackedBy2[them.ordinal()];
        long c = attackedBy[us.ordinal()][6] & kingFlank[ksq.file().ordinal()] & camp;


        int kingFlankAttack = Long.bitCount(a) + Long.bitCount(b);
        int kingFlankDefense = Long.bitCount(c);


        kingDanger += kingAttackersCount[them.ordinal()] * kingAttackersWeight[them.ordinal()]
                    + 183 * Long.bitCount(kingRing[us.ordinal()] & weak)
                    + 148 * Long.bitCount(unsafeChecks)
                    + 98 * Long.bitCount(board.sliderBlockers(ksq, board.bitboard(them)))
                    + 69 * kingAttacksCount[them.ordinal()]
                    + 3 * kingFlankAttack * kingFlankAttack / 8
                    + mgValue(mobilities[them.ordinal()] - mobilities[us.ordinal()])
                    - 873 * _int(Long.bitCount(queens[them.ordinal()]) == 0)
                    - 100 * _int((attackedBy[us.ordinal()][1] & attackedBy[us.ordinal()][5]) != 0L)
                    - 6 * mgValue(score) / 8
                    - 4 * kingFlankDefense
                    + 37;


        if (kingDanger > 100)
            score -= S(kingDanger * kingDanger / 4096, kingDanger / 16);


        score -= flankAttacks * kingFlankAttack;
        if (((pawns[0] | pawns[1]) & kingFlank[ksq.file().ordinal()]) == 0L)
            score -= pawnlessFlank;


        if(isTracing())
            set("King", us.ordinal(), score);


        return score;
    }


    private static int kingProximity(Side side, Square square) {
        return Math.min(board.kingSquare(side).distance(square), 5);
    }


    private static int passed(Side us) {
        Side them = us.flip();
        long materials = passedPawns[us.ordinal()];
        long bb = 0L;
        int score = 0;


        long blockedPassers = materials & shiftDown(us, pawns[them.ordinal()]);
        if (blockedPassers != 0L) {
            long helpers = shiftUp(us, pawns[us.ordinal()]) & ~board.bitboard(them) &
                    (~attackedBy2[them.ordinal()] | attackedBy[us.ordinal()][6]);
            materials &= ~blockedPassers | ((helpers & ~fileBB(FILE_A)) >> 1) | ((helpers & ~fileBB(FILE_H)) << 1);
        }


        while (materials != 0L) {
            int index = lsb(materials);
            materials = extractLsb(materials);
            Square square = Square.squareAt(index);


            int rank = relativeRank(us, square.rank()).ordinal();
            int bonus = passedRank[rank];


            if (rank > Rank.RANK_3.ordinal()) {
                int w = 5 * rank - 13;
                Square blockSquare = Square.squareAt((us.equals(Side.WHITE) ? square.ordinal() + 8 : square.ordinal() - 8));
                bonus += S(0, (kingProximity(them, blockSquare) * 19 / 4 - kingProximity(us, blockSquare) * 2) * w);


                if (rank != Rank.RANK_7.ordinal())
                    bonus -= S(0, kingProximity(us, Square.squareAt((us.equals(Side.WHITE) ? blockSquare.ordinal() + 8 : blockSquare.ordinal() - 8))) * w);


                if (board.pieceAt(square).equals(Piece.NONE)) {
                    long squaresToQueen = forwardFileBB(us, square);
                    long unsafeSquares = passedMaskBB(us, square);
                    bb = forwardFileBB(them, square) & ((rooks[0] | rooks[1]) | (queens[0] | queens[1]));


                    if ((board.bitboard(them) & bb) == 0L)
                        unsafeSquares &= attackedBy[them.ordinal()][6] | board.bitboard(them);


                    int k = unsafeSquares == 0L ? 36 :
                            (unsafeSquares & ~attackedBy[us.ordinal()][0]) == 0L ? 30 :
                                    (unsafeSquares & squaresToQueen) == 0L ? 17 :
                                            (unsafeSquares & squareBB(index)) == 0L ? 7 :
                                                    0;


                    if ((board.bitboard(us) & bb) != 0L || (attackedBy[us.ordinal()][6] & squareBB(index)) != 0L)
                        k += 5;


                    bonus += S(k * w, k * w);
                }
            }


            score += bonus - passedFile * edgeDistance(square.file());
        }


        if (isTracing())
            set("Passed", us.ordinal(), score);


        return score;
    }


    private static int threats(Side us) {
        Side them = us.flip();
        int score = 0;


        long bb = 0L;
        long nonPawnEnemies = board.bitboard(them) & ~pawns[them.ordinal()];
        long strong = attackedBy[them.ordinal()][0] | (attackedBy2[them.ordinal()] & ~attackedBy2[us.ordinal()]);
        long defended = nonPawnEnemies & strong;
        long weak = board.bitboard(them) & ~strong & attackedBy[us.ordinal()][6];


        if ((defended | weak) != 0L) {
            bb = (defended | weak) & (attackedBy[us.ordinal()][1] | attackedBy[us.ordinal()][2]);
            while (bb != 0L) {
                int index = lsb(bb);
                bb = extractLsb(bb);
                score += threatByMinor[board.pieceAt(squareAt(index)).pieceType().ordinal()];
            }


            bb = weak & attackedBy[us.ordinal()][3];
            while (bb != 0L) {
                int index = lsb(bb);
                bb = extractLsb(bb);
                score += threatByRook[board.pieceAt(squareAt(index)).pieceType().ordinal()];
            }


            if ((weak & attackedBy[us.ordinal()][5]) != 0L)
                score += threatByKing;


            bb = ~attackedBy[them.ordinal()][6] | (nonPawnEnemies & attackedBy2[us.ordinal()]);
            score += hanging * Long.bitCount(weak & bb);
            score += weakQueenProtection * Long.bitCount(weak & attackedBy[them.ordinal()][4]);
        }


        bb = attackedBy[them.ordinal()][6] & ~strong & attackedBy[us.ordinal()][6];
        score += restrictedPiece * Long.bitCount(bb);


        long safe = ~attackedBy[them.ordinal()][6] | attackedBy[us.ordinal()][6];
        bb = pawns[us.ordinal()] & safe;
        bb = pseudoAttacks(us, PieceType.PAWN, bb) & nonPawnEnemies;
        score += threatBySafePawn * Long.bitCount(bb);


        long TRank3 = bb & (us.equals(Side.WHITE) ? rankBB(RANK_3) : rankBB(RANK_6));
        bb = shiftUp(us, pawns[us.ordinal()]) & ~board.bitboard();
        bb |= shiftUp(us, bb & TRank3) & ~board.bitboard();
        bb &= ~attackedBy[them.ordinal()][0] & safe;
        bb = pseudoAttacks(us, PieceType.PAWN, bb) & nonPawnEnemies;
        score += threatByPawnPush * Long.bitCount(bb);


        if (Long.bitCount(queens[them.ordinal()]) == 1) {
            boolean queenImbalance = board.pieceCount(Piece.WHITE_QUEEN) + board.pieceCount(Piece.BLACK_QUEEN) == 1;
            Square square = squareAt(lsb(queens[them.ordinal()]));


            safe = mobilityArea[us.ordinal()] & ~pawns[us.ordinal()] & ~strong;
            bb = attackedBy[us.ordinal()][1] & knightAttacks[square.ordinal()];
            score += knightOnQueen * Long.bitCount(bb & safe) * (1 + _int(queenImbalance));


            bb = (attackedBy[us.ordinal()][2] & bishopAttacks(square, board.bitboard()) |
                    (attackedBy[us.ordinal()][3] & rookAttacks(square, board.bitboard())));
            score += sliderOnQueen * Long.bitCount(bb & safe & attackedBy2[us.ordinal()])
                    * (1 + _int(queenImbalance));
        }


        if (isTracing())
            set("Threats", us.ordinal(), score);


        return score;
    }


    private static int space(Side us) {
        if (board.nonPawnMaterial() < spaceThreshold)
            return 0;


        Side them = us.flip();
        long spaceMask = (us.equals(Side.WHITE) ? centerFiles & (rankBB(RANK_2) | rankBB(RANK_3) | rankBB(RANK_4))
                                                : centerFiles & (rankBB(RANK_7) | rankBB(RANK_6) | rankBB(RANK_5)));


        long safe = spaceMask & ~pawns[us.ordinal()] & ~attackedBy[them.ordinal()][0];
        long behind = pawns[us.ordinal()];
        behind |= shiftDown(us, behind);
        behind |= shiftDown(us, shiftDown(us, behind));


        int bonus = Long.bitCount(safe) + Long.bitCount(behind & safe & ~attackedBy[them.ordinal()][6]);
        int weight = Long.bitCount(board.bitboard(us)) - 3 + Math.min(blockedPawnCount, 9);
        int score = S(bonus * weight * weight / 16, 0);


        if (isTracing())
            set("Space", us.ordinal(), score);


        return score;
    }


    private static int forceMate(Side us) {
        Side them = us.flip();
        int score = 0;


        Square enemySquare = board.kingSquare(them);
        int enemyFile = enemySquare.file().ordinal();
        int enemyRank = enemySquare.rank().ordinal();
        int dstToCenterFile = Math.max(3 - enemyFile, enemyFile - 4);
        int dstToCenterRank = Math.max(3 - enemyRank, enemyRank - 4);
        score += dstToCenterFile + dstToCenterRank;


        Square allySquare = board.kingSquare();
        int allyFile = allySquare.file().ordinal();
        int allyRank = allySquare.rank().ordinal();
        int dstBetweenKingsFile = Math.abs(allyFile - enemyFile);
        int dstBetweenKingsRank = Math.abs(allyRank - enemyRank);
        score += 14 - (dstBetweenKingsFile + dstBetweenKingsRank);
        score += 8 - Long.bitCount(kingAttacks[enemySquare.ordinal()] & ~(board.bitboard(them) | attackedBy[us.ordinal()][6]));


        return score;
    }


    private static int winnable(int score) {
        int mg = mgValue(score);
        int eg = egValue(score);


        if (eg != 0) {
            Side strongSide = eg > 0 ? Side.WHITE : Side.BLACK;
            int npmStrong = strongSide.equals(Side.WHITE) ? board.nonPawnMaterial(Side.WHITE) : board.nonPawnMaterial(Side.BLACK);
            int npmWeak = strongSide.equals(Side.WHITE) ? board.nonPawnMaterial(Side.BLACK) : board.nonPawnMaterial(Side.WHITE);


            if (npmStrong >= mgValue(knight) * 2 && npmWeak == 0) {
                int value = eg > 0 ? forceMate(strongSide) : -forceMate(strongSide);
                value = value * npmStrong / 64;
                eg += value;
            }
        }


        int outflanking = board.kingSquare(Side.WHITE).file().distance(board.kingSquare(Side.BLACK).file())
                + (board.kingSquare(Side.WHITE).rank().ordinal() - board.kingSquare(Side.BLACK).rank().ordinal());
        int pawnsOnBothFlanks = _int(((pawns[0] | pawns[1]) & queenSide) != 0L && ((pawns[0] | pawns[1]) & kingSide) != 0L);
        int almostUnwinnable = _int(outflanking < 0 && pawnsOnBothFlanks == 0);
        int infiltration = _int(board.kingSquare(Side.WHITE).rank().ordinal() > RANK_4.ordinal() ||
                board.kingSquare(Side.BLACK).rank().ordinal() < RANK_5.ordinal());
        int complexity = 9 * passedPawnCount
                + 12 * Long.bitCount(pawns[0] | pawns[1])
                + 9 * outflanking
                + 21 * pawnsOnBothFlanks
                + 24 * infiltration
                + 51 * _int(board.nonPawnMaterial() == 0)
                - 43 * almostUnwinnable
                - 110;


        int u = (_int(mg > 0) - _int(mg < 0)) * clamp(complexity + 50, -Math.abs(mg), 0);
        int v = (_int(eg > 0) - _int(eg < 0)) * Math.max(complexity, -Math.abs(eg));


        mg += u;
        eg += v;


        Side strongSide = eg > 0 ? Side.WHITE : Side.BLACK;
        int npmW = board.nonPawnMaterial(Side.WHITE);
        int npmB = board.nonPawnMaterial(Side.BLACK);
        int SF = 64;


        if (Long.bitCount(pawns[0]) == 0 && npmW - npmB <= mgValue(bishop))
            SF = npmW < mgValue(rook) ? 0 : npmB <= mgValue(bishop) ? 4 : 14;


        if (SF == 64) {
            if (Long.bitCount(bishops[0]) == 1 && Long.bitCount(bishops[1]) == 1) {
                boolean oppositeBishops = board.piecesOnSameSquareColor(squareAt(lsb(bishops[0])), bishops[1]) == 0;
                if (oppositeBishops) {
                    if (npmW == mgValue(bishop) && npmB == mgValue(bishop))
                        SF = 18 + 4 * Long.bitCount(passedPawns[strongSide.ordinal()]);
                    else
                        SF = 22 + 3 * Long.bitCount(board.bitboard(strongSide));
                } else if (npmW == mgValue(rook) &&
                        npmB == mgValue(rook) &&
                        Long.bitCount(pawns[strongSide.ordinal()]) - Long.bitCount(pawns[strongSide.flip().ordinal()]) <= 1 &&
                        ((kingSide & pawns[strongSide.ordinal()]) == 0L) == ((queenSide & pawns[strongSide.ordinal()]) != 0L) &&
                        (kingAttacks[board.kingSquare(strongSide.flip()).ordinal()] & pawns[strongSide.flip().ordinal()]) != 0L)
                    SF = 36;
                else if (Long.bitCount(queens[0] | queens[1]) == 1)
                    SF = 37 + 3 * (Long.bitCount(queens[0]) == 1 ?
                            Long.bitCount(bishops[1]) + Long.bitCount(knights[1]) :
                            Long.bitCount(bishops[0]) + Long.bitCount(knights[0]));
                else
                    SF = Math.min(SF, 36 + 7 * Long.bitCount(pawns[strongSide.ordinal()]) - 4 * _int(pawnsOnBothFlanks == 0));
                SF -= 4 * _int(pawnsOnBothFlanks == 0);
            }
        }


        int mgl = 15258;
        int egl = 3915;
        int npm = clamp(board.nonPawnMaterial(), egl, mgl);
        int phase = ((npm - egl) * 128) / (mgl - egl);
        int value = (mg * phase + eg * (128 - phase * SF / 64)) / 128;


        if (isTracing()) {
            set("Winnable", 2, S(u, eg * SF / 64 - egValue(score)));
            set("Total", 2, S(mg, eg * SF / 64));
        }


        return value;
    }


    public static int evaluate(Board board) {
        Evaluator.board = board;
        update();
        int score = board.psqScore();
        score += imbalance();
        score += pawnEvaluation(Side.WHITE) - pawnEvaluation(Side.BLACK);


        if (lazySkip(score, lazyThreshold1) || isTracing()) {
            initialize(Side.WHITE);
            initialize(Side.BLACK);


            score += pieces(Side.WHITE, PieceType.KNIGHT) - pieces(Side.BLACK, PieceType.KNIGHT)
                   + pieces(Side.WHITE, PieceType.BISHOP) - pieces(Side.BLACK, PieceType.BISHOP)
                   + pieces(Side.WHITE, PieceType.ROOK) - pieces(Side.BLACK, PieceType.ROOK)
                   + pieces(Side.WHITE, PieceType.QUEEN) - pieces(Side.BLACK, PieceType.QUEEN);


            score += mobilities[0] - mobilities[1];
            score += king(Side.WHITE) - king(Side.BLACK);
            score += passed(Side.WHITE) - passed(Side.BLACK);


            if (lazySkip(score, lazyThreshold2) | isTracing()) {
                score += threats(Side.WHITE) - threats(Side.BLACK);
                score += space(Side.WHITE) - space(Side.BLACK);
            }
        }


        if (isTracing()) {
            set("Material", 2, board.psqScore());
            set("Mobility", 0, mobilities[0]);
            set("Mobility", 1, mobilities[1]);
        }


        int value = winnable(score);
        value = (value / 16) * 16;
        value += (board.sideToMove().equals(Side.WHITE) ? 28 : -28);
        value = (board.sideToMove().equals(Side.WHITE) ? value : -value);
        value = value * (100 - board.fiftyMove()) / 100;
        return value;
    }


    public static void trace(Board board) {
        startTracing();
        double eval = evaluate(board) / (double) Score.egValue(ScoreConstants.pawn);
        endTracing();
        printTrace();
        clear();
        System.out.printf("Evaluation: %.2f (white side)", (board.sideToMove().equals(Side.WHITE) ? eval : -eval));
        System.out.println();
    }


}
