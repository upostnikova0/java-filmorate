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
import java.util.HashSet;

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
                "JOIN MPA_RATING M ON M.MPA_RATING_ID = FILMS.MPA_RATING_ID WHERE film_id = ?";

        Film film = jdbcTemplate.query(sql, FilmDbStorage::filmMapper, filmId).stream().findFirst().orElse(null);
        if (film == null) {
            throw new FilmNotFoundException("Фильм с ID " + filmId + " не найден.");
        }

        return film;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT * FROM FILMS " +
                "JOIN MPA_RATING M ON M.MPA_RATING_ID = FILMS.MPA_RATING_ID";
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
    public Film remove(Film film) {
        String sql = "DELETE FROM FILMS WHERE film_id = ?";
        jdbcTemplate.update(sql, film.getId());
        return film;
    }

    @Override
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        String sqlQuery = "SELECT * FROM films JOIN mpa_rating m ON m.mpa_rating_id = films.mpa_rating_id " +
                "WHERE film_id IN(SELECT film_id FROM likes WHERE user_id IN(?,?) " +
                "GROUP BY film_id HAVING COUNT(user_id) > 1)";
        return jdbcTemplate.query(sqlQuery, FilmDbStorage::filmMapper, userId, friendId);
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
}
