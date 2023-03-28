package ru.yandex.practicum.filmorate.storage.film_genres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Slf4j
@Component("filmGenresDbStorage")
public class FilmGenresDbStorage implements FilmGenresStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmGenresDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addGenre(long filmId, int genreId) {
        String sql = "INSERT INTO FILM_GENRES (film_id, genre_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, genreId);
        log.info("Фильму с ID {} добавлен новый жанр с ID {}", filmId, genreId);
    }

    @Override
    public Genre findGenre(long filmId, int genreId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "SELECT GENRE_ID = ? FROM FILM_GENRES JOIN GENRES " +
                "ON FILM_GENRES.GENRE_ID = GENRES.GENRE_ID " +
                "WHERE FILM_ID = ?",
                genreId,
                filmId
        );
        if (userRows.next()) {
            Genre genre = new Genre(
                    genreId,
                    userRows.getString("genre_name")
            );
            log.info(String.format("Жанр с ID %d у фильма с ID %d найден.", genreId, filmId));
            return genre;
        } else {
            throw new GenreNotFoundException(String.format("Жанр с ID %d у фильма с ID %d не найден.", genreId, filmId));
        }
    }

    @Override
    public Collection<Genre> findAll(long filmId) {
        String sql = "SELECT * FROM FILM_GENRES JOIN GENRES " +
                "ON FILM_GENRES.GENRE_ID = GENRES.GENRE_ID " +
                "WHERE FILM_ID = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(
                rs.getInt("genre_id"),
                rs.getString("genre_name")),
                filmId
        );
    }

    @Override
    public void update(Film film) {
        jdbcTemplate.update("DELETE FROM FILM_GENRES WHERE film_id = ?", film.getId());

        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update("INSERT INTO FILM_GENRES (film_id, genre_id) VALUES (?, ?)",
                    film.getId(), genre.getId());
        }
    }

    @Override
    public void remove(long filmId, int genreId) {
        String sql = "DELETE FROM FILM_GENRES WHERE film_id = ? AND genre_id = ?";
        jdbcTemplate.update(sql, filmId, genreId);
    }

    @Override
    public void removeAll(long filmId) {
        String sql = "DELETE FROM FILM_GENRES WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }
}
