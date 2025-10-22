package app.security.controllers.impl;

import app.security.controllers.IAuthController;
import app.security.utils.JwtUtil;
import app.services.impl.UserService;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ConflictResponse;

import java.util.Map;

public class AuthController implements IAuthController {

    private final UserService userService = new UserService();

    @Override
    public void register(Context ctx) {
        var body = ctx.bodyAsClass(RegisterRequest.class);
        if (body.email == null || body.email.isBlank()) throw new BadRequestResponse("Missing email");
        if (body.password == null || body.password.isBlank()) throw new BadRequestResponse("Missing password");

        var created = userService.register(body.email.trim(), body.password, "USER"); // default USER
        if (created == null) throw new ConflictResponse("User already exists");

        String token = JwtUtil.issue(created.id(), created.role(), 60 * 60 * 8); // 8 timer
        ctx.status(201).json(Map.of(
                "token", token,
                "userId", created.id(),
                "email", created.email(),
                "role", created.role()
        ));
    }

    @Override
    public void login(Context ctx) {
        var body = ctx.bodyAsClass(LoginRequest.class);
        if (body.email == null || body.email.isBlank()) throw new BadRequestResponse("Missing email");
        if (body.password == null || body.password.isBlank()) throw new BadRequestResponse("Missing password");

        var verified = userService.verify(body.email.trim(), body.password);
        if (verified == null) throw new BadRequestResponse("Invalid email or password");

        String token = JwtUtil.issue(verified.id(), verified.role(), 60 * 60 * 8);
        ctx.json(Map.of(
                "token", token,
                "userId", verified.id(),
                "email", verified.email(),
                "role", verified.role()
        ));
    }

    // === request DTOs (små lokale klasser) ===
    public static class RegisterRequest { public String email; public String password; }
    public static class LoginRequest    { public String email; public String password; }
}
