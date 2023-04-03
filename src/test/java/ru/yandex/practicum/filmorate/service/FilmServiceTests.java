package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

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

    private EmbeddedDatabase embeddedDatabase;
    private FilmDbStorage filmDbStorage;

    @BeforeEach
    void beforeEach() {
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .addScripts("schema.sql")
                .addScript("data.sql")
                .setType(EmbeddedDatabaseType.H2)
                .build();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(embeddedDatabase);
        filmDbStorage = new FilmDbStorage(jdbcTemplate);

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

    @AfterEach
    void afterEach() {
        embeddedDatabase.shutdown();
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

        assertEquals(1, filmDbStorage.findFilm(film1.getId()).getId());
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
