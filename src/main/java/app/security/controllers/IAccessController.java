package app.security.controllers;

import io.javalin.http.Context;

public interface IAccessController {
    /** Returnér info om den aktuelt loggede bruger (aflæst fra JWT-middleware) */
    void me(Context ctx);
}
