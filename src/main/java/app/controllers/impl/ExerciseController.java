// app/controllers/impl/ExerciseController.java
package app.controllers.impl;

import app.services.impl.ExerciseService;
import io.javalin.http.*;

public class ExerciseController {
    private final ExerciseService svc = new ExerciseService();

    public void list(Context ctx){ ctx.json(svc.list()); }

    public void get(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));
        var dto = svc.get(id); if (dto==null) throw new NotFoundResponse("Exercise not found");
        ctx.json(dto);
    }

    public void create(Context ctx){
        var b = ctx.bodyAsClass(ExerciseBody.class);
        if (b.name==null || b.name.isBlank()) throw new BadRequestResponse("Missing name");
        if (b.muscleGroup==null || b.muscleGroup.isBlank()) throw new BadRequestResponse("Missing muscleGroup");
        var dto = svc.create(b.name.trim(), b.muscleGroup.trim());
        ctx.status(201).json(dto);
    }

    public void update(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));
        var b = ctx.bodyAsClass(ExerciseBody.class);
        var dto = svc.update(id, b.name, b.muscleGroup);
        if (dto==null) throw new NotFoundResponse("Exercise not found");
        ctx.json(dto);
    }

    public void delete(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));
        boolean ok = svc.delete(id);
        if (!ok) throw new NotFoundResponse("Exercise not found");
        ctx.status(204);
    }

    public static class ExerciseBody { public String name; public String muscleGroup; }
}
