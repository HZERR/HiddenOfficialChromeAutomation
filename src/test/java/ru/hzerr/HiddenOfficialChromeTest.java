package ru.hzerr;

import org.apache.commons.io.file.PathUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.hzerr.parameters.HiddenChromeV144InstanceParameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HiddenOfficialChromeTest {

    private HiddenChromeInstance chromeInstance;
    private Path userDataLocation;

    @BeforeEach
    public void initialize() throws Exception {
        this.userDataLocation = Files.createTempDirectory("hidden-chrome");
        HiddenChromeV144InstanceParameters parameters = HiddenChromeV144InstanceParameters.create()
                .withChromeLocation("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe")
                .withRemoteDebuggingPort("6374")
                .withUserDataLocation(userDataLocation.toString())
                .withNoFirstRun(true);

        this.chromeInstance = new HiddenChromeInstance(parameters);
    }

    @Test
    public void getDevToolsVersionTest() {
        Assertions.assertDoesNotThrow(() -> chromeInstance.launch());
        String arguments = Assertions.assertDoesNotThrow(() -> chromeInstance.getDevToolsVersion());
        System.out.printf("DevTools: %s%n", arguments);
    }

    @AfterEach
    public void close() throws Exception {
        chromeInstance.close();
        removeUserDataDirectory();
    }

    private void removeUserDataDirectory() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            try {
                PathUtils.deleteDirectory(userDataLocation);
                System.out.println("✅ Chrome profile folder has been successfully removed!");
                return;
            } catch (IOException e) {
                Thread.sleep(200);
            }
        }

        System.err.println("❌ Failed to remove chrome profile folder at: " + userDataLocation.toAbsolutePath());
    }
}
