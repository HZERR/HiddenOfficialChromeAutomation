package ru.hzerr;

import ru.hzerr.ex.ChromeLaunchException;
import ru.hzerr.ex.ChromeShutdownException;

public interface IChrome extends AutoCloseable {

    void launch() throws ChromeLaunchException;
    void close() throws ChromeShutdownException;
}
