package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    protected static Integer counter = 1;
    protected final Map<Integer, Film> films = new HashMap<>();

    @PostMapping
    public Film add(@RequestBody Film film) {
        validityCheck(film);

        film.setId(counter++);

        log.info("Добавлен новый фильм с ID: " + film.getId() + ".");
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        validityCheck(film);

        if (!films.containsKey(film.getId())) {
            log.debug("Фильма с таким ID не существует");
            throw new ValidationException("Фильма с таким ID не существует");
        }

        log.info("Фильм с ID: " + film.getId() + " обновлен.");
        films.put(film.getId(), film);
        return film;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Количество фильмов: " + films.size() + ".");
        return films.values();
    }

    private void validityCheck(@Valid Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.debug("Название фильма не может быть пустым.");
            throw new ValidationException("Название фильма не может быть пустым.");
        }

        int maxDescriptionLength = 200;
        if (film.getDescription().length() > maxDescriptionLength) {
            log.debug("Длина описания должна быть не больше 200 символов.");
            throw new ValidationException("Длина описания должна быть не больше 200 символов.");
        }

        LocalDate earliestReleaseDate = LocalDate.of(1895, 12,28);
        if (film.getReleaseDate().isBefore(earliestReleaseDate)) {
            log.debug("Дата релиза — не раньше 28 декабря 1895 года.");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года.");
        }

        if (film.getDuration() < 0) {
            log.debug("Продолжительность фильма должна быть положительной.");
            throw new ValidationException("Продолжительность фильма должна быть положительной.");
        }
    }
}
