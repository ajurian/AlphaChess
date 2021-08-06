package com.chess;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.chess.Bitboard.*;
import static com.chess.Piece.*;
import static com.chess.Rank.*;
import static com.chess.File.*;
import static com.chess.Square.*;
import static com.chess.Move.*;
import static com.chess.ScoreConstants.*;
import static com.chess.Score.*;
import static com.chess.SearchConstants.*;

public class Board {

    public static final long[][] pieceKeys = new long[12][64];
    public static final long[] enPassantKeys = new long[64];
    public static final long[] castlingKeys = new long[16];
    public static final long sideKey;


    static {
        for (int piece = 0; piece < 12; piece++)
            for (int square = 0; square < 64; square++)
                pieceKeys[piece][square] = random64();
        for (int square = 0; square < 64; square++)
            enPassantKeys[square] = random64();
        for (int i = 0; i <= 15; i++)
            castlingKeys[i] = random64();
        sideKey = random64();
    }


    private final LinkedList<Backup> backups;
    private final Square[] kingSquares;
    private final long[] repetitionTable;
    private int repetitionIndex;


    private Side sideToMove;
    private Square enPassant;
    private Square enPassantTarget;
    private final Piece[] pieces;
    private final long[] bitboards;
    private final long[] typeBitboards;
    private final long[] occupancies;
    private long hashKey;
    private int castlingRight;
    private int fiftyMove;
    private int moveCounter;
    private int gamePly;


    private final int[] pieceCount;
    private final int[] nonPawnMaterial;
    private final int[] psqScore;


    public Board() {
        backups = new LinkedList<>();
        kingSquares = new Square[2];


        // 11797 is the max number of plies (5898 moves) in game
        repetitionTable = new long[11797];
        repetitionIndex = 0;


        pieces = new Piece[64];
        Arrays.fill(pieces, Piece.NONE);


        bitboards = new long[12];
        Arrays.fill(bitboards, 0L);


        typeBitboards = new long[6];
        Arrays.fill(typeBitboards, 0L);


        occupancies = new long[2];
        Arrays.fill(occupancies, 0L);


        sideToMove = Side.WHITE;
        enPassant = Square.NONE;
        enPassantTarget = Square.NONE;


        hashKey = 0L;
        castlingRight = 0;
        fiftyMove = 0;
        moveCounter = 0;
        gamePly = 0;


        pieceCount = new int[12];
        nonPawnMaterial = new int[2];
        psqScore = new int[2];


        setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }


    public void setSideToMove(Side sideToMove) {
        this.sideToMove = sideToMove;
    }
    public void setEnPassant(Square enPassant) {
        this.enPassant = enPassant;
    }
    public void setEnPassantTarget(Square enPassantTarget) {
        this.enPassantTarget = enPassantTarget;
    }
    public void setHashKey(long hashKey) {
        this.hashKey = hashKey;
    }
    public void setCastlingRight(int castlingRight) {
        this.castlingRight = castlingRight;
    }
    public void setFiftyMove(int fiftyMove) {
        this.fiftyMove = fiftyMove;
    }
    public void setMoveCounter(int moveCounter) {
        this.moveCounter = moveCounter;
    }
    public void setGamePly(int gamePly) {
        this.gamePly = gamePly;
    }


    public LinkedList<Backup> backups() {
        return backups;
    }
    public Piece pieceAt(Square square) {
        return pieces[square.ordinal()];
    }
    public Side sideToMove() {
        return sideToMove;
    }
    public Square enPassant() {
        return enPassant;
    }
    public Square enPassantTarget() {
        return enPassantTarget;
    }
    public Piece[] pieces() {
        return pieces;
    }
    public long[] bitboards() {
        return bitboards;
    }
    public long[] typeBitboards() {
        return typeBitboards;
    }
    public long[] occupancies() {
        return occupancies;
    }
    public int castlingRight() {
        return castlingRight;
    }
    public long hashKey() {
        return hashKey;
    }
    public int fiftyMove() {
        return fiftyMove;
    }
    public int moveCounter() {
        return moveCounter;
    }
    public int gamePly() {
        return gamePly;
    }


    public int[] pieceCount() {
        return pieceCount;
    }


