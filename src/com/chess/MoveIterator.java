package com.chess;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.chess.Bitboard.*;
import static com.chess.Bitboard.squareBB;
import static com.chess.Move.*;
import static com.chess.Rank.*;
import static com.chess.SearchConstants.*;
import static com.chess.Square.*;
import static com.chess.ScoreConstants.*;
import static com.chess.Score.*;

public class MoveIterator {

    public static int[] lowPlyHistory = new int[4 * 64 * 64];
    public static int[] butterflyHistory = new int[2 * 64 * 64];
    public static int[] captureHistory = new int[12 * 64 * 6];
    public static int[] counterMoves = new int[12 * 64];


    private static final int[] plyOffsets = {1, 2, 4, 6};
    private static final int[] MVV_LVA = {
            105, 205, 305, 405, 505, 605,
            104, 204, 304, 404, 504, 604,
            103, 203, 303, 403, 503, 603,
            102, 202, 302, 402, 502, 602,
            101, 201, 301, 401, 501, 601,
            100, 200, 300, 400, 500, 600,
    };


    private enum Stage {
        TT,
        PV,
        GOOD_CAPTURES_PROMOS,
        EQUAL_CAPTURES,
        KILLER1,
        KILLER2,
        KILLER3,
        KILLER4,
        CM,
        NON_CAPTURES,
        BAD_CAPTURES,
        END;


        public Stage increment() {
            return allStages[ordinal() + 1];
        }
        public static Stage[] allStages = values();
    }


    private enum GenerateType {
        ALL_MOVES,
        CAPTURES,
        NON_CAPTURES,
        PROMOS,
        CHECKS
    }


    private final Engine engine;
    private final Board board;
    private Stage stage;


    private int lastMove;
    private int lastMoveScore;
    private int lastMoveSee;
    private int goodCaptureIndex;
    private int equalCaptureIndex;
    private int badCaptureIndex;
    private int nonCaptureIndex;
    private int ttMove;
    private int pvMove;
    private int prevMove;
    private int killerMove1;
    private int killerMove2;
    private int killerMove3;
    private int killerMove4;
    private int counterMove;


    private boolean lastMoveKiller;
    private boolean foundTT;
    private boolean foundPV;
    private boolean foundKM1;
    private boolean foundKM2;
    private boolean foundKM3;
    private boolean foundKM4;
    private boolean foundCM;


    private final int[] goodCaptures;
    private final int[] goodCapturesSee;
    private final int[] goodCapturesScore;
    private final int[] equalCaptures;
    private final int[] equalCapturesSee;
    private final int[] equalCapturesScore;
    private final int[] badCaptures;
    private final int[] badCapturesSee;
    private final int[] badCapturesScores;
    private final int[] nonCaptures;
    private final int[] nonCapturesSee;
    private final int[] nonCaptureScore;
    private Node node;


    public MoveIterator(Engine engine) {
        this.engine = engine;
        board = engine.board();


        clear();
        goodCaptures = new int[maxMoves];
        goodCapturesSee = new int[maxMoves];
        goodCapturesScore = new int[maxMoves];
        equalCaptures = new int[maxMoves];
        equalCapturesSee = new int[maxMoves];
        equalCapturesScore = new int[maxMoves];
        badCaptures = new int[maxMoves];
        badCapturesSee = new int[maxMoves];
        badCapturesScores = new int[maxMoves];
        nonCaptures = new int[maxMoves];
        nonCapturesSee = new int[maxMoves];
        nonCaptureScore = new int[maxMoves];
    }


    public int lastMoveScore() {
        return lastMoveScore;
    }
    public int lastMoveSee() {
        return lastMoveSee;
    }
    public boolean isLastMoveKiller() {
        return lastMoveKiller;
    }


