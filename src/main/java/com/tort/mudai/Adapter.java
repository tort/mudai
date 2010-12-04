package com.tort.mudai;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.tort.mudai.exception.AdapterException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Adapter {
    private static final int OUT_BUF_SIZE = 128;
    private static final int IN_BUF_SIZE = 4096;

    private final ByteBuffer _outByteBuffer = ByteBuffer.allocate(OUT_BUF_SIZE);
    private final ByteBuffer _inByteBuffer = ByteBuffer.allocateDirect(IN_BUF_SIZE);
    private final CharBuffer _inCharBuffer = CharBuffer.allocate(IN_BUF_SIZE);

    private final Charset charset = Charset.forName("KOI8-R");
    private final CharsetDecoder decoder = charset.newDecoder();
    private final CharsetEncoder encoder = charset.newEncoder();

    private AdapterEventListener _adapterEventListener = new EmptyEventListener();
    private SocketChannel _channel;

    private void setEventListener(final AdapterEventListener adapterEventListener) {
        _adapterEventListener = adapterEventListener;
    }

    private void start() {
        try {
            _channel = SocketChannel.open();
            _channel.connect(new InetSocketAddress("mud.ru", 4000));
        } catch (IOException e) {
            _adapterEventListener.networkException(new AdapterException(e));
        }

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            public void run() {
                try {
                    read();
                    _adapterEventListener.connectionClosed();
                } catch (IOException e) {
                    _adapterEventListener.networkException(new AdapterException(e));
                }
            }
        });
    }

    private void read() throws IOException {
        while (_channel.read(_inByteBuffer) != -1) {
            _inByteBuffer.flip();
            decoder.decode(_inByteBuffer, _inCharBuffer, false);
            _inCharBuffer.flip();
            _adapterEventListener.rawInput(_inCharBuffer);
            _inByteBuffer.clear();
            _inCharBuffer.clear();
        }
    }

    private void rawWrite(final CharBuffer charBuffer) {
        encoder.encode(charBuffer, _outByteBuffer, false);
        try {
            _outByteBuffer.flip();
            _channel.write(_outByteBuffer);
            _outByteBuffer.clear();
        } catch (IOException e) {
            _adapterEventListener.networkException(new AdapterException(e));
        }
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new MudaiModule());
        Adapter adapter = injector.getInstance(Adapter.class);

        adapter.setEventListener(new AdapterEventListener() {
            public void networkException(final AdapterException e) {
                print("network error: " + e.getMessage());
            }

            public void connectionClosed() {
                print("connection closed");
                System.exit(0);
            }

            public void rawInput(final CharBuffer charBuffer) {
                System.out.print(charBuffer.toString());
            }

            private void print(final String message) {
                System.out.println(message);
            }
        });
        adapter.start();

        final InputStreamReader reader = new InputStreamReader(System.in);
        final CharBuffer charBuffer = CharBuffer.allocate(OUT_BUF_SIZE);
        try {
            while (true) {
                final int number = reader.read(charBuffer);
                charBuffer.flip();
                adapter.rawWrite(charBuffer);
                charBuffer.clear();
            }
        } catch (IOException e) {
            System.out.println("read keyboard input error");
        }
    }
}
