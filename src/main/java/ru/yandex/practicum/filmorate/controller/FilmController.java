package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    protected final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.add(film);
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/{filmId}")
    public Film getFilm(@Valid @PathVariable("filmId") Long filmId) {
        return filmService.findFilm(filmId);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return filmService.update(film);
    }

    @DeleteMapping("/{filmId}")
    public void delete(@Valid @PathVariable("filmId") Long filmId) {
        filmService.remove(filmId);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") Long filmId, @PathVariable("userId") Long userId) {
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") Long filmId, @PathVariable("userId") Long userId) {
        filmService.deleteLike(filmId, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopular(@RequestParam(required = false, defaultValue = "10") Integer count) {
        return filmService.getPopular(count);
    }

    @GetMapping("/common")
    public Collection<Film> getCommonFilms(@RequestParam Long userId, @RequestParam Long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getSortedFilmsByYearOrLikes(@PathVariable("directorId") Long directorId,
                                                        @RequestParam(required = false) String sortBy) {
        return filmService.getSortedFilmsByDirectorId(directorId, sortBy);
    }
    @GetMapping("/search")
    protected Collection<Film> searchFilms(@RequestParam(name = "query") Optional<String> query,
                                           @RequestParam(required = false,name = "by") List<String> by) {
        log.info("Запрос на поиск фильмов...");
        return filmService.searchFilms(query, by);
    }
}
