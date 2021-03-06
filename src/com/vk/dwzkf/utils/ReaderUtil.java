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
        if (in!=null) reader = new BufferedReader(new InputStreamReader(in));
        if (out!=null) writer = new PrintWriter(new OutputStreamWriter(out));
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
            in.close();
        } catch (Exception e) {}
        try {
            out.close();
        } catch (Exception e) {}
        try {
            reader.close();
        } catch (Exception e) {}
        try {
            writer.close();
        } catch (Exception e) {}
    }

    public void sendMessage(String message) {
        if (writer!=null) {
            writer.println(message);
            writer.flush();
        }
    }

    public String getMessage() throws Exception {
        if (reader!=null)
            return reader.readLine();
        else
            return null;
    }
}
