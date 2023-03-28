package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("genreDbStorage")
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Genre findGenre(int genreId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT GENRE_NAME FROM GENRES WHERE GENRE_ID = ?", genreId);
        if (userRows.next()) {
            return Genre.builder()
                    .id(genreId)
                    .name(userRows.getString("genre_name"))
                    .build();
        } else {
            throw new GenreNotFoundException(String.format("Жанр с ID %d не найден.", genreId));
        }
    }

    @Override
    public Collection<Genre> findAll() {
        String sql = "SELECT * FROM GENRES";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(
                                rs.getInt("genre_id"),
                                rs.getString("genre_name")
                        )
                ).stream()
                .sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toList());
    }
}
