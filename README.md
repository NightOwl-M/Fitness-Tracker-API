
Hele projektet kan genbruges som en skabelon til nye applikationer.
Både backend og frontend er klar til at blive containeriseret og kørt via docker-compose.
Strukturen er designet til at være skalerbar, testbar og CI/CD-klar.

Nedenunder finder du en oversigt over mappestrukturen samt små uddybninger over hvad de enkelte mapper/filer er til.


```
ApiTemplate1

En skabelon til hurtigt at opsætte et moderne Java-baseret REST API med fuld 3-lags arkitektur,
sikkerhed, Docker, CI/CD og et simpelt React-baseret frontend.

📂 Projektstruktur


ApiTemplate1/
├── .idea/                         ← IntelliJ projektfiler
├── .mvn/                          ← Maven wrapper filer
│
├── docker/
│   ├── Dockerfile                 ← Bygger Java backend container
│   └── docker-compose.yml         ← Starter hele stacken (API + DB)
│
├── docs/
│   ├── api-contracts.md           ← Dokumentation af endpoints og API-kontrakter
│   ├── architecture.md            ← Systemarkitektur og flow-beskrivelse
│   └── erd.puml                   ← ER-diagram (PlantUML)
│
├── frontend/
│   ├── public/
│   │   └── index.html             ← Root HTML-fil hvor React mountes
│   │
│   ├── src/
│   │   ├── assets/                ← Billeder, ikoner, fonte osv.
│   │   ├── components/            ← Genbrugelige UI-komponenter (Button, Card, Navbar)
│   │   │   ├── Button.jsx
│   │   │   ├── Card.jsx
│   │   │   └── Navbar.jsx
│   │   ├── pages/                 ← Fuld-sider (fx Homepage.jsx)
│   │   │   └── Homepage.jsx
│   │   ├── styles/                ← CSS-filer og global styling
│   │   │   └── index.css
│   │   ├── App.jsx                ← Hovedkomponent – samler hele React-appen
│   │   └── main.jsx               ← Indgangspunkt – mount’er app’en
│   │
│   ├── package.json               ← NPM-afhængigheder og scripts
│   └── vite.config.js             ← Vite build-konfiguration
│
├── github/
│   └── workflows/                 ← GitHub Actions (CI/CD pipelines)
│
├── scripts/
│   ├── dev-up.sh                  ← Starter container-setup (backend + DB)
│   ├── dev-down.sh                ← Stopper container-setup
│   ├── migrate.sh                 ← Kører database-migrationer (fx Flyway)
│   └── run.cli.sh                 ← CLI-kørsel under udvikling
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── app/
│   │   │       ├── config/               ← Konfiguration (database, logger, miljø)
│   │   │       │
│   │   │       ├── controllers/          ← API-controllere (endpoints og interfaces)
│   │   │       │   ├── impl/             ← Implementeringer af controllere
│   │   │       │   └── IController.java  ← Interface for controllere
│   │   │       │
│   │   │       ├── daos/                 ← Data Access Objects (database-laget)
│   │   │       │   ├── impl/             ← Implementeringer af DAO-klasser
│   │   │       │   └── IDAO.java         ← Interface for DAO’er
│   │   │       │
│   │   │       ├── dtos/                 ← Data Transfer Objects mellem lag
│   │   │       │
│   │   │       ├── entities/             ← Entity-klasser (JPA) – repræsenterer database-tabeller
│   │   │       │
│   │   │       ├── exceptions/           ← Håndtering af fejl og custom exceptions
│   │   │       │   ├── ApiException.java ← Custom API-fejlklasse
│   │   │       │   └── Message.java      ← Record til JSON-beskeder (status + message)
│   │   │       │
│   │   │       ├── routes/               ← API routing-lag
│   │   │       │   ├── modules/          ← Modulopdelte routes (fx UserRoute, HotelRoute)
│   │   │       │   └── Routes.java       ← Samler alle route-definitioner
│   │   │       │
│   │   │       ├── security/             ← Alt relateret til login, JWT og autorisation
│   │   │       │   ├── controllers/      ← Security-controllere
│   │   │       │   │   ├── impl/         ← Implementeringer (AuthController, AccessController)
│   │   │       │   │   ├── IAuthController.java
│   │   │       │   │   └── IAccessController.java
│   │   │       │   │
│   │   │       │   ├── daos/             ← Security-relaterede DAO’er
│   │   │       │   │   ├── impl/         ← Implementeringer (UserAuthDAO, RoleDAO)
│   │   │       │   │   ├── IRoleDAO.java
│   │   │       │   │   └── IUserAuthDAO.java
│   │   │       │   │
│   │   │       │   ├── entities/         ← Entiteter som User og Role
│   │   │       │   ├── enums/            ← Roller, permissions osv.
│   │   │       │   ├── exceptions/       ← Auth-relaterede exceptions
│   │   │       │   ├── http/             ← HTTP-testfiler til IntelliJ eller Postman
│   │   │       │   ├── routes/           ← Routes relateret til authentication
│   │   │       │   └── service/          ← Auth- og token-services
│   │   │       │       ├── impl/         ← Implementeringer (AuthService, TokenService)
│   │   │       │       ├── IAuthService.java
│   │   │       │       └── ITokenService.java
│   │   │       │
│   │   │       ├── service/              ← Forretningslogik (application-laget)
│   │   │       │   ├── impl/             ← Implementeringer af services
│   │   │       │   └── IService.java     ← Interface for service-laget
│   │   │       │
│   │   │       ├── utils/                ← Hjælpeklasser og værktøjsmetoder
│   │   │       │
│   │   │       └── Main.java             ← Applikationens entry point (starter Javalin-serveren)
│   │   │
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migration/
│   │       │       └── V1__baseline.sql  ← Database-migration (Flyway)
│   │       ├── META-INF/
│   │       │   └── persistence.xml       ← JPA-konfiguration
│   │       ├── application.properties    ← Miljøvariabler og DB-forbindelser
│   │       └── logback.xml               ← Logging-konfiguration (SLF4J/Logback)
│   │
│   └── test/
│       ├── resources/
│       │   ├── db/migration/             ← Testdatabase-migrationer
│       │   ├── fixtures/                 ← Testdata (JSON / SQL)
│       │   ├── application-test.properties ← Test-konfiguration
│       │   └── logback-test.xml          ← Logging under tests
│       └── java/
│           └── app/                      ← Testpakker (samme struktur som main)
│
├── .gitignore
├── pom.xml                              ← Maven afhængigheder og build-konfiguration
└── README.md                            ← Dokumentation (denne fil)

```

