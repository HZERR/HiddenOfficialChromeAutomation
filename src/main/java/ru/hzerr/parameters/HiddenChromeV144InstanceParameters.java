package ru.hzerr.parameters;

import ru.hzerr.ex.ChromeMissingParametersException;
import ru.hzerr.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class HiddenChromeV144InstanceParameters extends ChromeInstanceParameters {

    private String userDataLocation;
    private String remoteDebuggingPort;
    private boolean noFirstRun;

    private HiddenChromeV144InstanceParameters(String chromeInstanceLocation) {
        super(chromeInstanceLocation);
    }

    public String getUserDataLocation() {
        return userDataLocation;
    }

    private HiddenChromeV144InstanceParameters withUserDataLocation(String userDataLocation) {
        this.userDataLocation = userDataLocation;
        return this;
    }

    public String getRemoteDebuggingPort() {
        return remoteDebuggingPort;
    }

    private HiddenChromeV144InstanceParameters withRemoteDebuggingPort(String remoteDebuggingPort) {
        this.remoteDebuggingPort = remoteDebuggingPort;
        return this;
    }

    public boolean isNoFirstRun() {
        return noFirstRun;
    }

    private HiddenChromeV144InstanceParameters withNoFirstRun(boolean noFirstRun) {
        this.noFirstRun = noFirstRun;
        return this;
    }

    @Override
    protected List<String> getArguments() {
        List<String> args = new ArrayList<>();
        args.add("--remote-debugging-port=" + remoteDebuggingPort);
        args.add("--user-data-dir=\"%s\"".formatted(userDataLocation));

        if (noFirstRun)
            args.add("--no-first-run");

        return args;
    }

    public static class HiddenChromeV144InstanceParametersBuilder implements IChromeInstanceParametersBuilder<HiddenChromeV144InstanceParameters> {

        private String chromeInstanceLocation;
        private String userDataLocation;
        private String remoteDebuggingPort;
        private boolean noFirstRun;

        private HiddenChromeV144InstanceParametersBuilder() {
        }

        public HiddenChromeV144InstanceParametersBuilder withChromeInstanceLocation(String chromeInstanceLocation) {
            this.chromeInstanceLocation = chromeInstanceLocation;
            return this;
        }

        public HiddenChromeV144InstanceParametersBuilder withUserDataLocation(String userDataLocation) {
            this.userDataLocation = userDataLocation;
            return this;
        }

        public HiddenChromeV144InstanceParametersBuilder withRemoteDebuggingPort(String remoteDebuggingPort) {
            this.remoteDebuggingPort = remoteDebuggingPort;
            return this;
        }

        public HiddenChromeV144InstanceParametersBuilder withNoFirstRun(boolean noFirstRun) {
            this.noFirstRun = noFirstRun;
            return this;
        }

        @Override
        public HiddenChromeV144InstanceParameters build() {
            if (StringUtils.isEmpty(chromeInstanceLocation)) throw new ChromeMissingParametersException("Chrome parameter 'chromeInstanceLocation' isn't provided");
            if (StringUtils.isEmpty(remoteDebuggingPort)) throw new ChromeMissingParametersException("Chrome parameter 'remoteDebuggingPort' isn't provided");
            if (StringUtils.isEmpty(userDataLocation)) throw new ChromeMissingParametersException("Chrome parameter 'userDataLocation' isn't provided");

            return new HiddenChromeV144InstanceParameters(chromeInstanceLocation)
                    .withRemoteDebuggingPort(remoteDebuggingPort)
                    .withUserDataLocation(userDataLocation)
                    .withNoFirstRun(noFirstRun);
        }

        public static HiddenChromeV144InstanceParametersBuilder create() {
            return new HiddenChromeV144InstanceParametersBuilder();
        }
    }
}
