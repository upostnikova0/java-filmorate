package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    protected static Long globalId = 1L;

    protected final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film add(Film film) {
        checkValidity(film);

        film.setId(getNextId());

        log.info("Добавлен новый фильм с ID: " + film.getId() + ".");
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film remove(Film film) {
        films.remove(film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        checkValidity(film);

        if (!films.containsKey(film.getId())) {
            log.warn("Фильма с таким ID не существует");
            throw new FilmNotFoundException("id");
        }

        log.info("Фильм с ID: " + film.getId() + " обновлен.");
        films.put(film.getId(), film);

        return film;
    }

    @Override
    public Collection<Film> findAll() {
        log.info("Количество фильмов: " + films.size() + ".");
        return films.values();
    }

    @Override
    public Film findFilm(long id) {
        if (films.containsKey(id)) {
            log.info("Найден фильм : " + films.get(id));
            return films.get(id);
        } else {
            throw new FilmNotFoundException(String.format("Фильм с ID %d не найден.", id));
        }
    }

    private static Long getNextId() {
        return globalId++;
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
