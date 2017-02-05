package com.happyslowly.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FlatFileTest {
    private FlatFile file;

    @Before
    public void setUp() throws IOException {
        file = new FlatFile("src/test/resources/a.txt", ",");
    }

    @Test
    public void testNormal() {
        Iterator<Map<String, Object>> it = file.iterator();
        Map<String, Object> entry = it.next();
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("A", "1");
        expected.put("C", "NULL");
        assertEquals(expected, entry);
    }

    @Test
    public void testIgnoreNull() {
        file.setNullValuesIgnored(true);
        Iterator<Map<String, Object>> it = file.iterator();
        Map<String, Object> entry = it.next();
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("A", "1");
        assertEquals(expected, entry);
    }

    @Test
    public void testNonIgnored() {
        file.setEmptyValuesIgnored(false);
        file.setNullValuesIgnored(false);
        Iterator<Map<String, Object>> it = file.iterator();
        Map<String, Object> entry = it.next();
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("A", "1");
        expected.put("B", "");
        expected.put("C", "NULL");
        assertEquals(expected, entry);
    }

    @After
    public void tearDown() throws Exception {
        file.close();
    }
}
