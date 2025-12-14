package org.eclipsetrader.jessx.utils;

public class Logger {
    private String name;

    private Logger(String name) {
        this.name = name;
    }

    public static Logger getLogger(String name) {
        return new Logger(name);
    }

    public void debug(Object message) {
        System.out.println("[DEBUG] " + name + ": " + message);
    }

    public void debug(Object message, Throwable t) {
        System.out.println("[DEBUG] " + name + ": " + message);
        t.printStackTrace(System.out);
    }

    public void info(Object message) {
        System.out.println("[INFO] " + name + ": " + message);
    }

    public void info(Object message, Throwable t) {
        System.out.println("[INFO] " + name + ": " + message);
        t.printStackTrace(System.out);
    }

    public void warn(Object message) {
        System.err.println("[WARN] " + name + ": " + message);
    }

    public void warn(Object message, Throwable t) {
        System.err.println("[WARN] " + name + ": " + message);
        t.printStackTrace(System.err);
    }

    public void error(Object message) {
        System.err.println("[ERROR] " + name + ": " + message);
    }

    public void error(Object message, Throwable t) {
        System.err.println("[ERROR] " + name + ": " + message);
        t.printStackTrace(System.err);
    }

    public void fatal(Object message) {
        System.err.println("[FATAL] " + name + ": " + message);
    }

    public void fatal(Object message, Throwable t) {
        System.err.println("[FATAL] " + name + ": " + message);
        t.printStackTrace(System.err);
    }
}
