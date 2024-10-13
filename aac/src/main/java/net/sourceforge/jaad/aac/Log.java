/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2011-2019 The JCodec Project
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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