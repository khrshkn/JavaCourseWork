package khoroshkin.coursework;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void testProcessArguments_ValidInput() {
        String[] args = {"3", "20", "spotify", "assembly", "virustotal", "json"};
        Main.Arguments result = Main.processArguments(args);

        assertEquals(3, result.numThreads);
        assertEquals(20, result.timeout);
        assertArrayEquals(new String[]{"spotify", "assembly", "virustotal"}, result.services);
        assertEquals("json", result.format);
    }

    @Test
    void testProcessArguments_ValidInputWithCsvFormat() {
        String[] args = {"2", "30", "spotify", "csv"};
        Main.Arguments result = Main.processArguments(args);

        assertEquals(2, result.numThreads);
        assertEquals(30, result.timeout);
        assertArrayEquals(new String[]{"spotify"}, result.services);
        assertEquals("csv", result.format);
    }

    @Test
    void testProcessArguments_InsufficientArguments() {
        String[] args = {};
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Main.processArguments(args);
        });
        assertTrue(exception.getMessage().contains("Required format"));
    }

    @Test
    void testProcessArguments_InvalidThreads() {
        String[] args = {"invalid", "20", "spotify", "json"};
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Main.processArguments(args);
        });
        assertEquals("For input string: \"invalid\"", exception.getMessage());
    }

    @Test
    void testProcessArguments_NegativeThreads() {
        String[] args = {"-1", "20", "spotify", "json"};
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Main.processArguments(args);
        });
        assertEquals("Threads and timeout must be integers and greater than 0", exception.getMessage());
    }

    @Test
    void testProcessArguments_ZeroTimeout() {
        String[] args = {"3", "0", "spotify", "json"};
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Main.processArguments(args);
        });
        assertEquals("Threads and timeout must be integers and greater than 0", exception.getMessage());
    }

    @Test
    void testProcessArguments_NoServices() {
        String[] args = {"3", "20", "json"};
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Main.processArguments(args);
        });
        assertEquals("Services must have at least one service", exception.getMessage());
    }

    @Test
    void testProcessArguments_InvalidFormat() {
        String[] args = {"3", "20", "spotify", "xml"};
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Main.processArguments(args);
        });
        assertEquals("Format must be either \"json\" or \"csv\"", exception.getMessage());
    }
}