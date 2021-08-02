package com.chess;

import java.text.DecimalFormat;
import java.util.HashMap;

import static com.chess.Score.*;
import static com.chess.ScoreConstants.*;

public class Trace {

    private static HashMap<String, Integer[]> scores = new HashMap<>();
    private static boolean traceEvaluation = false;
    static { clear(); }


    private static int get(String term, int side) {
        return scores.get(term)[side];
    }
    public static double cp(int value) {
        return (double) value / egValue(pawn);
    }
    public static void startTracing() {
        traceEvaluation = true;
    }
    public static void endTracing() {
        traceEvaluation = false;
    }
    public static boolean isTracing() {
        return traceEvaluation;
    }


    public static void clear() {
        scores.put("Material", new Integer[]{0, 0, 0});
        scores.put("Imbalance", new Integer[]{0, 0, 0});
        scores.put("Pawns", new Integer[]{0, 0, 0});
        scores.put("Knights", new Integer[]{0, 0, 0});
        scores.put("Bishops", new Integer[]{0, 0, 0});
        scores.put("Rooks", new Integer[]{0, 0, 0});
        scores.put("Queens", new Integer[]{0, 0, 0});
        scores.put("King", new Integer[]{0, 0, 0});
        scores.put("Mobility", new Integer[]{0, 0, 0});
        scores.put("Passed", new Integer[]{0, 0, 0});
        scores.put("Threats", new Integer[]{0, 0, 0});
        scores.put("Space", new Integer[]{0, 0, 0});
        scores.put("Winnable", new Integer[]{0, 0, 0});
        scores.put("Total", new Integer[]{0, 0, 0});
    }


    private static double[] getStats(String term) {
        if (term.equals("Material") || term.equals("Imbalance") || term.equals("Winnable") || term.equals("Total"))
            return new double[]{cp(mgValue(get(term, 2))), cp(egValue(get(term, 2)))};


        double termWhiteMG = cp(mgValue(get(term, 0)));
        double termWhiteEG = cp(egValue(get(term, 0)));
        double termBlackMG = cp(mgValue(get(term, 1)));
        double termBlackEG = cp(egValue(get(term, 1)));
        double termTotalMG = termWhiteMG - termBlackMG;
        double termTotalEG = termWhiteEG - termBlackEG;


        return new double[]{termWhiteMG, termWhiteEG, termBlackMG, termBlackEG, termTotalMG, termTotalEG};
    }


    public static void set(String term, int side, int score) {
        if (traceEvaluation)
            scores.get(term)[side] = score;
    }


