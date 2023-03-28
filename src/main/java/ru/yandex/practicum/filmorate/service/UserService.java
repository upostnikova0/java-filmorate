package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friends.FriendStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    protected final UserStorage userStorage;
    private final FriendStorage friendStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, @Qualifier("friendDbStorage") FriendStorage friendStorage) {
        this.userStorage = userStorage;
        this.friendStorage = friendStorage;
    }

    public User create(User user) {
        checkValidity(user);
        return userStorage.add(user);
    }

    public User findUser(long id) {
        return userStorage.findUser(id);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User update(User user) {
        checkValidity(user);
        findUser(user.getId());
        return userStorage.update(user);
    }

    public void removeUser(User user) {
        userStorage.findUser(user.getId());
        userStorage.remove(user);
        friendStorage.remove(user.getId(), user.getId());
    }

    public void addFriend(long userId, long friendId) {
        userStorage.findUser(userId);
        userStorage.findUser(friendId);
        friendStorage.add(userId, friendId);
    }

    public Collection<User> getAllFriends(long id) {
        userStorage.findUser(id);
        return friendStorage.findAll(id);
    }

    public void deleteFriend(long id, long friendId) {
        userStorage.findUser(id);
        userStorage.findUser(friendId);
        friendStorage.remove(id, friendId);
    }

    public Collection<User> getCommonFriends(long id, long friendId) {
        userStorage.findUser(id);
        userStorage.findUser(friendId);
        return friendStorage.findAll(id)
                .stream()
                .filter(x -> friendStorage.findAll(friendId).contains(x))
                .collect(Collectors.toList());
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
