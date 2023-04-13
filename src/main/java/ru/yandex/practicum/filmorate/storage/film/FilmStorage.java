package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film add(Film film);

    Film findFilm(long filmId);

    Collection<Film> findAll();

    Film update(Film film, int ratingId);

    Film remove(Film film);

    Collection<Film> getCommonFilms(Long userId, Long friendId);

    Collection<Film> searchFilms(Optional<String> query, List<String> by);

}
