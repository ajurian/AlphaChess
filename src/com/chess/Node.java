package com.chess;

import java.util.Arrays;

import static com.chess.SearchConstants.*;

public class Node {

    public int staticEval;
    public int ttMove;
    public int currentMove;
    public int excludedMove;
    public int movesIterated;
    public int killerMove1;
    public int killerMove2;


    public int quietMoveIndex;
    public int captureIndex;
    public int[] quietMoves;
    public int[] captures;
    public int[] quietHistory;


    public MoveIterator moveIterator;
    public boolean pvNode;
    public boolean inCheck;


    public Node(Engine engine) {
        quietMoves = new int[64];
        captures = new int[32];
        quietHistory = new int[12 * 64];
        moveIterator = new MoveIterator(engine);
        clear();
    }


    public void clear() {
        moveIterator.reset();
        staticEval = 0;
        ttMove = 0;
        currentMove = 0;
        movesIterated = 0;
        killerMove1 = 0;
        killerMove2 = 0;
        pvNode = false;
        inCheck = false;


        quietMoveIndex = 0;
        captureIndex = 0;
        Arrays.fill(quietMoves, 0);
        Arrays.fill(captures, 0);
        Arrays.fill(quietHistory, 0);
    }


}
