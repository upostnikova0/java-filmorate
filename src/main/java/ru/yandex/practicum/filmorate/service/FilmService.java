package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    protected final FilmStorage filmStorage;
    protected final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film add(Film film) {
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findFilm(long id) {
        return filmStorage.findFilm(id);
    }
    public void addLike(long filmId, long userId) {
        Film film = filmStorage.findFilm(filmId);
        User user = userService.getUser(userId);
        film.getLikes().add(user.getId());
    }

    public void deleteLike(long filmId, long userId) {
        Film film = filmStorage.findFilm(filmId);
        User user = userService.getUser(userId);
        film.getLikes().remove(user.getId());
    }

    public Set<Film> getTopFilms(Integer count) {
        Set<Film> topFilms = new HashSet<>(filmStorage.findAll()).
                stream().
                sorted(Comparator.comparing(Film::likesAmount).reversed()).
                limit(count).
                collect(Collectors.toSet());
        return topFilms;
    }
}
