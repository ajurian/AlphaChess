package com.chess;

import java.util.Objects;

public class TTEntry {

    public int value, depth, bound, move, eval;
    public long key;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TTEntry that = (TTEntry) o;
        return value == that.value
            && depth == that.depth
            && bound == that.bound
            && move == that.move
            && eval == that.eval
            && key == that.key;
    }


    @Override
    public String toString() {
        return "TTEntry {" +
                "\n\tvalue: " + value +
                ",\n\tdepth: " + depth +
                ",\n\tbound: " + bound +
                ",\n\tmove: " + move +
                ",\n\teval: " + eval +
                ",\n\tkey: " + key +
                "\n}";
    }


}
