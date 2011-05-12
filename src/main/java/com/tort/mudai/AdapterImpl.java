package com.tort.mudai;

import com.google.inject.Inject;
import com.google.inject.name.Named;
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
    private SocketChannel _channel;
    private BlockingQueue<Command> _commands;
    private final ExecutorService _executor;
    private List<EventTrigger> _eventTriggers = new ArrayList<EventTrigger>();
    private List<SimpleTrigger> _simpleTriggers = new ArrayList<SimpleTrigger>();
    public static final int OUT_BUF_SIZE = 128;
    private TelnetReader _telnetReader;
    private AdapterEventListener _listener;

    @Inject
    protected AdapterImpl(final BlockingQueue<Command> commands, final ExecutorService executor, @Named("person") AdapterEventListener listener) {
        _commands = commands;
        _executor = executor;
        _listener = listener;

        _eventTriggers.add(new LoginPromptTrigger());
        _eventTriggers.add(new PasswordPromptTrigger());
        _eventTriggers.add(new MoveTrigger());
        _eventTriggers.add(new LookAroundTrigger());

        _simpleTriggers.add(new SimpleTrigger(".*^\\* В связи с проблемами перевода фразы ANYKEY нажмите ENTER.*", new String[]{"", "смотр"}));
        _simpleTriggers.add(new SimpleTrigger(".*^Select one : $", new String[]{ENCODING}));

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
        _listener.handle(event);
    }

    private void executeCommands() {
        //noinspection InfiniteLoopStatement
        do {
            try {
                final Command command = _commands.take();
                if (command instanceof StartSessionCommand) {
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

        for (SimpleTrigger trigger : _simpleTriggers) {
            if (trigger.matches(input)) {
                final String[] actions = trigger.getAction();
                for (String action : actions) {
                    _commands.put(new SimpleCommand(action));
                }
            }
        }

        for (EventTrigger trigger : _eventTriggers) {
            if (trigger.matches(input)) {
                events.add(trigger.createEvent(input));
            }
        }

        return events;
    }
}
