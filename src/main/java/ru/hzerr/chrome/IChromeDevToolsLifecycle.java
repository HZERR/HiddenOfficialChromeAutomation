package ru.hzerr.chrome;

import ru.hzerr.ex.ChromeConnectionException;
import ru.hzerr.ex.ChromeLaunchException;
import ru.hzerr.ex.ChromeShutdownException;

public interface IChromeDevToolsLifecycle extends AutoCloseable {

    void launch() throws ChromeLaunchException;
    void connect() throws ChromeConnectionException;
    void close() throws ChromeShutdownException;
}
