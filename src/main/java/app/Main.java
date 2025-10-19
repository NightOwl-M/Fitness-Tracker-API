package app;

import app.exceptions.ErrorHandlers;
import app.routes.Routes;
import io.javalin.Javalin;

public class Main {
    public static void main(String[] args) {
        Javalin app = Javalin.create(cfg -> {
            cfg.http.defaultContentType = "application/json";
        }).start(7070);

        ErrorHandlers.register(app);  //global error handling
        // dine ruter
    }
}
