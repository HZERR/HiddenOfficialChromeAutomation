package ru.hzerr;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.hzerr.chrome.HiddenChromeInstance;
import ru.hzerr.generated.ExecutionContextCollection;
import ru.hzerr.model.ChromeDevToolsMetaData;
import ru.hzerr.model.GoogleReCaptchaV3ScoreMetaData;
import ru.hzerr.model.base.BaseChromeCommandResponse;
import ru.hzerr.model.base.BaseChromeEvent;
import ru.hzerr.parameters.HiddenChromeV144InstanceParameters;
import ru.hzerr.parameters.HiddenChromeV144InstanceParameters.HiddenChromeV144InstanceParametersBuilder;
import ru.hzerr.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

// https://fingerprint-scan.com
// https://pixelscan.net/fingerprint-check
// https://bot.sannysoft.com
// https://nopecha.com/demo/cloudflare
// https://www.browserscan.net
// https://bot-detector.rebrowser.net
// https://rucaptcha.com/demo/recaptcha-v3
public class HiddenOfficialChromeTest {

    private static final String CHROME_INSTANCE_LOCATION = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";                                                        // <<< CHANGE IT IF NEEDED
    private static final String CHROME_DEV_TOOLS_SPECIFICATION_LOCATION = "C:\\Innova\\devtools-specification.json";                                                            // <<< CHANGE IT IF NEEDED
    private static final String REMOTE_DEBUGGING_PORT = "7777";                                                                                                                 // <<< CHANGE IT IF NEEDED

    private HiddenChromeInstance chromeInstance;
    private Path userDataLocation;

    @BeforeEach
    public void initialize() {
        Assertions.assertDoesNotThrow(() -> this.userDataLocation = Files.createTempDirectory("hidden-chrome"), "❌ Failed to create chrome profile directory");
        HiddenChromeV144InstanceParameters parameters = Assertions.assertDoesNotThrow(() -> HiddenChromeV144InstanceParametersBuilder.create()
                .withChromeInstanceLocation(CHROME_INSTANCE_LOCATION)
                .withRemoteDebuggingPort(REMOTE_DEBUGGING_PORT)
                .withUserDataLocation(userDataLocation.toString())
                .withNoFirstRun(true)
                .build(), "❌ Failed to construct chrome instance parameters. Please add missing parameters or fix existing ones");

        this.chromeInstance = new HiddenChromeInstance(parameters);
        Assertions.assertDoesNotThrow(() -> chromeInstance.launch(), "❌ Chrome instance failed to launch. Please check chrome location '%s' and remoteDebuggingPort '%s'".formatted(CHROME_INSTANCE_LOCATION, REMOTE_DEBUGGING_PORT));
        System.out.println("✅ Chrome instance has been successfully created!");
    }

    @Test
    public void getDevToolsMetaDataTest() {
        Assertions.assertDoesNotThrow(() -> Thread.sleep(1000));
        ChromeDevToolsMetaData chromeDevToolsMetaData = Assertions.assertDoesNotThrow(() -> chromeInstance.getDevToolsMetaData(), "❌ Failed to retrieve Chrome DevTools metadata");
        System.out.printf("✅ Chrome DevTools data has been successfully retrieved! Please check: %s%n", chromeDevToolsMetaData);
    }

    @Test
    public void getDevToolsSpecificationTest() {
        Assertions.assertDoesNotThrow(() -> Thread.sleep(1000));
        String chromeDevToolsSpecification = Assertions.assertDoesNotThrow(() -> chromeInstance.getDevToolsSpecification(), "❌ Failed to retrieve Chrome DevTools specification");
        Assertions.assertDoesNotThrow(() -> FileUtils.writeStringToFile(new File(CHROME_DEV_TOOLS_SPECIFICATION_LOCATION), chromeDevToolsSpecification, StandardCharsets.UTF_8), "❌ Failed to save Chrome DevTools specification to file");
        System.out.printf("✅ Chrome DevTools specification has been successfully saved! Please look at '%s'%n", CHROME_DEV_TOOLS_SPECIFICATION_LOCATION);
    }

