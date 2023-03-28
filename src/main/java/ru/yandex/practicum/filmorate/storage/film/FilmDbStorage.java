package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

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
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM FILMS WHERE film_id = ?", filmId);
        if (filmRows.next()) {
            Film film = Film.builder()
                    .id(filmRows.getLong("film_id"))
                    .name(filmRows.getString("name"))
                    .description(filmRows.getString("description"))
                    .releaseDate(filmRows.getDate("release_date").toLocalDate())
                    .duration(filmRows.getLong("duration"))
                    .mpa(Mpa.builder().id(filmRows.getInt("mpa_rating_id")).build())
                    .build();
            log.info("Найден фильм = {}", film);
            return film;
        } else {
            log.info(String.format("Фильм с ID %d не найден.", filmId));
            throw new FilmNotFoundException(String.format("Фильм с ID %d не найден", filmId));
        }
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT * FROM FILMS ";
        return jdbcTemplate.query(sql, (rs, rowNum) -> Film.builder()
                        .id(rs.getLong("film_id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .releaseDate(rs.getDate("release_date").toLocalDate())
                        .duration(rs.getLong("duration"))
                        .mpa(Mpa.builder().id(rs.getInt("mpa_rating_id")).build())
                .build()
        );
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
}
