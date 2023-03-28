//package ru.yandex.practicum.filmorate.inMemoryStorage;
//
//import org.junit.jupiter.api.*;
//import ru.yandex.practicum.filmorate.model.Film;
//import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
//
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//public class InMemoryFilmStorageTests extends InMemoryFilmStorage {
//    Film film1;
//    Film film2;
//    InMemoryFilmStorage inMemoryFilmStorage = new InMemoryFilmStorage();
//
//    @BeforeEach
//    void createValidFilm() {
//        InMemoryFilmStorage.globalId = 1L;
//        inMemoryFilmStorage.findAll().clear();
//
//        film1 = Film.builder().build();
//        film1.setName("Film Name");
//        film1.setDescription("Film Description");
//        film1.setReleaseDate(LocalDate.of(2020,10,15));
//        film1.setDuration(180L);
//
//        film2 = Film.builder().build();
//        film2.setName("Second Film Name");
//        film2.setDescription("Second Film Description");
//        film2.setReleaseDate(LocalDate.of(2022,10,15));
//        film2.setDuration(120L);
//    }
//
//    @Test
//    public void add_shouldReturnValidFilmsSize() {
//        inMemoryFilmStorage.add(film1);
//
//        assertEquals(1, inMemoryFilmStorage.findAll().size());
//
//        inMemoryFilmStorage.add(film2);
//
//        assertEquals(2, inMemoryFilmStorage.findAll().size());
//    }
//
//    @Test
//    public void remove_shouldRemoveRightFilm() {
//        inMemoryFilmStorage.add(film1);
//        inMemoryFilmStorage.add(film2);
//
//        assertEquals(2, inMemoryFilmStorage.findAll().size());
//
//        inMemoryFilmStorage.remove(film2);
//
//        assertEquals(1, inMemoryFilmStorage.findAll().size());
//        assertEquals(film1, inMemoryFilmStorage.findAll().toArray()[0]);
//    }
//
//    @Test
//    public void update_shouldUpdate() {
//        inMemoryFilmStorage.add(film1);
//
//        film1.setName("Terminator");
//
//        assertEquals("Terminator", inMemoryFilmStorage.findFilm(film1.getId()).getName());
//    }
//
//    @Test
//    public void findAll_shouldReturnRightSize() {
//        assertEquals(0, inMemoryFilmStorage.findAll().size());
//
//        inMemoryFilmStorage.add(film1);
//
//        assertEquals(1, inMemoryFilmStorage.findAll().size());
//    }
//
//    @Test
//    public void findFilm_shouldReturnRightFilm() {
//        inMemoryFilmStorage.add(film2);
//
//        assertEquals(film2, inMemoryFilmStorage.findFilm(film2.getId()));
//    }
//}
