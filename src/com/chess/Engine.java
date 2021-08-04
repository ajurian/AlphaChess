package com.chess;

import java.util.*;
import java.util.stream.Stream;

import static com.chess.Evaluator.*;
import static com.chess.ScoreConstants.*;
import static com.chess.Score.*;
import static com.chess.Move.*;
import static com.chess.MoveIterator.*;
import static com.chess.SearchConstants.*;
import static com.chess.IntegerUtil.*;
import com.chess.TranspositionTable;

public class Engine {

    private static int statBonus(int depth) {
        return (depth > 14 ? 73 : 6 * depth * depth + 229 * depth - 215);
    }


    private final Node[] tree;
    private final RootMove rm;
    private final TimeManager tm;
    private final Board board;
    private final TranspositionTable TT;


    private int selDepth = 0;
    private int rootDepth = 0;
    private int ply = 0;
    private long nodes = 0L;


    public Engine(Board board) {
        this.board = board;
        tree = new Node[maxPly];
        rm = new RootMove(this);
        tm = new TimeManager();
        TT = new TranspositionTable(64);


        for (int i = 0; i < tree.length; i++)
            tree[i] = new Node(this);
    }


    public Node[] tree() {
        return tree;
    }
    public Node currentNode() { return tree[ply]; }
    public TimeManager timeManager() {
        return tm;
    }
    public RootMove rootMove() {
        return rm;
    }
    public Board board() {
        return board;
    }
    public TranspositionTable TT() {
        return TT;
    }
    public int ply() {
        return ply;
    }


    private static final int[] plyOffsets = {1, 2, 4, 6};
    private void updateHistoryStats(int ply, int move, int bonus) {
        for (int offset : plyOffsets) {
            if (tree[ply].inCheck && offset > 2)
                break;


            if (ply - offset >= 0 && tree[ply - offset].currentMove != 0) {
                Node node = tree[ply - offset];
                Piece movingPiece = movingPiece(move);
                Square to = to(move);
                int entry = node.quietHistory[movingPiece.ordinal() * 64 + to.ordinal()];
                node.quietHistory[movingPiece.ordinal() * 64 + to.ordinal()] += bonus - entry * Math.abs(bonus) / 29952;
            }
        }
    }


    private void updateButterflyStats(Side side, int move, int bonus) {
        Square from = from(move);
        Square to = to(move);
        int entry = butterflyHistory[to.ordinal() + 64 * (from.ordinal() + 64 * side.ordinal())];
        butterflyHistory[to.ordinal() + 64 * (from.ordinal() + 64 * side.ordinal())] += bonus - entry * Math.abs(bonus) / 13365;
    }


    private void updateQuietStats(Node node, int move, int bonus) {
        if (node.killerMove1 != move) {
            node.killerMove2 = node.killerMove1;
            node.killerMove1 = move;
        }


        updateButterflyStats(board.sideToMove(), move, bonus);
        updateHistoryStats(ply, move, bonus);
        if (!movingPiece(move).pieceType().equals(PieceType.PAWN))
            updateButterflyStats(board.sideToMove(), encodeMove(to(move), from(move), Piece.NONE, Piece.NONE, 0, 0, 0, 0), -bonus);


        if (ply > 0 && tree[ply - 1].currentMove != 0) {
            int prevMove = tree[ply - 1].currentMove;
            counterMoves[movingPiece(prevMove).ordinal() * 64 + to(prevMove).ordinal()] = move;
        }
    }


    private void updateCaptureStats(int move, int bonus) {
        Piece capturedPiece = board.pieceAt(to(move));
        if (capturedPiece.equals(Piece.NONE) && isEnPassant(move))
            capturedPiece = board.pieceAt(board.enPassantTarget());


        Piece movingPiece = movingPiece(move);
        Square to = to(move);
        int entry = captureHistory[capturedPiece.pieceType().ordinal() + 6 * (to.ordinal() + 64 * movingPiece.ordinal())];
        captureHistory[capturedPiece.pieceType().ordinal() + 6 * (to.ordinal() + 64 * movingPiece.ordinal())] += bonus - entry * Math.abs(bonus) / 10692;
    }


