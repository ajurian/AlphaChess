package com.chess;

import java.io.*;
import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.chess.Trace.*;

public class Main {

    static class ScoreState {
        public double min, max;
        public String name;
        public ScoreState(String name, double min, double max) {
            this.name = name;
            this.min = min;
            this.max = max;
        }
    }


    public static void main(String[] args) {
        Board board = new Board();
        Engine engine = new Engine(board);
//        board.setFen("1n1k4/R5N1/7p/1P6/2P1bp2/2B4P/5PP1/5BK1 w - - 1 44 ");
//        board.setFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
//        board.setFen("4r3/3r2k1/1bp2p2/2Np1bp1/3P3p/P1R4P/1P1R1PPB/6K1 w - - 12 34");
//        board.setFen("8/7k/4Q3/1p1K4/1P1P2PP/4PP2/2p5/2R5 w - - 3 60 ");
//        board.setFen("6k1/2R5/7P/4pp2/p4p2/r7/2P2PK1/8 w - - 2 47 ");
//        board.setFen("4Rb1r/p1p5/2pk2p1/4R1Bp/2PN3P/qP6/P5P1/6K1 w - - 1 26 ");
//        board.setFen("1r5k/2pQ2p1/p1N5/6Pp/5q2/1P6/P1P3P1/1R2b2K w - - 0 29 ");
//        board.setFen("8/1K5k/1P3Q2/8/8/8/8/8 w - - 1 90 ");
//        board.setFen("8/8/p4ppk/7p/1r1P1PPK/8/R6P/4rb2 b - - 0 55 ");
//        board.setFen("8/1k6/8/8/1K6/8/P7/8 b - - 0 59 ");
//        board.setFen("5R2/P7/1K6/7p/6k1/1P3p2/8/r7 w - - 0 60 ");


//        board.setFen("2kr1b1r/ppp2ppp/2nq1n2/1N1p4/3P2b1/P2B1N2/1PP2PPP/R1BQ1RK1 b - - 6 9 ");
//        board.setFen("3Q4/kr6/8/4K3/8/8/8/8 w - - 39 94 ");


        System.out.println(board);
        Scanner scanner = new Scanner(System.in);
        while (!board.isGameOver()) {
            if (board.sideToMove().equals(Side.BLACK)) {
                String move = scanner.nextLine();
                if (!board.doMove(move)) {
                    System.out.println("illegal move");
                    continue;
                }
            } else {
                TimeLimit limit = new TimeLimit();
//                limit.moveTime = 2000L;
//                limit.depth = 1;


                int move = engine.search(limit);
                board.doMove(move);
                System.out.println(board.gamePly());
            }
            System.out.println(board);
        }
    }


}
