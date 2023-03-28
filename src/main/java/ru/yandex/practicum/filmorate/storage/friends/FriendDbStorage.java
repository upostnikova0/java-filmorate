package ru.yandex.practicum.filmorate.storage.friends;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.LinkedHashSet;

@Slf4j
@Component("friendDbStorage")
public class FriendDbStorage implements FriendStorage {
    private final JdbcTemplate jdbcTemplate;

    public FriendDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(long userId, long friendId) {
        String sql = "INSERT INTO USER_FRIENDS (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
        log.info("Пользователю с ID {} добавлен новый друг с ID {}", userId, friendId);

    }

    @Override
    public Collection<User> findAll(long id) {
        String sql = "SELECT FRIEND_ID, EMAIL, LOGIN, NAME, BIRTHDAY FROM USER_FRIENDS JOIN USERS U " +
                "ON USER_FRIENDS.FRIEND_ID = U.USER_ID WHERE " +
                "USER_FRIENDS.USER_ID = ?";
        return new LinkedHashSet<>(
                jdbcTemplate.query(sql, (rs, rowNum) -> new User(
                        rs.getLong("friend_id"),
                        rs.getString("email"),
                        rs.getString("login"),
                        rs.getString("name"),
                        rs.getDate("birthday").toLocalDate()
                    ),
                    id
                )
        );
    }

    @Override
    public void remove(long id, long friendId) {
        String sql = "DELETE FROM USER_FRIENDS WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, id, friendId);
    }
}
