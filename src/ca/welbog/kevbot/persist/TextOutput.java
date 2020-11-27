package ca.welbog.kevbot.persist;
//package kevbot;

/* 
Copyright 2004

This file is part of KevBot.

Use KevBot at your own risk.
*/

import java.io.*;

/**
 * TextOutput is designed to output text to a file.
 * <p>
 * Its methods allow storing Strings into files.
 * @version 1.0.0
 * @see Kevbot
 * @see TextInput
 */
@SuppressWarnings("all")
public class TextOutput {
    private String filename = System.getProperty("user.dir");
    private BufferedWriter out;
    
    /** 
     * Creates a new instance of TextOutput 
     * @param file The name of the file to be written to.
     */
    public TextOutput(String file) throws IOException {
        filename+=file;
        out = new BufferedWriter(new FileWriter(file, false));
    }
    
    /**
     * Print a String to file.
     * @param word The String to print.
     */
    public synchronized void print(String word) throws IOException {
        out.write(word);
    }
    
    /**
     * Print a String to file, followed by a newline.
     * @param line The String to print.
     */
    public synchronized void println(String line) throws IOException {
        print(line + "\n");
    }
    
    /**
     * Close the IO stream.
     */
    public synchronized void close() throws IOException {
        out.close();
    }
}
