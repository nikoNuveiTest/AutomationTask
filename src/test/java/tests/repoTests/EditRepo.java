package tests.repoTests;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import utils.PropertyReader;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class EditRepo {

    public PropertyReader propertyReader = new PropertyReader("src/test/java/properties/config.properties");

    public String BASE_URL = propertyReader.getProperty("BASE_URL");
    public String REPO_NAME = propertyReader.getProperty("REPO_NAME");
    public String OWNER_USERNAME = propertyReader.getProperty("OWNER_USERNAME");
    public String AUTH_TOKEN = propertyReader.getProperty("AUTH_TOKEN");

    public String INVALID_REPO_NAME = "invalidRepo";
    public String INVALID_OWNER_USERNAME = "invalidOwner";
    public String INVALID_AUTH_TOKEN = "INVALID_AUTH_TOKEN";

    @BeforeClass
    public void setUp() {
        // Set the base URL
        RestAssured.baseURI = BASE_URL;
    }

    @DataProvider(name = "newDescriptions")
    public Object[][] getNewDescriptions() {
        return new Object[][]{
                {"Updated description only string"},
                {"12345"},
                {"!#$%^&*()_+|"}
        };
    }

    @Test(dataProvider = "newDescriptions")
    public void editRepo(String newDescription) {
        // Define the request body with the new description
        String requestBody = "{ \"description\": \"" + newDescription + "\" }";

        // Set up Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + AUTH_TOKEN);

        // Send the PATCH request to update the repository description
        given()
                .header(authHeader)
                .body(requestBody)
                .when()
                .patch("/repos/" + OWNER_USERNAME + "/" + REPO_NAME)
                .then()
                .statusCode(200)
                .body("description", equalTo(newDescription));
    }

    /*** Negative Tests ***/
    @Test
    public void editRepoWithoutAuthHeader() {
        // Define the new description for the repository
        String newDescription = "Update description " + System.currentTimeMillis();

        // Define the request body with the new description
        String requestBody = "{ \"description\": \"" + newDescription + "\" }";

        // Send the PATCH request to update the repository description
        given()
                .body(requestBody)
                .when()
                .patch("/repos/" + OWNER_USERNAME + "/" + REPO_NAME)
                .then()
                .statusCode(404)
                .body("message", equalTo("Not Found"))
                .body("documentation_url", containsString("https://docs.github.com/"));
    }

    @Test
    public void editRepoWithInvalidOwner() {
        // Define the new description for the repository
        String newDescription = "Update description " + System.currentTimeMillis();

        // Define the request body with the new description
        String requestBody = "{ \"description\": \"" + newDescription + "\" }";

        // Set up Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + AUTH_TOKEN);

        // Send the PATCH request to update the repository description
        given()
                .header(authHeader)
                .body(requestBody)
                .when()
                .patch("/repos/" + INVALID_OWNER_USERNAME + "/" + REPO_NAME)
                .then()
                .statusCode(404)
                .body("message", equalTo("Not Found"))
                .body("documentation_url", containsString("https://docs.github.com/"));
    }

    @Test
    public void editRepoWithInvalidName() {
        // Define the new description for the repository
        String newDescription = "Update description " + System.currentTimeMillis();

        // Define the request body with the new description
        String requestBody = "{ \"description\": \"" + newDescription + "\" }";

        // Set up Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + AUTH_TOKEN);

        // Send the PATCH request to update the repository description
        given()
                .header(authHeader)
                .body(requestBody)
                .when()
                .patch("/repos/" + OWNER_USERNAME + "/" + INVALID_REPO_NAME)
                .then()
                .statusCode(404)
                .body("message", equalTo("Not Found"))
                .body("documentation_url", containsString("https://docs.github.com/"));
    }

    @Test
    public void editRepoWithInvalidToken() {
        // Define the new description for the repository
        String newDescription = "Update description " + System.currentTimeMillis();

        // Define the request body with the new description
        String requestBody = "{ \"description\": \"" + newDescription + "\" }";

        // Set up invalid Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + INVALID_AUTH_TOKEN);

        // Send the PATCH request to update the repository description
        given()
                .header(authHeader)
                .body(requestBody)
                .when()
                .patch("/repos/" + OWNER_USERNAME + "/" + REPO_NAME)
                .then()
                .statusCode(401)
                .body("message", equalTo("Bad credentials"))
                .body("documentation_url", containsString("https://docs.github.com/"));
    }
}
