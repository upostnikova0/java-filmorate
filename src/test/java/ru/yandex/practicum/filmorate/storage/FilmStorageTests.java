//package ru.yandex.practicum.filmorate.storage;
//
//import lombok.RequiredArgsConstructor;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
//import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
//import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
//import ru.yandex.practicum.filmorate.model.*;
//import ru.yandex.practicum.filmorate.model.enums.EventType;
//import ru.yandex.practicum.filmorate.model.enums.OperationType;
//import ru.yandex.practicum.filmorate.storage.event.EventDbStorage;
//import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
//import ru.yandex.practicum.filmorate.storage.likes.LikesDbStorage;
//import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
//
//import java.time.LocalDate;
//import java.time.Month;
//import java.util.ArrayList;
//
//import static org.junit.jupiter.api.Assertions.assertArrayEquals;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//@SpringBootTest
//@AutoConfigureTestDatabase
//@RequiredArgsConstructor(onConstructor_ = @Autowired)
//public class FilmStorageTests {
//    Mpa mpa1;
//    Mpa mpa2;
//    Genre genre1;
//    Genre genre2;
//    Film film1;
//    Film film2;
//    User user1;
//    User user2;
//    Event event1;
//    Event event2;
//
//    private EmbeddedDatabase embeddedDatabase;
//    private FilmDbStorage filmDbStorage;
//    private UserDbStorage userDbStorage;
//    private LikesDbStorage likesDbStorage;
//    private EventDbStorage eventDbStorage;
//
//    @BeforeEach
//    void beforeEach() {
//        embeddedDatabase = new EmbeddedDatabaseBuilder()
//                .addScripts("schema.sql")
//                .addScript("data.sql")
//                .setType(EmbeddedDatabaseType.H2)
//                .build();
//
//        JdbcTemplate jdbcTemplate = new JdbcTemplate(embeddedDatabase);
//        filmDbStorage = new FilmDbStorage(jdbcTemplate);
//
//        userDbStorage = new UserDbStorage(jdbcTemplate);
//
//        likesDbStorage = new LikesDbStorage(jdbcTemplate);
//
//        eventDbStorage = new EventDbStorage(jdbcTemplate);
//
//        mpa1 = Mpa.builder()
//                .id(1)
//                .build();
//
//        mpa2 = Mpa.builder()
//                .id(2)
//                .build();
//
//        genre1 = Genre.builder()
//                .id(1)
//                .build();
//
//        genre2 = Genre.builder()
//                .id(2)
//                .build();
//
//        film1 = Film.builder()
//                .name("Film Name")
//                .description("Film Description")
//                .releaseDate(LocalDate.of(2020, Month.AUGUST, 15))
//                .duration(180L)
//                .mpa(mpa1)
//                .build();
//
//        film2 = Film.builder()
//                .name("Film2 Name")
//                .description("Film2 Description")
//                .releaseDate(LocalDate.of(2022, Month.MARCH, 13))
//                .duration(120L)
//                .mpa(mpa2)
//                .genres(new ArrayList<>())
//                .build();
//        film2.getGenres().add(genre2);
//
//        user1 = User.builder()
//                .email("fff@mail.ru")
//                .login("testLogin")
//                .birthday(LocalDate.of(2000, 10, 14))
//                .name("Oleg")
//                .build();
//
//        user2 = User.builder()
//                .email("bbb@mail.ru")
//                .login("secondLogin")
//                .birthday(LocalDate.of(1990, 12, 12))
//                .name("Ivan")
//                .build();
//
//        event1 = Event.builder()
//                .timestamp(System.currentTimeMillis())
//                .userId(1L)
//                .eventType(EventType.LIKE)
//                .operation(OperationType.ADD)
//                .entityId(2L).build();
//
//        event2 = Event.builder()
//                .timestamp(System.currentTimeMillis())
//                .userId(1L)
//                .eventType(EventType.LIKE)
//                .operation(OperationType.REMOVE)
//                .entityId(2L).build();
//    }
//
//    @AfterEach
//    void afterEach() {
//        embeddedDatabase.shutdown();
//    }
//
//    @Test
//    public void findAll_shouldReturnRightFilmsSize() {
//        assertEquals(0, filmDbStorage.findAll().size());
//
//        filmDbStorage.add(film1);
//
//        assertEquals(1, filmDbStorage.findAll().size());
//
//        filmDbStorage.add(film2);
//
//        assertEquals(2, filmDbStorage.findAll().size());
//    }
//
//    @Test
//    public void findFilm_shouldReturnRightFilm() {
//        filmDbStorage.add(film1);
//
//        assertEquals(1, filmDbStorage.findFilm(film1.getId()).getId());
//    }
//
//    @Test
//    public void update_shouldUpdate() {
//        filmDbStorage.add(film1);
//
//        film1.setName("Terminator 2");
//        Mpa mpa = film1.getMpa();
//        filmDbStorage.update(film1, mpa.getId());
//
//
//        assertEquals("Terminator 2", filmDbStorage.findFilm(film1.getId()).getName());
//    }
//
//    @Test
//    public void remove_shouldRemoveFilm() {
//        assertEquals(0, filmDbStorage.findAll().size());
//
//        filmDbStorage.add(film1);
//
//        assertEquals(1, filmDbStorage.findAll().size());
//
//        filmDbStorage.remove(film1);
//        assertEquals(0, filmDbStorage.findAll().size());
//    }
//
//    @Test
//    public void getCommonFilms_shouldGetCommonFilms() {
//        filmDbStorage.add(film1);
//
//
//        userDbStorage.add(user1);
//        userDbStorage.add(user2);
//
//        likesDbStorage.add(film1.getId(), user1.getId());
//        likesDbStorage.add(film1.getId(), user2.getId());
//
//        assertArrayEquals(filmDbStorage.findAll().toArray(),
//                filmDbStorage.getCommonFilms(user1.getId(), user2.getId()).toArray());
//    }
//
//    @Test
//    public void addEventWhenAddLike() {
//        userDbStorage.add(user1);
//        filmDbStorage.add(film2);
//
//        eventDbStorage.add(event1);
//
//        assertEquals(1, eventDbStorage.findAll(user1.getId()).size());
//    }
//
//    @Test
//    public void addEventWhenRemoveLike() {
//        userDbStorage.add(user1);
//        filmDbStorage.add(film2);
//
//        eventDbStorage.add(event1);
//        assertEquals(1, eventDbStorage.findAll(user1.getId()).size());
//
//        eventDbStorage.add(event1);
//        assertEquals(2, eventDbStorage.findAll(user1.getId()).size());
//    }
//}