    public static void printTrace() {
        double[] materialStats = getStats("Material");
        double[] imbalanceStats = getStats("Imbalance");
        double[] pawnsStats = getStats("Pawns");
        double[] knightsStats = getStats("Knights");
        double[] bishopsStats = getStats("Bishops");
        double[] rooksStats = getStats("Rooks");
        double[] queenStats = getStats("Queens");
        double[] kingStats = getStats("King");
        double[] mobilityStats = getStats("Mobility");
        double[] threatsStats = getStats("Threats");
        double[] passedStats = getStats("Passed");
        double[] spaceStats = getStats("Space");
        double[] winnableStats = getStats("Winnable");
        double[] totalStats = getStats("Total");
        double[][] allStats = {
                materialStats,
                imbalanceStats,
                pawnsStats,
                knightsStats,
                bishopsStats,
                rooksStats,
                queenStats,
                kingStats,
                mobilityStats,
                threatsStats,
                passedStats,
                spaceStats,
                winnableStats,
                totalStats
        };


        int maxWidth = 0;
        for (int i = 0; i < allStats.length; i++) {
            for (int j = 0; j < allStats[i].length; j++) {
                DecimalFormat df = new DecimalFormat("0.00");
                String str = df.format(allStats[i][j]);
                int count = 0;
                for (int k = 0; k < str.length(); k++) {
                    if (Character.isDigit(str.charAt(k)) || str.charAt(k) == '.' || str.charAt(k) == '-')
                        count++;
                }


                if (count > maxWidth)
                    maxWidth = count;
            }
        }


        String ln = "-".repeat(maxWidth * 2 + 6);
        String e = "-".repeat(maxWidth);


        String prettyTable = String.format("\n" +
                        "+------------+%s+%s+%s+\n" +
                        "|    Term    |%sWhite%s|%sBlack%s|%sTotal%s|\n" +
                        "|            |  %sMG%sEG%s  |  %sMG%sEG%s  |  %sMG%sEG%s  |\n" +
                        "+------------+%s+%s+%s+\n" +
                        "|   Material |  %s  %s  |  %s  %s  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |\n" +
                        "|  Imbalance |  %s  %s  |  %s  %s  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |\n" +
                        "|      Pawns |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |\n" +
                        "|    Knights |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |\n" +
                        "|    Bishops |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |\n" +
                        "|      Rooks |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |\n" +
                        "|     Queens |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |\n" +
                        "|       King |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |\n" +
                        "|   Mobility |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |\n" +
                        "|    Threats |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |\n" +
                        "|     Passed |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |\n" +
                        "|      Space |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |\n" +
                        "|   Winnable |  %s  %s  |  %s  %s  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |\n" +
                        "+------------+%s+%s+%s+\n" +
                        "|      Total |  %s  %s  |  %s  %s  |  %" + maxWidth + ".2f  %" + maxWidth + ".2f  |\n" +
                        "+------------+%s+%s+%s+\n",
                ln, ln, ln,
                " ".repeat(maxWidth + 1), " ".repeat(maxWidth), " ".repeat(maxWidth + 1), " ".repeat(maxWidth), " ".repeat(maxWidth + 1), " ".repeat(maxWidth),
                " ".repeat(maxWidth / 2), " ".repeat(maxWidth - maxWidth / 4), " ".repeat(maxWidth / 2 - (Math.floor((double) maxWidth / 2) == (double) maxWidth / 2 ? 1 : 0)), " ".repeat(maxWidth / 2), " ".repeat(maxWidth - maxWidth / 4), " ".repeat(maxWidth / 2 - (Math.floor((double) maxWidth / 2) == (double) maxWidth / 2 ? 1 : 0)), " ".repeat(maxWidth / 2), " ".repeat(maxWidth - maxWidth / 4), " ".repeat(maxWidth / 2 - (Math.floor((double) maxWidth / 2) == (double) maxWidth / 2 ? 1 : 0)),
                ln, ln, ln,
                e, e, e, e, materialStats[0], materialStats[1],
                e, e, e, e, imbalanceStats[0], imbalanceStats[1],
                pawnsStats[0], pawnsStats[1], pawnsStats[2], pawnsStats[3], pawnsStats[4], pawnsStats[5],
                knightsStats[0], knightsStats[1], knightsStats[2], knightsStats[3], knightsStats[4], knightsStats[5],
                bishopsStats[0], bishopsStats[1], bishopsStats[2], bishopsStats[3], bishopsStats[4], bishopsStats[5],
                rooksStats[0], rooksStats[1], rooksStats[2], rooksStats[3], rooksStats[4], rooksStats[5],
                queenStats[0], queenStats[1], queenStats[2], queenStats[3], queenStats[4], queenStats[5],
                kingStats[0], kingStats[1], kingStats[2], kingStats[3], kingStats[4], kingStats[5],
                mobilityStats[0], mobilityStats[1], mobilityStats[2], mobilityStats[3], mobilityStats[4], mobilityStats[5],
                threatsStats[0], threatsStats[1], threatsStats[2], threatsStats[3], threatsStats[4], threatsStats[5],
                passedStats[0], passedStats[1], passedStats[2], passedStats[3], passedStats[4], passedStats[5],
                spaceStats[0], spaceStats[1], spaceStats[2], spaceStats[3], spaceStats[4], spaceStats[5],
                e, e, e, e, winnableStats[0], winnableStats[1],
                ln, ln, ln,
                e, e, e, e, totalStats[0], totalStats[1],
                ln, ln, ln
        );


        System.out.println(prettyTable);
    }
}
