package ru.yandex.practicum.filmorate.storage.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Slf4j
@Component("eventDbStorage")
public class EventDbStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;

    public EventDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Event add(Event event) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("EVENTS")
                .usingGeneratedKeyColumns("event_id");
        event.setEventId(simpleJdbcInsert.executeAndReturnKey(event.toMap()).longValue());

        log.info("Добавлено новое событие: {}", event);
        return event;
    }

    @Override
    public Collection<Event> findAll(long userId) {
        String sql = "SELECT * FROM EVENTS WHERE USER_ID = ?"
                + "ORDER BY EVENT_ID";

        return jdbcTemplate.query(sql, EventDbStorage::eventMapper, userId);
    }

    @Override
    public void remove(long filmId, long userId) {
        String sql = "DELETE FROM EVENTS WHERE film_id = ? AND USER_id = ?";
        jdbcTemplate.update(sql, filmId, userId);


    }

    public static Event eventMapper(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder()
                .eventId(rs.getLong("event_id"))
                .timestamp(rs.getLong("timestamp"))
                .userId(rs.getLong("user_id"))
                .eventType(EventType.valueOf(rs.getString("event_type")))
                .operation(OperationType.valueOf(rs.getString("operation")))
                .entityId(rs.getLong("entity_id"))
                .build();
    }
}
