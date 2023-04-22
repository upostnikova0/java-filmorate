package ru.yandex.practicum.filmorate.storage.filmdirectors;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FilmDirectorsStorage {
    void addDirector(long filmId, long directorId);

    void addDirectorList(long filmId, Collection<Director> directors);

    Director findDirector(long filmId, long directorId);

    Collection<Director> findAll(long filmId);

    Map<Long, List<Director>> findAll();

    void update(Film film);

    void remove(long filmId, long directorId);

    void removeAll(long filmId);

    void removeAllByDirector(long directorId);

    Collection<Film> getDirectorFilmsByLikes(long directorId);

    Collection<Film> getDirectorFilmsByYear(long directorId);
}
