package com.vk.dwzkf;

import com.vk.dwzkf.server.Server;

public class App {
    public static void main(String[] args) {
        Server server = new Server();
        server.setUp();
        server.start();
    }
}
