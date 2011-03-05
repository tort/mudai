package com.tort.mudai;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.tort.mudai.command.RawWriteCommand;
import com.tort.mudai.event.*;
import com.tort.mudai.task.Person;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

public class SimpleMudClient {
    private Person _person;
    private static final String FIND_PATH_COMMAND = "/путь";
    private static final String LIST_LOCATIONS_COMMAND = "/лист";
    private static final String TRAVEL_COMMAND = "/го";

    @Inject
    public SimpleMudClient(final Person person) {
        _person = person;
    }

    public void start() {
        _person.subscribe(new SimpleEventListener());
        _person.start();

        final InputStreamReader reader = new InputStreamReader(System.in);
        final CharBuffer charBuffer = CharBuffer.allocate(AdapterImpl.OUT_BUF_SIZE);
        try {
            while (reader.read(charBuffer) != -1) {
                charBuffer.flip();
                final String command = charBuffer.toString();
                if(command.startsWith(FIND_PATH_COMMAND)){
                    final String path = _person.pathTo(command.substring(FIND_PATH_COMMAND.length() + 1, command.length() - 1));
                    System.out.println("PATH: " + path);
                } else if(command.startsWith(LIST_LOCATIONS_COMMAND)) {
                    for (String location : _person.locationTitles()) {
                        System.out.println("LOCATION: " + location);
                    }
                } else if(command.startsWith(TRAVEL_COMMAND)) {
                    _person.travel(command.substring(TRAVEL_COMMAND.length() + 1, command.length() - 1));
                } else {
                    _person.submit(new RawWriteCommand(command));
                }
                
                charBuffer.clear();
            }
        } catch (IOException e) {
            System.out.println("read keyboard input error");
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
