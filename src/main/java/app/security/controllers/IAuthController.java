package app.security.controllers;

import io.javalin.http.Context;

public interface IAuthController {
    void register(Context ctx);
    void login(Context ctx);
}
