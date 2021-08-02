package com.chess;

import java.util.List;

import static com.chess.SearchConstants.*;
import static com.chess.Move.*;

public class MoveSorter {

    public static int[] historyMoves = new int[12 * 64];
    public static int[] killerMoves = new int[2 * maxPly];
    public static boolean followPv = false, scorePv = false;


    private static final int[] MVV_LVA = {
            105, 205, 305, 405, 505, 605,
            104, 204, 304, 404, 504, 604,
            103, 203, 303, 403, 503, 603,
            102, 202, 302, 402, 502, 602,
            101, 201, 301, 401, 501, 601,
            100, 200, 300, 400, 500, 600,
    };


    private static int scoreMove(Engine engine, int move) {
        int ply = engine.ply();
        if (engine.rootMove().pvTable[ply] == move) {
            // follow pv line
            if (scorePv) {
                scorePv = false;
                return 20000;
            }
        }


        Board board = engine.board();
        Square to = to(move);
        Piece movingPiece = movingPiece(move);
        Piece capturedPiece = board.pieceAt(to);


        if (isEnPassant(move))
            capturedPiece = board.pieceAt(board.enPassantTarget());


        if (!capturedPiece.equals(Piece.NONE)) {
            int see = board.see(move);
            // don't add bonus for bad captures, just return the SEE when it is less than 0
            if (see < 0)
                return see;
            else {
                PieceType attacker = movingPiece.pieceType();
                PieceType victim = capturedPiece.pieceType();


                // good capture
                if (see > 0)
                    return 10000 + MVV_LVA[6 * attacker.ordinal() + victim.ordinal()];
                    // equal capture
                else
                    return 9000 + MVV_LVA[6 * attacker.ordinal() + victim.ordinal()];
            }
        } else {
            if (engine.tree()[ply].killerMove1 == move)
                return 8000;
            if (engine.tree()[ply].killerMove2 == move)
                return 7000;
            if (ply >= 2 && engine.tree()[ply - 2].killerMove1 == move)
                return 6000;
            if (ply >= 2 && engine.tree()[ply - 2].killerMove2 == move)
                return 5000;


            return promotion(move).equals(Piece.NONE)
                    ? historyMoves[12 * movingPiece.ordinal() + to(move).ordinal()]
                    : Score.egValue(ScoreConstants.pieceScores[promotion(move).pieceType().ordinal()]);
        }
    }


    public static void sortMoves(List<Integer> moves, Engine engine, int bestMove) {
        int[] scores = new int[moves.size()];
        for (int i = 0; i < moves.size(); i++) {
            int move = moves.get(i);
            if (bestMove == moves.get(i))
                scores[i] = 30000;
            else
                scores[i] = scoreMove(engine, move);
        }


        for (int i = 0; i < moves.size(); i++) {
            for (int j = i + 1; j < moves.size(); j++) {
                if (scores[j] > scores[i]) {
                    int tempMove = moves.get(j);
                    moves.set(j, moves.get(i));
                    moves.set(i, tempMove);


                    int tempScore = scores[j];
                    scores[j] = scores[i];
                    scores[i] = tempScore;
                }
            }
        }
    }


}
