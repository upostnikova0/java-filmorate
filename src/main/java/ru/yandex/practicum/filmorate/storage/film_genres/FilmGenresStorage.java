package ru.yandex.practicum.filmorate.storage.film_genres;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Set;

public interface FilmGenresStorage {
    void addGenre(long filmId, int genreId);

    Genre findGenre(long filmId, int genreId);

    Collection<Genre> findAll(long filmId);

    void update(Film film);

    void remove(long filmId, int genreId);

    void removeAll(long filmId);
}