    private void updateAllStats(Node node, int depth, int beta, int bestValue, int bestMove) {
        int captureBonus = statBonus(depth + 1);
        int quietBonus = (bestValue > beta + mgValue(pawn) ? captureBonus : Math.min(captureBonus, statBonus(depth)));


        if (!isTactical(bestMove)) {
            updateQuietStats(node, bestMove, quietBonus);
            for (int i = 0; i < node.quietMoveIndex; i++) {
                int quietMove = node.quietMoves[i];
                updateButterflyStats(board.sideToMove(), quietMove, -quietBonus);
                updateHistoryStats(ply, quietMove, -quietBonus);
            }
        } else if (isCapture(bestMove))
            updateCaptureStats(bestMove, captureBonus);


        for (int i = 0; i < node.captureIndex; i++) {
            int capture = node.captures[i];
            updateCaptureStats(capture, -captureBonus);
        }
    }


    private void clearSearch() {
        tm.resetTimeControl();
        rm.clear();
        TT.recordCount(0);


        selDepth = 0;
        rootDepth = 0;
        ply = 0;
        nodes = 0L;


        for (Node node : tree)
            node.clear();
        Arrays.fill(lowPlyHistory, 0);
        Arrays.fill(butterflyHistory, 0);
        Arrays.fill(captureHistory, 0);
        Arrays.fill(counterMoves, 0);
    }


    private void printInfo(int value, int alpha, int beta, int depth) {
        if (!(rm.pvLength[0] > 0))
            return;


        System.out.print("info ");
        System.out.printf("depth %d ", depth);
        System.out.printf("seldepth %d ", rm.selDepth);
        System.out.print("score ");


        if (Math.abs(value) < mateValue - maxPly)
            System.out.printf("cp %d ", (value * 100 / egValue(pawn)));
        else
            System.out.printf("mate %d ", ((value > 0) ? (mateValue - value + 1) : (-mateValue - value)) / 2);


        if (value >= beta)
            System.out.print("lowerbound ");
        else if (value <= alpha)
            System.out.print("upperbound ");


        System.out.printf("nodes %d ", nodes);
        System.out.printf("nps %d ", nodes * 1000L / (tm.elapsed() + 1));
        System.out.printf("time %d ", tm.elapsed());


        System.out.print("pv");
        for (int i = 0; i < rm.pvLength[0]; i++)
            System.out.print(" " + notation(rm.pvTable[i]));
        System.out.println();
    }


    private int qsearch(int alpha, int beta, int depth) {
        if (tm.didStopped())
            return 0;


        nodes++;
        Node node = tree[ply];
        node.inCheck = board.isKingAttacked();
        node.pvNode = beta - alpha > 1;


        if (ply >= maxPly - 1)
            return evaluate(board);


        alpha = Math.max(alpha, -mateValue + ply);
        beta = Math.min(beta, mateValue - (ply + 1));
        if (alpha >= beta)
            return alpha;


        int bestValue, futilityBase, move;


        node.staticEval = evaluate(board);
        bestValue = node.staticEval;
        futilityBase = 155 + bestValue;


        if (bestValue >= beta)
            return bestValue;
        if (bestValue > alpha)
            alpha = bestValue;


        MoveIterator iterator = node.moveIterator;
        iterator.initialize();
        node.movesIterated = 0;


        while ((move = iterator.next()) != 0) {
            if (!board.doMove(move))
                continue;


            node.movesIterated++;
            boolean givesCheck = board.isKingAttacked();
            if (!isCapture(move) && promotion(move).equals(Piece.NONE)) {
                board.undoMove();
                continue;
            }


            if (promotion(move).equals(Piece.NONE) && !givesCheck) {
                Piece capturedPiece = board.backups().getLast().capturedPiece();
                int futilityValue = futilityBase + egValue(pieceScores[capturedPiece.pieceType().ordinal()]);
                if (futilityValue <= alpha) {
                    bestValue = Math.max(bestValue, futilityValue);
                    board.undoMove();
                    continue;
                }


                if (futilityBase <= alpha && iterator.lastMoveSee() <= 0) {
                    bestValue = Math.max(bestValue, futilityBase);
                    board.undoMove();
                    continue;
                }
            }


            ply++;
            int value = -qsearch(-beta, -alpha, depth - 1);
            board.undoMove();
            ply--;


            if (tm.didStopped())
                return 0;


            if (value > bestValue) {
                bestValue = value;
                if (value > alpha) {
                    alpha = value;
                    if (value >= beta)
                        break;
                }
            }
        }


        if (node.movesIterated == 0)
            bestValue = (node.inCheck ? -mateValue + ply : 0);


        return bestValue;
    }


