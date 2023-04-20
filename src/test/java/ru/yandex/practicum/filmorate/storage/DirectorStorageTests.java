package ru.yandex.practicum.filmorate.storage;

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
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DirectorStorageTests {
    Director director1;
    Director director2;

    private EmbeddedDatabase embeddedDatabase;
    private DirectorStorage directorStorage;

    @BeforeEach
    void beforeEach() {
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .addScripts("schema.sql")
                .addScript("data.sql")
                .setType(EmbeddedDatabaseType.H2)
                .build();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(embeddedDatabase);
        directorStorage = new DirectorDbStorage(jdbcTemplate);

        director1 = Director.builder()
                .name("Альфред Хичкок")
                .build();

        director2 = Director.builder()
                .name("Гай Ричи")
                .build();
    }

    @AfterEach
    void afterEach() {
        embeddedDatabase.shutdown();
    }

    @Test
    public void add_shouldAddValidDirector() {
        assertEquals(0, directorStorage.findAll().size());

        directorStorage.add(director1);
        assertEquals(1, directorStorage.findAll().size());
    }

    @Test
    public void findDirector_shouldReturnValidDirector() {
        directorStorage.add(director1);

        assertEquals(director1, directorStorage.findAll().toArray()[0]);
    }

    @Test
    public void findAll_shouldReturnAllDirectors() {
        directorStorage.add(director1);
        directorStorage.add(director2);

        List<Director> directors = new ArrayList<>();
        directors.add(director1);
        directors.add(director2);

        assertEquals(directors, new ArrayList<>(directorStorage.findAll()));
    }

    @Test
    public void update_shouldUpdateDirector() {
        directorStorage.add(director1);

        director1.setName("Стивен Спилберг");

        directorStorage.update(director1);

        assertEquals("Стивен Спилберг", directorStorage.find(director1.getId()).getName());
    }

    @Test
    public void remove_shouldRemoveValidDirector() {
        directorStorage.add(director1);
        directorStorage.add(director2);

        assertEquals(2, directorStorage.findAll().size());

        directorStorage.remove(director1);

        assertEquals(director2.getName(), directorStorage.find(director2.getId()).getName());
    }
}
