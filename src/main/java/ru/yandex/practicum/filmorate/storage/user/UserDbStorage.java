package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;

@Slf4j
@Component("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User add(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("USERS")
                .usingGeneratedKeyColumns("user_id");
        user.setId(simpleJdbcInsert.executeAndReturnKey(user.toMap()).longValue());

        log.info("Добавлен пользователь: {}", user);
        return user;
    }

    @Override
    public User findUser(long id) {
        String sql = "SELECT * FROM USERS WHERE user_id = ?";
        User user = jdbcTemplate.query(sql, UserDbStorage::userMapper, id).stream().findFirst().orElse(null);
        if (user == null) {
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден.");
        }

        return user;
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT * FROM USERS ";
        return new LinkedHashSet<>(
                jdbcTemplate.query(sql, UserDbStorage::userMapper)
        );
    }

    @Override
    public User update(User user) {
        String sqlQuery = "UPDATE USERS SET " +
                "email = ?, login = ?, name = ?, birthday = ? " +
                "WHERE user_id = ?";
        jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        log.info("Обновлен пользователь {}.", user);
        return user;
    }

    @Override
    public void remove(User user) {
        String sql = "DELETE FROM USERS WHERE user_id = ?";
        jdbcTemplate.update(sql, user.getId());

        log.info("Удален пользователь {}.", user);
    }

    public static User userMapper(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("user_id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }
}