    @Test
    public void bypassCloudflareTest() {
        Assertions.assertDoesNotThrow(() -> Thread.sleep(1000));
        Assertions.assertDoesNotThrow(() -> chromeInstance.connect());
        chromeInstance.invokeMethod("""
                    {
                      "id": 0,
                      "method": "Network.setExtraHTTPHeaders",
                      "params": {
                        "headers": {
                          "Referer": "https://www.google.com/"
                        }
                      }
                    }
                """);
        waitForResponse(0);
        chromeInstance.invokeMethod("""
                    {
                      "id": 1,
                      "method": "Target.createTarget",
                      "params": {
                        "url": "https://nopecha.com/demo/cloudflare",
                        "newWindow": false
                      }
                    }
                """);
        BaseChromeCommandResponse openNewWindowResponse = null;
        while ((openNewWindowResponse = chromeInstance.getDevToolsResponse(1)) == null) {
            Assertions.assertDoesNotThrow(() -> Thread.sleep(500));
        }

        chromeInstance.invokeMethod("""
                    {
                      "id": 2,
                      "method": "Target.attachToTarget",
                      "params": {
                        "targetId": "%s",
                        "flatten": true
                      }
                    }
                """.formatted(openNewWindowResponse.getResult().get("targetId").asString()));

        BaseChromeCommandResponse attachToNewWindowResponse = null;
        while ((attachToNewWindowResponse = chromeInstance.getDevToolsResponse(2)) == null) {
            Assertions.assertDoesNotThrow(() -> Thread.sleep(500));
        }

        chromeInstance.invokeMethod("""
                    {
                      "id": 3,
                      "sessionId": "%s",
                      "method": "Page.enable"
                    }
                """.formatted(attachToNewWindowResponse.getResult().get("sessionId").asString()));
        waitForResponse(3);

        List<BaseChromeEvent> events = chromeInstance.getDevToolsEvents("Page.frameRequestedNavigation");
        while (events == null || events.stream().filter(chromeEvent -> chromeEvent.getPayload().has("url") && chromeEvent.getPayload().get("url").asString().startsWith("https://challenges.cloudflare.com/cdn-cgi/challenge-platform")).findFirst().isEmpty()) {
            Assertions.assertDoesNotThrow(() -> Thread.sleep(100));
            events = chromeInstance.getDevToolsEvents("Page.frameRequestedNavigation");
        }

        String frameId = events.stream().filter(chromeEvent -> chromeEvent.getPayload().has("url") && chromeEvent.getPayload().get("url").asString().startsWith("https://challenges.cloudflare.com/cdn-cgi/challenge-platform")).findFirst().get().getPayload().get("frameId").asString();
        List<BaseChromeEvent> events2 = chromeInstance.getDevToolsEvents("Page.frameDetached");
        while (events2 == null || events2.stream().filter(chromeEvent -> chromeEvent.getPayload().has("frameId") && chromeEvent.getPayload().get("frameId").asString().equals(frameId)).findFirst().isEmpty()) {
            Assertions.assertDoesNotThrow(() -> Thread.sleep(100));
            events2 = chromeInstance.getDevToolsEvents("Page.frameDetached");
        }

        chromeInstance.invokeMethod("""
                {
                  "id": 4,
                  "method": "Target.getTargets"
                }
                """);
        BaseChromeCommandResponse getTargetsResponse = null;
        while ((getTargetsResponse = chromeInstance.getDevToolsResponse(4)) == null) {
            Assertions.assertDoesNotThrow(() -> Thread.sleep(500));
        }
        ExecutionContextCollection executionContextCollection = JsonUtils.readValue(getTargetsResponse.getResult(), ExecutionContextCollection.class);
        System.out.printf("✅ Target.getTargets: %s%n", executionContextCollection);

        chromeInstance.invokeMethod("""
                    {
                      "id": 5,
                      "method": "Target.attachToTarget",
                      "params": {
                        "targetId": "%s",
                        "flatten": true
                      }
                    }
                """.formatted(Arrays.stream(executionContextCollection.getExecutionContexts()).filter(executionContext -> executionContext.getTitle() != null && executionContext.getTitle().startsWith("https://challenges.cloudflare.com/cdn-cgi/challenge-platform/h/b/turnstile")).findFirst().get().getTargetId()));

        BaseChromeCommandResponse attachToIFrameResponse = null;
        while ((attachToIFrameResponse = chromeInstance.getDevToolsResponse(5)) == null) {
            Assertions.assertDoesNotThrow(() -> Thread.sleep(500));
        }
        System.out.printf("✅ IFrame.attachToTarget: %s%n", attachToIFrameResponse);
        String cloudflareIFrameSessionId = attachToIFrameResponse.getResult().get("sessionId").asString();
        System.out.printf("✅ Подключение к IFrame успешно: %s%n", cloudflareIFrameSessionId);

        chromeInstance.invokeMethod("""
                    {
                      "id": 6,
                      "sessionId": "%s",
                      "method": "Page.enable"
                    }
                """.formatted(cloudflareIFrameSessionId));
        waitForResponse(6);
        Assertions.assertDoesNotThrow(() -> Thread.sleep(5000));

        String getBoundingClientRectScript = """
                (function(){
                var iframes=document.querySelectorAll('iframe');for(var i=0;i<iframes.length;i++){try{if(iframes[i].contentDocument&&iframes[i].contentDocument.querySelector('.cf-turnstile-checkbox, #cf-turnstile-checkbox, [data-sitekey], div[class*="turnstile"], div[class*="cf-challenge"]')){var r=iframes[i].getBoundingClientRect();return{x:r.left+r.width/2,y:r.top+r.height/2};}}catch(e){}}return{x:document.documentElement.clientWidth/2,y:document.documentElement.clientHeight/2};
                })()
                """.replace("\"", "\\\"").replace("\n", " ");
        chromeInstance.invokeMethod("""
                {
                  "id": 7,
                  "sessionId": "%s",
                  "method": "Runtime.evaluate",
                  "params": {
                    "expression": "%s",
                    "returnByValue": true
                  }
                }
                """.formatted(cloudflareIFrameSessionId, getBoundingClientRectScript));

        BaseChromeCommandResponse getBoundingClientRectResponse = null;
        while ((getBoundingClientRectResponse = chromeInstance.getDevToolsResponse(7)) == null) {
            Assertions.assertDoesNotThrow(() -> Thread.sleep(500));
        }
        System.out.printf("✅ Runtime.evaluate getBoundingClientRect: %s%n", getBoundingClientRectResponse);

        int x = Math.toIntExact(Math.round(getBoundingClientRectResponse.getResult().get("result").get("value").get("x").asDouble()));
        int y = Math.toIntExact(Math.round(getBoundingClientRectResponse.getResult().get("result").get("value").get("y").asDouble()));
        click(cloudflareIFrameSessionId, x, y);

        Assertions.assertDoesNotThrow(() -> Thread.sleep(10_000));
        chromeInstance.invokeMethod("""
                {
                  "id": 8,
                  "method": "Target.getTargets"
                }
                """);
        BaseChromeCommandResponse getTargetsResponse2 = null;
        while ((getTargetsResponse2 = chromeInstance.getDevToolsResponse(8)) == null) {
            Assertions.assertDoesNotThrow(() -> Thread.sleep(500));
        }
        ExecutionContextCollection executionContextCollection2 = JsonUtils.readValue(getTargetsResponse2.getResult(), ExecutionContextCollection.class);
        System.out.printf("✅ Target.getTargets: %s%n", executionContextCollection2);
        Assertions.assertTrue(Arrays.stream(executionContextCollection2.getExecutionContexts()).filter(executionContext -> executionContext.getUrl().startsWith("https://challenges.cloudflare.com/cdn-cgi/challenge-platform")).findFirst().isEmpty());
        System.out.printf("✅ Cloudflare has been successfully bypassed!!!!!%n");
    }