    public void resetBoard() {
        backups.clear();
        Arrays.fill(kingSquares, Square.NONE);
        Arrays.fill(repetitionTable, 0L);
        repetitionIndex = 0;


        Arrays.fill(pieces, Piece.NONE);
        Arrays.fill(bitboards, 0L);
        Arrays.fill(typeBitboards, 0L);
        Arrays.fill(occupancies, 0L);


        sideToMove = Side.WHITE;
        enPassant = Square.NONE;
        enPassantTarget = Square.NONE;


        hashKey = 0L;
        castlingRight = 0;
        fiftyMove = 0;
        moveCounter = 0;
        gamePly = 0;


        Arrays.fill(pieceCount, 0);
        Arrays.fill(nonPawnMaterial, 0);
        Arrays.fill(psqScore, 0);
    }


    public void setFen(String fen) {
        resetBoard();


        // split fen into parts
        String[] parts = fen.split(" ");
        int rank = 7;


        // set pieces on board
        for (String r : parts[0].split("/")) {
            int file = 0;
            for (int i = 0; i < r.length(); i++) {
                char c = r.charAt(i);


                if (Character.isDigit(c))
                    file += c - '0';
                else {
                    Square square = encodeSquare(rankAt(rank), fileAt(file));
                    Piece piece = encodePiece(c + "");


                    if (piece.pieceType().equals(PieceType.KING))
                        kingSquares[piece.pieceSide().ordinal()] = square;


                    setPiece(piece, square);
                    file++;
                }
            }
            rank--;
        }


        // set side to move
        sideToMove = (parts[1].equalsIgnoreCase("w") ? Side.WHITE : Side.BLACK);


        // set castling right
        for (String c : parts[2].split("")) {
            switch (c) {
                case "K" -> castlingRight |= CastlingRight.WHITE_KING.bit();
                case "Q" -> castlingRight |= CastlingRight.WHITE_QUEEN.bit();
                case "k" -> castlingRight |= CastlingRight.BLACK_KING.bit();
                case "q" -> castlingRight |= CastlingRight.BLACK_QUEEN.bit();
            }
        }


        // set enPassant
        if (!parts[3].equals("-")) {
            Rank R = rankAt(parts[3].charAt(1) - '1');
            File F = fileAt(parts[3].toLowerCase().charAt(0) - 'a');
            Square square = encodeSquare(R, F);


            enPassant = square;
            enPassantTarget = sideToMove.equals(Side.WHITE)
                            ? squareAt(square.ordinal() + 8)
                            : squareAt(square.ordinal() - 8);
        }


        // set fifty move
        fiftyMove = parts[4].charAt(0) - '0';
        // set move counter
        moveCounter = parts[5].charAt(0) - '0';
        // initialize hash key
        hashKey = generateHashKey();
        repetitionTable[repetitionIndex++] = hashKey;
    }


    public String generateFen() {
        StringBuilder builder = new StringBuilder();


        for (int rank = 7; rank >= 0; rank--) {
            int empty = 0;
            for (int file = 0; file < 8; file++) {
                int index = rank * 8 + file;
                Piece piece = pieces[index];


                if (piece.equals(Piece.NONE))
                    empty++;
                else {
                    if (empty > 0) {
                        builder.append(empty);
                        empty = 0;
                    }
                    builder.append(piece.symbol());
                }
            }
            if (empty > 0)
                builder.append(empty);
            if (rank > 0)
                builder.append('/');
            else
                builder.append(' ');
        }


        builder.append((sideToMove.toString().charAt(0) + "").toLowerCase())
                .append(' ');


        if (castlingRight == 0)
            builder.append('-');
        else {
            for (int i = 0; i < 4; i++) {
                int index = 1 << i;
                if ((castlingRight & index) != 0) {
                    builder.append((
                            index == 1 ? 'K' :
                            index == 2 ? 'Q' :
                            index == 4 ? 'k' :
                            index == 8 ? 'q' :
                            ""
                            ));
                }
            }
        }
        builder.append(' ');


        if (enPassant == Square.NONE)
            builder.append('-');
        else
            builder.append(enPassant.toString().toLowerCase());
        builder.append(' ');


        builder.append(fiftyMove)
                .append(' ')
                .append(moveCounter);


        return builder.toString();
    }


