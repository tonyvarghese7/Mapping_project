package mapping;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class Automation {

    public static void main(String[] args) {
        run();
    }

    public static void run() {

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

// ===============================
// LOGIN BUTTON
// ===============================
            WebElement loginBtn = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[@type='submit']")
                    )
            );
            loginBtn.click();

            System.out.println("✅ Login clicked");

            // ===============================
            // STEP 2: WAIT FOR DASHBOARD
            // ===============================
            wait.until(ExpectedConditions.urlContains("/publisher/bid/list/open"));

            System.out.println("✅ Login successful");

            // ===============================
// STEP 2: CLICK ACTIVE
// ===============================
            WebElement activeBtn = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("cv-active-menu"))
            );
            activeBtn.click();

            System.out.println("✅ Clicked Active");

// ===============================
// STEP 3: WAIT FOR TABLE
// ===============================
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table")));

// ===============================
// STEP 4: FIND ROW WITH ORDER ID
// ===============================
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

        } catch (Exception e) {
            System.out.println("❌ Automation error: " + e.getMessage());
        }
    }
}