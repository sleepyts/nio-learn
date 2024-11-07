package org.ts;

public class Main {
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                new Server("localhost",9090).start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();

    }


}

