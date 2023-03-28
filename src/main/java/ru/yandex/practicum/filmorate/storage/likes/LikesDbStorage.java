package ru.yandex.practicum.filmorate.storage.likes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component("likesDbStorage")
public class LikesDbStorage implements LikesStorage {
    private final JdbcTemplate jdbcTemplate;

    public LikesDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(long filmId, long userId) {
        String sql = "INSERT INTO LIKES (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("Пользователь с ID: {} добавил лайк фильму с ID: {}.", filmId, userId);
    }

    @Override
    public void remove(long filmId, long userId) {
        String sql = "DELETE FROM LIKES WHERE user_id = ? AND film_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public Collection<Long> findAll(long filmId) {
        String sql = "SELECT * FROM LIKES WHERE FILM_ID = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sql, filmId);
        Set<Long> likes = new LinkedHashSet<>();
            if (userRows.next()) {
                likes.add(userRows.getLong("user_id"));
            }
        return likes;
    }

    public Collection<Long> getPopular(int count) {
        String sql = "SELECT FILM_ID, FROM LIKES " +
                "GROUP BY FILM_ID " +
                "ORDER BY COUNT(USER_ID) DESC LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("film_id"), count);
    }

}
