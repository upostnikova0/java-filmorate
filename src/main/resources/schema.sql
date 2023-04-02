CREATE TABLE IF NOT EXISTS MPA_RATING (
    mpa_rating_id INTEGER GENERATED BY DEFAULT AS IDENTITY,
    mpa_rating_name VARCHAR(50) NOT NULL,
    CONSTRAINT mpa_pk PRIMARY KEY (mpa_rating_id),
    CONSTRAINT mpa_name UNIQUE (mpa_rating_name)
);

CREATE TABLE IF NOT EXISTS GENRES (
    genre_id INTEGER GENERATED BY DEFAULT AS IDENTITY,
    genre_name VARCHAR(100) NOT NULL,
    CONSTRAINT genres_pk PRIMARY KEY (genre_id),
    CONSTRAINT genre_name UNIQUE (genre_name)
);

CREATE TABLE IF NOT EXISTS FILMS (
    film_id BIGINT GENERATED BY DEFAULT AS IDENTITY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    release_date DATE,
    duration BIGINT,
    mpa_rating_id INTEGER,
    CONSTRAINT films_pk PRIMARY KEY (film_id),
    FOREIGN KEY (mpa_rating_id) REFERENCES MPA_RATING (mpa_rating_id)
);

CREATE TABLE IF NOT EXISTS FILM_GENRES (
    film_id BIGINT NOT NULL,
    genre_id INTEGER NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    CONSTRAINT film_genres_pk FOREIGN KEY (film_id) REFERENCES FILMS (film_id),
    CONSTRAINT genre_pk FOREIGN KEY (genre_id) REFERENCES GENRES (genre_id)
);

CREATE TABLE IF NOT EXISTS USERS (
    user_id BIGINT GENERATED BY DEFAULT AS IDENTITY,
    email VARCHAR(100) NOT NULL,
    login VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    birthday DATE,
    CONSTRAINT users_pk PRIMARY KEY (user_id),
    CONSTRAINT user_email UNIQUE (email),
    CONSTRAINT user_login UNIQUE (login)
);

CREATE TABLE IF NOT EXISTS LIKES (
    film_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (film_id, user_id),
    CONSTRAINT film_pk FOREIGN KEY (film_id) REFERENCES FILMS (film_id),
    CONSTRAINT user_pk FOREIGN KEY (user_id) REFERENCES USERS (user_id)
);

CREATE TABLE IF NOT EXISTS USER_FRIENDS (
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, friend_id),
    CONSTRAINT user_fr_pk FOREIGN KEY (user_id) REFERENCES USERS (user_id),
    CONSTRAINT friend_id FOREIGN KEY (user_id) REFERENCES USERS (user_id)
);
