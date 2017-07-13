package nl.rubensten.texifyidea.util;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author Ruben Schellekens
 */
public final class Kindness {

    private static final Random RANDOM = new Random();

    private static final List<String> KIND_WORDS = Arrays.asList(
            "You are beautiful. You matter. You are awesome.",
            "While it is always best to believe in oneself, a little help from others is a great blessing.",
            "You have light and peace inside of you. If you let it out, you can change the world around you.",
            "You can do the thing!",
            "I hope that you are having a fantastic day.",
            "Confidence comes not from always being right but from not fearing to be wrong.",
            "There is nothing wrong with letting people who love you help you.",
            "If you look for the light you can often find it, but if you look for the dark that " +
                    "is all you will ever see.",
            "Sometimes the best way to solve your own problems is to help somebody else."
    );

    public static String getKindWords() {
        return KIND_WORDS.get(RANDOM.nextInt(KIND_WORDS.size()));
    }
}
