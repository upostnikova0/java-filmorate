package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

public interface DirectorStorage {
    Director add(Director director);

    Collection<Director> findAll();

    Director find(long directorId);

    Director update(Director director);

    void remove(Director director);
}
