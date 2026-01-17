package ru.hzerr;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.hzerr.chrome.HiddenChromeInstance;
import ru.hzerr.model.ChromeDevToolsMetaData;
import ru.hzerr.parameters.HiddenChromeV144InstanceParameters;
import ru.hzerr.parameters.HiddenChromeV144InstanceParameters.HiddenChromeV144InstanceParametersBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class HiddenOfficialChromeTest {

    private static final String CHROME_INSTANCE_LOCATION = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";// <<< CHANGE IT IF NEEDED
    private static final String CHROME_DEV_TOOLS_SPECIFICATION_LOCATION = "C:\\Innova\\devtools-specification.json";    // <<< CHANGE IT IF NEEDED
    private static final String REMOTE_DEBUGGING_PORT = "7777";                                                         // <<< CHANGE IT IF NEEDED

    private HiddenChromeInstance chromeInstance;
    private Path userDataLocation;

    @BeforeEach
    public void initialize() throws Exception {
        Assertions.assertDoesNotThrow(() -> this.userDataLocation = Files.createTempDirectory("hidden-chrome"), "❌ Failed to create chrome profile directory");
        HiddenChromeV144InstanceParameters parameters = Assertions.assertDoesNotThrow(() -> HiddenChromeV144InstanceParametersBuilder.create()
                .withChromeInstanceLocation(CHROME_INSTANCE_LOCATION)
                .withRemoteDebuggingPort(REMOTE_DEBUGGING_PORT)
                .withUserDataLocation(userDataLocation.toString())
                .withNoFirstRun(true)
                .build(), "❌ Failed to construct chrome instance parameters. Please add missing parameters or fix existing ones");

        this.chromeInstance = new HiddenChromeInstance(parameters);
        Assertions.assertDoesNotThrow(() -> chromeInstance.launch(), "❌ Chrome instance failed to launch. Please check chrome location '%s' and remoteDebuggingPort '%s'".formatted(CHROME_INSTANCE_LOCATION, REMOTE_DEBUGGING_PORT));
        Thread.sleep(1000);
        System.out.println("✅ Chrome instance has been successfully created!");
    }

    @Test
    public void getDevToolsMetaDataTest() {
        ChromeDevToolsMetaData chromeDevToolsMetaData = Assertions.assertDoesNotThrow(() -> chromeInstance.getDevToolsMetaData(), "❌ Failed to retrieve Chrome DevTools metadata");
        System.out.printf("✅ Chrome DevTools data has been successfully retrieved! Please check: %s%n", chromeDevToolsMetaData);
    }

    @Test
    public void getDevToolsSpecificationTest() {
        String chromeDevToolsSpecification = Assertions.assertDoesNotThrow(() -> chromeInstance.getDevToolsSpecification(), "❌ Failed to retrieve Chrome DevTools specification");
        Assertions.assertDoesNotThrow(() -> FileUtils.writeStringToFile(new File(CHROME_DEV_TOOLS_SPECIFICATION_LOCATION), chromeDevToolsSpecification, StandardCharsets.UTF_8), "❌ Failed to save Chrome DevTools specification to file");
        System.out.printf("✅ Chrome DevTools specification has been successfully saved! Please look at '%s'%n", CHROME_DEV_TOOLS_SPECIFICATION_LOCATION);
    }

    @AfterEach
    public void close() throws Exception {
        chromeInstance.close();
        removeUserDataDirectory();
        System.out.println("✅ Chrome instance has been successfully closed!");
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
