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

public class Adapter {
    private AdapterEventListener _adapterEventListener = new EmptyEventListener();
    private SocketChannel _channel;
    private final Charset charset = Charset.forName("KOI8-R");
    private final CharsetDecoder decoder = charset.newDecoder();
    private final CharsetEncoder encoder = charset.newEncoder();
    private ByteBuffer _outBuffer = ByteBuffer.allocate(128);

    private void setEventListener(final AdapterEventListener adapterEventListener) {
        _adapterEventListener = adapterEventListener;
    }

    private void start() {
        try {
            _channel = SocketChannel.open();
            _channel.connect(new InetSocketAddress("mud.ru", 4000));

            final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
            final CharBuffer charBuffer = CharBuffer.allocate(4096);
            while(_channel.read(byteBuffer) != -1){
                byteBuffer.flip();
                decoder.decode(byteBuffer, charBuffer, false);
                charBuffer.flip();
                _adapterEventListener.rawInput(charBuffer);
                byteBuffer.clear();
                charBuffer.clear();
            }

            _adapterEventListener.connectionClosed();
        } catch (IOException e) {
            _adapterEventListener.networkException(new AdapterException(e));
        }
    }

    private void rawWrite(final CharBuffer charBuffer) {
        encoder.encode(charBuffer, _outBuffer, false);
        try {
            _outBuffer.flip();
            _channel.write(_outBuffer);
            _outBuffer.clear();
        } catch (IOException e) {
            _adapterEventListener.networkException(new AdapterException(e));
        }
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new MudaiModule());
        Adapter adapter = injector.getInstance(Adapter.class);

        adapter.setEventListener(new AdapterEventListener(){
            public void networkException(final AdapterException e) {
                print("network error: " + e.getMessage());
            }

            public void connectionClosed() {
                print("connection closed");
            }

            public void rawInput(final CharBuffer charBuffer) {
                System.out.println(charBuffer.toString());
            }

            private void print(final String message) {
                System.out.println(message);
            }
        });
        adapter.start();

        final InputStreamReader reader = new InputStreamReader(System.in);
        final CharBuffer charBuffer = CharBuffer.allocate(128);
        try {
            while(true){
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
