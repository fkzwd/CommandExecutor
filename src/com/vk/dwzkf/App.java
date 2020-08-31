package com.vk.dwzkf;

import com.vk.dwzkf.server.Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class App {
    public static void main(String[] args) {
        Server server = new Server();
        server.setUp();
        server.start();

        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                String s = r.readLine();
                if (s.equals("exit")) {
                    server.shutdown();
                    server.join();
                    break;
                }
                else if (s.equals("restart")) {
                    server.shutdown();
                    server.join();
                    server = new Server();
                    server.setUp();
                    server.start();
                }
                else if (s.equals("config")) {
                    server.showInfo();
                }
                else if (s.matches("set [a-zA-Z]+ [a-zA-Z0-9.]+")) {
                    String[] split = s.split(" ");
                    server.saveProperty(split[1], split[2]);
                }
            }
            catch (Exception e) {

            }
        }
    }
}
