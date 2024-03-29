package ru.yandex.practicum.filmorate.storage.friends;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface FriendStorage {
    void add(long userId, long friendId);

    Collection<User> findAll(long id);

    void remove(long id, long friendId);

    boolean isFriendsExist(long userId, long friendId);

    void removeAll(long id);

    Collection<Long> getCommonFriends(Long userId, Long friendId);
}
