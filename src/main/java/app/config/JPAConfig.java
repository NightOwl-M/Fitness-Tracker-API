package app.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.flywaydb.core.Flyway;

import java.util.HashMap;
import java.util.Map;

public final class JPAConfig {
    private static EntityManagerFactory emf;
    private static EntityManagerFactory emfTest;

    private JPAConfig() {}

    /** Normal EMF til appen (dev/prod) */
    public static synchronized EntityManagerFactory emf() {
        if (emf == null) {
            runFlyway(false);
            emf = Persistence.createEntityManagerFactory("app-unit", runtimeOverrides(false));
        }
        return emf;
    }

    /** EMF til integrationstests (Testcontainers) */
    public static synchronized EntityManagerFactory emfTest() {
        if (emfTest == null) {
            runFlyway(true);
            emfTest = Persistence.createEntityManagerFactory("app-unit", runtimeOverrides(true));
        }
        return emfTest;
    }

    /** Kør Flyway migrationer */
    private static void runFlyway(boolean test) {
        String url;
        String user;
        String pass;

        if (test) {
            // Testcontainers: genbrug samme container for Flyway og Hibernate
            url  = "jdbc:tc:postgresql:15.3-alpine3.18:///test_db?TC_REUSABLE=true";
            user = "postgres";
            pass = "postgres";
        } else if (System.getenv("DEPLOYED") != null) {
            // Prod/deploy fra ENV
            String conn = getenvOr("CONNECTION_STR", "jdbc:postgresql://localhost:5433/");
            String name = getenvOr("DB_NAME", "app");
            url  = conn + name;
            user = getenvOr("DB_USERNAME", "postgres");
            pass = getenvOr("DB_PASSWORD", "postgres");
        } else {
            // Dev (ENV kan overstyre)
            url  = getenvOr("DB_URL", "jdbc:postgresql://localhost:5433/fitness");
            user = getenvOr("DB_USER", "app");
            pass = getenvOr("DB_PASSWORD", "secret");
        }

        Flyway.configure()
                .dataSource(url, user, pass)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
                .migrate();
    }

    /** Runtime overrides til JPA properties */
    private static Map<String, Object> runtimeOverrides(boolean test) {
        Map<String, Object> m = new HashMap<>();

        if (test) {
            // Integrationstest via Testcontainers JDBC-driver (genbrug container)
            m.put("jakarta.persistence.jdbc.driver", "org.testcontainers.jdbc.ContainerDatabaseDriver");
            m.put("jakarta.persistence.jdbc.url", "jdbc:tc:postgresql:15.3-alpine3.18:///test_db?TC_REUSABLE=true");
            m.put("jakarta.persistence.jdbc.user", "postgres");
            m.put("jakarta.persistence.jdbc.password", "postgres");
            m.put("hibernate.hbm2ddl.auto", "validate"); // Flyway styrer schema
            m.put("hibernate.show_sql", "true");
            m.put("hibernate.format_sql", "true");

        } else if (System.getenv("DEPLOYED") != null) {
            // Deploy/Prod – ENV er sandheden
            String conn = getenvOr("CONNECTION_STR", "jdbc:postgresql://localhost:5433/");
            String name = getenvOr("DB_NAME", "app");
            m.put("jakarta.persistence.jdbc.url", conn + name);
            m.put("jakarta.persistence.jdbc.user", getenvOr("DB_USERNAME", "postgres"));
            m.put("jakarta.persistence.jdbc.password", getenvOr("DB_PASSWORD", "postgres"));
            m.put("hibernate.hbm2ddl.auto", "validate");
            m.put("hibernate.show_sql", getenvOr("HIBERNATE_SHOW_SQL", "false"));
            m.put("hibernate.format_sql", getenvOr("HIBERNATE_FORMAT_SQL", "false"));

        } else {
            // Dev (5433)
            m.put("jakarta.persistence.jdbc.url",      getenvOr("DB_URL", "jdbc:postgresql://localhost:5433/fitness"));
            m.put("jakarta.persistence.jdbc.user",     getenvOr("DB_USER", "app"));
            m.put("jakarta.persistence.jdbc.password", getenvOr("DB_PASSWORD", "secret"));
            m.put("hibernate.hbm2ddl.auto", "validate"); // Flyway styrer
            m.put("hibernate.show_sql",   getenvOr("HIBERNATE_SHOW_SQL", "true"));
            m.put("hibernate.format_sql", getenvOr("HIBERNATE_FORMAT_SQL", "true"));
        }

        // Hikari pool
        m.put("hibernate.hikari.maximumPoolSize", getenvOr("DB_POOL_MAX", "10"));
        m.put("hibernate.hikari.minimumIdle",     getenvOr("DB_POOL_MIN_IDLE", "2"));
        m.put("hibernate.hikari.poolName",        getenvOr("DB_POOL_NAME", "ApiPool"));
        m.put("hibernate.archive.autodetection",  getenvOr("HIBERNATE_AUTODETECT", "class"));

        return m;
    }

    private static String getenvOr(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (emf != null && emf.isOpen()) emf.close();
            if (emfTest != null && emfTest.isOpen()) emfTest.close();
        }));
    }
}
