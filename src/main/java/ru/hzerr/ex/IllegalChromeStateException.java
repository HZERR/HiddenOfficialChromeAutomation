package ru.hzerr.ex;

public class IllegalChromeStateException extends RuntimeException {

    public IllegalChromeStateException(String message) { super(message); }
    public IllegalChromeStateException(String message, Exception cause) { super(message, cause); }
}
