package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

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
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE user_id = ?", id);
        if (userRows.first()) {
            User user = new User(
                    userRows.getLong("user_id"),
                    userRows.getString("email"),
                    userRows.getString("login"),
                    userRows.getString("name"),
                    userRows.getDate("birthday").toLocalDate()
            );
            log.info(String.format("Найден пользователь с ID %d", id));
            return user;
        } else {
            log.info(String.format("Пользователь с ID %d не найден.", id));
            throw new UserNotFoundException(String.format("Пользователь с ID %d не найден.", id));
        }
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT * FROM USERS ";
        return new LinkedHashSet<>(
                jdbcTemplate.query(sql, (rs, rowNum) -> new User(
                        rs.getLong("user_id"),
                        rs.getString("email"),
                        rs.getString("login"),
                        rs.getString("name"),
                        rs.getDate("birthday").toLocalDate())
                )
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
        log.info("Удален пользователь {}.", user);
        jdbcTemplate.update(sql, user.getId());
    }
}
