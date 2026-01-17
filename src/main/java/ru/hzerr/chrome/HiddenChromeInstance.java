package ru.hzerr.chrome;

import ru.hzerr.ex.ChromeInstanceException;
import ru.hzerr.model.ChromeDevToolsMetaData;
import ru.hzerr.parameters.HiddenChromeV144InstanceParameters;
import ru.hzerr.utils.JsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/*

https://chromium.googlesource.com/chromium/src/+/HEAD/chrome/common/chrome_switches.cc
chrome/common/chrome_switches.cc (основные аргументы)
content/public/common/content_switches.cc (общие для движка)
services/network/public/cpp/network_switches.cc (сетевые)
https://peter.sh/experiments/chromium-command-line-switches/#stable-release-mode

 */
public class HiddenChromeInstance extends ChromeInstance {

    private final HiddenChromeV144InstanceParameters chromeInstanceParameters;

    public HiddenChromeInstance(HiddenChromeV144InstanceParameters chromeInstanceParameters) {
        this.chromeInstanceParameters = chromeInstanceParameters;
    }

    public ChromeDevToolsMetaData getDevToolsMetaData() throws ChromeInstanceException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest versionRequest = HttpRequest.newBuilder(new URI(CHROME_DEVTOOLS_VERSION.formatted(chromeInstanceParameters.getRemoteDebuggingPort())))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(versionRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return JsonUtils.readValue(response.body(), ChromeDevToolsMetaData.class);
            }

            throw new ChromeInstanceException("""
                    DevTools responded with a non-200 status code: %d.
                    This usually means remote debugging is not enabled or the port is blocked.
                    """.formatted(response.statusCode()));
        } catch (InterruptedException | URISyntaxException | IOException e) {
            throw new ChromeInstanceException("""
                    Failed to query DevTools at the specified URI: "%s".
                    Ensure Chrome is running with --remote-debugging-port and the port is correct.
                    Error: %s
                    """.formatted(CHROME_DEVTOOLS_VERSION.formatted(chromeInstanceParameters.getRemoteDebuggingPort()), e.getMessage()), e);
        }
    }

    public String getDevToolsSpecification() throws ChromeInstanceException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest versionRequest = HttpRequest.newBuilder(new URI(CHROME_DEVTOOLS_PROTOCOL_SPECIFICATION.formatted(chromeInstanceParameters.getRemoteDebuggingPort())))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(versionRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            }

            throw new ChromeInstanceException("""
                    DevTools responded with a non-200 status code: %d.
                    This usually means remote debugging is not enabled or the port is blocked.
                    """.formatted(response.statusCode()));
        } catch (InterruptedException | URISyntaxException | IOException e) {
            throw new ChromeInstanceException("""
                    Failed to query DevTools at the specified URI: "%s".
                    Ensure Chrome is running with --remote-debugging-port and the port is correct.
                    Error: %s
                    """.formatted(CHROME_DEVTOOLS_VERSION.formatted(chromeInstanceParameters.getRemoteDebuggingPort()), e.getMessage()), e);
        }
    }

    @Override
    protected String getConnectionEndpoint() throws ChromeInstanceException {
        return getDevToolsMetaData().getChromeDevToolsWebSocketUrl();
    }

    @Override
    protected List<String> getCommands() {
        return chromeInstanceParameters.getCommands();
    }
}
