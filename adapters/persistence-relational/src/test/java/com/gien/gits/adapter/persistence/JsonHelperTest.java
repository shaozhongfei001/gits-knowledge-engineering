package com.gien.gits.adapter.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class JsonHelperTest {

    @Test
    void toJsonArrayWithItems() {
        List<String> items = List.of("alpha", "beta", "gamma");
        String json = JsonHelper.toJsonArray(items);

        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
        assertTrue(json.contains("alpha"));
        assertTrue(json.contains("beta"));
        assertTrue(json.contains("gamma"));
    }

    @Test
    void toJsonArrayWithNullReturnsEmptyArray() {
        assertEquals("[]", JsonHelper.toJsonArray(null));
    }

    @Test
    void toJsonArrayWithEmptyListReturnsEmptyArray() {
        assertEquals("[]", JsonHelper.toJsonArray(List.of()));
    }

    @Test
    void parseStringListWithValidJson() {
        String json = "[\"alpha\", \"beta\", \"gamma\"]";
        List<String> result = JsonHelper.parseStringList(json);

        assertEquals(3, result.size());
        assertEquals("alpha", result.get(0));
        assertEquals("beta", result.get(1));
        assertEquals("gamma", result.get(2));
    }

    @Test
    void parseStringListWithNullReturnsEmpty() {
        assertEquals(List.of(), JsonHelper.parseStringList(null));
    }

    @Test
    void parseStringListWithEmptyStringReturnsEmpty() {
        assertEquals(List.of(), JsonHelper.parseStringList(""));
        assertEquals(List.of(), JsonHelper.parseStringList("  "));
    }

    @Test
    void roundTripStringList() {
        List<String> original = List.of("item1", "item2", "item3");
        String json = JsonHelper.toJsonArray(original);
        List<String> restored = JsonHelper.parseStringList(json);

        assertEquals(original, restored);
    }

    @Test
    void toJsonArrayEscapesSpecialCharacters() {
        List<String> items = List.of("value\"with\"quotes", "back\\slash");
        String json = JsonHelper.toJsonArray(items);

        assertTrue(json.contains("\\\""));
        assertTrue(json.contains("\\\\"));
    }
}
