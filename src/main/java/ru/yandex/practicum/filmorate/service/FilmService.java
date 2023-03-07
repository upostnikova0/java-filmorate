package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.time.Month;
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
        checkValidity(film);

        return filmStorage.add(film);
    }

    public Film update(Film film) {
        checkValidity(film);

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
        return new HashSet<>(filmStorage.findAll()).
                stream().
                sorted(Comparator.comparing(Film::likesAmount).reversed()).
                limit(count).
                collect(Collectors.toSet());
    }

    private void checkValidity(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Название фильма не может быть пустым.");
            throw new ValidationException("name");
        }

        int maxDescriptionLength = 200;
        if (film.getDescription().length() > maxDescriptionLength) {
            log.warn("Длина описания должна быть не больше 200 символов.");
            throw new ValidationException("description");
        }

        LocalDate earliestReleaseDate = LocalDate.of(1895, Month.DECEMBER,28);
        if (film.getReleaseDate().isBefore(earliestReleaseDate)) {
            log.warn("Дата релиза — не раньше 28 декабря 1895 года.");
            throw new ValidationException("release date");
        }

        if (film.getDuration() < 0) {
            log.warn("Продолжительность фильма должна быть положительной.");
            throw new ValidationException("duration");
        }
        ResponseEntity.ok(film);
    }
}
