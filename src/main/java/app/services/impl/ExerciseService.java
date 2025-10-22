package app.services.impl;

import app.daos.impl.ExerciseRepository;
import app.entities.Exercise;
import app.services.dto.ExerciseDTO;

import java.util.List;

public class ExerciseService {

    private final ExerciseRepository repo = new ExerciseRepository();

    public List<ExerciseDTO> list() {
        return repo.findAll().stream()
                .map(e -> new ExerciseDTO(e.getId(), e.getName(), e.getMuscleGroup()))
                .toList();
    }

    public ExerciseDTO get(int id) {
        var e = repo.find(id);
        if (e == null) return null;
        return new ExerciseDTO(e.getId(), e.getName(), e.getMuscleGroup());
    }

    public ExerciseDTO create(String name, String muscleGroup) {
        var e = new Exercise();
        e.setName(name);
        e.setMuscleGroup(muscleGroup);
        var saved = repo.create(e);
        return new ExerciseDTO(saved.getId(), saved.getName(), saved.getMuscleGroup());
    }

    public ExerciseDTO update(int id, String name, String muscleGroup) {
        var in = new Exercise();
        in.setName(name);
        in.setMuscleGroup(muscleGroup);
        var updated = repo.update(id, in);
        if (updated == null) return null;
        return new ExerciseDTO(updated.getId(), updated.getName(), updated.getMuscleGroup());
    }

    public boolean delete(int id) {
        return repo.delete(id);
    }
}
