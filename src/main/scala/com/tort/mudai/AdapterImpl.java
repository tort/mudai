package com.tort.mudai;

import com.google.inject.Inject;
import com.tort.mudai.command.MultiCommand;
import com.tort.mudai.command.RenderableCommand;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.command.StartSessionCommand;
import com.tort.mudai.event.*;
import com.tort.mudai.gui.OutputPrinter;
import com.tort.mudai.task.EventDistributor;
import com.tort.mudai.telnet.ChannelReader;
import com.tort.mudai.telnet.TelnetReader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class AdapterImpl implements LegacyAdapter {
    private static final String ENCODING = "0";

    private final ByteBuffer _outByteBuffer = ByteBuffer.allocate(OUT_BUF_SIZE);
    private final Charset _charset = Charset.forName("KOI8-R");
    private volatile SocketChannel _channel;
    private BlockingQueue<RenderableCommand> _commands;
    private final ExecutorService _executor;
    private List<EventTrigger> _eventTriggers = new ArrayList<EventTrigger>();
    private List<SimpleTrigger> _simpleTriggers = new ArrayList<SimpleTrigger>();
    public static final int OUT_BUF_SIZE = 128;
    private TelnetReader _telnetReader;
    private EventDistributor _eventDistributor;
    private volatile Future telnetReaderFuture;
    private OutputPrinter _outputPrinter;

    @Inject
    public AdapterImpl(final BlockingQueue<RenderableCommand> commands,
                          final ExecutorService executor,
                          final EventDistributor eventDistributor,
                          final OutputPrinter outputPrinter
                          ) {
        _commands = commands;
        _executor = executor;
        _eventDistributor = eventDistributor;
        _outputPrinter = outputPrinter;

        _eventTriggers.add(new LoginPromptTrigger());
        _eventTriggers.add(new PasswordPromptTrigger());
        _eventTriggers.add(new GlanceTrigger());
        _eventTriggers.add(new FeelThirstTrigger(eventDistributor));
        _eventTriggers.add(new FeelHungerTrigger(eventDistributor));
        _eventTriggers.add(new InventoryTrigger(eventDistributor));
        _eventTriggers.add(new ExamineLiquidContainerTrigger(eventDistributor));
        _eventTriggers.add(new NotThirstyTrigger(eventDistributor));
        _eventTriggers.add(new NotHungryTrigger(eventDistributor));
        _eventTriggers.add(new EmptyLiquidContainerTrigger(eventDistributor));
        _eventTriggers.add(new CantFindItemTrigger(eventDistributor));
        _eventTriggers.add(new CantFindItemInContainerTrigger(eventDistributor));
        _eventTriggers.add(new EatTrigger(eventDistributor));
        _eventTriggers.add(new DrinkTrigger(eventDistributor));
        _eventTriggers.add(new DiscoverObstacleTrigger());
        _eventTriggers.add(new KillTrigger());
        _eventTriggers.add(new TakeItemTrigger(eventDistributor));
        _eventTriggers.add(new StatTrigger(eventDistributor));

        _simpleTriggers.add(new SimpleTrigger(".*^\\* В связи с проблемами перевода фразы ANYKEY нажмите ENTER.*", new String[]{"", "смотр"}));
        _simpleTriggers.add(new SimpleTrigger(".*^Select one : $", new String[]{ENCODING}));

        _executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    executeCommands();
                } catch (Throwable e) {
                    _eventDistributor.programmerError(e);
                }
            }
        });
    }

    private void start(String host, int port) {
        try {
            _channel = SocketChannel.open();
            _channel.connect(new InetSocketAddress(host, port));
            ChannelReader channelReader = new ChannelReader(_channel, _charset);
            _telnetReader = new TelnetReader(channelReader);
        } catch (IOException e) {
            _eventDistributor.adapterException(e);
        }

        telnetReaderFuture = _executor.submit(new Runnable() {
            public void run() {
                try {
                    String[] message;
                    while ((message = _telnetReader.read()) != null) {
                        for (String ga_block : message) {
                            _eventDistributor.rawReadEvent(ga_block);
                            parseAndFire(ga_block);
                        }
                    }
                    _eventDistributor.connectionClose();
                } catch (IOException e) {
                    _eventDistributor.adapterException(e);
                } catch (Throwable e) {
                    _eventDistributor.programmerError(e);
                }
            }
        });
    }

    private void executeCommands() {
        //noinspection InfiniteLoopStatement
        do {
            try {
                final RenderableCommand command = _commands.take();
                if (command instanceof StartSessionCommand) {
                    StartSessionCommand cmd = (StartSessionCommand) command;
                    start(cmd.getHost(), cmd.getPort());
                } else if(command instanceof CloseSessionCommand) {
                    telnetReaderFuture.cancel(true);
                    _channel.close();
                } else if(command instanceof MultiCommand) {
                    MultiCommand multiCommand = (MultiCommand) command;
                    for (RenderableCommand comm : multiCommand.getCommands()) {
                        send(comm);
                    }
                } else {
                    send(command);
                }
            } catch (InterruptedException e) {
                _eventDistributor.adapterException(e);
            } catch (IOException e) {
                _eventDistributor.adapterException(e);
            }
        } while (true);
    }

    private void send(final RenderableCommand command) {
        _outputPrinter.print(command.render() + "\n");
        final String commandText = command.render() + "\n";
        final byte[] bytes = commandText.getBytes(_charset);
        try {
            _outByteBuffer.flip();
            _channel.write(ByteBuffer.wrap(bytes));
            _outByteBuffer.clear();
        } catch (IOException e) {
            _eventDistributor.adapterException(e);
        }
    }

    @Override
    public void submit(final RenderableCommand command) {
        try {
            _commands.put(command);
        } catch (InterruptedException e) {
            System.out.println("error populating command queue");
        }
    }

    private void parseAndFire(final String input) throws InterruptedException {
        for (SimpleTrigger trigger : _simpleTriggers) {
            if (trigger.matches(input)) {
                final String[] actions = trigger.command();
                for (String action : actions) {
                    _commands.put(new SimpleCommand(action));
                }
            }
        }

        for (EventTrigger trigger : _eventTriggers) {
            if (trigger.matches(input)) {
                trigger.fireEvent(input);
            }
        }
    }
}
