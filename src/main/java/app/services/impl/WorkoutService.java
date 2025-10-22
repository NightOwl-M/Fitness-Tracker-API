package app.services.impl;

import app.daos.impl.WorkoutRepository;
import app.entities.Workout;
import app.services.dto.WorkoutDTO;

import java.time.LocalDate;
import java.util.List;

public class WorkoutService {

    private final WorkoutRepository repo = new WorkoutRepository();

    public List<WorkoutDTO> listMine(int userId) {
        return repo.findAllByUser(userId).stream()
                .map(w -> new WorkoutDTO(w.getId(), w.getUserId(), w.getDate(), w.getNotes()))
                .toList();
    }

    public WorkoutDTO getMine(int userId, int id) {
        var w = repo.findByUser(userId, id);
        if (w == null) return null;
        return new WorkoutDTO(w.getId(), w.getUserId(), w.getDate(), w.getNotes());
    }

    public WorkoutDTO createMine(int userId, LocalDate date, String notes) {
        var w = new Workout();
        w.setUserId(userId);
        w.setDate(date);
        w.setNotes(notes);
        var saved = repo.create(w);
        return new WorkoutDTO(saved.getId(), saved.getUserId(), saved.getDate(), saved.getNotes());
    }

    public WorkoutDTO updateMine(int userId, int id, LocalDate date, String notes) {
        var in = new Workout();
        in.setDate(date);
        in.setNotes(notes);
        var updated = repo.update(userId, id, in);
        if (updated == null) return null;
        return new WorkoutDTO(updated.getId(), updated.getUserId(), updated.getDate(), updated.getNotes());
    }

    public boolean deleteMine(int userId, int id) {
        return repo.delete(userId, id);
    }
}
