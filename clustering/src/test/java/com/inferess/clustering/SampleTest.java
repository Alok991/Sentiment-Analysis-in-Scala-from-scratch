package com.inferess.clustering;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SampleTest {

    @Test
    void sample() {
      assertEquals(2, 2);
       assertEquals(4, 4, "The optional assertion message is now the last parameter.");
       assertTrue(2 == 2, () -> "Assertion messages can be lazily evaluated -- "
               + "to avoid constructing complex messages unnecessarily.");
    }

}