    @Test
    public void bypassRecaptchaV3Test() throws InterruptedException {
        Assertions.assertDoesNotThrow(() -> Thread.sleep(1000));
        Assertions.assertDoesNotThrow(() -> chromeInstance.connect());
        chromeInstance.invokeMethod("""
                    {
                      "id": 0,
                      "method": "Network.setExtraHTTPHeaders",
                      "params": {
                        "headers": {
                          "Referer": "https://www.google.com/"
                        }
                      }
                    }
                """);

        waitForResponse(0);
        chromeInstance.invokeMethod("""
                {
                  "id": 1,
                  "method": "Target.createTarget",
                  "params": {
                    "url": "https://rucaptcha.com/demo/recaptcha-v3",
                    "newWindow": false
                  }
                }
                """);
        BaseChromeCommandResponse openNewWindowResponse = null;
        while ((openNewWindowResponse = chromeInstance.getDevToolsResponse(1)) == null) {
            Assertions.assertDoesNotThrow(() -> Thread.sleep(500));
        }
        System.out.printf("✅ Target.createTarget: %s%n", openNewWindowResponse);

        chromeInstance.invokeMethod("""
                {
                  "id": 2,
                  "method": "Target.attachToTarget",
                  "params": {
                    "targetId": "%s",
                    "flatten": true
                  }
                }
                """.formatted(openNewWindowResponse.getResult().get("targetId").asString()));

        BaseChromeCommandResponse attachToNewWindowResponse = null;
        while ((attachToNewWindowResponse = chromeInstance.getDevToolsResponse(2)) == null) {
            Assertions.assertDoesNotThrow(() -> Thread.sleep(500));
        }
        System.out.printf("✅ Target.attachToTarget: %s%n", attachToNewWindowResponse);

        chromeInstance.invokeMethod("""
                {
                  "id": 777,
                  "sessionId": "%s",
                  "method": "Page.enable"
                }
                """.formatted(attachToNewWindowResponse.getResult().get("sessionId").asString()));
        waitForResponse(777);

        List<BaseChromeEvent> events = chromeInstance.getDevToolsEvents("Page.frameRequestedNavigation");
        while (events == null || events.stream().filter(chromeEvent -> chromeEvent.getPayload().has("url") && chromeEvent.getPayload().get("url").asString().startsWith("https://www.google.com/recaptcha/api2/anchor?")).findFirst().isEmpty()) {
            Thread.sleep(100);
            events = chromeInstance.getDevToolsEvents("Page.frameRequestedNavigation");
        }

        String frameId = events.stream().filter(chromeEvent -> chromeEvent.getPayload().has("url") && chromeEvent.getPayload().get("url").asString().startsWith("https://www.google.com/recaptcha/api2/anchor?")).findFirst().get().getPayload().get("frameId").asString();
        List<BaseChromeEvent> events2 = chromeInstance.getDevToolsEvents("Page.frameDetached");
        while (events2 == null || events2.stream().filter(chromeEvent -> chromeEvent.getPayload().has("frameId") && chromeEvent.getPayload().get("frameId").asString().equals(frameId)).findFirst().isEmpty()) {
            Thread.sleep(100);
            events2 = chromeInstance.getDevToolsEvents("Page.frameDetached");
        }

        String getBoundingClientRectScript = """
                (function(){
                const boundingClientRect = document.evaluate("//button[contains(text(),'Проверить')]", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.getBoundingClientRect();
                return {x: boundingClientRect.left + boundingClientRect.width/2, y: boundingClientRect.top + boundingClientRect.height/2};
                })()
                """.replace("\"", "\\\"").replace("\n", " ");
        chromeInstance.invokeMethod("""
                {
                  "id": 3,
                  "sessionId": "%s",
                  "method": "Runtime.evaluate",
                  "params": {
                    "expression": "%s",
                    "returnByValue": true
                  }
                }
                """.formatted(attachToNewWindowResponse.getResult().get("sessionId").asString(), getBoundingClientRectScript));
        BaseChromeCommandResponse getBoundingClientRectResponse = null;
        while ((getBoundingClientRectResponse = chromeInstance.getDevToolsResponse(3)) == null) {
            Assertions.assertDoesNotThrow(() -> Thread.sleep(500));
        }
        System.out.printf("✅ Runtime.evaluate getBoundingClientRect: %s%n", getBoundingClientRectResponse);

        int x = Math.toIntExact(Math.round(getBoundingClientRectResponse.getResult().get("result").get("value").get("x").asDouble()));
        int y = Math.toIntExact(Math.round(getBoundingClientRectResponse.getResult().get("result").get("value").get("y").asDouble()));
        click(attachToNewWindowResponse.getResult().get("sessionId").asString(), x, y);
        Assertions.assertDoesNotThrow(() -> Thread.sleep(6000));
        String getScoreScript = """
                (function(){
                return document.evaluate("//code[contains(text(),'{')]", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.textContent;
                })()
                """.replace("\"", "\\\"").replace("\n", " ");
        chromeInstance.invokeMethod("""
                {
                  "id": 10000,
                  "sessionId": "%s",
                  "method": "Runtime.evaluate",
                  "params": {
                    "expression": "%s",
                    "returnByValue": true
                  }
                }
                """.formatted(attachToNewWindowResponse.getResult().get("sessionId").asString(), getScoreScript));
        BaseChromeCommandResponse getScoreScriptResponse = null;
        while ((getScoreScriptResponse = chromeInstance.getDevToolsResponse(10000)) == null) {
            Assertions.assertDoesNotThrow(() -> Thread.sleep(500));
        }
        System.out.printf("✅ Runtime.evaluate getScore: %s%n", getScoreScriptResponse);

        GoogleReCaptchaV3ScoreMetaData googleReCaptchaV3ScoreMetaData = JsonUtils.readValue(getScoreScriptResponse.getResult().get("result").get("value").asString(), GoogleReCaptchaV3ScoreMetaData.class);
        Assertions.assertTrue(Double.parseDouble(googleReCaptchaV3ScoreMetaData.getScore()) >= 0.7);
        System.out.printf("✅ Google reCAPTCHA V3 has been successfully bypassed with score: %s!!!!!%n", googleReCaptchaV3ScoreMetaData.getScore());
    }