    int[] reductions = new int[maxMoves];
    {
        for (int i = 0; i < reductions.length; i++)
            reductions[i] = (int) (21.9 * Math.log(i));
    }


    private int search(int alpha, int beta, int depth) {
        if (tm.didStopped())
            return 0;


        nodes++;
        rm.pvLength[ply] = ply;


        Side us;
        Node node;
        TTEntry tte;
        int ttValue, oldAlpha, bestValue, value, bestMove, move;
        boolean priorCapture, improving, didLMR, givesCheck;


        if (depth <= 0)
            return qsearch(alpha, beta, 0);


        us = board.sideToMove();
        node = tree[ply];
        node.inCheck = board.isKingAttacked();
        node.pvNode = beta - alpha > 1;
        node.quietMoveIndex = 0;
        node.captureIndex = 0;
        node.movesIterated = 0;
        priorCapture = board.backups().size() > 0 && !board.backups().getLast().capturedPiece().equals(Piece.NONE);
        improving = false;


        if ((ply == 0 || node.pvNode) && selDepth <= ply)
            selDepth = ply + 1;


        // draw detection
        if (ply > 0) {
            if (board.isDraw(false) || ply >= maxPly - 1)
                return (ply >= maxPly - 1 ? evaluate(board) : (2 * ((int) nodes & 1) - 1));


            // mate distance pruning
            alpha = Math.max(alpha, -mateValue + ply);
            beta = Math.min(beta, mateValue - (ply + 1));
            if (alpha >= beta)
                return alpha;
        }


        tte = TT.probe(board.hashKey());
        ttValue = -infinity;
        oldAlpha = alpha;
        node.ttMove = (tte == null ? 0 : tte.move);


        if (tte != null) {
            ttValue = tte.value;
            if (ttValue >= winValueInMaxPly) {
                if (ttValue >= mateValue - maxPly && mateValue - ttValue >= 100 - board.fiftyMove())
                    ttValue = mateValue - maxPly - 1;
                else
                    ttValue -= ply;
            }


            if (ttValue <= lossValueInMaxPly) {
                if (ttValue <= -mateValue + maxPly && mateValue + ttValue >= 100 - board.fiftyMove())
                    ttValue = -mateValue + maxPly + 1;
                else
                    ttValue += ply;
            }


            if (!node.pvNode && ply > 0) {
                if (tte.depth >= depth
                 && (ttValue >= beta ? tte.bound == BOUND_LOWER
                                     : tte.bound == BOUND_UPPER)) {
                    if (node.ttMove != 0) {
                        if (ttValue >= beta) {
                            if (!isTactical(node.ttMove))
                                updateQuietStats(node, node.ttMove, statBonus(depth));
//                            if (ply > 0 && tree[ply - 1].movesIterated <= 4 && !priorCapture)
//                                updateHistoryStats(ply - 1, tree[ply - 1].currentMove, -statBonus(depth + 1));
                        } else if (!isTactical(node.ttMove)) {
                            updateButterflyStats(us, node.ttMove, -statBonus(depth));
                            updateHistoryStats(ply, node.ttMove, -statBonus(depth));
                        }
                    }


                    if (board.fiftyMove() < 90)
                        return ttValue;
                }
            }
        }


        node.staticEval = evaluate(board);
        if (!node.inCheck) {
            if (ply - 2 >= 0 && tree[ply - 2].inCheck)
                improving = (ply - 4 >= 0 && (node.staticEval > tree[ply - 4].staticEval
                         || tree[ply - 4].inCheck));
            else if (ply - 2 >= 0)
                improving = node.staticEval > tree[ply - 2].staticEval;


            if (ply > 0 && tree[ply - 1].currentMove != 0 && !tree[ply - 1].inCheck && !priorCapture) {
                int bonus = -depth * 4 * (tree[ply - 1].staticEval + node.staticEval);
                updateButterflyStats(us.flip(), tree[ply - 1].currentMove, bonus);
            }


            if (!node.pvNode && depth < 9 && node.staticEval < winValue) {
                int evalMargin = 214 * (depth - _int(improving));
                value = node.staticEval - evalMargin;
                if (value >= beta)
                    return node.staticEval;
            }


            int R = (1090 + 81 * depth) / 256 + Math.min((node.staticEval - beta) / 205, 3);
            if (!node.pvNode && depth >= R && Math.abs(beta) < mateValue && node.staticEval >= beta
             && node.staticEval >= beta - 20 * depth - 22 * _int(improving) + 168 * _int(tte != null) + 159
             && !(ply > 0 && tree[ply - 1].currentMove != 0) && board.nonPawnMaterial(us) != 0) {
                board.doNullMove();
                ply++;


                node.currentMove = 0;
                value = -search(-beta, -beta + 1, depth - R);


                board.undoMove();
                ply--;


                if (tm.didStopped())
                    return 0;


                if (value >= beta) {
                    // do not return unproven mate
                    if (value >= winValueInMaxPly)
                        value = beta;


                    if (Math.abs(beta) < winValue && depth < 14)
                        return value;


                    // do verification search at high depths
                    int v = search(beta - 1, beta, depth - R);
                    if (v >= beta)
                        return value;
                }
            }
        }


        MoveIterator iterator = node.moveIterator;
        bestValue = -infinity;
        bestMove = 0;


        iterator.initialize();
        while ((move = iterator.next()) != 0) {
            if (!board.doMove(move))
                continue;


            node.movesIterated++;
            if (ply == 0 && tm.elapsed() > 3000 && !tm.didStopped()) {
                System.out.printf("info depth %d currmove %s currmovenumber %d", depth, notation(move), node.movesIterated);
                System.out.println();
            }


            int extension = 0;
            int newDepth = depth - 1;
            givesCheck = board.isKingAttacked();


            /*if (ply > 0 && board.nonPawnMaterial(us) != 0 && bestValue > lossValueInMaxPly) {
                int R = ((reductions[depth] * reductions[node.movesIterated]) + 534) / 1024;
                R += _int(!improving && R > 904);


                int lmrDepth = Math.max(newDepth - R, 0);
                if (isCapture(move) || givesCheck) {
                    if (!givesCheck && lmrDepth < 1) {
                        Piece capturedPiece = board.pieceAt(to(move));
                        if (capturedPiece.equals(Piece.NONE) && to(move).equals(board.enPassant()))
                            capturedPiece = board.pieceAt(board.enPassantTarget());


                        if (captureHistory[capturedPiece.pieceType().ordinal() + 6 * (to(move).ordinal() + 64 * movingPiece(move).ordinal())] < 0) {
                            board.undoMove();
                            continue;
                        }
                    }


                    if (iterator.lastMoveSee() < -218 * depth) {
                        board.undoMove();
                        continue;
                    }
                } else {
                    if (lmrDepth < 5
                     && (ply - 1 >= 0 && tree[ply - 1].quietHistory[movingPiece(move).ordinal() * 64 + to(move).ordinal()] < 0)
                     && (ply - 2 >= 0 && tree[ply - 2].quietHistory[movingPiece(move).ordinal() * 64 + to(move).ordinal()] < 0)) {
                        board.undoMove();
                        continue;
                    }


                    if (iterator.lastMoveSee() < -(30 - Math.min(lmrDepth, 18)) * lmrDepth * lmrDepth) {
                        board.undoMove();
                        continue;
                    }
                }
            }*/


            if (depth > 6 && givesCheck && Math.abs(node.staticEval) > 100)
                extension = 1;


            newDepth += extension;
            node.currentMove = move;
            ply++;


            if (node.movesIterated == 1)
                value = -search(-beta, -alpha, newDepth);
            else {
                if (node.movesIterated >= 4 &&
                    depth >= 3 &&
                    !node.inCheck &&
                    !givesCheck &&
                    !isTactical(move)) {
                    int R = ((reductions[depth] * reductions[node.movesIterated]) + 534) / 1024;
                    R += _int(!improving && R > 904);


                    int statScore = butterflyHistory[to(move).ordinal() + 64 * (from(move).ordinal() + 64 * us.ordinal())];
                    for (int offset : plyOffsets) {
                        if (offset != 6 && ply - 1 - offset >= 0)
                            statScore += tree[ply - 1 - offset].quietHistory[movingPiece(move).ordinal() * 64 + to(move).ordinal()];
                    }


                    R -= (statScore - 4923) / 14721;


                    int d = clamp(newDepth - R, 1, newDepth);
                    value = -search(-(alpha + 1), -alpha, d);
                    didLMR = true;
                } else {
                    value = alpha + 1;
                    didLMR = false;
                }


                // pv search
                if (value > alpha) {
                    value = -search(-(alpha + 1), -alpha, newDepth);
                    if (didLMR && !isTactical(move)) {
                        int bonus = (value > alpha ? statBonus(newDepth) : -statBonus(newDepth));
                        updateHistoryStats(ply - 1, move, bonus);
                    }


                    if (value > alpha && value < beta)
                        value = -search(-beta, -alpha, newDepth);
                }
            }


            board.undoMove();
            ply--;


            if (tm.didStopped())
                return 0;


            if (node.movesIterated == 1 || value > alpha) {
                if (ply == 0)
                    rm.selDepth = selDepth;
                rm.pvLength[ply]++;
                rm.pvTable[ply * maxPly + ply] = move;
            }


            if (value > bestValue) {
                bestValue = value;
                if (value > alpha) {
                    alpha = value;
                    bestMove = move;
                    rm.updatePv();
                    if (value >= beta)
                        break;
                }
            }


            if (bestMove != move) {
                if (isCapture(move) && node.captureIndex < 32)
                    node.captures[node.captureIndex++] = move;
                else if (!isTactical(move) && node.quietMoveIndex < 64)
                    node.quietMoves[node.quietMoveIndex++] = move;
            }
        }


        if (node.movesIterated == 0)
            bestValue = (node.inCheck ? -mateValue + ply : 0);
        else if (bestValue >= beta)
            updateAllStats(node, depth, beta, bestValue, bestMove);
        else if ((depth >= 3 || node.pvNode) && !priorCapture && ply > 0)
            updateHistoryStats(ply - 1, tree[ply - 1].currentMove, statBonus(depth));


        if (node.excludedMove == 0 && ply > 0)
            TT.store(ply, bestValue, depth, (bestValue >= beta ? BOUND_LOWER
                                           : bestValue <= oldAlpha ? BOUND_UPPER
                                           : BOUND_EXACT), bestMove, node.staticEval, board.hashKey());
        return bestValue;
    }


