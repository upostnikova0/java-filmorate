package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.filmgenres.FilmGenresStorage;
import ru.yandex.practicum.filmorate.storage.friends.FriendStorage;
import ru.yandex.practicum.filmorate.storage.likes.LikesStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendStorage friendStorage;
    private final EventStorage eventStorage;
    private final FilmGenresStorage filmGenresStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("friendDbStorage") FriendStorage friendStorage,
                       @Qualifier("eventDbStorage") EventStorage eventStorage,
                       @Qualifier("filmGenresDbStorage") FilmGenresStorage filmGenresStorage) {
        this.userStorage = userStorage;
        this.friendStorage = friendStorage;
        this.eventStorage = eventStorage;
        this.filmGenresStorage = filmGenresStorage;
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

    public void remove(long userId) {
        User user = findUser(userId);

        userStorage.remove(user);
    }

    public void addFriend(long userId, long friendId) {
        userStorage.findUser(userId);
        userStorage.findUser(friendId);

        if (!friendStorage.isFriendsExist(userId, friendId)) {
            friendStorage.add(userId, friendId);

            eventStorage.add(Event.builder()
                    .timestamp(System.currentTimeMillis())
                    .userId(userId)
                    .eventType(EventType.FRIEND)
                    .operation(OperationType.ADD)
                    .entityId(friendId)
                    .build());
        }
    }

    public Collection<User> getAllFriends(long id) {
        userStorage.findUser(id);
        return friendStorage.findAll(id);
    }

    public void deleteFriend(long id, long friendId) {
        userStorage.findUser(id);
        userStorage.findUser(friendId);

        if (friendStorage.isFriendsExist(id, friendId)) {
            log.info(String.format("Пользователь с ID %d удалил из друзей пользователя с ID %d", id, friendId));

            friendStorage.remove(id, friendId);

            eventStorage.add(Event.builder()
                    .timestamp(System.currentTimeMillis())
                    .userId(id)
                    .eventType(EventType.FRIEND)
                    .operation(OperationType.REMOVE)
                    .entityId(friendId)
                    .build());
        }
    }

    public Collection<User> getCommonFriends(long id, long friendId) {
        userStorage.findUser(id);
        userStorage.findUser(friendId);

        Collection<Long> commonFriendsId = friendStorage.getCommonFriends(id, friendId);
        Collection<User> users = userStorage.findAll();

        List<User> commonFriends = new ArrayList<>();

        for (Long userId : commonFriendsId) {
            for (User user : users) {
                if (user.getId().equals(userId)) {
                    commonFriends.add(user);
                }
            }
        }

        return commonFriends;
    }

    public Collection<Event> getFeed(long id) {
        findUser(id);
        return eventStorage.findAll(id);
    }

    public Collection<Film> getRecommendations(long id) {
        findUser(id);
        Collection<Film> recommendFilms = userStorage.getRecommendations(id);
        List<Map<Long, Genre>> allGenres = filmGenresStorage.findAll();

        if (allGenres != null) {
            for (Map<Long, Genre> map : allGenres) {
                for (Film film : recommendFilms) {
                    if (map.containsKey(film.getId())) {
                        film.getGenres().add(map.get(film.getId()));
                    }
                }
            }
        }
        return recommendFilms;
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

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения не может быть в будущем.");
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
        ResponseEntity.ok("valid");
    }
}