```
Uddybning af dependencys brugt i skabalonen.


| Sektion                            | Forklaring                                                |
| ---------------------------------- | --------------------------------------------------------- |
| GroupId / ArtifactId / Version | Identificerer projektet unikt.                            |
| Properties                     | Indeholder versionsnumre og build-indstillinger.          |
| Dependencies                   | Eksterne biblioteker, som projektet bruger.               |
| Build                          | Maven-plugins til kompilering, tests og pakning af appen. |

| Dependency                                                 | Funktion                                                                                             |
| ---------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| io.javalin:javalin-bundle                              | Webframework til REST-API’er. Indeholder alt du skal bruge til routes, endpoints og JSON-håndtering. |
| com.fasterxml.jackson.core:jackson-databind            | Konverterer Java-objekter til JSON og omvendt.                                                       |
| com.fasterxml.jackson.datatype:jackson-datatype-jsr310 | Gør Jackson i stand til at håndtere Java-datoer (LocalDateTime osv.).                                |

| Dependency                               | Funktion                                                                |
| ---------------------------------------- | ----------------------------------------------------------------------- |
| org.hibernate.orm:hibernate-core     | ORM-motor (JPA-implementering) der mapper dine entiteter til databasen. |
| org.postgresql:postgresql            | JDBC-driver til PostgreSQL. Forbinder din app til databasen.            |
| com.zaxxer:HikariCP                  | Connection-pool til hurtigere og stabil databaseadgang.                 |
| org.hibernate.orm:hibernate-hikaricp | Integrerer Hibernate med HikariCP.                                      |
| org.flywaydb:flyway-core               | Håndterer database-migrationer (V1__baseline.sql, V2__… osv.).          |

| Dependency                       | Funktion                                                      |
| -------------------------------- | ------------------------------------------------------------- |
| io.jsonwebtoken:jjwt-api     | API til at oprette, validere og dekode JWT-tokens.            |
| io.jsonwebtoken:jjwt-impl    | Implementation af JWT-funktionalitet (runtime).               |
| io.jsonwebtoken:jjwt-jackson | Integration med Jackson, så JWT’er kan serialiseres til JSON. |

| Dependency                                      | Funktion                                                             |
| ----------------------------------------------- | -------------------------------------------------------------------- |
| jakarta.validation:jakarta.validation-api   | Understøtter annotations som `@NotNull`, `@Email`, `@Size` i DTO’er. |
| org.hibernate.validator:hibernate-validator | Implementation af Jakarta Validation-standarden.                     |
| org.projectlombok:lombok                    | Fjerner boilerplate-kode (getters, setters, constructors osv.).      |

| Dependency                            | Funktion                                                        |
| ------------------------------------- | --------------------------------------------------------------- |
| **ch.qos.logback:logback-classic    | Logger-framework, der skriver til konsol eller fil.             |
| org.slf4j:slf4j-api (indirekte)     | Standardiseret logging-interface. Bruges automatisk af Logback. |

| Dependency                                 | Funktion                                                          |
| ------------------------------------------ | ----------------------------------------------------------------- |
| org.junit.jupiter:junit-jupiter-api    | JUnit 5 testframework – brugt til unit-tests.                     |
| org.junit.jupiter:junit-jupiter-engine | Motoren som kører JUnit 5-tests.                                  |
| org.junit.jupiter:junit-jupiter-params | Giver mulighed for parameteriserede tests.                        |
| org.hamcrest:hamcrest                  | Matcher-bibliotek til læsbare testasserts (`assertThat(...)`).    |
| org.testcontainers:testcontainers      | Starter rigtige containere (fx Postgres) under integrationstests. |
| org.testcontainers:postgresql          | Klar Postgres-container til tests.                                |
| org.testcontainers:jdbc                | Forbinder JDBC med Testcontainers.                                |
| org.testcontainers:junit-jupiter       | Integration mellem JUnit 5 og Testcontainers.                     |
| io.rest-assured:rest-assured           | Tester REST-endpoints via HTTP-kald.                              |
| io.rest-assured:json-schema-validator  | Validerer JSON-respons mod et schema.                             |

| Plugin                    | Funktion                                                               |
| ------------------------- | ---------------------------------------------------------------------- |
| maven-compiler-plugin | Kompilerer koden (med korrekt Java-version og Lombok-support).         |
| maven-surefire-plugin  | Kører *unit tests* under `mvn test`.                                   |
| maven-failsafe-plugin | Kører *integrationstests* (typisk navngivet `*IT.java`).               |
| maven-shade-plugin    | Pakker hele projektet som én “fat-jar” (fx `app.jar`) klar til Docker. |
```


## Fixtures
Eksempler på testdata findes i `src/test/resources/fixtures/`.
Her kan du tilføje JSON- eller SQL-filer med testdata, som bruges under testkørsel.

Eksempel:
```json
[
  {
    "id": 1,
    "name": "Example Hotel",
    "address": "Example Street 42"
  }
]