    public void clear() {
        stage = Stage.TT;
        goodCaptureIndex = 0;
        equalCaptureIndex = 0;
        badCaptureIndex = 0;
        nonCaptureIndex = 0;
        lastMove = 0;
        lastMoveScore = 0;
        lastMoveSee = 0;
        ttMove = 0;
        pvMove = 0;
        prevMove = 0;
        killerMove1 = 0;
        killerMove2 = 0;
        killerMove3 = 0;
        killerMove4 = 0;
        counterMove = 0;
        lastMoveKiller = false;
        foundTT = false;
        foundPV = false;
        foundKM1 = false;
        foundKM2 = false;
        foundKM3 = false;
        foundKM4 = false;
        foundCM = false;
        node = engine.currentNode();
    }


    public void reset() {
        clear();
        Arrays.fill(goodCaptures, 0);
        Arrays.fill(goodCapturesSee, 0);
        Arrays.fill(goodCapturesScore, 0);
        Arrays.fill(equalCaptures, 0);
        Arrays.fill(equalCapturesSee, 0);
        Arrays.fill(equalCapturesScore, 0);
        Arrays.fill(badCaptures, 0);
        Arrays.fill(badCapturesSee, 0);
        Arrays.fill(badCapturesScores, 0);
        Arrays.fill(nonCaptures, 0);
        Arrays.fill(nonCapturesSee, 0);
        Arrays.fill(nonCaptureScore, 0);
    }


    public void initialize() {
        clear();
        int ply = engine.ply();
        ttMove = node.ttMove;
        pvMove = engine.rootMove().pvTable[ply];
        prevMove = (ply > 0 ? engine.tree()[ply - 1].currentMove : 0);
        killerMove1 = node.killerMove1;
        killerMove2 = node.killerMove2;
        killerMove3 = (ply >= 2 ? engine.tree()[ply - 2].killerMove1 : 0);
        killerMove4 = (ply >= 2 ? engine.tree()[ply - 2].killerMove2 : 0);
        generateMoves();
    }


    public int next() {
        switch (stage) {
            case TT -> {
                stage = stage.increment();
                if (foundTT) {
                    lastMove = ttMove;
                    lastMoveSee = board.see(ttMove);
                    return ttMove;
                }
                return next();
            }
            case PV -> {
                stage = stage.increment();
                if (foundPV) {
                    foundPV = false;
                    lastMove = pvMove;
                    lastMoveSee = board.see(pvMove);
                    return pvMove;
                }
                return next();
            }
            case GOOD_CAPTURES_PROMOS -> {
                lastMove = pickMoveFromArray(goodCaptureIndex, goodCaptures, goodCapturesScore, goodCapturesSee);
                if (lastMove != 0)
                    return lastMove;
                stage = stage.increment();
                return next();
            }
            case EQUAL_CAPTURES -> {
                lastMove = pickMoveFromArray(equalCaptureIndex, equalCaptures, equalCapturesScore, equalCapturesSee);
                if (lastMove != 0)
                    return lastMove;
                stage = stage.increment();
                return next();
            }
            case KILLER1 -> {
                stage = stage.increment();
                lastMoveKiller = true;
                if (foundKM1) {
                    lastMove = killerMove1;
                    lastMoveSee = board.see(killerMove1);
                    return killerMove1;
                }
                return next();
            }
            case KILLER2 -> {
                stage = stage.increment();
                if (foundKM2) {
                    lastMove = killerMove2;
                    lastMoveSee = board.see(killerMove2);
                    return killerMove2;
                }
                return next();
            }
            case KILLER3 -> {
                stage = stage.increment();
                if (foundKM3) {
                    lastMove = killerMove3;
                    lastMoveSee = board.see(killerMove3);
                    return killerMove3;
                }
                return next();
            }
            case KILLER4 -> {
                stage = stage.increment();
                if (foundKM4) {
                    lastMove = killerMove4;
                    lastMoveSee = board.see(killerMove4);
                    return killerMove4;
                }
                return next();
            }
            case CM -> {
                stage = stage.increment();
                if (foundCM) {
                    lastMove = counterMove;
                    lastMoveSee = board.see(counterMove);
                    return counterMove;
                }
                return next();
            }
            case NON_CAPTURES -> {
                lastMove = pickMoveFromArray(nonCaptureIndex, nonCaptures, nonCaptureScore, nonCapturesSee);
                if (lastMove != 0)
                    return lastMove;
                stage = stage.increment();
                return next();
            }
            case BAD_CAPTURES -> {
                lastMove = pickMoveFromArray(badCaptureIndex, badCaptures, badCapturesScores, badCapturesSee);
                if (lastMove != 0)
                    return lastMove;
                stage = stage.increment();
                return next();
            }
        }


        return 0;
    }


