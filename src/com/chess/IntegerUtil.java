package com.chess;

import static com.chess.SearchConstants.*;

public class IntegerUtil {

    public static int _int(boolean bool) {
        return bool ? 1 : 0;
    }
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
    public static int index2(int x, int y, int height) {
        return height * y + x;
    }
    public static int index3(int x, int y, int z, int width, int height) {
        return x + width * (y + height * z);
    }


}
