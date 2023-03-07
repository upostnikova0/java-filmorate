package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User add(User user);

    void remove(User user);

    User update(User user);

    Collection<User> findAll();

    User findUser(long id);
}