    private int pickMoveFromArray(int length, int[] moves, int[] scores, int[] seeScores) {
        if (length == 0)
            return 0;


        int maxScore = -infinity;
        int bestIndex = -1;


        for (int i = 0; i < length; i++) {
            if (scores[i] > maxScore) {
                maxScore = scores[i];
                bestIndex = i;
            }
        }


        if (bestIndex != -1) {
            int move = moves[bestIndex];
            lastMove = move;
            lastMoveScore = maxScore;
            lastMoveSee = seeScores[bestIndex];
            moves[bestIndex] = 0;
            scores[bestIndex] = -infinity;
            return move;
        }


        return 0;
    }


    private void addMove(int move) {
        if (move == ttMove) {
            foundTT = true;
            return;
        }


        if (move == pvMove) {
            foundPV = true;
            return;
        }


        final Square from = from(move);
        final Square to = to(move);
        final Piece movingPiece = movingPiece(move);
        final Piece promotion = promotion(move);
        final boolean isCapture = isCapture(move);
        final boolean isPush = isPush(move);
        final boolean isEnPassant = isEnPassant(move);
        final boolean isCastling = isCastling(move);
        final int see = board.see(move);


        Piece capturedPiece = board.pieceAt(to);
        if (capturedPiece.equals(Piece.NONE) && to.equals(board.enPassant()) && movingPiece.pieceType().equals(PieceType.PAWN))
            capturedPiece = board.pieceAt(board.enPassantTarget());


        if (!isCapture) {
            if (move == killerMove1) {
                foundKM1 = true;
                return;
            }
            if (move == killerMove2) {
                foundKM2 = true;
                return;
            }
            if (move == killerMove3) {
                foundKM3 = true;
                return;
            }
            if (move == killerMove4) {
                foundKM4 = true;
                return;
            }
        }


        if (!isCapture) {
            if (move == counterMoves[movingPiece(prevMove).ordinal() * 64 + to(prevMove).ordinal()]) {
                counterMove = move;
                foundCM = true;
                return;
            }
        }


        if (isTactical(move)) {
            int score = 0;
            if (isCapture)
                score = mgValue(pieceScores[capturedPiece.pieceType().ordinal()]) - mgValue(pieceScores[movingPiece.pieceType().ordinal()])
                      + captureHistory[capturedPiece.pieceType().ordinal() + 6 * (to.ordinal() + 64 * movingPiece.ordinal())];


            if (!promotion.equals(Piece.NONE))
                score += egValue(pieceScores[promotion.pieceType().ordinal()]);


            if (see > 0 || !promotion.equals(Piece.NONE)) {
                goodCaptures[goodCaptureIndex] = move;
                goodCapturesScore[goodCaptureIndex] = score;
                goodCapturesSee[goodCaptureIndex] = see;
                goodCaptureIndex++;
            } else if (see == 0) {
                equalCaptures[equalCaptureIndex] = move;
                equalCapturesScore[equalCaptureIndex] = score;
                equalCapturesSee[equalCaptureIndex] = see;
                equalCaptureIndex++;
            } else {
                badCaptures[badCaptureIndex] = move;
                badCapturesScores[badCaptureIndex] = score;
                badCapturesSee[badCaptureIndex] = see;
                badCaptureIndex++;
            }
            return;
        }


        nonCaptures[nonCaptureIndex] = move;
        nonCaptureScore[nonCaptureIndex] = butterflyHistory[to.ordinal() + 64 * (from.ordinal() + 64 * board.sideToMove().ordinal())];


        int ply = engine.ply();
        for (int offset : plyOffsets) {
            if (ply - offset >= 0) {
                int score = engine.tree()[ply - offset].quietHistory[movingPiece.ordinal() * 64 + to.ordinal()];
                score = (offset == 1 ? score * 2 : score);
                nonCaptureScore[nonCaptureIndex] += score;
            }
        }


        nonCapturesSee[nonCaptureIndex] = see;
        nonCaptureIndex++;
    }


