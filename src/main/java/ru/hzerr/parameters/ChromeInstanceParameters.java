package ru.hzerr.parameters;

import java.util.ArrayList;
import java.util.List;

public abstract class ChromeInstanceParameters<T extends ChromeInstanceParameters<T>> {

    protected String chromeInstanceLocation;
    protected List<String> arguments = new ArrayList<>();

    protected ChromeInstanceParameters() {
    }

    public String getChromeLocation() {
        return chromeInstanceLocation;
    }

    @SuppressWarnings("unchecked")
    public T withArgument(String argument) {
        arguments.add(argument);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withChromeLocation(String chromeInstanceLocation) {
        this.chromeInstanceLocation = chromeInstanceLocation;
        return (T) this;
    }

    public List<String> getCommands() {
        List<String> commands = new ArrayList<>();
        commands.add(chromeInstanceLocation);
        commands.addAll(arguments);
        return commands;
    }
}
