MERGE INTO mpa_rating (mpa_rating_id, mpa_rating_name)
    VALUES (1, 'G'),
    (2, 'PG'),
    (3, 'PG-13'),
    (4, 'R'),
    (5, 'NC-17');

MERGE INTO genres (genre_id, genre_name)
    VALUES (1, 'Комедия'),
    (2, 'Драма'),
    (3, 'Мультфильм'),
    (4, 'Триллер'),
    (5, 'Документальный'),
    (6, 'Боевик');

MERGE INTO films (film_id, name, description, release_date, duration, mpa_rating_id)
    VALUES (1, 'FIRST MOVIE', 'DESCRIPTION LESS THAN 200', '2000-01-01', 100, 1),
    (2, 'SECOND MOVIE', 'NEVER GONNA GIVE YUO UP', '2010-01-01', 200, 2),
    (3, 'THIRD MOVIE', 'EMPIRE STRIKES BACK', '2020-01-01', 300, 3),
    (4, 'FOURTH MOVIE', 'DEVOCHKA WENSDAY FROM LAST PARTA', '2020-01-01', 400, 4);

MERGE INTO users (user_id, email, login, name, birthday)
    VALUES (1, 'first@yandex.ru', 'PERFORMANCE ARTIST', 'VAN', '2000-01-01'),
    (2, 'second@yandex.ru', 'GYM BOSS', 'BILLY', '2010-01-01'),
    (3, 'third@yandex.ru', 'HYPNO DANCER', 'RICARDO MILOS','2020-01-01'),
    (4, 'fourth@yandex.ru', 'OXXXYMIRON', 'MARK', '1990-01-01');