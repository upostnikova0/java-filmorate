package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

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
    public void remove(User user) {
        users.remove(findUser(user.getId()).getId());
        log.info("Пользователь с ID: " + user.getId() + " удален.");
    }

    @Override
    public User update(User user) {
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
}
