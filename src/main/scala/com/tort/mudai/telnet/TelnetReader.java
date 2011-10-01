package com.tort.mudai.telnet;

import java.io.IOException;
import java.nio.CharBuffer;

public class TelnetReader {

    private final String IAC_GA = new String(new char[]{255, 249});

    private StringBuilder _builder = new StringBuilder();
    private Mode _mode = Mode.NON_GA;
    private final ChannelReader _channelReader;

    public TelnetReader(final ChannelReader channelReader) {
        _channelReader = channelReader;
    }

    public String[] read() throws IOException {
        String inCharBuffer;
        while ((inCharBuffer = _channelReader.read()) != null) {
            _builder.append(inCharBuffer);

            int i = _builder.lastIndexOf(IAC_GA);
            if (i > -1)
                _mode = Mode.GA;

            if (_mode == Mode.GA) {
                if (i > -1) {
                    String result = _builder.substring(0, i + 2);
                    _builder.delete(0, i + 2);

                    return result.split(IAC_GA);
                }
            } else {
                String result = _builder.toString();
                _builder = new StringBuilder();

                return new String[]{result};
            }
        }

        return null;
    }

    private enum Mode {
        GA,
        NON_GA;
    }

}