    private void click(String sessionId, int x, int y) {
        Random random = new Random(System.nanoTime() ^ Thread.currentThread().threadId());
        int id = 200 + random.nextInt(1000);

        int startX = x - 50 + random.nextInt(100);
        int startY = y - 40 + random.nextInt(80);

        int steps = 4 + random.nextInt(5);
        List<Integer> trajectoryX = new ArrayList<>();
        List<Integer> trajectoryY = new ArrayList<>();

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;

            double ease = 3 * t * t - 2 * t * t * t;
            double randomFactor = 1 + (random.nextDouble() - 0.5) * 0.1;

            int curX = (int) (startX + (x - startX) * ease * randomFactor);
            int curY = (int) (startY + (y - startY) * ease * randomFactor);

            if (t > 0.7) {
                curX += random.nextInt(3) - 1;
                curY += random.nextInt(3) - 1;
            }

            trajectoryX.add(curX);
            trajectoryY.add(curY);
        }

        for (int i = 0; i < trajectoryX.size(); i++) {
            chromeInstance.invokeMethod(String.format("""
                    {
                      "id": %d,
                      "sessionId": "%s",
                      "method": "Input.dispatchMouseEvent",
                      "params": {
                        "type": "mouseMoved",
                        "x": %d,
                        "y": %d,
                        "button": "none",
                        "clickCount": 0,
                        "pointerType": "mouse"
                      }
                    }
                    """, id++, sessionId, trajectoryX.get(i), trajectoryY.get(i)));

            waitForResponse(id - 1);

            int delay;
            if (i < trajectoryX.size() - 2) {
                delay = 50 + random.nextInt(40);
            } else if (i == trajectoryX.size() - 2) {
                delay = 100 + random.nextInt(80);
            } else {
                delay = 120 + random.nextInt(100);
            }

            try {
                Thread.sleep(delay);
            } catch (Exception e) {
            }
        }

