package com.tort.mudai;

import com.tort.mudai.command.Command;
import com.tort.mudai.command.RawWriteCommand;
import com.tort.mudai.event.AdapterExceptionEvent;
import com.tort.mudai.event.ConnectionClosedEvent;
import com.tort.mudai.event.Event;
import com.tort.mudai.event.RawReadEvent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

public class SimpleMudClient {
    public static void main(String[] args) {
        final BlockingQueue<Command> commands = new PriorityBlockingQueue<Command>();
        final Adapter adapter = new Adapter(new SimpleEventListener(), commands, Executors.newFixedThreadPool(2));
        adapter.start();

        final InputStreamReader reader = new InputStreamReader(System.in);
        final CharBuffer charBuffer = CharBuffer.allocate(Adapter.OUT_BUF_SIZE);
        try {
            while (reader.read(charBuffer) != -1) {
                charBuffer.flip();
                commands.put(new RawWriteCommand(charBuffer.toString()));
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
            }
        }
    }
}
