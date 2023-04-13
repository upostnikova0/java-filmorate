package ru.yandex.practicum.filmorate.storage.filmgenres;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FilmGenresStorage {
    void addGenre(long filmId, int genreId);

    void addGenreList(long filmId, Collection<Genre> genres);

    Genre findGenre(long filmId, int genreId);

    Collection<Genre> findAll(long filmId);

    List<Map<Long, Genre>> findAll();

    void update(Film film);

    void remove(long filmId, int genreId);

    void removeAll(long filmId);
}
