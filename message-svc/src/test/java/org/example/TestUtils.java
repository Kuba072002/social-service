package org.example;

import org.apache.commons.text.RandomStringGenerator;

public class TestUtils {

    private static final RandomStringGenerator STRING_GENERATOR = new RandomStringGenerator.Builder()
            .withinRange('A', 'Z')
            .withinRange('a', 'z')
            .filteredBy(Character::isLetter)
            .get();

    public static String randomAlphabetic(int length) {
        return STRING_GENERATOR.generate(length);
    }
}
