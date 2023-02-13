package ru.yandex.practicum.filmorate.controller;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FilmControllerTests extends FilmController{
    FilmController filmController = new FilmController();
    Film film1;
    Film film2;

    @BeforeEach
    void createValidFilm() {
        films.clear();
        FilmController.counter = 1;

        film1 = new Film();
        film1.setName("Film Name");
        film1.setDescription("Film Description");
        film1.setReleaseDate(LocalDate.of(2020,10,15));
        film1.setDuration(1200);

        film2 = new Film();
        film2.setName("Second Film Name");
        film2.setDescription("Second Film Description");
        film2.setReleaseDate(LocalDate.of(2022,10,15));
        film2.setDuration(500);
    }

    @Test
    public void add_shouldReturnValidIdWhenCreate() {
        createValidFilm();
        filmController.add(film1);

        assertEquals(1, film1.getId());
    }

    @Test
    public void add_shouldReturnExceptionWhenNameIsEmpty() {
        createValidFilm();
        film1.setName("  ");
        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> filmController.add(film1));

        assertEquals("Название фильма не может быть пустым.", thrown.getMessage());
    }

    @Test
    public void add_shouldReturnExceptionWhenDescriptionLengthMoreThen200() {
        createValidFilm();
        film1.setDescription("A".repeat(201));
        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> filmController.add(film1));

        assertEquals("Длина описания должна быть не больше 200 символов.", thrown.getMessage());
    }

    @Test
    public void add_shouldReturnExceptionWhenReleaseDateIsEarlier() {
        createValidFilm();
        film1.setReleaseDate(LocalDate.of(1000, 10, 10));
        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> filmController.add(film1));

        assertEquals("Дата релиза — не раньше 28 декабря 1895 года.", thrown.getMessage());
    }

    @Test
    public void add_shouldReturnExceptionWhenDurationIsNegative() {
        createValidFilm();
        film1.setDuration(-1);
        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> filmController.add(film1));

        assertEquals("Продолжительность фильма должна быть положительной.", thrown.getMessage());
    }

    @Test
    public void update_shouldReturnValidIdWhenUpdate() {
        createValidFilm();
        filmController.add(film1);

        film1.setName("Hello World");
        filmController.update(film1);

        assertEquals(1, film1.getId());
    }

    @Test
    public void update_shouldReturnExceptionWhenNameIsEmpty() {
        createValidFilm();
        filmController.add(film1);

        film1.setName("");
        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> filmController.update(film1));

        assertEquals("Название фильма не может быть пустым.", thrown.getMessage());
    }

    @Test
    public void update_shouldReturnExceptionWhenDescriptionLengthMoreThen200() {
        createValidFilm();
        filmController.add(film1);

        film1.setDescription("A".repeat(201));
        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> filmController.update(film1));

        assertEquals("Длина описания должна быть не больше 200 символов.", thrown.getMessage());
    }

    @Test
    public void update_shouldReturnExceptionWhenReleaseDateIsEarlier() {
        createValidFilm();
        filmController.add(film1);

        film1.setReleaseDate(LocalDate.of(1010, 10, 10));
        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> filmController.update(film1));

        assertEquals("Дата релиза — не раньше 28 декабря 1895 года.", thrown.getMessage());
    }

    @Test
    public void update_shouldReturnExceptionWhenDurationIsNegative() {
        createValidFilm();
        filmController.add(film1);

        film1.setDuration(-1);
        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> filmController.update(film1));

        assertEquals("Продолжительность фильма должна быть положительной.", thrown.getMessage());
    }

    @Test
    public void findAll_shouldReturnValidSize() {
        createValidFilm();
        filmController.add(film1);

        assertEquals(1, filmController.films.size());

        filmController.add(film2);

        assertEquals(2, filmController.films.size());

        film1.setDescription("newDescription");
        filmController.update(film1);

        assertEquals(2, filmController.films.size());
    }
}
