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

    @Override
    public Collection<Film> searchFilms(Optional<String> query, List<String> by) {
        List<Film> searchedFilms = new ArrayList<>();
        if (query.get().isEmpty() || query.isEmpty() || query.get().equals(" ")) {
            return searchedFilms;
        }
        String stringInSql = query.get().toLowerCase();
        String searchFilmsSqlByName = "select f.FILM_ID\n" +
                "  ,f.NAME\n" +
                "  ,f.DESCRIPTION \n" +
                "  ,f.RELEASE_DATE \n" +
                "  ,f.DURATION \n" +
                "  ,f.RATE\n" +
                "  ,rm.RATING_ID\n" +
                "  ,rm.RATING_NAME\n" +
                "  ,GROUP_CONCAT(DISTINCT Concat(g.GENRE_ID,'-',g.GENRE_NAME) ORDER BY Concat(g.GENRE_ID,'-',g.GENRE_NAME)) AS GENRE_ID_NAME\n" +
                "  ,GROUP_CONCAT(DISTINCT Concat(d.DIRECTOR_ID, '-', d.NAME) ORDER BY Concat(d.DIRECTOR_ID, '-', d.NAME)) AS DIRECTOR_ID_NAME\n" +
                "from (\n" +
                "  SELECT fi.* \n" +
                "        FROM FILMS fi \n" +
                "        LEFT JOIN \n" +
                "        (SELECT FILM_ID,COUNT(*) cLike \n" +
                "            FROM FILMS_LIKE \n" +
                "            GROUP BY FILM_ID\n" +
                "        ) fil \n" +
                "        ON fil.FILM_ID = fi.FILM_ID \n" +
                "        ORDER BY clike DESC\n" +
                ") f\n" +
                "LEFT JOIN RATINGS_MPA rm  ON f.RATING_ID =rm.RATING_ID \n" +
                "LEFT JOIN FILMS_GENRE fg ON f.FILM_ID =fg.FILM_ID \n" +
                "LEFT JOIN GENRE g ON fg.GENRE_ID =g.GENRE_ID\n" +
                "LEFT JOIN FILMS_DIRECTOR fd ON f.FILM_ID = fd.FILM_ID\n" +
                "LEFT JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID\n" +
                "WHERE LOWER(f.NAME) LIKE '%" + stringInSql + "%'\n" +
                "GROUP BY f.FILM_ID;";
        String searchFilmsSqlByDirector = "select f.FILM_ID\n" +
                "  ,f.NAME\n" +
                "  ,f.DESCRIPTION \n" +
                "  ,f.RELEASE_DATE \n" +
                "  ,f.DURATION \n" +
                "  ,f.RATE\n" +
                "  ,rm.RATING_ID\n" +
                "  ,rm.RATING_NAME\n" +
                "  ,GROUP_CONCAT(DISTINCT Concat(g.GENRE_ID,'-',g.GENRE_NAME) ORDER BY Concat(g.GENRE_ID,'-',g.GENRE_NAME)) AS GENRE_ID_NAME\n" +
                "  ,GROUP_CONCAT(DISTINCT Concat(d.DIRECTOR_ID, '-', d.NAME) ORDER BY Concat(d.DIRECTOR_ID, '-', d.NAME)) AS DIRECTOR_ID_NAME\n" +
                "from (\n" +
                "  SELECT fi.* \n" +
                "        FROM FILMS fi \n" +
                "        LEFT JOIN \n" +
                "        (SELECT FILM_ID,COUNT(*) cLike \n" +
                "            FROM FILMS_LIKE \n" +
                "            GROUP BY FILM_ID\n" +
                "        ) fil \n" +
                "        ON fil.FILM_ID = fi.FILM_ID \n" +
                "        ORDER BY clike DESC\n" +
                ") f\n" +
                "LEFT JOIN RATINGS_MPA rm  ON f.RATING_ID =rm.RATING_ID \n" +
                "LEFT JOIN FILMS_GENRE fg ON f.FILM_ID =fg.FILM_ID \n" +
                "LEFT JOIN GENRE g ON fg.GENRE_ID =g.GENRE_ID\n" +
                "LEFT JOIN FILMS_DIRECTOR fd ON f.FILM_ID = fd.FILM_ID\n" +
                "LEFT JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID\n" +
                "WHERE LOWER(d.NAME) LIKE '%" + stringInSql + "%'\n" +
                "GROUP BY f.FILM_ID;";
        String searchFilmsSqlByAll = "select f.FILM_ID\n" +
                "  ,f.NAME\n" +
                "  ,f.DESCRIPTION \n" +
                "  ,f.RELEASE_DATE \n" +
                "  ,f.DURATION \n" +
                "  ,f.RATE\n" +
                "  ,rm.RATING_ID\n" +
                "  ,rm.RATING_NAME\n" +
                "  ,GROUP_CONCAT(DISTINCT Concat(g.GENRE_ID,'-',g.GENRE_NAME) ORDER BY Concat(g.GENRE_ID,'-',g.GENRE_NAME)) AS GENRE_ID_NAME\n" +
                "  ,GROUP_CONCAT(DISTINCT Concat(d.DIRECTOR_ID, '-', d.NAME) ORDER BY Concat(d.DIRECTOR_ID, '-', d.NAME)) AS DIRECTOR_ID_NAME\n" +
                "from (\n" +
                "  SELECT fi.* \n" +
                "        FROM FILMS fi \n" +
                "        LEFT JOIN \n" +
                "        (SELECT FILM_ID,COUNT(*) cLike \n" +
                "            FROM FILMS_LIKE \n" +
                "            GROUP BY FILM_ID\n" +
                "        ) fil \n" +
                "        ON fil.FILM_ID = fi.FILM_ID \n" +
                "        ORDER BY clike DESC\n" +
                ") f\n" +
                "LEFT JOIN RATINGS_MPA rm  ON f.RATING_ID =rm.RATING_ID \n" +
                "LEFT JOIN FILMS_GENRE fg ON f.FILM_ID =fg.FILM_ID \n" +
                "LEFT JOIN GENRE g ON fg.GENRE_ID =g.GENRE_ID\n" +
                "LEFT JOIN FILMS_DIRECTOR fd ON f.FILM_ID = fd.FILM_ID\n" +
                "LEFT JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID\n" +
                "WHERE LOWER(f.NAME) LIKE '%" + stringInSql + "%'\n" +
                "OR LOWER(d.NAME) LIKE '%" + stringInSql + "%'\n" +
                "GROUP BY f.FILM_ID;";

        if (by != null) {
            log.debug("Получен запрос с параметром by");
            if (by.size() == 1 & by.contains("title")) {
                log.debug("Получен запрос на поиск фильма по названию");
                return getSearchedFilms(searchedFilms, searchFilmsSqlByName);
            }
            if (by.size() == 1 & by.contains("director")) {
                log.debug("Получен запрос на поиск фильма по имени режиссера");
                return getSearchedFilms(searchedFilms, searchFilmsSqlByDirector);
            }
            if (by.size() == 2 & by.contains("title") & by.contains("director")) {
                log.debug("Получен запрос на поиск фильма по имени режиссера и по названию фильма");
                return getSearchedFilms(searchedFilms, searchFilmsSqlByAll);
            }
            if (by.size() >= 2 & (!by.contains("title") || !by.contains("directors"))) {
                throw new IllegalArgumentException("Передан некорректный параметр by!");
            }
        }
        log.debug("Получен запрос без параметра by, выполнен поиск по умолчанию");
        return getSearchedFilms(searchedFilms, searchFilmsSqlByName);
    }

    private List<Film> getSearchedFilms(List<Film> searchedFilms, String sql) {
        searchedFilms = jdbcTemplate.query(sql, FilmDbStorage::filmMapper);
        log.debug("Результаты поиска:");
        for (Film film : searchedFilms) {
            log.debug("Фильм с film_id={}: {}", film.getId(), film);
        }
        return searchedFilms;
    }
}
