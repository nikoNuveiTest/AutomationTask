package tests.repoTests;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.PropertyReader;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class DeleteRepo {

    public PropertyReader propertyReader = new PropertyReader("src/test/java/properties/config.properties");

    public String BASE_URL = propertyReader.getProperty("BASE_URL");
    public String REPO_NAME = propertyReader.getProperty("REPO_NAME");
    public String OWNER_USERNAME = propertyReader.getProperty("OWNER_USERNAME");
    public String AUTH_TOKEN = propertyReader.getProperty("AUTH_TOKEN");

    public String INVALID_REPO_NAME = "invalidRepo";
    public String INVALID_OWNER_USERNAME = "invalidOwner";
    public String INVALID_AUTH_TOKEN = "INVALID_AUTH_TOKEN";

    public String createdRepoName;
    private String repoName;

    @BeforeClass
    public void setUp() {
        // Set the base URL
        RestAssured.baseURI = BASE_URL;

        // Generate a unique repository name
        repoName = "testRepo_" + System.currentTimeMillis();

        // Define the request body
        String requestBody = "{ \"name\": \"" + repoName + "\", \"description\": \"This is a test repository for deletion\" }";

        // Set up Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + AUTH_TOKEN);

        // Create a repository using POST request
        Response response = given()
                .header(authHeader)
                .body(requestBody)
                .when()
                .post("/user/repos")
                .then()
                .statusCode(201)
                .body("name", equalTo(repoName))
                .body("description", equalTo("This is a test repository for deletion"))
                .extract().response();

        createdRepoName = response.jsonPath().getString("name");
    }

    @Test
    public void deleteRepo() {
        // Set up Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + AUTH_TOKEN);

        // Delete the created repository using DELETE request
        given()
                .header(authHeader)
                .when()
                .delete("/repos/{owner}/{repo}", OWNER_USERNAME, repoName)
                .then()
                .statusCode(204);

        createdRepoName = null;
    }

    /*** Negative Tests ***/
    @Test
    public void deleteRepoWithoutAuthHeader() {
        // Delete the created repository without Authorization header
        given()
                .when()
                .delete("/repos/{owner}/{repo}", OWNER_USERNAME, REPO_NAME)
                .then()
                .statusCode(403)
                .body("message", equalTo("Must have admin rights to Repository."))
                .body("documentation_url", containsString("https://docs.github.com/"));
    }

    @Test
    public void deleteRepoWithInvalidToken() {
        // Set up invalid Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + INVALID_AUTH_TOKEN);

        // Delete the created repository using DELETE request
        given()
                .header(authHeader)
                .when()
                .delete("/repos/{owner}/{repo}", OWNER_USERNAME, REPO_NAME)
                .then()
                .statusCode(401) // Assert the response status code
                .body("message", equalTo("Bad credentials"))
                .body("documentation_url", containsString("https://docs.github.com/"));
    }

    @Test
    public void deleteRepoWithInvalidOwner() {
        // Set up Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + AUTH_TOKEN);

        // Delete the created repository using DELETE request with invalid owner
        given()
                .header(authHeader)
                .when()
                .delete("/repos/{owner}/{repo}", INVALID_OWNER_USERNAME, REPO_NAME)
                .then()
                .statusCode(404)
                .body("message", equalTo("Not Found"))
                .body("documentation_url", containsString("https://docs.github.com/"));
    }

    @Test
    public void deleteRepoWithInvalidName() {
        // Set up Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + AUTH_TOKEN);

        // Delete the created repository using DELETE request with invalid repo name
        given()
                .header(authHeader)
                .when()
                .delete("/repos/{owner}/{repo}", OWNER_USERNAME, INVALID_REPO_NAME)
                .then()
                .statusCode(404)
                .body("message", equalTo("Not Found"))
                .body("documentation_url", containsString("https://docs.github.com/"));
    }

    @AfterClass
    public void tearDown() {
        // Set up Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + AUTH_TOKEN);

        // Delete the repository that was created during the test
        if (createdRepoName != null) {
            given()
                    .header(authHeader)
                    .when()
                    .delete("/repos/{owner}/{repo}", OWNER_USERNAME, createdRepoName)
                    .then()
                    .statusCode(204);
        }
    }
}
