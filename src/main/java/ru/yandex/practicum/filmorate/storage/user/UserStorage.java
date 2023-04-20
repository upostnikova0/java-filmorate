package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User add(User user);

    User findUser(long id);

    Collection<User> findAll();

    User update(User user);

    void remove(User user);

    Collection<Film> getRecommendations(long id);
}