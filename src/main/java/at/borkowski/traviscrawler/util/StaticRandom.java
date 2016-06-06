package at.borkowski.traviscrawler.util;

import java.util.Random;

public class StaticRandom {
    private static final Random random = new Random();

    public static long nextLong() {
        return random.nextLong();
    }
}
