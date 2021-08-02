package com.chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static com.chess.SearchConstants.*;

public class RootMove {

    private final Engine engine;
    public int[] pvTable;
    public int[] pvLength;
    public int selDepth;
    public int score;
    public int prevScore;

    public RootMove(Engine engine) {
        pvTable = new int[maxPly * maxPly];
        pvLength = new int[maxPly];
        this.engine = engine;
    }


    public void clear() {
        pvTable = null;
        pvLength = null;
        pvTable = new int[maxPly * maxPly];
        pvLength = new int[maxPly];
    }


    public void updatePv() {
        int ply = engine.ply();
        if (pvLength[ply + 1] - (ply + 1) >= 0)
            System.arraycopy(pvTable, maxPly * (ply + 1) + ply + 1, pvTable, maxPly * ply + ply + 1, pvLength[ply + 1] - (ply + 1));
        pvLength[ply] = pvLength[ply + 1];
    }


}
