package com.amoura.book;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.function.Function;
import java.util.function.Predicate;

@SpringBootTest
class BookNetworkApiApplicationTests {

    @Test
    void contextLoads() {
        Function<String, Integer> converter = str -> {
            String trimmed = str.trim();
            int length = trimmed.length();
            return length;
        };
        int length = converter.apply(" Hello, Lambda ");
    }

    @Test
    void testPredicat() {
        Predicate<String> isPalindrome = str -> {
            String cleanStr = str.replaceAll("\\s+", "").toLowerCase();
            return cleanStr.equals(new StringBuilder(cleanStr).reverse().toString());
        };

        boolean result = isPalindrome.test("A Santa at NASA"); // Output: true

    }


}
