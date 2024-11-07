package org.ts;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server("localhost",9090);
        server.start();
    }


}

