package ru.yandex.practicum.filmorate.storage.likes;

import java.util.Collection;

public interface LikesStorage {
    void add(long filmId, long userId);

    void remove(long filmId, long userId);

    void remove(long userId);

    boolean isLikeExist(long filmId, long userId);

    void removeAll(long filmId);

    Collection<Long> findAll(long filmId);

    Collection<Long> findAll();
}
