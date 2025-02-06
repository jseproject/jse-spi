package org.concentus;

public class OpusException extends Exception {

    private String _message;
    private int _opus_error_code;

    public OpusException() {
        this("", 0);
    }

    public OpusException(String message) {
        this(message, 1);
    }

    public OpusException(String message, int opus_error_code) {
        _message = message + ": " + CodecHelpers.opus_strerror(opus_error_code);
        _opus_error_code = opus_error_code;
    }

    @Override
    public String getMessage() {
        return _message;
    }
}
