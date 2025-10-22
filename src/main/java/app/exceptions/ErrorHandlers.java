package app.exceptions;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.ConflictResponse;
import io.jsonwebtoken.JwtException; // ← NY import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ErrorHandlers {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandlers.class);
    private ErrorHandlers() {}

    public static void register(Javalin app) {

        // Domæne-fejl
        app.exception(ApiException.class, (e, ctx) -> {
            log.warn("ApiException: {} (status={})", e.getMessage(), e.getStatusCode());
            ctx.status(e.getStatusCode()).json(new ApiError(e.getStatusCode(), e.getMessage()));
        });

        // 401 Unauthorized
        app.exception(UnauthorizedResponse.class, (e, ctx) -> {
            log.info("Unauthorized: {}", e.getMessage());
            ctx.status(401).json(new ApiError(401, e.getMessage() == null ? "Unauthorized" : e.getMessage()));
        });

        // 401 ved ugyldig/korrupt JWT  ← NY
        app.exception(JwtException.class, (e, ctx) -> {
            log.info("Invalid JWT: {}", e.getMessage());
            ctx.status(401).json(new ApiError(401, "Invalid token"));
        });

        // Nogle JWT-parse fejl kan kastes som IllegalArgumentException  ← NY
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            if (ctx.header("Authorization") != null) {
                log.info("Illegal token format: {}", e.getMessage());
                ctx.status(401).json(new ApiError(401, "Invalid token"));
            } else {
                log.error("Unhandled exception", e);
                ctx.status(500).json(new ApiError(500, "Internal server error"));
            }
        });

        // 403 Forbidden
        app.exception(ForbiddenResponse.class, (e, ctx) -> {
            log.info("Forbidden: {}", e.getMessage());
            ctx.status(403).json(new ApiError(403, e.getMessage() == null ? "Forbidden" : e.getMessage()));
        });

        // 400 Bad Request
        app.exception(BadRequestResponse.class, (e, ctx) -> {
            log.info("Bad request: {}", e.getMessage());
            ctx.status(400).json(new ApiError(400, e.getMessage() == null ? "Bad request" : e.getMessage()));
        });

        // 409 Conflict
        app.exception(ConflictResponse.class, (e, ctx) -> {
            log.info("Conflict: {}", e.getMessage());
            ctx.status(409).json(new ApiError(409, e.getMessage() == null ? "Conflict" : e.getMessage()));
        });

        // 404 Not Found
        app.exception(NotFoundResponse.class, (e, ctx) -> {
            log.info("Not found: {}", e.getMessage());
            ctx.status(404).json(new ApiError(404, e.getMessage() == null ? "Not found" : e.getMessage()));
        });

        // Fallback for alt andet
        app.exception(Exception.class, (e, ctx) -> {
            log.error("Unhandled exception", e);
            ctx.status(500).json(new ApiError(500, "Internal server error"));
        });

        // 404 på udefinerede ruter
        app.error(404, ctx -> ctx.json(new ApiError(404, "Route not found")));
    }
}
