package com.tort.mudai.telnet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class ChannelReader {
    private final SocketChannel _channel;
    private static final int IN_BUF_SIZE = 4096;
    private final ByteBuffer _inByteBuffer = ByteBuffer.allocateDirect(IN_BUF_SIZE);
    private final CharBuffer _inCharBuffer = CharBuffer.allocate(IN_BUF_SIZE);
    private final CharsetDecoder _decoder;

    public ChannelReader(final SocketChannel channel, final Charset charset) {
        _channel = channel;
        _decoder = charset.newDecoder();
    }

    public String read() throws IOException {
        int read = _channel.read(_inByteBuffer);
        if(read <= 0){
            return null;
        }

        _inByteBuffer.flip();
        _decoder.decode(_inByteBuffer, _inCharBuffer, false);
        _inCharBuffer.flip();

        final StringBuilder builder = new StringBuilder(_inCharBuffer);

        _inCharBuffer.clear();
        _inByteBuffer.clear();

        return builder.toString();
    }
}