    public long sliderBlockers(Square square, long sliders) {
        long queens = bitboard(Piece.WHITE_QUEEN) | bitboard(Piece.BLACK_QUEEN);
        long rooks = bitboard(Piece.WHITE_ROOK) | bitboard(Piece.BLACK_ROOK);
        long bishops = bitboard(Piece.WHITE_BISHOP) | bitboard(Piece.BLACK_BISHOP);
        long snipers = ((rookAttacks(square, 0L) & (queens | rooks)) |
                        (bishopAttacks(square, 0L) & (queens | bishops))) & sliders;
        long occupancy = bitboard() & ~snipers & ~squareBB(square);
        long blockers = 0L;


        while (snipers != 0L) {
            Square sniperSquare = squareAt(lsb(snipers));
            snipers = extractLsb(snipers);


            long bb = lineBB[square.ordinal()][sniperSquare.ordinal()]
                    & bitsBetween(occupancy, square.ordinal(), sniperSquare.ordinal());
            if (bb != 0L && !moreThanOne(bb))
                blockers |= bb;
        }


        return blockers;
    }


    public boolean isPinned(Side them, Square square) {
        return (sliderBlockers(kingSquare(them.flip()), bitboard(them)) & squareBB(square)) != 0L;
    }


    public long attackersToSquare(Side them, Square square) {
        return attackersToSquare(square, bitboard(them));
    }


    public long attackersToSquare(Square square, long occupancy) {
        long result = (pawnAttacks[0][square.ordinal()] & bitboard(BLACK_PAWN) |
                    pawnAttacks[1][square.ordinal()] & bitboard(WHITE_PAWN)) & occupancy;
        result |= knightAttacks[square.ordinal()] & bitboard(PieceType.KNIGHT) & occupancy;
        result |= bishopAttacks(square, occupancy) & bitboard(PieceType.BISHOP, PieceType.QUEEN) & occupancy;
        result |= rookAttacks(square, occupancy) & bitboard(PieceType.ROOK, PieceType.QUEEN) & occupancy;
        result |= kingAttacks[square.ordinal()] & bitboard(PieceType.KING) & occupancy;
        return result;
    }


    public long attackersXrayToSquare(Side them, Square square) {
        return attackersXrayToSquare(square, bitboard(them));
    }


    public long attackersXrayToSquare(Square square, long occupancy) {
        long result = (pawnAttacks[0][square.ordinal()] & bitboard(BLACK_PAWN) |
                    pawnAttacks[1][square.ordinal()] & bitboard(WHITE_PAWN)) & occupancy;
        result |= knightAttacks[square.ordinal()] & bitboard(PieceType.KNIGHT) & occupancy;
        result |= bishopAttacks(square, occupancy & ~bitboard(PieceType.BISHOP, PieceType.QUEEN))
                & bitboard(PieceType.BISHOP, PieceType.QUEEN) & occupancy;
        result |= rookAttacks(square, occupancy & ~bitboard(PieceType.ROOK, PieceType.QUEEN))
                & bitboard(PieceType.ROOK, PieceType.QUEEN) & occupancy;
        result |= kingAttacks[square.ordinal()] & bitboard(PieceType.KING) & occupancy;
        return result;
    }


    public boolean isSquareAttackedBy(Side them, Square square) {
        Side us = them.flip();
        if ((pawnAttacks[us.ordinal()][square.ordinal()] & bitboard(encodePiece(them, PieceType.PAWN))) != 0L)
            return true;
        if ((knightAttacks[square.ordinal()] & bitboard(encodePiece(them, PieceType.KNIGHT))) != 0L)
            return true;
        if ((bishopAttacks(square, bitboard()) & (bitboard(encodePiece(them, PieceType.BISHOP)) |
                                                  bitboard(encodePiece(them, PieceType.QUEEN)))) != 0L)
            return true;
        if ((rookAttacks(square, bitboard()) & (bitboard(encodePiece(them, PieceType.ROOK)) |
                                                bitboard(encodePiece(them, PieceType.QUEEN)))) != 0L)
            return true;
        return (kingAttacks[square.ordinal()] & bitboard(encodePiece(them, PieceType.KING))) != 0L;
    }


