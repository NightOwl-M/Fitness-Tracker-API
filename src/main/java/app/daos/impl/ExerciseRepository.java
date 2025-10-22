package app.daos.impl;

import app.config.JPAConfig;
import app.daos.IExerciseRepository;
import app.entities.Exercise;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ExerciseRepository implements IExerciseRepository {

    private EntityManager em() { return JPAConfig.emf().createEntityManager(); }

    @Override
    public List<Exercise> findAll() {
        try (var em = em()) {
            return em.createQuery("select e from Exercise e", Exercise.class).getResultList();
        }
    }

    @Override
    public Exercise find(Integer id) {
        try (var em = em()) {
            return em.find(Exercise.class, id);
        }
    }

    @Override
    public Exercise create(Exercise e) {
        try (var em = em()) {
            em.getTransaction().begin();
            em.persist(e);
            em.getTransaction().commit();
            return e;
        }
    }

    @Override
    public Exercise update(Integer id, Exercise in) {
        try (var em = em()) {
            em.getTransaction().begin();
            Exercise e = em.find(Exercise.class, id);
            if (e == null) { em.getTransaction().rollback(); return null; }
            e.setName(in.getName());
            e.setMuscleGroup(in.getMuscleGroup());
            em.getTransaction().commit();
            return e;
        }
    }

    @Override
    public boolean delete(Integer id) {
        try (var em = em()) {
            em.getTransaction().begin();
            Exercise e = em.find(Exercise.class, id);
            if (e == null) { em.getTransaction().rollback(); return false; }
            em.remove(e);
            em.getTransaction().commit();
            return true;
        }
    }
}
