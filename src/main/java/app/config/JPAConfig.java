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
            // (valgfrit) behold din gamle testgren – vi kører dog i CI via dev-grenen
            url  = "jdbc:tc:postgresql:15-alpine:///test_db";
            user = "postgres";
            pass = "postgres";
        } else if (System.getenv("DEPLOYED") != null) {
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

        boolean isTestcontainersJdbc = url.startsWith("jdbc:tc:");

        var cfg = org.flywaydb.core.Flyway.configure()
                .dataSource(url, user, pass)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true);

        // Flyway skal også kende Testcontainers-driveren ved jdbc:tc:
        if (isTestcontainersJdbc) {
            cfg.driver("org.testcontainers.jdbc.ContainerDatabaseDriver");
        }

        cfg.load().migrate();
    }

    private static Map<String, Object> runtimeOverrides(boolean test) {
        Map<String, Object> m = new HashMap<>();

        if (test) {
            // (kan beholdes, men CI kører via dev-grenen)
            m.put("jakarta.persistence.jdbc.driver", "org.testcontainers.jdbc.ContainerDatabaseDriver");
            m.put("jakarta.persistence.jdbc.url", "jdbc:tc:postgresql:15-alpine:///test_db");
            m.put("jakarta.persistence.jdbc.user", "postgres");
            m.put("jakarta.persistence.jdbc.password", "postgres");
            m.put("hibernate.hbm2ddl.auto", "validate");
            m.put("hibernate.show_sql", "true");
            m.put("hibernate.format_sql", "true");

        } else if (System.getenv("DEPLOYED") != null) {
            String conn = getenvOr("CONNECTION_STR", "jdbc:postgresql://localhost:5433/");
            String name = getenvOr("DB_NAME", "app");
            String url  = conn + name;

            m.put("jakarta.persistence.jdbc.url", url);
            m.put("jakarta.persistence.jdbc.user", getenvOr("DB_USERNAME", "postgres"));
            m.put("jakarta.persistence.jdbc.password", getenvOr("DB_PASSWORD", "postgres"));
            m.put("hibernate.hbm2ddl.auto", "validate");
            m.put("hibernate.show_sql", getenvOr("HIBERNATE_SHOW_SQL", "false"));
            m.put("hibernate.format_sql", getenvOr("HIBERNATE_FORMAT_SQL", "false"));

            if (url.startsWith("jdbc:tc:")) {
                m.put("jakarta.persistence.jdbc.driver", "org.testcontainers.jdbc.ContainerDatabaseDriver");
            }

        } else {
            // Dev (5433 som default) – MEN i CI sætter vi DB_URL=jdbc:tc:...
            String url  = getenvOr("DB_URL", "jdbc:postgresql://localhost:5433/fitness");
            String user = getenvOr("DB_USER", "app");
            String pass = getenvOr("DB_PASSWORD", "secret");

            m.put("jakarta.persistence.jdbc.url", url);
            m.put("jakarta.persistence.jdbc.user", user);
            m.put("jakarta.persistence.jdbc.password", pass);
            m.put("hibernate.hbm2ddl.auto", "validate");
            m.put("hibernate.show_sql", getenvOr("HIBERNATE_SHOW_SQL", "true"));
            m.put("hibernate.format_sql", getenvOr("HIBERNATE_FORMAT_SQL", "true"));

            // NØGLEN: når vi bruger jdbc:tc i CI (dev-grenen), skal driveren være Testcontainers-driveren
            if (url.startsWith("jdbc:tc:")) {
                m.put("jakarta.persistence.jdbc.driver", "org.testcontainers.jdbc.ContainerDatabaseDriver");
            }
        }

        // Hikari pool (uændret)
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
