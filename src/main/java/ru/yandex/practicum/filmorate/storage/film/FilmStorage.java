package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film add(Film film);

    Film findFilm(long filmId);

    Collection<Film> findAll();

    Film update(Film film, int ratingId);

    Film remove(Film film);
}