    public boolean isSquareAttackedBy(Square square, long occupancy) {
        if (((pawnAttacks[0][square.ordinal()] & bitboard(BLACK_PAWN) & occupancy) |
                (pawnAttacks[1][square.ordinal()] & bitboard(WHITE_PAWN) & occupancy)) != 0L)
            return true;
        if ((knightAttacks[square.ordinal()] & bitboard(PieceType.KNIGHT) & occupancy) != 0L)
            return true;
        if ((bishopAttacks(square, occupancy) & bitboard(PieceType.BISHOP, PieceType.QUEEN) & occupancy) != 0L)
            return true;
        if ((rookAttacks(square, occupancy) & bitboard(PieceType.ROOK, PieceType.QUEEN) & occupancy) != 0L)
            return true;
        return (kingAttacks[square.ordinal()] & bitboard(PieceType.KING) & occupancy) != 0L;
    }


    public long generateHashKey() {
        long key = 0L;


        for (int piece = 0; piece < 12; piece++) {
            long bb = bitboards[piece];
            while (bb != 0L) {
                int index = lsb(bb);
                bb = extractLsb(bb);
                key ^= pieceKeys[piece][index];
            }
        }


        if (!enPassant.equals(Square.NONE))
            key ^= enPassantKeys[enPassant.ordinal()];


        key ^= castlingKeys[castlingRight];


        if (sideToMove.equals(Side.BLACK))
            key ^= sideKey;


        return key;
    }


    public boolean isInsufficientMaterial() {
        if (pieceCount(WHITE_PAWN) + pieceCount(BLACK_PAWN) == 0) {
            if (pieceCount(WHITE_ROOK) + pieceCount(BLACK_ROOK) == 0
             && pieceCount(WHITE_QUEEN) + pieceCount(BLACK_QUEEN) == 0) {
                if (pieceCount(WHITE_BISHOP) + pieceCount(BLACK_BISHOP) == 0) {
                    return pieceCount(WHITE_KNIGHT) < 3 && pieceCount(BLACK_KNIGHT) < 3;
                } else if (pieceCount(WHITE_KNIGHT) + pieceCount(BLACK_KNIGHT) == 0) {
                    return Math.abs(pieceCount(WHITE_BISHOP) - pieceCount(BLACK_BISHOP)) < 2;
                } else if ((pieceCount(WHITE_KNIGHT) < 3 && pieceCount(WHITE_BISHOP) == 0)
                        || (pieceCount(WHITE_BISHOP) == 1 && pieceCount(WHITE_KNIGHT) == 0)) {
                    return (pieceCount(BLACK_KNIGHT) < 3 && pieceCount(BLACK_BISHOP) == 0)
                            || (pieceCount(BLACK_BISHOP) == 1 && pieceCount(BLACK_KNIGHT) == 0);
                }
            } else if (pieceCount(WHITE_QUEEN) + pieceCount(BLACK_QUEEN) == 0) {
                if (pieceCount(WHITE_ROOK) == 1 && pieceCount(BLACK_ROOK) == 0) {
                    return pieceCount(WHITE_KNIGHT) + pieceCount(WHITE_BISHOP) == 0
                            && (pieceCount(BLACK_KNIGHT) + pieceCount(BLACK_BISHOP) == 1
                            || pieceCount(BLACK_KNIGHT) + pieceCount(BLACK_BISHOP) == 2);
                } else if (pieceCount(WHITE_ROOK) == 0 && pieceCount(BLACK_ROOK) == 1) {
                    return pieceCount(BLACK_KNIGHT) + pieceCount(BLACK_BISHOP) == 0
                            && (pieceCount(WHITE_KNIGHT) + pieceCount(WHITE_BISHOP) == 1
                            || pieceCount(WHITE_KNIGHT) + pieceCount(WHITE_BISHOP) == 2);
                }
            }
        }
        return false;
    }


    public boolean isThreeFoldRepetition() {
        return isRepetition(3);
    }
    public boolean isRepetition(int count) {
        if (repetitionIndex >= 3) {
            int repetitions = 0;
            for (int i = repetitionIndex - 1; i >= 0; i -= 4) {
                final long hashKey = repetitionTable[i];
                if (this.hashKey == hashKey && ++repetitions >= count)
                    return true;
            }
        }
        return false;
    }


    public boolean isStaleMate() {
        if (!isKingAttacked())
            return legalMoves().stream().noneMatch(move -> {
                boolean valid = doMove(move);
                if (valid) {
                    undoMove();
                    return true;
                }
                return false;
            });
        return false;
    }


