package com.tort.mudai;

import com.google.inject.Inject;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.command.StartSessionCommand;
import com.tort.mudai.event.*;
import com.tort.mudai.telnet.TelnetParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class AdapterImpl implements Adapter {
    private static final int IN_BUF_SIZE = 4096;
    private static final String ENCODING = "0";

    private final ByteBuffer _outByteBuffer = ByteBuffer.allocate(OUT_BUF_SIZE);
    private final ByteBuffer _inByteBuffer = ByteBuffer.allocateDirect(IN_BUF_SIZE);
    private final CharBuffer _inCharBuffer = CharBuffer.allocate(IN_BUF_SIZE);

    private final Charset _charset = Charset.forName("KOI8-R");
    private final CharsetDecoder _decoder = _charset.newDecoder();

    private final List<AdapterEventListener> _listeners = new ArrayList<AdapterEventListener>();
    private SocketChannel _channel;
    private BlockingQueue<Command> _commands;
    private final ExecutorService _executor;
    private TelnetParser _telnetParser;
    private List<MatchingEvent> _events = new ArrayList<MatchingEvent>();
    private List<Trigger> _triggers = new ArrayList<Trigger>();
    public static final int OUT_BUF_SIZE = 128;

    @Inject
    public AdapterImpl(final BlockingQueue<Command> commands, final ExecutorService executor, final TelnetParser telnetParser) {
        _commands = commands;
        _executor = executor;
        _telnetParser = telnetParser;

        _events.add(new LoginPromptEvent());
        _events.add(new PasswordPromptEvent());

        _triggers.add(new Trigger(".*^\\* В связи с проблемами перевода фразы ANYKEY нажмите ENTER.*", ""));
        _triggers.add(new Trigger(".*^Select one : $", ENCODING));

        _executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    executeCommands();
                } catch (Throwable e) {
                    notifySubscribers(new ProgrammerErrorEvent(e));
                }
            }
        });
    }

    @Override
    public void subscribe(AdapterEventListener listener){
        _listeners.add(listener);
    }

    @Override
    public void unsubscribe(AdapterEventListener listener){
        _listeners.remove(listener);
    }

    private void start() {
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
                } catch (Throwable e){
                    notifySubscribers(new ProgrammerErrorEvent(e));
                }
            }
        });
    }

    private void notifySubscribers(final Collection<Event> events) {
        for (Event event : events) {
            notifySubscribers(event);
        }
    }

    private void notifySubscribers(final Event event) {
        for (AdapterEventListener listener : _listeners) {
            listener.handle(event);
        }
    }

    private void executeCommands() {
        do {
            try {
                final Command command = _commands.take();
                if(command instanceof StartSessionCommand){
                    start();
                } else {
                    send(command);
                }
            } catch (InterruptedException e) {
                notifySubscribers(new AdapterExceptionEvent(e));
            }
        } while (true);
    }

    private void send(final Command command) {
        final String commandText = command.render() + "\n";
        final byte[] bytes = commandText.getBytes(_charset);
        try {
            _outByteBuffer.flip();
            _channel.write(ByteBuffer.wrap(bytes));
            _outByteBuffer.clear();
        } catch (IOException e) {
            notifySubscribers(new AdapterExceptionEvent(e));
        }
    }

    @Override
    public void submit(final Command command) {
        try {
            _commands.put(command);
        } catch (InterruptedException e) {
            System.out.println("error populating command queue");
        }
    }

    private void read() throws IOException, InterruptedException {
        while (_channel.read(_inByteBuffer) != -1) {
            _inByteBuffer.flip();
            _decoder.decode(_inByteBuffer, _inCharBuffer, false);
            _inCharBuffer.flip();

            notifySubscribers(new RawReadEvent(_inCharBuffer));
            notifySubscribers(parseInput(_inCharBuffer));
            _inByteBuffer.clear();
            _inCharBuffer.clear();
        }
    }

    private Collection<Event> parseInput(final CharBuffer inCharBuffer) throws InterruptedException {
        final Collection<Event> events = new ArrayList<Event>();

        final String parseResult = _telnetParser.parse(inCharBuffer);

        for (Trigger trigger : _triggers) {
            if(trigger.matches(parseResult)){
                _commands.put(new SimpleCommand(trigger.getAction()));
            }
        }

        for (MatchingEvent event : _events) {
            if(event.matches(parseResult)){
                events.add(event);
            }
        }

        return events;
    }
}