    public int search(TimeLimit limit) {
        if (board.gamePly() < limit.bookDepth) {
            int bookMove = Book.findMove(board);
            if (bookMove != 0) {
                System.out.println("bestmove " + notation(bookMove));
                return bookMove;
            }
        }


        if (limit.depth <= 0)
            limit.depth = maxPly;


        clearSearch();
        tm.initialize(limit, board);


        new Thread(() -> {
            if (!tm.timeSet())
                return;
            while (!tm.didStopped()) {
                if (System.currentTimeMillis() > tm.stopTime())
                    tm.stop();
            }
        }).start();


        int value = -infinity;
        int delta = -infinity;
        int alpha = -infinity;
        int beta = infinity;
        int lastBestMove = 0;
        int lastPonder = 0;
        int initialVal;


        for (rootDepth = 1; rootDepth <= limit.depth; rootDepth++) {
            lastBestMove = rm.pvTable[0];
            lastPonder = rm.pvTable[1];
            initialVal = value;
            selDepth = 0;


            if (rootDepth >= 4) {
                delta = 17;
                alpha = Math.max(initialVal - delta, -infinity);
                beta = Math.min(initialVal + delta, infinity);
            }


            int failHighCount = 0;
            while (true) {
                int adjustedDepth = Math.max(1, rootDepth);
                value = search(alpha, beta, adjustedDepth);


                if (tm.didStopped())
                    break;


                if ((value <= alpha || value >= beta) && tm.elapsed() > 3000)
                    printInfo(value, alpha, beta, rootDepth);


                if (value <= alpha) {
                    beta = (alpha + beta) / 2;
                    alpha = Math.max(value - delta, -infinity);
                    failHighCount = 0;
                } else if (value >= beta) {
                    beta = Math.min(value + delta, infinity);
                    failHighCount++;
                } else
                    break;


                delta += delta / 4 + 5;
            }


            if (tm.didStopped())
                break;


            printInfo(value, alpha, beta, rootDepth);
        }


        int bestMove = tm.didStopped() ? lastBestMove : rm.pvTable[0];
        int ponder = tm.didStopped() ? lastPonder : rm.pvTable[1];


        System.out.print("bestmove ");
        if (bestMove != 0)
            System.out.print(notation(bestMove));
        else
            System.out.print("(none)");


        System.out.print(" ponder ");
        if (ponder != 0)
            System.out.print(notation(ponder));
        else
            System.out.print("(none)");
        System.out.println();


        return bestMove;
    }


}
