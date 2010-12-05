package com.tort.mudai;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.tort.mudai.event.AdapterExceptionEvent;
import com.tort.mudai.event.ConnectionClosedEvent;
import com.tort.mudai.event.Event;
import com.tort.mudai.event.RawInputEvent;

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

    private AdapterEventListener _adapterEventListener;
    private SocketChannel _channel;

    @Inject
    public Adapter(final AdapterEventListener adapterEventListener) {
        _adapterEventListener = adapterEventListener;
    }

    private void start() {
        try {
            _channel = SocketChannel.open();
            _channel.connect(new InetSocketAddress("mud.ru", 4000));
        } catch (IOException e) {
            _adapterEventListener.raise(new AdapterExceptionEvent(e));
        }

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            public void run() {
                try {
                    read();
                    _adapterEventListener.raise(new ConnectionClosedEvent());
                } catch (IOException e) {
                    _adapterEventListener.raise(new AdapterExceptionEvent(e));
                }
            }
        });
    }

    private void read() throws IOException {
        while (_channel.read(_inByteBuffer) != -1) {
            _inByteBuffer.flip();
            decoder.decode(_inByteBuffer, _inCharBuffer, false);
            _inCharBuffer.flip();
            _adapterEventListener.raise(new RawInputEvent(_inCharBuffer));
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
            _adapterEventListener.raise(new AdapterExceptionEvent(e));
        }
    }

    public static void main(String[] args) {
        final AdapterEventListener listener = new AdapterEventListener() {
            private void print(final String message) {
                System.out.println(message);
            }

            @Override
            public void raise(final Event event) {
                if (event instanceof AdapterExceptionEvent) {
                    AdapterExceptionEvent aee = (AdapterExceptionEvent) event;
                    print("network error: " + aee.getException());
                } else if (event instanceof ConnectionClosedEvent) {
                    print("connection closed");
                    System.exit(0);
                } else if (event instanceof RawInputEvent) {
                    RawInputEvent rie = (RawInputEvent) event;
                    System.out.print(rie.getInCharBuffer());
                }
            }
        };
        final Adapter adapter = new Adapter(listener);
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
