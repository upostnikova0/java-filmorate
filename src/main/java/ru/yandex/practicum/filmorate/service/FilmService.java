package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.filmdirectors.FilmDirectorsStorage;
import ru.yandex.practicum.filmorate.storage.filmgenres.FilmGenresStorage;
import ru.yandex.practicum.filmorate.storage.likes.LikesStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final FilmGenresStorage filmGenresStorage;
    private final LikesStorage likesStorage;
    private final FilmDirectorsStorage filmDirectorsStorage;
    private final EventStorage eventStorage;
    private final UserService userService;
    private final GenreService genreService;
    private final DirectorService directorService;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("filmGenresDbStorage") FilmGenresStorage filmGenresStorage,
                       @Qualifier("likesDbStorage") LikesStorage likesStorage,
                       @Qualifier("filmDirectorsDbStorage") FilmDirectorsStorage filmDirectorsStorage,
                       @Qualifier("eventDbStorage") EventStorage eventStorage,
                       UserService userService,
                       GenreService genreService,
                       DirectorService directorService
    ) {
        this.filmStorage = filmStorage;
        this.filmGenresStorage = filmGenresStorage;
        this.likesStorage = likesStorage;
        this.filmDirectorsStorage = filmDirectorsStorage;
        this.eventStorage = eventStorage;
        this.userService = userService;
        this.genreService = genreService;
        this.directorService = directorService;
    }

    public Collection<Film> getSortedFilmsByDirectorId(long directorId, String filter) {
        directorService.getDirector(directorId);
        Map<Long, List<Genre>> allGenres = filmGenresStorage.findAll();
        Map<Long, List<Director>> allDirectors = filmDirectorsStorage.findAll();

        if (filter != null) {
            if (filter.equals("year")) {
                List<Film> allFilms = new ArrayList<>(filmDirectorsStorage.getDirectorFilmsByYear(directorId));

                return getFilmsWithAllFields(allFilms, allGenres, allDirectors);
            }

            if (filter.equals("likes")) {
                List<Film> allFilms = new ArrayList<>(filmDirectorsStorage.getDirectorFilmsByLikes(directorId));

                return getFilmsWithAllFields(allFilms, allGenres, allDirectors);
            }

        }
        throw new ValidationException("Невозможно отсортировать фильмы: фильтр для сортировки не задан или задан неверно.");
    }

    public Film add(Film film) {
        checkValidity(film);

        Film newFilm = filmStorage.add(film);

        if (film.getGenres() != null) {
            filmGenresStorage.addGenreList(film.getId(), film.getGenres());
        }

        if (film.getDirectors() != null) {
            filmDirectorsStorage.addDirectorList(film.getId(), film.getDirectors());
        }

        newFilm.setGenres(new ArrayList<>(filmGenresStorage.findAll(newFilm.getId())));
        newFilm.setDirectors(new ArrayList<>(filmDirectorsStorage.findAll(newFilm.getId())));
        return newFilm;
    }

    public Collection<Film> findAll() {
        List<Film> allFilms = new ArrayList<>(filmStorage.findAll());
        Map<Long, List<Genre>> allGenres = filmGenresStorage.findAll();
        Map<Long, List<Director>> allDirectors = filmDirectorsStorage.findAll();
        return getFilmsWithAllFields(allFilms, allGenres, allDirectors);
    }

    private Collection<Film> getFilmsWithAllFields(List<Film> allFilms, Map<Long, List<Genre>> allGenres, Map<Long, List<Director>> allDirectors) {
        for (Film film : allFilms) {
            if (allGenres != null) {
                Optional<List<Genre>> genres = Optional.ofNullable(allGenres.get(film.getId()));
                film.setGenres(genres.orElse(new ArrayList<>()));
            }
            if (allDirectors != null) {
                Optional<List<Director>> directors = Optional.ofNullable(allDirectors.get(film.getId()));
                film.setDirectors(directors.orElse(new ArrayList<>()));
            }
        }
        return allFilms;
    }

    public Film findFilm(long filmId) {
        Film film = filmStorage.findFilm(filmId);
        Collection<Genre> genres = filmGenresStorage.findAll(filmId);
        Collection<Director> directors = filmDirectorsStorage.findAll(filmId);

        if (!genres.isEmpty()) {
            for (Genre genre : genres) {
                film.getGenres().add(genre);
            }
        }

        if (!directors.isEmpty()) {
            for (Director director : directors) {
                film.getDirectors().add(director);
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

        if (film.getDirectors() != null) {
            Set<Director> filmDirectors = new HashSet<>(film.getDirectors());
            for (Director director : filmDirectors) {
                directorService.getDirector(director.getId());
            }

            List<Director> filmDirectorsWithName = new ArrayList<>();
            for (Director director : filmDirectors) {
                filmDirectorsWithName.add(directorService.getDirector(director.getId()));
            }

            film.setDirectors(filmDirectorsWithName);

            filmDirectorsStorage.update(film);
        } else {
            filmDirectorsStorage.removeAll(film.getId());
        }

        int mpaId = film.getMpa().getId();
        filmStorage.update(film, mpaId);

        film = filmStorage.findFilm(film.getId());
        film.setGenres(new ArrayList<>(filmGenresStorage.findAll(film.getId())));
        film.setDirectors(new ArrayList<>(filmDirectorsStorage.findAll(film.getId())));
        return film;
    }

    public void remove(long filmId) {
        Film film = findFilm(filmId);

        filmStorage.remove(film);
    }

    public void addLike(long filmId, long userId) {
        findFilm(filmId);
        userService.findUser(userId);

        if (likesStorage.isLikeExist(filmId, userId)) {
            likesStorage.remove(filmId, userId);
        }
        likesStorage.add(filmId, userId);

        eventStorage.add(Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(EventType.LIKE)
                .operation(OperationType.ADD)
                .entityId(filmId)
                .build());
    }

    public void deleteLike(long filmId, long userId) {
        findFilm(filmId);
        userService.findUser(userId);

        if (likesStorage.isLikeExist(filmId, userId)) {
            likesStorage.remove(filmId, userId);

            eventStorage.add(Event.builder()
                    .timestamp(System.currentTimeMillis())
                    .userId(userId)
                    .eventType(EventType.LIKE)
                    .operation(OperationType.REMOVE)
                    .entityId(filmId)
                    .build());
        }
    }

    public Collection<Film> getPopular(Integer count, Integer genreId, Integer year) {
        List<Film> popularFilms = new ArrayList<>(filmStorage.getPopular(count, genreId, year));
        Map<Long, List<Genre>> allGenres = filmGenresStorage.findAll();
        Map<Long, List<Director>> allDirectors = filmDirectorsStorage.findAll();

        if (popularFilms.size() == 0) {
            return popularFilms;
        }

        popularFilms = new ArrayList<>(getFilmsWithAllFields(popularFilms, allGenres, allDirectors));
        log.info("Популярные фильмы: {}", popularFilms);

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

    public Collection<Film> getFilmSearch(String query, String by) {
        List<Film> foundFilms = filmStorage.getFilmSearch(query, by);
        Map<Long, List<Genre>> allGenres = filmGenresStorage.findAll();
        Map<Long, List<Director>> allDirectors = filmDirectorsStorage.findAll();
        return getFilmsWithAllFields(foundFilms, allGenres, allDirectors);
    }
}