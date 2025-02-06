package net.sourceforge.jaad.aac;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {

    private static final Logger LOGGER = java.util.logging.Logger.getLogger("JAADec");
    static {
        Level level;
        try {
            level = Level.parse(System.getProperty("jaad.log.level"));
        }
        catch (Exception e) {
            level = Level.OFF;
        }
        LOGGER.setLevel(level);
    }

    public static void setLevel(Level level) {
        LOGGER.setLevel(level == null ? Level.OFF : level);
    }

    public static void debug(String message) {
        LOGGER.log(Level.SEVERE, message);
    }
    
    public static void debug(String message, Object ... args) {
        LOGGER.log(Level.SEVERE, message, args);
    }

    public static void info(String message) {
        LOGGER.log(Level.INFO, message);
    }
    
    public static void info(String message, Object ...args) {
        LOGGER.log(Level.INFO, message, args);
    }

    public static void warn(String message) {
        LOGGER.log(Level.WARNING, message);
    }
    
    public static void warn(String message, Object ...args) {
        LOGGER.log(Level.WARNING, message, args);
    }

}