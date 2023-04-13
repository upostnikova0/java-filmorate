package ru.yandex.practicum.filmorate.storage.filmdirectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component("filmDirectorsDbStorage")
public class FilmDirectorsDbStorage implements FilmDirectorsStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDirectorsDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addDirector(long filmId, long directorId) {
        String sql = "INSERT INTO FILM_DIRECTORS (film_id, director_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, directorId);
        log.info("Фильму с ID {} добавлен новый режиссер с ID {}", filmId, directorId);
    }

    @Override
    public void addDirectorList(long filmId, Collection<Director> directors) {
        final ArrayList<Director> directorList = new ArrayList<>(directors);
        jdbcTemplate.batchUpdate(
                "INSERT INTO FILM_DIRECTORS (film_id, director_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, filmId);
                        ps.setLong(2, directorList.get(i).getId());
                    }

                    public int getBatchSize() {
                        return directorList.size();
                    }
                });
    }

    @Override
    public Director findDirector(long filmId, long directorId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "SELECT DIRECTOR_ID = ? FROM FILM_DIRECTORS JOIN DIRECTORS " +
                        "ON FILM_DIRECTORS.DIRECTOR_ID = DIRECTORS.DIRECTOR_ID " +
                        "WHERE FILM_ID = ?",
                directorId,
                filmId
        );
        if (userRows.next()) {
            Director director = Director.builder()
                    .id(directorId)
                    .name(userRows.getString("director_name"))
                    .build();

            log.info(String.format("Режиссер с ID %d у фильма с ID %d найден.", directorId, filmId));
            return director;
        } else {
            throw new DirectorNotFoundException(String.format("Режиссер с ID %d у фильма с ID %d не найден.", directorId, filmId));
        }
    }

    @Override
    public Collection<Director> findAll(long filmId) {
        String sql = "SELECT * FROM FILM_DIRECTORS JOIN DIRECTORS " +
                "ON FILM_DIRECTORS.DIRECTOR_ID = DIRECTORS.DIRECTOR_ID " +
                "WHERE FILM_ID = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> Director.builder()
                        .id(rs.getLong("director_id"))
                        .name(rs.getString("director_name"))
                        .build(),
                filmId
        );
    }

    @Override
    public List<Map<Long, Director>> findAll() {
        String sql = "SELECT * FROM FILM_DIRECTORS JOIN DIRECTORS " +
                "ON FILM_DIRECTORS.DIRECTOR_ID = DIRECTORS.DIRECTOR_ID ";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<Long, Director> result = new LinkedHashMap<>();
            result.put(rs.getLong("film_id"),
                    Director.builder()
                            .id(rs.getLong("director_id"))
                            .name(rs.getString("director_name"))
                            .build());
            return result;
        });
    }

    @Override
    public void update(Film film) {
        jdbcTemplate.update("DELETE FROM FILM_DIRECTORS WHERE film_id = ?", film.getId());

        for (Director director : film.getDirectors()) {
            jdbcTemplate.update("INSERT INTO FILM_DIRECTORS (film_id, director_id) VALUES (?, ?)",
                    film.getId(), director.getId());
        }
    }

    @Override
    public void remove(long filmId, long directorId) {
        String sql = "DELETE FROM FILM_DIRECTORS WHERE film_id = ? AND director_id = ?";
        jdbcTemplate.update(sql, filmId, directorId);
    }

    @Override
    public void removeAll(long filmId) {
        String sql = "DELETE FROM FILM_DIRECTORS WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    @Override
    public void removeAllByDirector(long directorId) {
        String sql = "DELETE FROM FILM_DIRECTORS WHERE director_id = ?";
        jdbcTemplate.update(sql, directorId);
    }

    @Override
    public Collection<Film> getDirectorFilmsByLikes(long directorId) {
        String sql = "SELECT films.*, mpa_rating.mpa_rating_name, COUNT(likes.user_id) AS rate " +
                "FROM films " +
                "LEFT JOIN mpa_rating ON films.mpa_rating_id = mpa_rating.mpa_rating_id " +
                "LEFT JOIN likes ON films.film_id = likes.film_id " +
                "INNER JOIN film_directors fd on films.film_id = fd.film_id " +
                "WHERE fd.director_id = ? " +
                "GROUP BY films.film_id " +
                "ORDER BY rate";
        return jdbcTemplate.query(sql, FilmDbStorage::filmMapper, directorId);
    }

    @Override
    public Collection<Film> getDirectorFilmsByYear(long directorId) {
        String sql = "SELECT films.*, mpa_rating.mpa_rating_name " +
                "FROM films " +
                "LEFT JOIN mpa_rating ON films.mpa_rating_id = mpa_rating.mpa_rating_id " +
                "INNER JOIN film_directors AS fd on films.film_id = fd.film_id " +
                "WHERE fd.director_id = ? " +
                "GROUP BY films.film_id " +
                "ORDER BY films.release_date";
        return jdbcTemplate.query(sql, FilmDbStorage::filmMapper, directorId);
    }
}