    public boolean isMated() {
        if (isKingAttacked())
            return legalMoves().stream().noneMatch(move -> {
                boolean valid = doMove(move);
                if (valid) {
                    undoMove();
                    return true;
                }
                return false;
            });
        return false;
    }


    public boolean isDraw(boolean includeStaleMate) {
        if (fiftyMove >= 100)
            return true;
        if (isRepetition(3))
            return true;
        if (isInsufficientMaterial())
            return true;
        if (includeStaleMate)
            return isStaleMate();
        return false;
    }


    public boolean isGameOver() {
        if (isDraw(true))
            return true;
        return isMated();
    }


    public int see(int move) {
        long fromBB = squareBB(from(move));
        long occupancy = bitboard();
        long attackers = attackersToSquare(to(move), occupancy);
        long fromCandidates = 0L;


        int[] seeGain = new int[32];
        int d = 0;


        Piece movingPiece = movingPiece(move);
        Piece capturedPiece = pieceAt(to(move));
        if (capturedPiece.equals(Piece.NONE) && to(move).equals(enPassant) && movingPiece.pieceType().equals(PieceType.PAWN))
            capturedPiece = pieceAt(enPassantTarget);


        seeGain[d] = capturedPiece.equals(Piece.NONE) ? 0 : mgValue(pieceScores[capturedPiece.pieceType().ordinal()]);
        do {
            d++;
            Side us = (d & 1) == 0 ? sideToMove : sideToMove.flip();
            seeGain[d] = mgValue(pieceScores[movingPiece.pieceType().ordinal()]) - seeGain[d - 1];


            occupancy &= ~fromBB;
            attackers &= ~fromBB;
            if ((fromBB & (bitboard(PieceType.PAWN,
                                    PieceType.KNIGHT,
                                    PieceType.BISHOP,
                                    PieceType.ROOK,
                                    PieceType.QUEEN))) != 0L)
                attackers |= attackersXrayToSquare(to(move), occupancy);


            if ((fromCandidates = attackers & bitboard(encodePiece(us, PieceType.PAWN))) != 0L)
                movingPiece = encodePiece(us, PieceType.PAWN);
            else if ((fromCandidates = attackers & bitboard(encodePiece(us, PieceType.KNIGHT))) != 0L)
                movingPiece = encodePiece(us, PieceType.KNIGHT);
            else if ((fromCandidates = attackers & bitboard(encodePiece(us, PieceType.BISHOP))) != 0L)
                movingPiece = encodePiece(us, PieceType.BISHOP);
            else if ((fromCandidates = attackers & bitboard(encodePiece(us, PieceType.ROOK))) != 0L)
                movingPiece = encodePiece(us, PieceType.ROOK);
            else if ((fromCandidates = attackers & bitboard(encodePiece(us, PieceType.QUEEN))) != 0L)
                movingPiece = encodePiece(us, PieceType.QUEEN);
            else if ((fromCandidates = attackers & bitboard(encodePiece(us, PieceType.KING))) != 0L)
                movingPiece = encodePiece(us, PieceType.KING);
            fromBB = squareBB(lsb(fromCandidates));
        } while (fromCandidates != 0L);


        while (--d != 0)
            seeGain[d - 1] = -Math.max(-seeGain[d - 1], seeGain[d]);


        return seeGain[0];
    }


    public void setPiece(Piece piece, Square square) {
        bitboards[piece.ordinal()] |= square.bitboard();
        typeBitboards[piece.pieceType().ordinal()] |= square.bitboard();
        occupancies[piece.pieceSide().ordinal()] |= square.bitboard();
        pieces[square.ordinal()] = piece;


        int psqScore = 0;
        if (!piece.pieceType().equals(PieceType.KING)) {
            if (!piece.pieceType().equals(PieceType.PAWN))
                nonPawnMaterial[piece.pieceSide().ordinal()] += mgValue(pieceScores[piece.pieceType().ordinal()]);
            psqScore += pieceScores[piece.pieceType().ordinal()];
        }


        int pstIndex = relativeSquare(piece.pieceSide(), square).ordinal();
        psqScore += pstBonus[piece.pieceType().ordinal()][pstIndex];
        this.psqScore[piece.pieceSide().ordinal()] += (piece.pieceSide().equals(Side.WHITE) ? psqScore : -psqScore);


        pieceCount[piece.ordinal()]++;
        if (!piece.equals(Piece.NONE) && !square.equals(Square.NONE))
            hashKey ^= pieceKeys[piece.ordinal()][square.ordinal()];
        if (piece.pieceType().equals(PieceType.KING))
            kingSquares[piece.pieceSide().ordinal()] = square;
    }


