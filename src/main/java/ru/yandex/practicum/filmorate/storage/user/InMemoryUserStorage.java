package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    protected static Long globalId = 1L;
    protected final Map<Long, User> users = new HashMap<>();

    @Override
    public User add(User user) {
        checkValidity(user);

        user.setId(getNextId());

        for (Map.Entry<Long, User> entry : users.entrySet()) {
            User temp = entry.getValue();

            if (user.getEmail().equals(temp.getEmail())) {
                log.warn("Пользователь с электронной почтой " + user.getEmail() + " уже зарегистрирован.");
                throw new ValidationException(
                        "Пользователь с электронной почтой " + user.getEmail() + " уже зарегистрирован."
                );
            }
        }

        log.info("Добавлен новый пользователь c ID: " + user.getId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User remove(User user) {
        return null;
    }

    @Override
    public User update(User user) {
        checkValidity(user);

        if (!users.containsKey(user.getId())) {
            log.warn("Пользователя с ID " + user.getId() + " не существует.");
            throw new UserNotFoundException("Пользователя с ID " + user.getId() + " не существует.");
        }

        log.info("Обновлен пользователь c ID: " + user.getId());
        users.put(user.getId(), user);
        return user;
    }

    public Collection<User> findAll() {
        log.info("Количество пользователей: " + users.size());
        return users.values();
    }

    @Override
    public User findUser(long id) {
        if(users.containsKey(id)) {
            log.info("Пользователь с ID " + id + " найден.");
            return users.get(id);
        } else {
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден");
        }
    }

    private static Long getNextId() {
        return globalId++;
    }

    private ResponseEntity<String> checkValidity(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Электронная почта не может быть пустой и должна содержать символ @.");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @.");
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Логин не может быть пустым и содержать пробелы.");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы.");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if(user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения не может быть в будущем.");
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
        return ResponseEntity.ok("valid");
    }
}
