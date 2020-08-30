package com.vk.dwzkf.utils;

import com.vk.dwzkf.thread.AbstractThread;

import java.io.*;

public class ReaderUtil extends AbstractThread {
    private InputStream in;
    private OutputStream out;
    private BufferedReader reader;
    private PrintWriter writer;

    public ReaderUtil(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public boolean setUpActions() {
        reader = new BufferedReader(new InputStreamReader(in));
        writer = new PrintWriter(new OutputStreamWriter(out));
        return true;
    }

    @Override
    public void preMain() {

    }

    @Override
    public void mainActions() {

    }

    @Override
    public void postMain() {

    }

    @Override
    public void closeActions() {
        try {
            reader.close();
            writer.close();
        }
        catch (Exception e) {

        }
    }

    public void sendMessage(String message) {
        writer.print(message);
    }

    public void sendMessageLn(String message) {
        writer.println(message);
    }

    public String getMessage() {
        try {
            return reader.readLine();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
