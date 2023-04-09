package tests.repoTests;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.PropertyReader;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class CreateRepo {

    public PropertyReader propertyReader = new PropertyReader("src/test/java/properties/config.properties");

    public String BASE_URL = propertyReader.getProperty("BASE_URL");
    public String OWNER_USERNAME = propertyReader.getProperty("OWNER_USERNAME");
    public String AUTH_TOKEN = propertyReader.getProperty("AUTH_TOKEN");

    public String INVALID_AUTH_TOKEN = "INVALID_AUTH_TOKEN";

    public String createdRepoName;

    @BeforeClass
    public void setUp() {
        // Set the base URL
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    public void createRepo() {
        // Define the request body with repository name and description
        String requestBody = "{ \"name\": \"my-repo\", \"description\": \"This is a test repository\" }";

        // Set up Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + AUTH_TOKEN);

        // Send the POST request to create a repository
        Response response = given()
                .header(authHeader)
                .body(requestBody)
                .when()
                .post("/user/repos")
                .then()
                .statusCode(201)
                .body("name", equalTo("my-repo"))
                .body("description", equalTo("This is a test repository"))
                .extract().response();

        // Extract the created repository name for the teardown
        createdRepoName = response.jsonPath().getString("name");
    }

    @Test
    public void createRepoWithoutDescription() {
        // Define the request body with repository name and description
        String requestBody = "{ \"name\": \"my-repo2\"}";

        // Set up Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + AUTH_TOKEN);

        // Send the POST request to create a repository
        Response response = given()
                .header(authHeader)
                .body(requestBody)
                .when()
                .post("/user/repos")
                .then()
                .statusCode(201)
                .body("name", equalTo("my-repo2"))
                .extract().response();

        // Extract the created repository name for the teardown
        createdRepoName = response.jsonPath().getString("name");
    }

    /*** Negative Tests ***/
    @Test
    public void createRepoWithoutAuthHeader() {
        // Define the request body with repository name and description
        String requestBody = "{ \"name\": \"my-repo\", \"description\": \"This is a test repository\" }";

        // Send the POST request to create a repository
        Response response = given()
                .body(requestBody)
                .when()
                .post("/user/repos")
                .then()
                .statusCode(401)
                .body("message", equalTo("Requires authentication"))
                .body("documentation_url", containsString("https://docs.github.com/"))
                .extract().response();

        // Extract the created repository name for the teardown
        createdRepoName = response.jsonPath().getString("name");
    }

    @Test
    public void createRepoWithInvalidToken() {
        // Define the request body with repository name and description
        String requestBody = "{ \"name\": \"my-repo\", \"description\": \"This is a test repository\" }";

        // Set up invalid Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + INVALID_AUTH_TOKEN);

        // Send the POST request to create a repository
        Response response = given()
                .body(requestBody)
                .when()
                .post("/user/repos")
                .then()
                .statusCode(401)
                .body("message", equalTo("Requires authentication"))
                .body("documentation_url", containsString("https://docs.github.com/"))
                .extract().response();

        // Extract the created repository name for the teardown
        createdRepoName = response.jsonPath().getString("name");
    }

    @Test
    public void createRepoWithoutName() {
        // Define the request body without repository name
        String requestBody = "{ \"description\": \"This is a test repository\" }";

        // Set up Authorization Bearer header
        Header authHeader = new Header("Authorization", "Bearer " + AUTH_TOKEN);

        // Send the POST request to create a repository
        Response response = given()
                .header(authHeader)
                .body(requestBody)
                .when()
                .post("/user/repos")
                .then()
                .statusCode(422)
                .body("message", equalTo("Repository creation failed."))
                .body("errors[0].code", equalTo("missing_field"))
                .body("errors[0].field", equalTo("name"))
                .extract().response();

        // Extract the created repository name for the teardown
        createdRepoName = response.jsonPath().getString("name");
    }

    @AfterMethod
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
