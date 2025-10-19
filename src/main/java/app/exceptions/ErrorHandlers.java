package app.exceptions;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UnauthorizedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ErrorHandlers {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandlers.class);
    private ErrorHandlers() {}

    public static void register(Javalin app) {

        // Domæne-fejl
        app.exception(ApiException.class, (e, ctx) -> {
            log.warn("ApiException: {} (status={})", e.getMessage(), e.getStatusCode());
            ctx.status(e.getStatusCode()).json(new Message(e.getStatusCode(), e.getMessage()));
        });

        // Typiske Javalin-fejl / HTTP-fejl
        app.exception(UnauthorizedResponse.class, (e, ctx) -> {
            log.info("Unauthorized: {}", e.getMessage());
            ctx.status(401).json(new Message(401, e.getMessage() == null ? "Unauthorized" : e.getMessage()));
        });

        app.exception(BadRequestResponse.class, (e, ctx) -> {
            log.info("Bad request: {}", e.getMessage());
            ctx.status(400).json(new Message(400, e.getMessage() == null ? "Bad request" : e.getMessage()));
        });

        app.exception(NotFoundResponse.class, (e, ctx) -> {
            log.info("Not found: {}", e.getMessage());
            ctx.status(404).json(new Message(404, e.getMessage() == null ? "Not found" : e.getMessage()));
        });

        // Fallback for alt andet (programmeringsfejl, NPE osv.)
        app.exception(Exception.class, (e, ctx) -> {
            log.error("Unhandled exception", e); // fuld stacktrace i logs, ikke til klienten
            ctx.status(500).json(new Message(500, "Internal server error"));
        });

        // 404 på udefinerede ruter
        app.error(404, ctx -> ctx.json(new Message(404, "Route not found")));
    }
}
