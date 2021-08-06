package com.chess;

import java.io.*;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.chess.Move.*;

public class Book {

    private static final InputStream inputStream = Book.class.getResourceAsStream("book");
    private static final List<Integer[]> variations = new ArrayList<>();
    private static final Random rand = new Random();


    static {
        Board tempBoard = new Board();
        try {
            StringBuilder content = new StringBuilder();
            int read;


            assert inputStream != null;
            while ((read = inputStream.read()) != -1) {
                char c = (char) read;
                content.append(c);
            }


            for (String ln : content.toString().split("\n")) {
                String[] moves = ln.split(" ");
                Integer[] moveBits = new Integer[moves.length];


                for (int i = 0; i < moves.length; i++) {
                    String move = moves[i];
                    int moveBit = fromString(tempBoard, move);
                    moveBits[i] = moveBit;
                    tempBoard.doMove(moveBit);
                }


                variations.add(moveBits);
                tempBoard.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            }
        }
        catch (IOException e) { e.printStackTrace(); }
    }


    public static void test() {
        for (Integer[] variation : variations) {
            System.out.println(Arrays.stream(variation).map(Move::notation).collect(Collectors.toList()));
        }
    }


    public static int findMove(Board board) {
        if (board.gamePly() == 0) {
            Integer[] randomVariation = variations.get(rand.nextInt(variations.size()));
            if (board.gamePly() < randomVariation.length)
                return randomVariation[board.gamePly()];
        }


        List<Integer[]> matchedVariations = new ArrayList<>();
        for (Integer[] variation : variations) {
            boolean foundVariation = false;
            for (int j = board.gamePly() - 1; j >= 0; j--) {
                if (board.backups().get(j).move() == variation[j])
                    foundVariation = true;
                else {
                    foundVariation = false;
                    break;
                }
            }
            if (foundVariation)
                matchedVariations.add(variation);
        }


        if (matchedVariations.size() == 0)
            return 0;


        Integer[] randomVariation = matchedVariations.get(rand.nextInt(matchedVariations.size()));
        if (board.gamePly() < randomVariation.length)
            return randomVariation[board.gamePly()];


        return 0;
    }


}
