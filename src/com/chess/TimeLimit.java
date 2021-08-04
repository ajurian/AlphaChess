package com.chess;

import java.util.Arrays;

public class TimeLimit {

    public long[] time = new long[2], inc = new long[2];
    public int depth = 0, nodes = 0, mate = 0, mtg = 0, bookDepth = 0;
    public long moveTime = 0L;


    @Override
    public String toString() {
        return "TimeLimit {" +
                "\n\tdepth: " + depth +
                ",\n\tnodes: " + nodes +
                ",\n\tmate: " + mate +
                ",\n\tmtg: " + mtg +
                ",\n\tmoveTime: " + moveTime +
                ",\n\ttime: " + Arrays.toString(time) +
                ",\n\tinc: " + Arrays.toString(inc) +
                "\n}";
    }


}
