package app.config;

import app.controllers.impl.ExerciseController;
import app.controllers.impl.WorkoutController;
import app.exceptions.ErrorHandlers;
import app.security.controllers.impl.AccessController;
import app.security.controllers.impl.AuthController;
import app.security.utils.JwtUtil;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;

import java.util.Map;
import java.util.function.Consumer;

public final class ApplicationConfig {

    private static final AuthController authController = new AuthController();
    private static final ExerciseController exerciseController = new ExerciseController();
    private static final WorkoutController workoutController = new WorkoutController();
    private static final AccessController accessController = new AccessController();

    private ApplicationConfig() {}

    public static Javalin create() {
        // Minimal setup
        Javalin app = Javalin.create(cfg -> {
            cfg.http.defaultContentType = "application/json";
        });

        // Global error handlers
        ErrorHandlers.register(app);

        // MANUEL CORS
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Authorization, Content-Type");
        });
        app.options("/*", ctx -> ctx.status(204));

        // Health (helt public)
        app.get("/health", ctx -> ctx.json(Map.of("status", "ok")));

        // Public auth
        app.post("/auth/register", authController::register);
        app.post("/auth/login",    authController::login);

        // JWT på /api/*
        app.before("/api/*", ApplicationConfig::authenticate);

        // Me (bag JWT fordi ruten ligger under /api/*)
        app.get("/api/auth/me", accessController::me);

        // Exercises (ADMIN)
        app.get   ("/api/exercises",            exerciseController::list);
        app.get   ("/api/exercises/{id}",       exerciseController::get);
        app.post  ("/api/exercises",            guard(ApplicationConfig::requireAdmin, exerciseController::create));
        app.put   ("/api/exercises/{id}",       guard(ApplicationConfig::requireAdmin, exerciseController::update));
        app.delete("/api/exercises/{id}",       guard(ApplicationConfig::requireAdmin, exerciseController::delete));

        // Workouts (USER - egne)
        app.get   ("/api/workouts",             workoutController::listMine);
        app.get   ("/api/workouts/{id}",        workoutController::getMine);
        app.post  ("/api/workouts",             workoutController::createMine);
        app.put   ("/api/workouts/{id}",        workoutController::updateMine);
        app.delete("/api/workouts/{id}",        workoutController::deleteMine);

        // Simpel logging
        app.before(ctx -> ctx.attribute("startNs", System.nanoTime()));
        app.after(ctx -> {
            Long startNs = ctx.attribute("startNs");
            long durMs = startNs == null ? -1 : (System.nanoTime() - startNs) / 1_000_000;
            String method = String.valueOf(ctx.method());
            String path   = ctx.path();
            String status = String.valueOf(ctx.status());
            System.out.printf("%s %s -> %s (%d ms)%n", method, path, status, durMs);
        });

        // “Route overview” placeholder (kan skiftes til rigtigt plugin senere)
        app.get("/api/routes", ctx -> ctx.json(Map.of(
                "info", "Route overview plugin disabled in this build (using manual CORS)."
        )));

        return app;
    }

    public static Javalin start(int port) {
        Javalin app = create();
        app.start(port);
        return app;
    }
    public static void stop(Javalin app) { if (app != null) app.stop(); }

    // ======= Middleware & Guards =======

    private static void authenticate(Context ctx) {
        // OPTIONS = preflight → lad passere
        if ("OPTIONS".equalsIgnoreCase(String.valueOf(ctx.method()))) return;

        String auth = ctx.header("Authorization");
        if (auth == null || !auth.startsWith("Bearer "))
            throw new UnauthorizedResponse("Unauthorized");

        String token = auth.substring("Bearer ".length());
        Integer userId = JwtUtil.userIdFrom(token);
        String  role   = JwtUtil.roleFrom(token);

        if (userId == null || role == null)
            throw new UnauthorizedResponse("Unauthorized");

        ctx.attribute("userId", userId);
        ctx.attribute("role", role);
    }

    private static io.javalin.http.Handler guard(Consumer<Context> guard, io.javalin.http.Handler handler) {
        return ctx -> { guard.accept(ctx); handler.handle(ctx); };
    }

    private static void requireAdmin(Context ctx) {
        String role = ctx.attribute("role");
        if (role == null) throw new UnauthorizedResponse("Unauthorized");
        if (!"ADMIN".equalsIgnoreCase(role)) throw new ForbiddenResponse("Forbidden");
    }
}
