package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.filmgenres.FilmGenresStorage;
import ru.yandex.practicum.filmorate.storage.likes.LikesStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    protected final FilmStorage filmStorage;
    protected final FilmGenresStorage filmGenresStorage;
    protected final LikesStorage likesStorage;
    protected final UserService userService;
    protected final MpaService mpaService;
    protected final GenreService genreService;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("filmGenresDbStorage") FilmGenresStorage filmGenresStorage,
                       @Qualifier("likesDbStorage") LikesStorage likesStorage,
                       UserService userService,
                       MpaService mpaService,
                       GenreService genreService
    ) {
        this.filmStorage = filmStorage;
        this.filmGenresStorage = filmGenresStorage;
        this.likesStorage = likesStorage;
        this.userService = userService;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    public Film add(Film film) {
        checkValidity(film);

        Film newFilm = filmStorage.add(film);

        if (film.getGenres() != null) {
            filmGenresStorage.addGenreList(film.getId(), film.getGenres());
        }
        newFilm.setGenres(new ArrayList<>(filmGenresStorage.findAll(newFilm.getId())));
        return newFilm;
    }

    public Collection<Film> findAll() {
        Map<Long, Film> allFilms = filmStorage.findAll().stream().collect(Collectors.toMap(Film::getId, film -> film));
        List<Map<Long, Genre>> allGenres = filmGenresStorage.findAll();

        if (!allGenres.isEmpty()) {
            for (Map<Long, Genre> map : allGenres) {
                for (Long filmId : map.keySet()) {
                    allFilms.get(filmId).getGenres().add(map.get(filmId));
                }
            }
        }

        return allFilms.values();
    }

    public Film findFilm(long filmId) {
        Film film = filmStorage.findFilm(filmId);
        Collection<Genre> genres = filmGenresStorage.findAll(filmId);

        if (!genres.isEmpty()) {
            for (Genre genre : genres) {
                film.getGenres().add(genre);
            }
        }

        return film;
    }

    public Film update(Film film) {
        checkValidity(film);
        filmStorage.findFilm(film.getId());

        if (film.getGenres() != null) {
            Set<Genre> filmGenres = new LinkedHashSet<>(film.getGenres());
            for (Genre genre : filmGenres) {
                genreService.findGenre(genre.getId());
            }

            List<Genre> filmGenresWithName = new ArrayList<>();
            for (Genre genre : filmGenres) {
                filmGenresWithName.add(genreService.findGenre(genre.getId()));
            }
            film.setGenres(filmGenresWithName);

            filmGenresStorage.update(film);
        } else {
            filmGenresStorage.removeAll(film.getId());
        }

        int mpaId = film.getMpa().getId();
        filmStorage.update(film, mpaId);

        film = filmStorage.findFilm(film.getId());
        film.setGenres(new ArrayList<>(filmGenresStorage.findAll(film.getId())));

        return film;
    }

    public void addLike(long filmId, long userId) {
        findFilm(filmId);
        userService.findUser(userId);
        likesStorage.add(filmId, userId);
    }

    public void deleteLike(long filmId, long userId) {
        findFilm(filmId);
        userService.findUser(userId);
        likesStorage.remove(filmId, userId);
    }

    public Collection<Film> getPopular(Integer count) {
        Collection<Long> popularFilmsId = likesStorage.getPopular(count);
        List<Film> popularFilms = new ArrayList<>();

        if (popularFilmsId.size() == count) {
            for (Long filmId : popularFilmsId) {
                Film film = filmStorage.findFilm(filmId);
                film.setMpa(mpaService.getMpa(film.getMpa().getId()));
                film.setGenres(new ArrayList<>(filmGenresStorage.findAll(filmId)));
                popularFilms.add(film);
            }

            return popularFilms;
        }

        if (popularFilmsId.size() < count) {
            for (Long filmId : popularFilmsId) {
                Film film = filmStorage.findFilm(filmId);
                film.setMpa(mpaService.getMpa(film.getMpa().getId()));
                film.setGenres(new ArrayList<>(filmGenresStorage.findAll(filmId)));
                popularFilms.add(film);
            }

            Collection<Film> allFilms = filmStorage.findAll();
            for (Film film : allFilms) {
                film.setMpa(mpaService.getMpa(film.getMpa().getId()));
                film.setGenres(new ArrayList<>(filmGenresStorage.findAll(film.getId())));
                if (!popularFilms.contains(film)) {
                    popularFilms.add(film);
                }
            }

            return popularFilms.stream()
                    .limit(count)
                    .collect(Collectors.toList());
        }

        if (popularFilmsId.isEmpty()) {
            popularFilms = new ArrayList<>(filmStorage.findAll());
            for (Film film : popularFilms) {
                film.setMpa(mpaService.getMpa(film.getMpa().getId()));
                film.setGenres(new ArrayList<>(filmGenresStorage.findAll(film.getId())));
            }
            return popularFilms.stream()
                    .limit(count)
                    .collect(Collectors.toList());
        }

        return popularFilms;
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
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

        LocalDate earliestReleaseDate = LocalDate.of(1895, Month.DECEMBER, 28);
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
