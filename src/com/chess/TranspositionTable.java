package com.chess;

import java.util.stream.Stream;
import static com.chess.SearchConstants.*;

public class TranspositionTable {

    public TranspositionTable(int MB) {
        resize(MB);
    }
    public TTEntry[] entries() {
        return entries;
    }
    public long recordCount() {
        return recordCount;
    }
    public void recordCount(int recordCount) {
        this.recordCount = recordCount;
    }
    public int hashSize() {
        return size;
    }


    private TTEntry[] entries;
    private long recordCount;
    private int size;


    public void resize(int MB) {
        // 28 = sizeof(TTEntry)
        size = 0x100000 * MB / 28;
        while (!clear())
            resize(MB / 2);
    }


    public boolean clear() {
        try {
            entries = null;
            entries = Stream.generate(TTEntry::new).limit(size).toArray(TTEntry[]::new);
        }
        catch (OutOfMemoryError e) { return false; }
        return true;
    }


    public void store(int ply, int value, int depth, int bound, int move, int eval, long key) {
        TTEntry tte = entries[(int) (key & Integer.MAX_VALUE) % size];
        if (value >= winValueInMaxPly)
            value += ply;
        if (value <= lossValueInMaxPly)
            value -= ply;


        tte.value = value;
        tte.depth = depth;
        tte.bound = bound;
        tte.move = move;
        tte.eval = eval;
        tte.key = key;
        recordCount++;
    }


    public TTEntry probe(long key) {
        TTEntry tte = entries[(int) (key & Integer.MAX_VALUE) % size];
        if (tte.key == key)
            return tte;
        return null;
    }


}