    public void removePiece(Piece piece, Square square) {
        assert piece == pieceAt(square);
        bitboards[piece.ordinal()] &= ~square.bitboard();
        typeBitboards[piece.pieceType().ordinal()] &= ~square.bitboard();
        occupancies[piece.pieceSide().ordinal()] &= ~square.bitboard();
        pieces[square.ordinal()] = Piece.NONE;


        int psqScore = 0;
        if (!piece.pieceType().equals(PieceType.KING)) {
            if (!piece.pieceType().equals(PieceType.PAWN))
                nonPawnMaterial[piece.pieceSide().ordinal()] -= mgValue(pieceScores[piece.pieceType().ordinal()]);
            psqScore += pieceScores[piece.pieceType().ordinal()];
        }


        int pstIndex = relativeSquare(piece.pieceSide(), square).ordinal();
        psqScore += pstBonus[piece.pieceType().ordinal()][pstIndex];
        this.psqScore[piece.pieceSide().ordinal()] -= (piece.pieceSide().equals(Side.WHITE) ? psqScore : -psqScore);


        pieceCount[piece.ordinal()]--;
        if (!piece.equals(Piece.NONE) && !square.equals(Square.NONE))
            hashKey ^= pieceKeys[piece.ordinal()][square.ordinal()];
        if (piece.pieceType().equals(PieceType.KING))
            kingSquares[piece.pieceSide().ordinal()] = Square.NONE;
    }


    private static final int[] castlingRights = {
            13, 15, 15, 15, 12, 15, 15, 14,
            15, 15, 15, 15, 15, 15, 15, 15,
            15, 15, 15, 15, 15, 15, 15, 15,
            15, 15, 15, 15, 15, 15, 15, 15,
            15, 15, 15, 15, 15, 15, 15, 15,
            15, 15, 15, 15, 15, 15, 15, 15,
            15, 15, 15, 15, 15, 15, 15, 15,
             7, 15, 15, 15,  3, 15, 15, 11
    };


    public boolean isLegal(int move) {
        Side us = sideToMove;
        Side them = us.flip();
        final Square from = from(move);
        final Square to = to(move);
        final Piece movingPiece = movingPiece(move);
        final Piece promotion = promotion(move);
        final boolean isCapture = isCapture(move);
        final boolean isPush = isPush(move);
        final boolean isEnPassant = isEnPassant(move);
        final boolean isCastling = isCastling(move);


        if (from.equals(Square.NONE))
            return false;
        if (to.equals(Square.NONE))
            return false;
        if (pieceAt(from).equals(Piece.NONE))
            return false;
        if (movingPiece.equals(Piece.NONE))
            return false;
        if (isEnPassant && !movingPiece.pieceType().equals(PieceType.PAWN))
            return false;
        if (isEnPassant && !to.equals(enPassant))
            return false;
        if (isCapture) {
            if (isEnPassant && pieceAt(enPassantTarget).equals(Piece.NONE))
                return false;
            if (!isEnPassant && pieceAt(to).equals(Piece.NONE))
                return false;
        }
        if (!promotion.equals(Piece.NONE)) {
            Rank relativePromoRank = relativeRank(us, RANK_8);
            if (!to.rank().equals(relativePromoRank))
                return false;
        }
        if (isPush && from.rank().distance(to.rank()) != 2 && !movingPiece.pieceType().equals(PieceType.PAWN))
            return false;
        if (isCastling) {
            if (!hasCastlingRight(us))
                return false;


            Square ooTarget = relativeSquare(us, G1);
            Square oooTarget = relativeSquare(us, C1);


            if (to.equals(ooTarget) && !hasKingCastlingRight(us))
                return false;
            if (to.equals(oooTarget) && !hasQueenCastlingRight(us))
                return false;
        }
        return !isPinned(them, from);
    }


    public boolean doMove(String move) {
        int bit = fromString(this, move);
        return doMove(bit);
    }


