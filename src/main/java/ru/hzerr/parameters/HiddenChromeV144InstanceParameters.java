package ru.hzerr.parameters;

import java.util.List;

public class HiddenChromeV144InstanceParameters extends ChromeInstanceParameters<HiddenChromeV144InstanceParameters> {

    private String userDataLocation;
    private String remoteDebuggingPort;
    private boolean noFirstRun;

    public HiddenChromeV144InstanceParameters() {
    }

    public String getUserDataLocation() {
        return userDataLocation;
    }

    public HiddenChromeV144InstanceParameters withUserDataLocation(String userDataLocation) {
        this.userDataLocation = userDataLocation;
        return this;
    }

    public String getRemoteDebuggingPort() {
        return remoteDebuggingPort;
    }

    public HiddenChromeV144InstanceParameters withRemoteDebuggingPort(String remoteDebuggingPort) {
        this.remoteDebuggingPort = remoteDebuggingPort;
        return this;
    }

    public HiddenChromeV144InstanceParameters withNoFirstRun(boolean noFirstRun) {
        this.noFirstRun = noFirstRun;
        return this;
    }

    @Override
    public List<String> getCommands() {
        List<String> commands = super.getCommands();
        if (remoteDebuggingPort != null)
            commands.add("--remote-debugging-port=" + remoteDebuggingPort);
        if (userDataLocation != null)
            commands.add("--user-data-dir=\"%s\"".formatted(userDataLocation));
        if (noFirstRun)
            commands.add("--no-first-run");
        return commands;
    }

    public static HiddenChromeV144InstanceParameters create() {
        return new HiddenChromeV144InstanceParameters();
    }
}
