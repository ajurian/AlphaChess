package com.chess;

import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static com.chess.Move.*;

public class Book {

    private static final Random rand = new Random();
    private static final File file = new File("./src/com/chess/book");
    private static final int[][] variations = new int[8][20];


    static {
        Board tempBoard = new Board();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            int variationCount = 0;
            String ln;


            while ((ln = br.readLine()) != null) {
                for (String move : ln.split(" ")) {
                    int moveBit = fromString(tempBoard, move);
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


    public static int findMove(Board board) {
        if (board.gamePly() == 0)
            return variations[rand.nextInt(variations.length)][board.gamePly()];
        else if (board.gamePly() < 20) {
            List<Integer> indexes = new ArrayList<>();
            for (int i = 0; i < variations.length; i++) {
                boolean foundVariation = false;
                for (int j = board.gamePly() - 1; j >= 0; j--) {
                    if (board.backups().get(j).move() == variations[i][j])
                        foundVariation = true;
                    else {
                        foundVariation = false;
                        break;
                    }
                }
                if (foundVariation)
                    indexes.add(i);
            }


            if (indexes.size() == 0)
                return 0;


            int index = (int) Math.floor(Math.random() * (indexes.get(indexes.size() - 1) - indexes.get(0) + 1) + indexes.get(0));
            return variations[index][board.gamePly()];
        }
        return 0;
    }


}
