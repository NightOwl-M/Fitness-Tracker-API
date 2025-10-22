package app.daos.impl;

import app.config.JPAConfig;
import app.daos.IWorkoutRepository;
import app.entities.Workout;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class WorkoutRepository implements IWorkoutRepository {

    private jakarta.persistence.EntityManager em() { return JPAConfig.emf().createEntityManager(); }

    // -------- IDAO (generisk CRUD) --------

    @Override
    public List<Workout> findAll() {
        try (var em = em()) {
            return em.createQuery("select w from Workout w order by w.date desc", Workout.class)
                    .getResultList();
        }
    }

    @Override
    public Workout find(Integer id) {
        try (var em = em()) {
            return em.find(Workout.class, id);
        }
    }

    @Override
    public Workout create(Workout w) {
        try (var em = em()) {
            em.getTransaction().begin();
            em.persist(w);
            em.getTransaction().commit();
            return w;
        }
    }

    @Override
    public Workout update(Integer id, Workout in) {
        try (var em = em()) {
            em.getTransaction().begin();
            Workout w = em.find(Workout.class, id);
            if (w == null) { em.getTransaction().rollback(); return null; }
            w.setDate(in.getDate());
            w.setNotes(in.getNotes());
            em.getTransaction().commit();
            return w;
        }
    }

    @Override
    public boolean delete(Integer id) {
        try (var em = em()) {
            em.getTransaction().begin();
            Workout w = em.find(Workout.class, id);
            if (w == null) { em.getTransaction().rollback(); return false; }
            em.remove(w);
            em.getTransaction().commit();
            return true;
        }
    }

    // -------- IWorkoutRepository (user-scoped) --------

    @Override
    public List<Workout> findAllByUser(int userId) {
        try (var em = em()) {
            TypedQuery<Workout> q = em.createQuery(
                    "select w from Workout w where w.userId = :uid order by w.date desc",
                    Workout.class
            );
            q.setParameter("uid", userId);
            return q.getResultList();
        }
    }

    @Override
    public Workout findByUser(int userId, int id) {
        try (var em = em()) {
            Workout w = em.find(Workout.class, id);
            return (w != null && w.getUserId() != null && w.getUserId().equals(userId)) ? w : null;
        }
    }

    @Override
    public Workout update(int userId, int id, Workout in) {
        try (var em = em()) {
            em.getTransaction().begin();
            Workout w = em.find(Workout.class, id);
            if (w == null || w.getUserId() == null || !w.getUserId().equals(userId)) {
                em.getTransaction().rollback();
                return null;
            }
            w.setDate(in.getDate());
            w.setNotes(in.getNotes());
            em.getTransaction().commit();
            return w;
        }
    }

    @Override
    public boolean delete(int userId, int id) {
        try (var em = em()) {
            em.getTransaction().begin();
            Workout w = em.find(Workout.class, id);
            if (w == null || w.getUserId() == null || !w.getUserId().equals(userId)) {
                em.getTransaction().rollback();
                return false;
            }
            em.remove(w);
            em.getTransaction().commit();
            return true;
        }
    }
}