    public boolean doMove(int move) {
//        if (!isLegal(move))
//            return false;


        final Square from = from(move);
        final Square to = to(move);
        final Piece movingPiece = movingPiece(move);
        final Piece promotion = promotion(move);
        final boolean isCapture = isCapture(move);
        final boolean isPush = isPush(move);
        final boolean isEnPassant = isEnPassant(move);
        final boolean isCastling = isCastling(move);


        Backup backup = new Backup(this, move);
        backups.add(backup);


        removePiece(movingPiece, from);
        Piece capturedPiece = pieceAt(to);


        if (isCapture && !isEnPassant) {
            removePiece(capturedPiece, to);
            backup.setCapturedSquare(to);
        }


        if (!promotion.equals(Piece.NONE))
            setPiece(promotion, to);
        else
            setPiece(movingPiece, to);


        if (isEnPassant) {
            capturedPiece = pieceAt(enPassantTarget);
            if (!capturedPiece.equals(Piece.NONE)) {
                removePiece(capturedPiece, enPassantTarget);
                backup.setCapturedSquare(enPassantTarget);
            }
        }


        backup.setCapturedPiece(capturedPiece);
        if (capturedPiece.equals(Piece.NONE))
            fiftyMove++;
        else
            fiftyMove = 0;


        if (!enPassant.equals(Square.NONE))
            hashKey ^= enPassantKeys[enPassant.ordinal()];


        enPassant = Square.NONE;
        enPassantTarget = Square.NONE;


        if (movingPiece.pieceType().equals(PieceType.PAWN)) {
            fiftyMove = 0;
            if (isPush) {
                // set enPassant behind the pawn
                if (sideToMove.equals(Side.WHITE))
                    enPassant = squareAt(to.ordinal() - 8);
                else
                    enPassant = squareAt(to.ordinal() + 8);
                enPassantTarget = to;
                hashKey ^= enPassantKeys[enPassant.ordinal()];
            }
        }


        if (isCastling) {
            switch (to) {
                case G1 -> {
                    removePiece(WHITE_ROOK, H1);
                    setPiece(WHITE_ROOK, F1);
                }
                case C1 -> {
                    removePiece(WHITE_ROOK, A1);
                    setPiece(WHITE_ROOK, D1);
                }
                case G8 -> {
                    removePiece(BLACK_ROOK, H8);
                    setPiece(BLACK_ROOK, F8);
                }
                case C8 -> {
                    removePiece(BLACK_ROOK, A8);
                    setPiece(BLACK_ROOK, D8);
                }
            }
        }


        hashKey ^= castlingKeys[castlingRight];
        castlingRight &= castlingRights[from.ordinal()];
        castlingRight &= castlingRights[to.ordinal()];
        hashKey ^= castlingKeys[castlingRight];


        if (sideToMove.equals(Side.BLACK))
            moveCounter++;


        sideToMove = sideToMove.flip();
        hashKey ^= sideKey;
        repetitionTable[repetitionIndex++] = hashKey;
        gamePly++;


        if (isKingAttacked(sideToMove.flip())) {
            undoMove();
            return false;
        }


        return true;
    }


    public void doNullMove() {
        backups.add(new Backup(this, 0));


        if (!enPassant.equals(Square.NONE))
            hashKey ^= enPassantKeys[enPassant.ordinal()];


        fiftyMove++;
        enPassant = Square.NONE;
        enPassantTarget = Square.NONE;
        sideToMove = sideToMove.flip();
        hashKey ^= sideKey;
        repetitionTable[repetitionIndex++] = hashKey;
        gamePly++;
    }


    public int undoMove() {
        final Backup backup = backups.removeLast();
        int move = 0;


        if (backup != null) {
            move = backup.move();
            backup.restore(this);
        }


        repetitionIndex--;
        return move;
    }


    public List<Integer> legalMoves() {
        return MoveGenerator.generateLegalMoves(this);
    }


    public int piecesOnSameSquareColor(Square square, long occupancy) {
        return Long.bitCount(occupancy & (square.isLightSquare() ? lightSquares : darkSquares));
    }


    public boolean semiOpenFile(Side side, Square square) {
        return (bitboard(encodePiece(side, PieceType.PAWN)) & fileBB(square)) == 0L;
    }
    

    public boolean isKingAttacked() {
        return isKingAttacked(sideToMove);
    }
    public boolean isKingAttacked(Side side) {
        return isSquareAttackedBy(side.flip(), kingSquare(side));
    }


