package com.tort.mudai;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.RawWriteCommand;
import com.tort.mudai.event.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.concurrent.BlockingQueue;

public class SimpleMudClient {
    private Person _person;
    private Adapter _adapter;
    private BlockingQueue<Command> _commands;

    @Inject
    public SimpleMudClient(final Adapter adapter, final BlockingQueue commands, final Person person) {
        _adapter = adapter;
        _commands = commands;
        _person = person;
    }

    public void start() {
        _adapter.subscribe(new SimpleEventListener());
        _adapter.subscribe(_person);
        _adapter.start();

        final InputStreamReader reader = new InputStreamReader(System.in);
        final CharBuffer charBuffer = CharBuffer.allocate(Adapter.OUT_BUF_SIZE);
        try {
            while (reader.read(charBuffer) != -1) {
                charBuffer.flip();
                _commands.put(new RawWriteCommand(charBuffer.toString()));
                charBuffer.clear();
            }
        } catch (IOException e) {
            System.out.println("read keyboard input error");
        } catch (InterruptedException e) {
            System.out.println("error populating command queue");
        }
    }

    private static class SimpleEventListener implements AdapterEventListener {
        private void print(final String message) {
            System.out.println(message);
        }

        @Override
        public void handle(final Event event) {
            if (event instanceof AdapterExceptionEvent) {
                AdapterExceptionEvent aee = (AdapterExceptionEvent) event;
                print("network error: " + aee.getException());
            } else if (event instanceof ConnectionClosedEvent) {
                print("connection closed");
                System.exit(0);
            } else if (event instanceof RawReadEvent) {
                RawReadEvent rie = (RawReadEvent) event;
                System.out.print(rie.getInCharBuffer());
            } else if (event instanceof ProgrammerErrorEvent){
                ProgrammerErrorEvent pee = (ProgrammerErrorEvent) event;
                pee.getException().printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new MudaiModule());
        final SimpleMudClient simpleMudClient = injector.getInstance(SimpleMudClient.class);

        simpleMudClient.start();
    }
}
