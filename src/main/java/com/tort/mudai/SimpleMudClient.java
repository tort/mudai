package com.tort.mudai;

import com.tort.mudai.command.RawWriteCommand;
import com.tort.mudai.event.AdapterExceptionEvent;
import com.tort.mudai.event.ConnectionClosedEvent;
import com.tort.mudai.event.Event;
import com.tort.mudai.event.RawReadEvent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

public class SimpleMudClient {
    public static void main(String[] args) {
        final Adapter adapter = new Adapter(new SimpleEventListener());
        adapter.start();

        final InputStreamReader reader = new InputStreamReader(System.in);
        final CharBuffer charBuffer = CharBuffer.allocate(Adapter.OUT_BUF_SIZE);
        try {
            while (true) {
                reader.read(charBuffer);
                charBuffer.flip();
                adapter.send(new RawWriteCommand(charBuffer));
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
            }
        }
    }
}
