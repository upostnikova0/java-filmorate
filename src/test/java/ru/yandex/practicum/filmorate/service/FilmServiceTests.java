//package ru.yandex.practicum.filmorate.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.jdbc.core.JdbcTemplate;
//import ru.yandex.practicum.filmorate.model.Film;
//import ru.yandex.practicum.filmorate.model.User;
//import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
//import ru.yandex.practicum.filmorate.storage.film_genres.FilmGenresDbStorage;
//import ru.yandex.practicum.filmorate.storage.friends.FriendDbStorage;
//import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
//import ru.yandex.practicum.filmorate.storage.likes.LikesDbStorage;
//import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
//import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
//
//import java.time.LocalDate;
//import java.time.Month;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class FilmServiceTests {
//    Film film1;
//    Film film2;
//    User user1;
//
//    User user2;
//
//    FilmService filmService = new FilmService(
//            new FilmDbStorage(new JdbcTemplate()),
//            new FilmGenresDbStorage(new JdbcTemplate()),
//            new LikesDbStorage(new JdbcTemplate()),
//            new UserService(
//                    new UserDbStorage(new JdbcTemplate()),
//                    new FriendDbStorage(new JdbcTemplate())
//            ),
//            new MpaService(
//                    new MpaDbStorage(new JdbcTemplate())
//            ),
//            new GenreService(
//                    new GenreDbStorage(new JdbcTemplate())
//            )
//    );
//
//    @BeforeEach
//    void createValidFilm() {
//        filmService.findAll().clear();
//
//        film1 = Film.builder().build();
//        film1.setName("Film Name");
//        film1.setDescription("Film Description");
//        film1.setReleaseDate(LocalDate.of(2020,Month.AUGUST,15));
//        film1.setDuration(180L);
//
//        film2 = Film.builder().build();
//        film2.setName("Second Film Name");
//        film2.setDescription("Second Film Description");
//        film2.setReleaseDate(LocalDate.of(2022,Month.OCTOBER,15));
//        film2.setDuration(120L);
//
//        user1 = new User();
//        user1.setEmail("francesy@gmail.com");
//        user1.setLogin("francesy");
//        user1.setName("Frances");
//        user1.setBirthday(LocalDate.of(1990, Month.JANUARY, 15));
//
//        user2 = new User();
//        user2.setEmail("petrov@yandex.ru");
//        user2.setLogin("vasyapetrov");
//        user2.setName("Vasya");
//        user2.setBirthday(LocalDate.of(1997, Month.OCTOBER,8));
//    }
//
//    @Test
//    public void add_shouldReturnValidIdWhenCreate() {
//        filmService.add(film1);
//
//        assertEquals(film1, filmService.findAll().toArray()[0]);
//    }
//
//    @Test
//    public void update_shouldUpdate() {
//        filmService.add(film1);
//
//        film1.setName("Terminator 2");
//
//        filmService.update(film1);
//
//        assertEquals("Terminator 2", filmService.findFilm(film1.getId()).getName());
//    }
//
//    @Test
//    public void findAll_shouldReturnRightFilmsSize() {
//        assertEquals(0, filmService.findAll().size());
//
//        filmService.add(film1);
//
//        assertEquals(1, filmService.findAll().size());
//
//        filmService.add(film2);
//
//        assertEquals(2, filmService.findAll().size());
//    }
//
//    @Test
//    public void findFilm_shouldReturnRightFilm() {
//        filmService.add(film1);
//
//        assertEquals(film1, filmService.findFilm(film1.getId()));
//    }
//
//    @Test
//    public void addLike_shouldAddLike() {
//        filmService.add(film1);
//        filmService.userService.create(user1);
//
//        filmService.addLike(film1.getId(), user1.getId());
//
//        assertEquals(user1.getId(), filmService.findFilm(film1.getId()).getLikes().toArray()[0]);
//    }
//
//    @Test
//    public void deleteLike_shouldDeleteLike() {
//        filmService.add(film1);
//        filmService.userService.create(user1);
//
//        filmService.addLike(film1.getId(), user1.getId());
//        filmService.deleteLike(film1.getId(), user1.getId());
//
//        assertTrue(filmService.findFilm(film1.getId()).getLikes().isEmpty());
//    }
//
//    @Test
//    public void getTopFilms_shouldReturnNTopFilms() {
//        filmService.add(film1);
//        filmService.add(film2);
//
//        filmService.userService.create(user1);
//        filmService.userService.create(user2);
//
//        filmService.addLike(film2.getId(), user1.getId());
//        filmService.addLike(film2.getId(), user2.getId());
//
//        assertEquals(film2, filmService.getPopular(1).toArray()[0]);
//    }
//}
