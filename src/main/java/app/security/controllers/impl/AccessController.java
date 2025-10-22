package app.security.controllers.impl;

import app.security.controllers.IAccessController;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;

import java.util.Map;

public class AccessController implements IAccessController {

    @Override
    public void me(Context ctx) {
        Integer userId = ctx.attribute("userId");
        String role     = ctx.attribute("role");
        if (userId == null || role == null) throw new UnauthorizedResponse("Unauthorized");
        ctx.json(Map.of("userId", userId, "role", role));
    }
}
