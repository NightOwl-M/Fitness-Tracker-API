// app/controllers/impl/WorkoutController.java
package app.controllers.impl;

import app.services.impl.WorkoutService;
import io.javalin.http.*;
import java.time.LocalDate;

public class WorkoutController {
    private final WorkoutService svc = new WorkoutService();

    private int uid(Context ctx){ Integer u = ctx.attribute("userId"); if (u==null) throw new UnauthorizedResponse(); return u; }

    public void listMine(Context ctx){ ctx.json(svc.listMine(uid(ctx))); }

    public void getMine(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));
        var dto = svc.getMine(uid(ctx), id); if (dto==null) throw new NotFoundResponse("Workout not found");
        ctx.json(dto);
    }

    public void createMine(Context ctx){
        var b = ctx.bodyAsClass(WorkoutBody.class);
        LocalDate d = (b.date==null ? LocalDate.now() : LocalDate.parse(b.date));
        var dto = svc.createMine(uid(ctx), d, b.notes);
        ctx.status(201).json(dto);
    }

    public void updateMine(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));
        var b = ctx.bodyAsClass(WorkoutBody.class);
        LocalDate d = (b.date==null ? LocalDate.now() : LocalDate.parse(b.date));
        var dto = svc.updateMine(uid(ctx), id, d, b.notes);
        if (dto==null) throw new NotFoundResponse("Workout not found");
        ctx.json(dto);
    }

    public void deleteMine(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));
        boolean ok = svc.deleteMine(uid(ctx), id);
        if (!ok) throw new NotFoundResponse("Workout not found");
        ctx.status(204);
    }

    public static class WorkoutBody { public String date; public String notes; }
}
