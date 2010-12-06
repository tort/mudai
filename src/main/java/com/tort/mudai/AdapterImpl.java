package com.tort.mudai;

import com.google.inject.Inject;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.RawWriteCommand;
import com.tort.mudai.event.AdapterExceptionEvent;
import com.tort.mudai.event.ConnectionClosedEvent;
import com.tort.mudai.event.Event;
import com.tort.mudai.event.RawReadEvent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class AdapterImpl implements Adapter {
    private static final int IN_BUF_SIZE = 4096;

    private final ByteBuffer _outByteBuffer = ByteBuffer.allocate(OUT_BUF_SIZE);
    private final ByteBuffer _inByteBuffer = ByteBuffer.allocateDirect(IN_BUF_SIZE);
    private final CharBuffer _inCharBuffer = CharBuffer.allocate(IN_BUF_SIZE);

    private final Charset _charset = Charset.forName("KOI8-R");
    private final CharsetDecoder _decoder = _charset.newDecoder();

    private List<AdapterEventListener> _listeners = new ArrayList<AdapterEventListener>();
    private SocketChannel _channel;
    private BlockingQueue<Command> _commands;
    private final ExecutorService _executor;

    @Inject
    public AdapterImpl(final BlockingQueue commands, final ExecutorService executor) {
        _commands = commands;
        _executor = executor;
    }

    @Override
    public void subscribe(AdapterEventListener listener){
        _listeners.add(listener);
    }

    @Override
    public void unsubscribe(AdapterEventListener listener){
        _listeners.remove(listener);
    }

    @Override
    public void start() {
        try {
            _channel = SocketChannel.open();
            _channel.connect(new InetSocketAddress("mud.ru", 4000));
        } catch (IOException e) {
            notifySubscribers(new AdapterExceptionEvent(e));
        }

        _executor.submit(new Runnable() {
            public void run() {
                try {
                    read();
                    notifySubscribers(new ConnectionClosedEvent());
                } catch (IOException e) {
                    notifySubscribers(new AdapterExceptionEvent(e));
                }
            }
        });

        _executor.submit(new Runnable() {
            @Override
            public void run() {
                executeCommands();
            }
        });
    }

    private void notifySubscribers(final Event event) {
        for (AdapterEventListener listener : _listeners) {
            listener.handle(event);
        }
    }

    private void executeCommands() {
        do {
            try {
                send(_commands.take());
            } catch (InterruptedException e) {
                notifySubscribers(new AdapterExceptionEvent(e));
            }
        } while (true);
    }

    @Override
    public void send(final Command command) {
        if (command instanceof RawWriteCommand) {
            RawWriteCommand rwc = (RawWriteCommand) command;
            final byte[] bytes = rwc.getCharBuffer().getBytes(_charset);
            try {
                _outByteBuffer.flip();
                _channel.write(ByteBuffer.wrap(bytes));
                _outByteBuffer.clear();
            } catch (IOException e) {
                notifySubscribers(new AdapterExceptionEvent(e));
            }
        }
    }

    private void read() throws IOException {
        while (_channel.read(_inByteBuffer) != -1) {
            _inByteBuffer.flip();
            _decoder.decode(_inByteBuffer, _inCharBuffer, false);
            _inCharBuffer.flip();
            notifySubscribers(new RawReadEvent(_inCharBuffer));
            _inByteBuffer.clear();
            _inCharBuffer.clear();
        }
    }
}
