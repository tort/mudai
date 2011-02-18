package com.tort.mudai.telnet;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.CharBuffer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@Test
public class TelnetReaderTest {
    private TelnetReader _telnetReader;
    private ChannelReader _channelReader;
    private final String IAC_GA = new String(new char[]{255, 249});

    @BeforeMethod
    public void setUp() throws Exception {
        _channelReader = mock(ChannelReader.class);

        _telnetReader = new TelnetReader(_channelReader);
    }

    public void testRead() throws IOException {
        when(_channelReader.read())
                .thenReturn("before" + IAC_GA + "after")
                .thenReturn(null);

        String[] read = _telnetReader.read();
        String result = read[0];

        assertEquals(result, "before");

        read = _telnetReader.read();
        result = read[0];

        assertEquals(result, "after");
    }

    public void nonGARead() throws IOException {
        when(_channelReader.read()).thenReturn("message");

        String[] read = _telnetReader.read();
        String result = read[0];

        assertEquals(result, "message");
    }

    public void manyReads() throws IOException {
        when(_channelReader.read())
                .thenReturn("before" + IAC_GA + "after");

        String[] read = new String[0];
        for (int i = 0; i < 1000; i++) {
            read = _telnetReader.read();
        }

        when(_channelReader.read()).thenReturn(null);
        read = _telnetReader.read();
        String result = read[0];

        assertEquals(result, "after");
    }
}
