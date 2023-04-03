package ru.yandex.practicum.filmorate.storage.likes;

import java.util.Collection;

public interface LikesStorage {
    void add(long filmId, long userId);

    void remove(long filmId, long userId);

    Collection<Long> findAll(long filmId);

    Collection<Long> findAll();

    Collection<Long> getPopular(int count);
}
