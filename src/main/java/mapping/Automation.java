package mapping;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileReader;
import java.io.FileWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Automation {

    public static void main(String[] args) {
        run(new ArrayList<>());
    }

    public static void run(List<String> unmappedColumns){

        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {

            // STEP 1: LOGIN

            driver.get("https://partner.anteriad.com/login");


            // USERNAME

            WebElement username = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("userName"))
            );
            username.sendKeys("accounts_aneesh");


            // PASSWORD

            WebElement password = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("password"))
            );
            password.sendKeys("Ai@20263!");

            // LOGIN BUTTON
            WebElement loginBtn = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[@type='submit']")
                    )
            );
            loginBtn.click();

            System.out.println("✅ Login clicked");


            // STEP 2: WAIT FOR DASHBOARD

            wait.until(ExpectedConditions.urlContains("/publisher/bid/list/open"));

            System.out.println("✅ Login successful");


            // STEP 2: CLICK ACTIVE

            WebElement activeBtn = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("cv-active-menu"))
            );
            activeBtn.click();

            System.out.println("✅ Clicked Active");


            // STEP 3: WAIT FOR TABLE

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table")));


            // STEP 4: FIND ROW WITH ORDER ID

            String targetOrderId = "68865";
            boolean found = false;

            while (!found) {

                List<WebElement> rows = driver.findElements(By.xpath("//table//tr"));

                for (WebElement row : rows) {

                    List<WebElement> cols = row.findElements(By.tagName("td"));

                    if (cols.size() > 1) {

                        String orderId = cols.get(0).getText().trim();

                        if (orderId.equals(targetOrderId)) {

                            System.out.println("✅ Found Order ID: " + orderId);

                            // CLICK 2nd COLUMN (NAME LINK)
                            WebElement link = cols.get(1).findElement(By.tagName("a"));

                    // Scroll properly (center, not top)
                            ((JavascriptExecutor) driver).executeScript(
                                    "arguments[0].scrollIntoView({block: 'center'});", link
                            );

                    // Wait until clickable
                            wait.until(ExpectedConditions.elementToBeClickable(link));

                            try {
                                link.click(); // normal click
                            } catch (Exception e) {
                                System.out.println(" Normal click failed, using JS click");

                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
                            }

                            found = true;
                            break;
                        }
                    }
                }

                // SCROLL DOWN IF NOT FOUND
                if (!found) {
                    System.out.println("🔽 Scrolling...");

                    ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 1000)");

                    Thread.sleep(1500); //lazy load
                }
            }

            System.out.println("✅ Navigated to Lead Criteria page");

            WebElement downloadBtn = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.id("ob-ptnr-lc-download"))
            );
            // STEP: CLICK DELIVERY CRITERIA

            WebElement deliveryTab = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.id("ib-por-top-tab-Delivery Criteria")
                    )
            );

            // Scroll into view
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", deliveryTab
            );

            // Wait clickable
            wait.until(ExpectedConditions.elementToBeClickable(deliveryTab));

            try {
                deliveryTab.click();
            } catch (Exception e) {
                System.out.println(" Normal click failed, using JS click");

                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].click();", deliveryTab
                );
            }

            System.out.println("✅ Clicked Delivery Criteria");

            wait.until(ExpectedConditions.urlContains("delivery-criteria"));


            // Mapping part

            Map<String, String> allowedValueMap = new HashMap<>();

            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.xpath("//table//tr[td]")));

            List<WebElement> rows = driver.findElements(By.xpath("//table//tr"));

            for (String field : unmappedColumns) {

                System.out.println(" Checking: " + field);

                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.xpath("//tr[contains(@class,'ng-star-inserted')]")
                ));

                List<WebElement> rows1 = driver.findElements(
                        By.xpath("//tr[contains(@class,'ng-star-inserted')]")
                );

                System.out.println("Total rows found: " + rows1.size());

                for (WebElement row : rows1) {

                    List<WebElement> cols = row.findElements(By.tagName("td"));

                    if (cols.size() < 9) continue;

                    String customerHeader = cols.get(0).getText().trim();

                    if (customerHeader.isEmpty() || customerHeader.equals("-")) {
                        continue;
                    }

                    System.out.println("UI: [" + customerHeader + "] vs FIELD: [" + field + "]");

                    // Normalize comparison
                    if (Utils.normalize(customerHeader).equals(Utils.normalize(field))) {

                        System.out.println("✅ Found match in UI: " + field);

                        WebElement allowedCell = cols.get(8);

                        try {

                            // STEP 1: CLICK "Allowed Values"

                            WebElement allowedBtn = allowedCell.findElement(
                                    By.xpath(".//span[contains(text(),'Allowed Values')]")
                            );

                            ((JavascriptExecutor) driver).executeScript(
                                    "arguments[0].scrollIntoView({block:'center'});", allowedBtn
                            );

                            try {
                                allowedBtn.click();
                            } catch (Exception e) {
                                ((JavascriptExecutor) driver).executeScript(
                                        "arguments[0].click();", allowedBtn
                                );
                            }


                            // STEP 2: WAIT FOR VALUES

                            Thread.sleep(1000);


                            // STEP 3: GET FIRST VALUE

                            WebElement firstValue = allowedCell.findElement(
                                    By.xpath(".//div[contains(@class,'allow-values-items')]//div[contains(@class,'item')][1]//span")
                            );

                            String value = firstValue.getText().trim();

                            // CLEAN VALUE (remove icon text)
                            value = value.replaceAll("^[^a-zA-Z0-9]+", "").trim();

                            System.out.println(" Selected value: " + value);

                            allowedValueMap.put(Utils.normalize(field), value);

                        } catch (Exception e) {
                            System.out.println("⚠️ No allowed values for: " + field);
                        }

                        break;
                    }
                }
            }

            // STEP: WRITE VALUES TO CSV

            List<String[]> data = new ArrayList<>();

            try (CSVReader reader = new CSVReader(new FileReader("output/final.csv"))) {
                data = reader.readAll();

                System.out.println("===== ALLOWED VALUE MAP =====");
                for (Map.Entry<String, String> entry : allowedValueMap.entrySet()) {
                    System.out.println(entry.getKey() + " → " + entry.getValue());
                }
            }

            String[] headers = data.get(0);

            for (int col = 0; col < headers.length; col++) {

                String header = headers[col];

                if (allowedValueMap.containsKey(Utils.normalize(header))) {

                    String value = allowedValueMap.get(Utils.normalize(header));

                    System.out.println(" Writing " + value + " into column: " + header);

                    for (int row = 1; row < data.size(); row++) {
                        data.get(row)[col] = value;
                    }
                }
            }

            try (CSVWriter writer = new CSVWriter(new FileWriter("output/final.csv"))) {
                writer.writeAll(data);
            }

            System.out.println("✅ CSV Updated Successfully!");

        } catch (Exception e) {
            System.out.println("❌ Automation error: " + e.getMessage());
        }
    }
}