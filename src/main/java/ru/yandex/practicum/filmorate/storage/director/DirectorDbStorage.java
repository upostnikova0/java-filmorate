package ru.yandex.practicum.filmorate.storage.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

@Slf4j
@Component("directorsDbStorage")
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Director add(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("DIRECTORS")
                .usingGeneratedKeyColumns("director_id");
        director.setId(simpleJdbcInsert.executeAndReturnKey(director.toMap()).longValue());

        log.info("Новый режиссер успешно добавлен: {}", director);
        return director;
    }

    @Override
    public Collection<Director> findAll() {
        String sqlQuery = "SELECT * FROM DIRECTORS";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> Director.builder()
                        .id(rs.getLong("director_id"))
                        .name(rs.getString("director_name"))
                        .build()
        );
    }

    @Override
    public Director find(long directorId) {
        String sqlQuery = "SELECT director_name FROM DIRECTORS WHERE director_id = ?";
        SqlRowSet directorRows = jdbcTemplate.queryForRowSet(sqlQuery, directorId);
        if (directorRows.next()) {
            return Director.builder()
                    .id(directorId)
                    .name(directorRows.getString("director_name"))
                    .build();
        } else {
            throw new DirectorNotFoundException(String.format("Режиссер с ID %d не найден.", directorId));
        }
    }

    @Override
    public Director update(Director director) {
        String sqlQuery = "UPDATE DIRECTORS SET director_name = ? WHERE director_id = ?";
        jdbcTemplate.update(sqlQuery, director.getName(), director.getId());

        log.info("Режиссер успешно обновлен: {}", director);
        return director;
    }

    @Override
    public void remove(Director director) {
        String sql = "DELETE FROM DIRECTORS WHERE director_id = ?";
        jdbcTemplate.update(sql, director.getId());

        log.info(String.format("Режиссер с ID %d удален.", director.getId()));
    }
}