    public void generatePawnMoves(GenerateType type) {
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
                    if (type.equals(GenerateType.ALL_MOVES) || type.equals(GenerateType.PROMOS)) {
                        addMove(encodeMove(from, to, ourPawn, Piece.encodePiece(us, PieceType.QUEEN), 0, 0, 0, 0));
                        addMove(encodeMove(from, to, ourPawn, Piece.encodePiece(us, PieceType.ROOK), 0, 0, 0, 0));
                        addMove(encodeMove(from, to, ourPawn, Piece.encodePiece(us, PieceType.BISHOP), 0, 0, 0, 0));
                        addMove(encodeMove(from, to, ourPawn, Piece.encodePiece(us, PieceType.KNIGHT), 0, 0, 0, 0));
                    }
                } else {
                    if (type.equals(GenerateType.ALL_MOVES) || type.equals(GenerateType.NON_CAPTURES)) {
                        // one push move
                        addMove(encodeMove(from, to, ourPawn, Piece.NONE, 0, 0, 0, 0));
                        if ((rankBB(from) & pushBB) != 0L && (squareAt(to.ordinal() + pawnPush).bitboard() & board.bitboard()) == 0L)
                            // two push move
                            addMove(encodeMove(from, squareAt(to.ordinal() + pawnPush), ourPawn, Piece.NONE, 0, 1, 0, 0));
                    }
                }
            }
        }
    }


    public void generatePawnCaptures() {
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
                addMove(encodeMove(from, board.enPassant(), ourPawn, Piece.NONE, 1, 0 , 1, 0));


            attacks &= board.bitboard(them);


            while (attacks != 0L) {
                Square to = squareAt(lsb(attacks));
                attacks = extractLsb(attacks);


                // pawn capture with promotion
                if ((rankBB(from) & promotionBB) != 0L) {
                    addMove(encodeMove(from, to, ourPawn, Piece.encodePiece(us, PieceType.QUEEN), 1, 0, 0, 0));
                    addMove(encodeMove(from, to, ourPawn, Piece.encodePiece(us, PieceType.ROOK), 1, 0, 0, 0));
                    addMove(encodeMove(from, to, ourPawn, Piece.encodePiece(us, PieceType.BISHOP), 1, 0, 0, 0));
                    addMove(encodeMove(from, to, ourPawn, Piece.encodePiece(us, PieceType.KNIGHT), 1, 0, 0, 0));
                } else
                    addMove(encodeMove(from, to, ourPawn, Piece.NONE, 1, 0, 0, 0));
            }
        }
    }


    public void generateKnightMoves(GenerateType type) {
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


                if ((to.bitboard() & board.bitboard(them)) != 0L) {
                    if (type.equals(GenerateType.ALL_MOVES) || type.equals(GenerateType.CAPTURES))
                        addMove(encodeMove(from, to, ourKnight, Piece.NONE, 1, 0, 0, 0));
                } else {
                    if (type.equals(GenerateType.ALL_MOVES) || type.equals(GenerateType.NON_CAPTURES))
                        addMove(encodeMove(from, to, ourKnight, Piece.NONE, 0, 0, 0, 0));
                }
            }
        }
    }


    public void generateBishopAttacks(GenerateType type) {
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


                if ((to.bitboard() & board.bitboard(them)) != 0L) {
                    if (type.equals(GenerateType.ALL_MOVES) || type.equals(GenerateType.CAPTURES))
                        addMove(encodeMove(from, to, piece, Piece.NONE, 1, 0, 0, 0));
                } else {
                    if (type.equals(GenerateType.ALL_MOVES) || type.equals(GenerateType.NON_CAPTURES))
                        addMove(encodeMove(from, to, piece, Piece.NONE, 0, 0, 0, 0));
                }
            }
        }
    }


    public void generateRookAttacks(GenerateType type) {
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


                if ((to.bitboard() & board.bitboard(them)) != 0L) {
                    if (type.equals(GenerateType.ALL_MOVES) || type.equals(GenerateType.CAPTURES))
                        addMove(encodeMove(from, to, piece, Piece.NONE, 1, 0, 0, 0));
                } else {
                    if (type.equals(GenerateType.ALL_MOVES) || type.equals(GenerateType.NON_CAPTURES))
                        addMove(encodeMove(from, to, piece, Piece.NONE, 0, 0, 0, 0));
                }
            }
        }
    }


    public void generateKingMoves(GenerateType type) {
        Side us = board.sideToMove();
        Side them = us.flip();
        Piece ourKing = Piece.encodePiece(us, PieceType.KING);


        long bb = board.bitboard(ourKing);
        Square from = squareAt(lsb(bb));


        long attacks = kingAttacks[from.ordinal()] & ~board.bitboard(us);
        while (attacks != 0L) {
            Square to = squareAt(lsb(attacks));
            attacks = extractLsb(attacks);


            if ((to.bitboard() & board.bitboard(them)) != 0L) {
                if (type.equals(GenerateType.ALL_MOVES) || type.equals(GenerateType.CAPTURES))
                    addMove(encodeMove(from, to, ourKing, Piece.NONE, 1, 0, 0, 0));
            } else {
                if (type.equals(GenerateType.ALL_MOVES) || type.equals(GenerateType.NON_CAPTURES))
                    addMove(encodeMove(from, to, ourKing, Piece.NONE, 0, 0, 0, 0));
            }
        }
    }


    public void generateCastlingMoves() {
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
                    addMove(encodeMove(relativeSquare(us, E1), relativeSquare(us, G1), ourKing, Piece.NONE, 0, 0, 0, 1));
            }
        }


        // check if king can castle queen side
        if ((board.castlingRight() & queenCastling) != 0) {
            if ((board.bitboard() & squareBB(relativeSquare(us, D1))) == 0L && (board.bitboard() & squareBB(relativeSquare(us, C1))) == 0L && (board.bitboard() & squareBB(relativeSquare(us, B1))) == 0L) {
                if (!board.isSquareAttackedBy(them, relativeSquare(us, E1)) && !board.isSquareAttackedBy(them, relativeSquare(us, D1)))
                    addMove(encodeMove(relativeSquare(us, E1), relativeSquare(us, C1), ourKing, Piece.NONE, 0, 0, 0, 1));
            }
        }
    }


    public void generateMoves() {
        generatePawnCaptures();
        generatePawnMoves(GenerateType.ALL_MOVES);
        generateKnightMoves(GenerateType.ALL_MOVES);
        generateBishopAttacks(GenerateType.ALL_MOVES);
        generateRookAttacks(GenerateType.ALL_MOVES);
        generateKingMoves(GenerateType.ALL_MOVES);
        generateCastlingMoves();
    }


    public void generateCaptures() {
        generatePawnCaptures();
        generatePawnMoves(GenerateType.PROMOS);
        generateKnightMoves(GenerateType.CAPTURES);
        generateBishopAttacks(GenerateType.CAPTURES);
        generateRookAttacks(GenerateType.CAPTURES);
        generateKingMoves(GenerateType.CAPTURES);
    }


    public void generateNonCaptures() {
        generatePawnMoves(GenerateType.NON_CAPTURES);
        generateKnightMoves(GenerateType.NON_CAPTURES);
        generateBishopAttacks(GenerateType.NON_CAPTURES);
        generateRookAttacks(GenerateType.NON_CAPTURES);
        generateKingMoves(GenerateType.NON_CAPTURES);
        generateCastlingMoves();
    }


}
