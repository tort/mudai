package com.tort.mudai;

import com.google.inject.Inject;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.command.StartSessionCommand;
import com.tort.mudai.event.*;
import com.tort.mudai.telnet.ChannelReader;
import com.tort.mudai.telnet.TelnetReader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class AdapterImpl implements Adapter {
    private static final String ENCODING = "0";

    private final ByteBuffer _outByteBuffer = ByteBuffer.allocate(OUT_BUF_SIZE);
    private final Charset _charset = Charset.forName("KOI8-R");
    private final List<AdapterEventListener> _listeners = new ArrayList<AdapterEventListener>();
    private SocketChannel _channel;
    private BlockingQueue<Command> _commands;
    private final ExecutorService _executor;
    private TelnetParser _telnetParser;
    private List<EventTrigger> _eventTriggers = new ArrayList<EventTrigger>();
    private List<SimpleTrigger> _simpleTriggers = new ArrayList<SimpleTrigger>();
    public static final int OUT_BUF_SIZE = 128;
    private TelnetReader _telnetReader;

    @Inject
    public AdapterImpl(final BlockingQueue<Command> commands, final ExecutorService executor) {
        _commands = commands;
        _executor = executor;

        _eventTriggers.add(new LoginPromptTrigger());
        _eventTriggers.add(new PasswordPromptTrigger());
        _eventTriggers.add(new LookAroundTrigger());
        _eventTriggers.add(new MoveTrigger());

        _simpleTriggers.add(new SimpleTrigger(".*^\\* В связи с проблемами перевода фразы ANYKEY нажмите ENTER.*", ""));
        _simpleTriggers.add(new SimpleTrigger(".*^Select one : $", ENCODING));

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
            ChannelReader channelReader = new ChannelReader(_channel, _charset);
            _telnetReader = new TelnetReader(channelReader);
        } catch (IOException e) {
            notifySubscribers(new AdapterExceptionEvent(e));
        }

        _executor.submit(new Runnable() {
            public void run() {
                try {
                    String[] message;
                    while ((message = _telnetReader.read()) != null) {
                        for (String ga_block : message) {
                            notifySubscribers(new RawReadEvent(ga_block));
                            notifySubscribers(parseInput(ga_block));
                        }
                    }
                    notifySubscribers(new ConnectionClosedEvent());
                } catch (IOException e) {
                    notifySubscribers(new AdapterExceptionEvent(e));
                } catch (Throwable e) {
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
        //noinspection InfiniteLoopStatement
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

    private Collection<Event> parseInput(final String input) throws InterruptedException {
        final Collection<Event> events = new ArrayList<Event>();

        final String parseResult = _telnetParser.parse(inCharBuffer);

        for (SimpleTrigger trigger : _simpleTriggers) {
            if(trigger.matches(input)){
                _commands.put(new SimpleCommand(trigger.getAction()));
            }
        }

        for (EventTrigger trigger : _eventTriggers) {
            if(trigger.matches(parseResult)){
                events.add(trigger.createEvent(input));
            }
        }

        return events;
    }
}
