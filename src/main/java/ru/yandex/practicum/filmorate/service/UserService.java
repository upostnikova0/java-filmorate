package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    protected final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        checkValidity(user);
        return userStorage.add(user);
    }

    public User getUser(long id) {
        return userStorage.findUser(id);
    }

    public User update(User user) {
        checkValidity(user);
        return userStorage.update(user);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public void addFriend(long id, long friendId) {
        User user = userStorage.findUser(id);
        User friend = userStorage.findUser(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(id);
    }

    public void deleteFriend(long id, long friendId) {
        User user = userStorage.findUser(id);
        User friend = userStorage.findUser(friendId);
        friend.getFriends().remove(user.getId());
        user.getFriends().remove(friend.getId());
    }

    public Set<User> getAllFriends(long id) {
        Set<User> friends = new LinkedHashSet<>();
        User user = userStorage.findUser(id);
        user.getFriends().forEach(x -> friends.add(getUser(x)));
        return friends;
    }

    public Set<User> getCommonFriends(long id, long friendId) {
        User user = userStorage.findUser(id);
        User friend = userStorage.findUser(friendId);
        Set<User> common = new LinkedHashSet<>();
        user.getFriends().stream()
                .filter(friend.getFriends()::contains)
                .forEach(x -> common.add(getUser(x)));
        return common;
    }

    private void checkValidity(User user) {
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
        ResponseEntity.ok("valid");
    }
}
