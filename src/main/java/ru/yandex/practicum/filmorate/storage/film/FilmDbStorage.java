package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film add(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("film_id");
        film.setId(simpleJdbcInsert.executeAndReturnKey(film.toMap()).longValue());

        log.info("Добавлен новый фильм: {}", film);
        return film;
    }

    @Override
    public Film findFilm(long filmId) {
        String sql = "SELECT * FROM FILMS " +
                "JOIN MPA_RATING M ON M.MPA_RATING_ID = FILMS.MPA_RATING_ID WHERE film_id = ? AND films.deleted = false";

        Film film = jdbcTemplate.query(sql, FilmDbStorage::filmMapper, filmId).stream().findFirst().orElse(null);
        if (film == null) {
            throw new FilmNotFoundException("Фильм с ID " + filmId + " не найден.");
        }

        return film;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT * FROM FILMS " +
                "JOIN MPA_RATING M ON M.MPA_RATING_ID = FILMS.MPA_RATING_ID AND films.deleted = false";
        return jdbcTemplate.query(sql, FilmDbStorage::filmMapper);
    }

    @Override
    public Film update(Film film, int mpaId) {
        String sqlQuery = "UPDATE FILMS SET " +
                "name = ?, description = ?, release_date = ?, duration = ?, " +
                "mpa_rating_id = ? WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpaId,
                film.getId()
        );

        log.info("Фильм успешно обновлен: {}", film);
        return film;
    }

    @Override
    public void remove(Film film) {
        String sqlQuery = "UPDATE FILMS SET " +
                "deleted = true WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery,
                film.getId()
        );

        log.info(String.format("Фильм с ID %d успешно удален.", film.getId()));
    }

    @Override
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        String sqlQuery = "SELECT * FROM films JOIN mpa_rating m ON m.mpa_rating_id = films.mpa_rating_id " +
                "WHERE film_id IN(SELECT film_id FROM likes WHERE user_id IN(?,?) " +
                "GROUP BY film_id HAVING COUNT(user_id) > 1)";
        return jdbcTemplate.query(sqlQuery, FilmDbStorage::filmMapper, userId, friendId);
    }

    @Override
    public Collection<Film> getPopular(int count, int genreId, int year) {
        String sqlQuery = "SELECT f.film_id, f.name, f.description, f.duration, f.release_date, f.mpa_rating_id, " +
                "m.mpa_rating_name FROM films as f " +
                "LEFT JOIN likes as l ON f.film_id = l.film_id " +
                "LEFT JOIN MPA_RATING as M ON M.MPA_RATING_ID = f.MPA_RATING_ID " +
                "WHERE f.deleted = false " +
                "GROUP BY l.film_id, f.film_id " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";

        if (genreId != 0 && year != 0) {
            sqlQuery = "SELECT f.film_id, f.name, f.description, f.duration, f.release_date, f.mpa_rating_id, " +
                    "m.mpa_rating_name FROM films as f " +
                    "LEFT JOIN likes as l ON f.film_id = l.film_id " +
                    "LEFT JOIN MPA_RATING AS M ON M.MPA_RATING_ID = f.MPA_RATING_ID " +
                    "WHERE EXTRACT(YEAR FROM f.release_date) = ? AND f.film_id IN (" +
                    "SELECT film_id FROM film_genres WHERE genre_id = ?) " +
                    "GROUP BY l.film_id, f.film_id " +
                    "ORDER BY COUNT(l.user_id) DESC " +
                    "LIMIT ?";

            return jdbcTemplate.query(sqlQuery, FilmDbStorage::filmMapper, year, genreId, count);
        }

        if (genreId != 0 || year != 0) {
            sqlQuery = "SELECT f.film_id, f.name, f.description, f.duration, f.release_date, f.mpa_rating_id, " +
                    "m.mpa_rating_name FROM films as f " +
                    "LEFT JOIN likes as l ON f.film_id = l.film_id " +
                    "LEFT JOIN MPA_RATING M ON M.MPA_RATING_ID = f.MPA_RATING_ID " +
                    "WHERE EXTRACT(YEAR FROM release_date) = ? OR f.film_id IN " +
                    "(SELECT film_id FROM film_genres WHERE genre_id = ?) " +
                    "GROUP BY l.film_id, f.film_id " +
                    "ORDER BY COUNT(l.user_id) DESC " +
                    "LIMIT ?";

            return jdbcTemplate.query(sqlQuery, FilmDbStorage::filmMapper, year, genreId, count);
        }

        return jdbcTemplate.query(sqlQuery, FilmDbStorage::filmMapper, count);
    }

    public static Film filmMapper(ResultSet rs, int rowNum) throws SQLException {
        return Film.builder()
                .id(rs.getLong("film_id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getLong("duration"))
                .mpa(Mpa.builder()
                        .id(rs.getInt("mpa_rating_id"))
                        .name(rs.getString("mpa_rating_name"))
                        .build())
                .genres(new ArrayList<>())
                .directors(new ArrayList<>())
                .build();
    }

    public List<Film> getFilmSearch(String query, String by) {
        /**
         * 1. вернуть фильмы по кол-ву лайков
         * 2. разобраться как поисковую подстроку из запроса засунуть в %%
         * 3. вернуть нормально список популярных фильмов или что там надо вернуть, если
         * by.isEmpty()*/
        String queryToLowerCase = query.toLowerCase();
        String sqlQuery = "";
        List<Film> films = new ArrayList<>();
        try {
            if (by.equals("title")) {
                log.info("поиск по названию");

                sqlQuery = "SELECT DISTINCT FILMS.film_id, FILMS.name, FILMS.description, FILMS.release_date, FILMS.duration, FILMS.mpa_rating_id, MR.MPA_RATING_NAME\n" +
                        "FROM FILMS\n" +
                        "LEFT JOIN MPA_RATING MR ON FILMS.MPA_RATING_ID = MR.MPA_RATING_ID\n" +
                        "LEFT JOIN LIKES ON FILMS.film_id = LIKES.film_id\n" +
                        "WHERE LOWER(FILMS.name) LIKE ('%update%')\n" +
                        "GROUP BY FILMS.film_id";
                films.addAll(jdbcTemplate.query(sqlQuery, FilmDbStorage::filmMapper));

                log.info("title: {}", films);
            } else if (by.equals("director")) {
                log.info("поиск по режисеру");

                sqlQuery = "SELECT DISTINCT FILMS.film_id, FILMS.name, FILMS.description, FILMS.release_date, FILMS.duration, FILMS.mpa_rating_id, MR.MPA_RATING_NAME\n" +
                        "FROM FILMS\n" +
                        "LEFT JOIN MPA_RATING MR ON FILMS.MPA_RATING_ID = MR.MPA_RATING_ID\n" +
                        "LEFT JOIN LIKES ON FILMS.film_id = LIKES.film_id\n" +
                        "LEFT JOIN FILM_DIRECTORS ON FILM_DIRECTORS.film_id = FILMS.film_id\n" +
                        "LEFT JOIN DIRECTORS ON DIRECTORS.director_id = FILM_DIRECTORS.director_id\n" +
                        "WHERE LOWER(DIRECTORS.director_name) LIKE ('%update%')\n" +
                        "GROUP BY FILMS.film_id , FILM_DIRECTORS.director_id\n";
                films.addAll(jdbcTemplate.query(sqlQuery, FilmDbStorage::filmMapper));

                log.info("director: {}", films);
            } else if (by.equals("title,director") || by.equals("director,title")) {
                log.info("поиск и то и другое");

                sqlQuery = "SELECT DISTINCT FILMS.film_id, FILMS.name, FILMS.description, FILMS.release_date, FILMS.duration, FILMS.mpa_rating_id, MR.MPA_RATING_NAME \n" +
                        "FROM FILMS\n" +
                        "LEFT JOIN MPA_RATING MR ON FILMS.MPA_RATING_ID = MR.MPA_RATING_ID \n" +
                        "LEFT JOIN LIKES ON FILMS.film_id = LIKES.film_id\n" +
                        "LEFT JOIN FILM_DIRECTORS ON FILM_DIRECTORS.film_id = FILMS.film_id\n" +
                        "LEFT JOIN DIRECTORS ON DIRECTORS.director_id = FILM_DIRECTORS.director_id\n" +
                        "WHERE LOWER(FILMS.name) LIKE ('%update%') --AND FILMS.DELETED = FALSE\n" +
                        "OR LOWER(DIRECTORS.director_name) LIKE ('%update%')\n" +
                        "GROUP BY FILMS.film_id, FILM_DIRECTORS.director_id";
                films.addAll(jdbcTemplate.query(sqlQuery, FilmDbStorage::filmMapper));

                log.info("title & director: {}", films);
            } else if (by.isEmpty()) {

                sqlQuery = "SELECT DISTINCT FILMS.film_id, FILMS.name, FILMS.description, FILMS.release_date, FILMS.duration, FILMS.mpa_rating_id, MR.MPA_RATING_NAME\n" +
                        "FROM FILMS\n" +
                        "LEFT JOIN MPA_RATING MR ON FILMS.MPA_RATING_ID = MR.MPA_RATING_ID\n" +
                        "LEFT JOIN LIKES ON FILMS.film_id = LIKES.film_id\n" +
                        "GROUP BY FILMS.film_id";
                films.addAll(jdbcTemplate.query(sqlQuery, FilmDbStorage::filmMapper));
            }

            if (films.isEmpty()) {
                throw new FilmNotFoundException("По данному запросу не найден ни один фильм");
            }
        } catch (Exception e) {
            e.getMessage();
        }
        return films;
    }

}