        try {
            Thread.sleep(150 + random.nextInt(100));
        } catch (Exception e) {
        }

        int clickX = x + random.nextInt(3) - 1;
        int clickY = y + random.nextInt(3) - 1;

        chromeInstance.invokeMethod(String.format("""
                {
                  "id": %d,
                  "sessionId": "%s",
                  "method": "Input.dispatchMouseEvent",
                  "params": {
                    "type": "mousePressed",
                    "x": %d,
                    "y": %d,
                    "button": "left",
                    "clickCount": 1,
                    "pointerType": "mouse"
                  }
                }
                """, id++, sessionId, clickX, clickY));

        waitForResponse(id - 1);

        try {
            Thread.sleep(80 + random.nextInt(60));
        } catch (Exception e) {
        }

        chromeInstance.invokeMethod(String.format("""
                {
                  "id": %d,
                  "sessionId": "%s",
                  "method": "Input.dispatchMouseEvent",
                  "params": {
                    "type": "mouseReleased",
                    "x": %d,
                    "y": %d,
                    "button": "left",
                    "clickCount": 1,
                    "pointerType": "mouse"
                  }
                }
                """, id++, sessionId, clickX, clickY));

        waitForResponse(id - 1);

        for (int i = 0; i < 2; i++) {
            chromeInstance.invokeMethod(String.format("""
                            {
                              "id": %d,
                              "sessionId": "%s",
                              "method": "Input.dispatchMouseEvent",
                              "params": {
                                "type": "mouseMoved",
                                "x": %d,
                                "y": %d,
                                "button": "none",
                                "clickCount": 0,
                                "pointerType": "mouse"
                              }
                            }
                            """, id++, sessionId,
                    clickX + 10 + random.nextInt(20),
                    clickY + 10 + random.nextInt(15)));

            waitForResponse(id - 1);
            try {
                Thread.sleep(50 + random.nextInt(50));
            } catch (Exception e) {
            }
        }
    }

    private void waitForResponse(int expectedId) {
        int attempts = 0;
        while (chromeInstance.getDevToolsResponse(expectedId) == null && attempts < 30) {
            try {
                Thread.sleep(new Random().nextInt(12));
            } catch (InterruptedException ignored) {}
            attempts++;
        }
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