    public Square kingSquare() {
        return kingSquare(sideToMove);
    }
    public Square kingSquare(Side side) {
        return kingSquares[side.ordinal()];
    }


    public long bitboard() {
        return occupancies[0] | occupancies[1];
    }
    public long bitboard(Side side) {
        return occupancies[side.ordinal()];
    }
    public long bitboard(Piece piece) {
        return bitboards[piece.ordinal()];
    }
    public long bitboard(Side side, PieceType ...types) {
        return bitboards[side.ordinal()] & bitboard(types);
    }
    public long bitboard(PieceType ...types) {
        long bb = 0L;
        for (PieceType type : types)
            bb |= typeBitboards[type.ordinal()];
        return bb;
    }


    public boolean hasCastlingRight(Side us) {
        return (castlingRight & ((us.equals(Side.WHITE)
                                ? CastlingRight.WHITE_KING.bit() | CastlingRight.WHITE_QUEEN.bit()
                                : CastlingRight.BLACK_KING.bit() | CastlingRight.BLACK_QUEEN.bit()))) != 0;
    }


    public boolean hasKingCastlingRight(Side us) {
        if (!hasCastlingRight(us))
            return false;
        return (us.equals(Side.WHITE)
                ? (castlingRight & CastlingRight.WHITE_KING.bit()) != 0
                : (castlingRight & CastlingRight.BLACK_KING.bit()) != 0);
    }


    public boolean hasQueenCastlingRight(Side us) {
        if (!hasCastlingRight(us))
            return false;
        return (us.equals(Side.WHITE)
                ? (castlingRight & CastlingRight.WHITE_QUEEN.bit()) != 0
                : (castlingRight & CastlingRight.BLACK_QUEEN.bit()) != 0);
    }


    public int pieceCount(Piece piece) {
        return pieceCount[piece.ordinal()];
    }
    public int pieceCount(Side side, PieceType type) {
        return pieceCount[encodePiece(side, type).ordinal()];
    }
    public int nonPawnMaterial() {
        return nonPawnMaterial[0] + nonPawnMaterial[1];
    }
    public int nonPawnMaterial(Side side) {
        return nonPawnMaterial[side.ordinal()];
    }
    public int psqScore() {
        return psqScore[0] + psqScore[1];
    }
    public int psqScore(Side side) {
        return psqScore[side.ordinal()];
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();


        builder.append("    a b c d e f g h\n  +-----------------+\n");
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                Square square = encodeSquare(rankAt(rank), fileAt(file));
                Piece piece = pieceAt(square);


                if (file == 0)
                    builder.append(String.format("%d |", rank + 1));


                builder.append(" ").append(piece.symbol());
            }
            builder.append(" |\n");
        }
        builder.append("  +-----------------+\n");


        return builder.toString();
    }


    private final Node[] tree = new Node[maxPly];
    private final Engine engine = new Engine(this);
    private int ply;
    {
        for (int i = 0; i < tree.length; i++)
            tree[i] = new Node(engine, false);
    }


    public long perft(int depth) {
        ply = 0;
        for (Node node : tree)
            node.clear();
        return perft(depth, true);
    }


    private long perft(int depth, boolean rootNode) {
        if (depth <= 0)
            return 1L;


        Node node = tree[ply];
        node.moveIterator.initialize();


        long start = System.currentTimeMillis();
        long nodes = 0L;
        int move;


        while ((move = node.moveIterator.next()) != 0) {
            if (!doMove(move))
                continue;


            ply++;
            long n = perft(depth - 1, false);
            nodes += n;
            undoMove();
            ply--;


            if (rootNode) {
                Move.printMove(move);
                System.out.printf(": %d\n", n);
            }
        }


        if (rootNode) {
            System.out.println();
            System.out.printf("Nodes searhed: %d\nTime: %d", nodes, System.currentTimeMillis() - start);
            System.out.println();
        }


        return nodes;
    }


    @Override
    protected Board clone() {
        Board board = new Board();
        board.setFen(generateFen());
        board.backups.addAll(backups);
        board.repetitionIndex = repetitionIndex;
        System.arraycopy(repetitionTable, 0, board.repetitionTable, 0, repetitionTable.length);
        return board;
    }


    @Override
    public int hashCode() {
        return (int) hashKey;
    }


}
