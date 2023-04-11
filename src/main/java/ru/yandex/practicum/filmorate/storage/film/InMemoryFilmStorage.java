package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

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
        film.setId(getNextId());

        log.info("Добавлен новый фильм с ID: " + film.getId() + ".");
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film findFilm(long filmId) {
        if (films.containsKey(filmId)) {
            log.info("Найден фильм : " + films.get(filmId));
            return films.get(filmId);
        } else {
            throw new FilmNotFoundException(String.format("Фильм с ID %d не найден.", filmId));
        }
    }

    @Override
    public Collection<Film> findAll() {
        log.info("Количество фильмов: " + films.size() + ".");
        return films.values();
    }

    @Override
    public Film update(Film film, int ratingId) {
        if (!films.containsKey(film.getId())) {
            log.warn("Фильма с таким ID не существует");
            throw new FilmNotFoundException("id");
        }

        log.info("Фильм с ID: " + film.getId() + " обновлен.");
        films.put(film.getId(), film);

        return film;
    }

    @Override
    public Film remove(Film film) {
        films.remove(film.getId());
        return film;
    }

    @Override
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        return null;
    }

    private static Long getNextId() {
        return globalId++;
    }
}
