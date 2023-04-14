package ru.yandex.practicum.filmorate.storage.likes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
        log.info("Пользователь с ID: {} добавил лайк фильму с ID: {}.", userId, filmId);
    }

    @Override
    public void remove(long filmId, long userId) {
        String sql = "DELETE FROM LIKES WHERE user_id = ? AND film_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void remove(long userId) {
        String sql = "DELETE FROM LIKES WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);

        log.info(String.format("Удалены все лайки от пользователя с ID %d.", userId));
    }

    @Override
    public boolean isLikeExist(long filmId, long userId) {
        String sql = "SELECT * FROM LIKES WHERE film_id = ? AND user_id = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sql, filmId, userId);
        return userRows.next();
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

    @Override
    public Collection<Long> findAll() {
        String sql = "SELECT * FROM LIKES";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sql);
        Set<Long> likes = new LinkedHashSet<>();
        if (userRows.next()) {
            likes.add(userRows.getLong("user_id"));
        }
        return likes;
    }

    @Override
    public void removeAll(long filmId) {
        String sql = "DELETE FROM LIKES WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);

        log.info(String.format("Все лайки фильма с ID %d успешно удалены.", filmId));
    }
}
