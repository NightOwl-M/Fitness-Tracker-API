// app.controllers.IController.java
package app.controllers;

import io.javalin.http.Context;

public interface IController {
    void list(Context ctx);              // GET /resource
    void get(Context ctx);               // GET /resource/{id}
    void create(Context ctx);            // POST /resource
    void update(Context ctx);            // PUT /resource/{id}
    void delete(Context ctx);            // DELETE /resource/{id}
}
