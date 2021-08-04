package com.chess;

import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Book {

    private static final Random rand = new Random();
    private static final File file = new File("D:\\Adolf Folder\\ChessEngines\\AlphaChess\\src\\com\\chess\\book");
    private static final int[][] variations = new int[8][20];
    private static int variationIndex = -1;


    static {
        Board tempBoard = new Board();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            int variationCount = 0;
            String ln;


            while ((ln = br.readLine()) != null) {
                for (String move : ln.split(" ")) {
                    int moveBit = Move.fromString(tempBoard, move);
                    variations[variationCount][tempBoard.gamePly()] = moveBit;
                    tempBoard.doMove(moveBit);
                }


                tempBoard.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
                variationCount++;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void randomizeVariationIndex() {
        variationIndex = rand.nextInt(variations.length);
    }


    public static void setVariationIndex(int index) {
        variationIndex = index;
    }


    public static int findMove(Board board) {
        if (board.gamePly() == 0)
            return variations[variationIndex][board.gamePly()];
        else if (board.gamePly() < 20) {
            int lastMove = board.backups().getLast().move();
            if (lastMove == variations[variationIndex][board.gamePly() - 1])
                return variations[variationIndex][board.gamePly()];


            List<Integer[]> list = new ArrayList<>();
            List<Integer> indexes = new ArrayList<>();
            for (int i = 0; i < variations.length; i++) {
                if (lastMove == variations[i][board.gamePly() - 1]) {
                    Integer[] o = new Integer[variations[i].length];
                    for (int j = 0; j < o.length; j++)
                        o[j] = variations[i][j];
                    list.add(o);
                    indexes.add(i);
                }
            }


            if (list.size() == 0 || indexes.size() == 0)
                return 0;


            variationIndex = (int) Math.floor(Math.random() * (indexes.get(indexes.size() - 1) - indexes.get(0) + 1) + indexes.get(0));
            return findMove(board);
        }
        return 0;
    }


}
