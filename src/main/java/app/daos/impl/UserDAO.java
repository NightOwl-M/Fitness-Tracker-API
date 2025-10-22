package app.daos.impl;

import app.config.JPAConfig;
import app.entities.User;
import jakarta.persistence.EntityManager;

public class UserDAO {

    public User findByEmail(String email){
        var emf = JPAConfig.emf();
        try (var em = emf.createEntityManager()) {
            var q = em.createQuery("SELECT u FROM User u WHERE lower(u.email) = lower(:e)", User.class);
            q.setParameter("e", email);
            return q.getResultStream().findFirst().orElse(null);
        }
    }

    public User create(User u){
        var emf = JPAConfig.emf();
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(u);
            em.getTransaction().commit();
            return u;
        } finally {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
    }
}
