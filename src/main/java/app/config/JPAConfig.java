package app.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public final class JPAConfig {
    private static EntityManagerFactory emf;
    private static EntityManagerFactory emfTest;

    private JPAConfig() {}

    // Normal EMF til appen (dev/prod)
    public static synchronized EntityManagerFactory emf() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("app-unit", runtimeOverrides(false));
        }
        return emf;
    }

    // EMF til tests (Testcontainers)
    public static synchronized EntityManagerFactory emfTest() {
        if (emfTest == null) {
            emfTest = Persistence.createEntityManagerFactory("app-unit", runtimeOverrides(true));
        }
        return emfTest;
    }

    private static Map<String, Object> runtimeOverrides(boolean test) {
        Map<String, Object> m = new HashMap<>();

        if (test) {
            // Kør integrationstest mod Testcontainers Postgres
            m.put("jakarta.persistence.jdbc.driver", "org.testcontainers.jdbc.ContainerDatabaseDriver");
            m.put("jakarta.persistence.jdbc.url", "jdbc:tc:postgresql:15.3-alpine3.18:///test_db");
            m.put("jakarta.persistence.jdbc.user", "postgres");
            m.put("jakarta.persistence.jdbc.password", "postgres");
            m.put("hibernate.hbm2ddl.auto", "create-drop"); // ren test-DB pr. kørsel
            m.put("hibernate.show_sql", "true");
        } else if (System.getenv("DEPLOYED") != null) {
            // Prod/Deploy (ENV prioriteres)
            String conn = getenvOr("CONNECTION_STR", "jdbc:postgresql://localhost:5432/");
            String name = getenvOr("DB_NAME", "app");
            m.put("jakarta.persistence.jdbc.url", conn + name);
            m.put("jakarta.persistence.jdbc.user", getenvOr("DB_USERNAME", "postgres"));
            m.put("jakarta.persistence.jdbc.password", getenvOr("DB_PASSWORD", "postgres"));
            m.put("hibernate.hbm2ddl.auto", "validate");
            m.put("hibernate.show_sql", "false");
        } else {
            // Dev: brug defaults fra persistence.xml, men tillad ENV override
            String url = getenvOr("DB_URL", null);
            if (url != null) m.put("jakarta.persistence.jdbc.url", url);
            String user = getenvOr("DB_USER", null);
            if (user != null) m.put("jakarta.persistence.jdbc.user", user);
            String pass = getenvOr("DB_PASSWORD", null);
            if (pass != null) m.put("jakarta.persistence.jdbc.password", pass);

            m.put("hibernate.hbm2ddl.auto", getenvOr("HIBERNATE_DDL", "update"));
            m.put("hibernate.show_sql", getenvOr("HIBERNATE_SHOW_SQL", "true"));
        }

        // Hikari – kan også overrides via ENV
        m.put("hibernate.hikari.maximumPoolSize", getenvOr("DB_POOL_MAX", "10"));
        m.put("hibernate.hikari.minimumIdle", getenvOr("DB_POOL_MIN_IDLE", "2"));
        m.put("hibernate.hikari.poolName", getenvOr("DB_POOL_NAME", "ApiTemplatePool"));

        return m;
    }

    private static String getenvOr(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    // Luk fabrikkene pænt når JVM stopper
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (emf != null && emf.isOpen()) emf.close();
            if (emfTest != null && emfTest.isOpen()) emfTest.close();
        }));
    }
}
