package tests.repoTests;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.PropertyReader;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetRepo {

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

    @Test
    public void getRepos() {
        // Set up Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + AUTH_TOKEN);

        // Send a GET request to get repositories
        given()
                .header(authHeader)
                .when()
                .get("/user/repos")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0)); // Assert that response contains repositories
    }

    @Test
    public void getSpecificRepo() {
        // Set up Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + AUTH_TOKEN);

        // Send request to get the specific repository by name
        given()
                .header(authHeader)
                .when()
                .get("/repos/{owner}/{repo}", OWNER_USERNAME, REPO_NAME)
                .then()
                .statusCode(200)
                .body("name", equalTo(REPO_NAME))
                .body("owner.login", equalTo(OWNER_USERNAME))
                .body("id", notNullValue())
                .body("node_id", notNullValue())
                .body("full_name", equalTo(OWNER_USERNAME + "/" + REPO_NAME))
                .body("private", equalTo(false));
    }

    /*** Negative Tests ***/
    @Test
    public void getReposWithoutAuthHeader() {
        // Send a GET request to get repositories without Authorization header
        given()
                .when()
                .get("/user/repos")
                .then()
                .statusCode(401)
                .body("message", equalTo("Requires authentication"))
                .body("documentation_url", containsString("https://docs.github.com/"));
    }

    @Test
    public void getReposWithInvalidToken() {
        // Set up invalid Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + INVALID_AUTH_TOKEN);

        // Send a GET request to get repositories with invalid Authorization header
        given()
                .header(authHeader)
                .when()
                .get("/user/repos")
                .then()
                .statusCode(401)
                .body("message", equalTo("Bad credentials"))
                .body("documentation_url", containsString("https://docs.github.com/"));
    }

    @Test
    public void getSpecificRepoWithInvalidToken() {
        // Set up invalid Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + INVALID_AUTH_TOKEN);

        // Send request to get the specific repository with invalid Authorization header
        given()
                .header(authHeader)
                .when()
                .get("/repos/{owner}/{repo}", OWNER_USERNAME, REPO_NAME)
                .then()
                .statusCode(401)
                .body("message", equalTo("Bad credentials"))
                .body("documentation_url", containsString("https://docs.github.com/"));
    }

    @Test
    public void getSpecificRepoWithInvalidName() {
        // Set up Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + AUTH_TOKEN);

        // Send request to get the specific repository with invalid name
        given()
                .header(authHeader)
                .when()
                .get("/repos/{owner}/{repo}", OWNER_USERNAME, INVALID_REPO_NAME)
                .then()
                .statusCode(404)
                .body("message", equalTo("Not Found"))
                .body("documentation_url", containsString("https://docs.github.com/"));
    }

    @Test
    public void getSpecificRepoWithInvalidOwner() {
        // Set up Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + AUTH_TOKEN);

        // Send request to get the specific repository with invalid owner
        given()
                .header(authHeader)
                .when()
                .get("/repos/{owner}/{repo}", INVALID_OWNER_USERNAME, REPO_NAME)
                .then()
                .statusCode(404)
                .body("message", equalTo("Not Found"))
                .body("documentation_url", containsString("https://docs.github.com/"));
    }
}
