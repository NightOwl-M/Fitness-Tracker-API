package app.daos;

import app.entities.Workout;
import java.util.List;

public interface IWorkoutRepository extends IDAO<Workout, Integer> {

    // User-specifikke metoder
    List<Workout> findAllByUser(int userId);
    Workout findByUser(int userId, int id);

    // User-scoped ændringer (ejerskab)
    Workout update(int userId, int id, Workout in);
    boolean delete(int userId, int id);
}
