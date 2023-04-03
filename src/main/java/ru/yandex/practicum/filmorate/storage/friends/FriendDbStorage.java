package ru.yandex.practicum.filmorate.storage.friends;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

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

        return new ArrayList<>(
                jdbcTemplate.query(sql, FriendDbStorage::friendMapper, id)
        );
    }

    @Override
    public void remove(long id, long friendId) {
        String sql = "DELETE FROM USER_FRIENDS WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, id, friendId);
    }

    @Override
    public Collection<Long> getCommonFriends(Long userId, Long friendId) {
        String sql = "SELECT FRIEND_ID FROM USER_FRIENDS WHERE USER_ID = ? " +
                "AND FRIEND_ID IN (SELECT FRIEND_ID FROM USER_FRIENDS WHERE USER_ID = ?)";

        Collection<Long> commonFriendsId = jdbcTemplate.queryForList(sql, Long.class, userId, friendId);

        return commonFriendsId;
    }

    public void removeAll(long id) {
        String sql = "DELETE FROM USER_FRIENDS WHERE user_id = ?";
        jdbcTemplate.update(sql, id);
    }

    public static User friendMapper(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("friend_id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }
}
