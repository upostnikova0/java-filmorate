package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Slf4j
@Component("mpaDbStorage")
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage (JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Mpa getMpa(int id) {
        String sql = "SELECT * FROM MPA_RATING WHERE MPA_RATING_ID = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sql, id);
        if (userRows.next()) {
            return new Mpa(
                    userRows.getInt("mpa_rating_id"),
                    userRows.getString("mpa_rating_name")
            );
        }
        log.info(String.format("Рейтинг с ID: %d не найден.", id));
        throw new MpaNotFoundException(String.format("Рейтинг с ID: %d не найден.", id));
    }

    @Override
    public List<Mpa> getAll() {
        String sql = "SELECT * FROM MPA_RATING";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Mpa(
                rs.getInt("mpa_rating_id"),
                rs.getString("mpa_rating_name"))
        );
    }
}
