package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.filmgenres.FilmGenresDbStorage;
import ru.yandex.practicum.filmorate.storage.likes.LikesDbStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmServiceTests {
    Mpa mpa1;
    Mpa mpa2;
    Genre genre1;
    Genre genre2;
    Film film1;
    Film film2;

    private final FilmDbStorage filmDbStorage;
    private final FilmGenresDbStorage filmGenresDbStorage;
    private final LikesDbStorage likesDbStorage;

    @BeforeEach
    void beforeEach() {
        filmDbStorage.findAll().forEach(x -> filmGenresDbStorage.removeAll(x.getId()));
        filmDbStorage.findAll().forEach(x -> likesDbStorage.removeAll(x.getId()));
        filmDbStorage.findAll().forEach(filmDbStorage::remove);

        mpa1 = Mpa.builder()
                .id(1)
                .build();

        mpa2 = Mpa.builder()
                .id(2)
                .build();

        genre1 = Genre.builder()
                .id(1)
                .build();

        genre2 = Genre.builder()
                .id(2)
                .build();

        film1 = Film.builder()
                .name("Film Name")
                .description("Film Description")
                .releaseDate(LocalDate.of(2020, Month.AUGUST, 15))
                .duration(180L)
                .mpa(mpa1)
                .build();

        film2 = Film.builder()
                .name("Film2 Name")
                .description("Film2 Description")
                .releaseDate(LocalDate.of(2022, Month.MARCH, 13))
                .duration(120L)
                .mpa(mpa2)
                .genres(new ArrayList<>())
                .build();
        film2.getGenres().add(genre2);
    }

    @Test
    public void findAll_shouldReturnRightFilmsSize() {
        assertEquals(0, filmDbStorage.findAll().size());

        filmDbStorage.add(film1);

        assertEquals(1, filmDbStorage.findAll().size());

        filmDbStorage.add(film2);

        assertEquals(2, filmDbStorage.findAll().size());
    }

    @Test
    public void findFilm_shouldReturnRightFilm() {
        filmDbStorage.add(film1);

        assertEquals(film1, filmDbStorage.findFilm(film1.getId()));
    }

    @Test
    public void update_shouldUpdate() {
        filmDbStorage.add(film1);

        film1.setName("Terminator 2");
        Mpa mpa = film1.getMpa();
        filmDbStorage.update(film1, mpa.getId());


        assertEquals("Terminator 2", filmDbStorage.findFilm(film1.getId()).getName());
    }

    @Test
    public void remove_shouldRemoveFilm() {
        assertEquals(0, filmDbStorage.findAll().size());

        filmDbStorage.add(film1);

        assertEquals(1, filmDbStorage.findAll().size());

        filmDbStorage.remove(film1);
        assertEquals(0, filmDbStorage.findAll().size());
    }
}
