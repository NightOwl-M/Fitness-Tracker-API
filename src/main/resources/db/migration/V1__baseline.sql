
CREATE TABLE users (
                       id           SERIAL PRIMARY KEY,
                       email        VARCHAR(150) NOT NULL UNIQUE,
                       passwordHash VARCHAR(400) NOT NULL,
                       role         VARCHAR(20)  NOT NULL
);


CREATE TABLE exercises (
                           id           SERIAL PRIMARY KEY,
                           name         VARCHAR(80)  NOT NULL,
                           muscleGroup  VARCHAR(40)  NOT NULL
);


CREATE TABLE workouts (
                          id      SERIAL PRIMARY KEY,
                          userId  INTEGER     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          date    DATE        NOT NULL,
                          notes   VARCHAR(200)
);

CREATE INDEX idx_workouts_userid_date ON workouts(userId, date DESC);
