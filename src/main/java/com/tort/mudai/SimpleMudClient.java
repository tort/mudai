package com.tort.mudai;

import com.tort.mudai.event.AdapterExceptionEvent;
import com.tort.mudai.event.ConnectionClosedEvent;
import com.tort.mudai.event.Event;
import com.tort.mudai.event.RawInputEvent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

public class SimpleMudClient {
    public static void main(String[] args) {
        final AdapterEventListener listener = new AdapterEventListener() {
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
                } else if (event instanceof RawInputEvent) {
                    RawInputEvent rie = (RawInputEvent) event;
                    System.out.print(rie.getInCharBuffer());
                }
            }
        };
        final Adapter adapter = new Adapter(listener);
        adapter.start();

        final InputStreamReader reader = new InputStreamReader(System.in);
        final CharBuffer charBuffer = CharBuffer.allocate(Adapter.OUT_BUF_SIZE);
        try {
            while (true) {
                reader.read(charBuffer);
                charBuffer.flip();
                adapter.rawWrite(charBuffer);
                charBuffer.clear();
            }
        } catch (IOException e) {
            System.out.println("read keyboard input error");
        }
    }
}
