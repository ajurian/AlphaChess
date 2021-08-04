package com.chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class UCI {

    private static Scanner scan;
    private static Board board;
    private static Engine engine;

    public static void main(String[] argv) {
        scan = new Scanner(System.in);
        board = new Board();
        engine = new Engine(board);


        while (true) {
            String input = scan.nextLine();
            if (input.length() == 0)
                continue;
            respond(input);
        }
    }


    private static int bookDepth = 8;
    private static void respond(String input) {
        ArrayList<String> args = new ArrayList<>(Arrays.asList(input.split(" ")));
        String command = args.get(0);


        switch (command) {
            case "go" -> {
                TimeLimit limit = new TimeLimit();
                limit.bookDepth = bookDepth;


                if (args.size() > 1) {
                    if (args.contains("infinite"))
                        limit.bookDepth = 0;
                    if (args.contains("depth"))
                        limit.depth = Integer.parseInt(after(args, "depth"));
                    if (args.contains("nodes"))
                        limit.nodes = Integer.parseInt(after(args, "nodes"));
                    if (args.contains("mate"))
                        limit.mate = Integer.parseInt(after(args, "mate"));
                    if (args.contains("movestogo"))
                        limit.mtg = Integer.parseInt(after(args, "movestogo"));
                    if (args.contains("movetime"))
                        limit.moveTime = Integer.parseInt(after(args, "movetime"));
                    if (args.contains("wtime"))
                        limit.time[0] = Long.parseLong(after(args, "wtime"));
                    if (args.contains("btime"))
                        limit.time[1] = Long.parseLong(after(args, "btime"));
                    if (args.contains("winc"))
                        limit.inc[0] = Long.parseLong(after(args, "winc"));
                    if (args.contains("binc"))
                        limit.inc[1] = Long.parseLong(after(args, "binc"));
                }


                new Thread(() -> engine.search(limit)).start();
            }
            case "setoption" -> {
                String name = between(args, "name", "value");
                String value = after(args, "value");


                switch (name.toLowerCase()) {
                    case "clear hash" -> {
                        engine.TT().clear();
                    }
                    case "hash" -> {
                        int MB = Integer.parseInt(value);
                        engine.TT().resize(MB);
                    }
                    case "book depth" -> {
                        bookDepth = Integer.parseInt(value);
                    }
                }
            }
            case "uci" -> {
                System.out.println("id name DeepJava");
                System.out.println("id author Adolf Urian");
                System.out.println("option name Clear Hash type button");
                System.out.println("option name Hash type spin default 16 min 1 max 256");
                System.out.println("option name Book Depth type spin default 8 min 0 max 20");
                System.out.println("uciok");
            }
            case "position" -> {
                if (args.size() == 1)
                    break;


                if (args.get(1).equalsIgnoreCase("fen")) {
                    if (args.contains("moves"))
                        board.setFen(between(args, "fen", "moves"));
                    else {
                        args.add(":");
                        board.setFen(between(args, "fen", ":"));
                        args.remove(":");
                    }
                } else if (args.get(1).equalsIgnoreCase("startpos"))
                    board.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");


                if (args.contains("moves")) {
                    args.add(":");
                    for (String move : between(args, "moves", ":").split(" "))
                        board.doMove(move);
                    args.remove(":");
                }


                System.out.println(board);
            }
            case "ucinewgame" -> {
                board.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
                engine.TT().clear();
            }
            case "isready" -> System.out.println("readyok");
            case "stop" -> engine.timeManager().stop();
            case "quit" -> System.exit(0);
            case "say" -> System.out.println(String.join(" ", args.subList(1, args.size()))); // experiments from gui
        }
    }


    private static String after(List<String> args, String target) {
        return args.get(args.indexOf(target) + 1);
    }
    private static String between(List<String> args, String start, String end) {
        return String.join(" ", args.subList(args.indexOf(start) + 1, args.indexOf(end)));
    }


}
