package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

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
        String sql = "SELECT * FROM USERS WHERE user_id = ? AND DELETED = FALSE";
        User user = jdbcTemplate.query(sql, UserDbStorage::userMapper, id).stream().findFirst().orElse(null);
        if (user == null) {
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден.");
        }

        return user;
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT * FROM USERS WHERE DELETED = FALSE";
        return new LinkedHashSet<>(
                jdbcTemplate.query(sql, UserDbStorage::userMapper)
        );
//        String sql = "SELECT * FROM USERS ";
//        return new LinkedHashSet<>(
//                jdbcTemplate.query(sql, UserDbStorage::userMapper)
//        );
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
        String sqlQuery = "UPDATE USERS SET " +
                "deleted = true " +
                "WHERE user_id = ?";
        jdbcTemplate.update(sqlQuery,
                user.getId());
        log.info("Удален пользователь {}.", user);
//        String sql = "DELETE FROM USERS WHERE user_id = ?";
//        jdbcTemplate.update(sql, user.getId());
//
//        log.info("Удален пользователь {}.", user);
    }

    @Override
    public Collection<Film> getRecommendations(long id) {
        String sqlQuery = "SELECT f.film_id, f.name, f.description, f.duration, f.release_date, f.mpa_rating_id, " +
                "r.mpa_rating_name FROM films as f \n" +
                "LEFT JOIN LIKES ON f.FILM_ID = LIKES.FILM_ID \n" +
                "LEFT JOIN MPA_RATING r ON r.mpa_rating_id = f.mpa_rating_id \n" +
                "WHERE LIKES.USER_ID IN (\n" +
                "SELECT USER_ID FROM LIKES fl \n" +
                "WHERE FILM_ID IN (SELECT FILM_ID FROM LIKES WHERE USER_ID = ?)AND USER_ID != ? \n" +
                "GROUP BY USER_ID \n" +
                "HAVING COUNT(FILM_ID) = (SELECT MAX(films_number) \n" +
                "FROM (\n" +
                "SELECT COUNT(FILM_ID) AS films_number \n" +
                "FROM LIKES \n" +
                "WHERE FILM_ID IN (SELECT FILM_ID FROM LIKES WHERE USER_ID = ?) AND USER_ID != ?\n" +
                "GROUP BY USER_ID)\n" +
                ")\n" +
                "ORDER BY COUNT(FILM_ID) DESC\n" +
                ") AND f.FILM_ID NOT IN (SELECT FILM_ID FROM LIKES fl2 WHERE USER_ID = ?)\n" +
                "ORDER BY f.FILM_ID";

        return jdbcTemplate.query(sqlQuery, FilmDbStorage::filmMapper, id, id, id, id, id);
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
