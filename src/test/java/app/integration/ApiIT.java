package app.integration;


import app.config.ApplicationConfig;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiTest {

    private Javalin app;

    @BeforeAll
    void start() {
        // Start på random port (0) og læs den faktiske port bagefter
        app = ApplicationConfig.create();
        app.start(0);
        int port = app.port();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterAll
    void stop() {
        if (app != null) app.stop();
    }

    @Test
    void health_public() {
        given().get("/health")
                .then().statusCode(200)
                .body("status", equalTo("ok"));
    }

    @Test
    void full_flow_user_and_admin() {
        // Register user (må returnere 201 eller 409 hvis den allerede findes)
        given().contentType("application/json")
                .body("""
                      {"email":"apitestuser@fitness.dk","password":"test123"}
                      """)
                .post("/auth/register")
                .then().statusCode(anyOf(is(201), is(409)));

        // Login user
        String jwtUser =
                given().contentType("application/json")
                        .body("""
                              {"email":"apitestuser@fitness.dk","password":"test123"}
                              """)
                        .post("/auth/login")
                        .then().statusCode(200)
                        .body("token", notNullValue())
                        .extract().path("token");

        // Register admin (samme logik)
        given().contentType("application/json")
                .body("""
                      {"email":"apitestadmin@fitness.dk","password":"admin123"}
                      """)
                .post("/auth/register")
                .then().statusCode(anyOf(is(201), is(409)));

        // Login admin
        String jwtAdmin =
                given().contentType("application/json")
                        .body("""
                              {"email":"apitestadmin@fitness.dk","password":"admin123"}
                              """)
                        .post("/auth/login")
                        .then().statusCode(200)
                        .body("token", notNullValue())
                        .extract().path("token");

        // Promote admin (kræver at DB er tilgængelig; i “rigtige” e2e vil man gøre dette via direkte DAO/JPA eller en admin-endpoint.
        // Her springer vi over at forfremme og antager at rollen var sat manuelt i DB i forvejen til testen.
        // Alternativt kunne man mocke requireAdmin – men det er uden for scope for en simpel end-to-end test.)

        // /api/auth/me (user)
        given().header("Authorization", "Bearer " + jwtUser)
                .get("/api/auth/me")
                .then().statusCode(200)
                .body("role", anyOf(equalTo("USER"), equalTo("ADMIN")));

        // Exercises list (user OK)
        given().header("Authorization", "Bearer " + jwtUser)
                .get("/api/exercises")
                .then().statusCode(200);

        // Exercises create (skal være 403 for USER)
        given().header("Authorization", "Bearer " + jwtUser)
                .contentType("application/json")
                .body("""
                      {"name":"Push-up","muscleGroup":"Chest"}
                      """)
                .post("/api/exercises")
                .then().statusCode(403);

        // Hvis du vil teste ADMIN create, kræver det at apitestadmin reelt har role=ADMIN i DB.
        // Når det er tilfældet, kan du fjerne kommentaren herunder:

        // int exId =
        //         given().header("Authorization", "Bearer " + jwtAdmin)
        //                 .contentType("application/json")
        //                 .body("{\"name\":\"Push-up\",\"muscleGroup\":\"Chest\"}")
        //                 .post("/api/exercises")
        //                 .then().statusCode(201)
        //                 .extract().path("id");

        // Workouts create (user)
        int wId =
                given().header("Authorization", "Bearer " + jwtUser)
                        .contentType("application/json")
                        .body("""
                              {"date":"2025-10-21","notes":"Leg day"}
                              """)
                        .post("/api/workouts")
                        .then().statusCode(201)
                        .extract().path("id");

        // Workouts get
        given().header("Authorization", "Bearer " + jwtUser)
                .get("/api/workouts/" + wId)
                .then().statusCode(200)
                .body("notes", equalTo("Leg day"));

        // Negative: invalid token -> 401
        given().header("Authorization", "Bearer not-a-valid-jwt")
                .get("/api/workouts")
                .then().statusCode(401);
    }
}
