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
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.storage.event.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.friends.FriendDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserStorageTests {
    private EmbeddedDatabase embeddedDatabase;
    private UserDbStorage userStorage;
    private FriendDbStorage friendDbStorage;
    private EventDbStorage eventDbStorage;
    User user1;
    User user2;
    User user3;
    Event event1;
    Event event2;


    @BeforeEach
    void beforeEach() {
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .addScript("schema.sql")
                .addScript("data.sql")
                .setType(EmbeddedDatabaseType.H2)
                .build();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(embeddedDatabase);

        userStorage = new UserDbStorage(jdbcTemplate);
        friendDbStorage = new FriendDbStorage(jdbcTemplate);
        eventDbStorage = new EventDbStorage(jdbcTemplate);

        user1 = User.builder().build();
        user1.setEmail("francesy@gmail.com");
        user1.setLogin("francesy");
        user1.setName("Frances");
        user1.setBirthday(LocalDate.of(1990, Month.JANUARY, 15));

        user2 = User.builder().build();
        user2.setEmail("petrov@yandex.ru");
        user2.setLogin("vasyapetrov");
        user2.setName("Vasya");
        user2.setBirthday(LocalDate.of(1997, Month.OCTOBER, 8));

        user3 = User.builder().build();
        user3.setEmail("dyadyaStyopa@yandex.ru");
        user3.setLogin("yatebenedyadya");
        user3.setName("Styopa");
        user3.setBirthday(LocalDate.of(1980, Month.APRIL, 1));

        event1 = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(1L)
                .eventType(EventType.FRIEND)
                .operation(OperationType.ADD)
                .entityId(2L).build();

        event2 = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(1L)
                .eventType(EventType.FRIEND)
                .operation(OperationType.REMOVE)
                .entityId(2L).build();
    }

    @AfterEach
    void afterEach() {
        embeddedDatabase.shutdown();
    }

    @Test
    public void create_shouldReturnValidIdWhenCreate() {
        userStorage.add(user1);

        assertEquals(1, user1.getId());
    }

    @Test
    public void findUser_shouldReturnValidUserWhenCreate() {
        userStorage.add(user1);
        userStorage.add(user2);

        assertEquals(user2, userStorage.findUser(user2.getId()));
        assertEquals(user1, userStorage.findUser(user1.getId()));
    }

    @Test
    public void findAll_shouldReturnRightUsersSize() {
        assertEquals(0, userStorage.findAll().size());

        userStorage.add(user1);

        assertEquals(1, userStorage.findAll().size());

        userStorage.add(user2);

        assertEquals(2, userStorage.findAll().size());
    }

    @Test
    public void update_shouldReturnValidUserWhenUpdate() {
        userStorage.add(user1);

        user1.setName("Акакий");
        userStorage.update(user1);

        assertEquals("Акакий", userStorage.findUser(user1.getId()).getName());
    }

    @Test
    public void remove_shouldRemoveUser() {
        assertEquals(0, userStorage.findAll().size());

        userStorage.add(user1);

        assertEquals(1, userStorage.findAll().size());

        userStorage.remove(user1);
        assertEquals(0, userStorage.findAll().size());
    }

    @Test
    public void addFriend_shouldAddFriend() {
        userStorage.add(user1);
        userStorage.add(user2);

        friendDbStorage.add(user1.getId(), user2.getId());

        assertEquals(1, friendDbStorage.findAll(user1.getId()).size());
        assertEquals(user2, friendDbStorage.findAll(user1.getId()).toArray()[0]);
        assertTrue(friendDbStorage.findAll(user2.getId()).isEmpty());
    }

    @Test
    public void getAllFriend_shouldReturnAllFriends() {
        userStorage.add(user1);
        userStorage.add(user2);
        userStorage.add(user3);

        friendDbStorage.add(user1.getId(), user2.getId());
        friendDbStorage.add(user1.getId(), user3.getId());

        assertEquals(2, friendDbStorage.findAll(user1.getId()).size());
        assertTrue(friendDbStorage.findAll(user1.getId()).contains(user2));
        assertEquals(user3, friendDbStorage.findAll(user1.getId()).toArray()[1]);
    }

    @Test
    public void deleteFriend_shouldDeleteFriend() {
        userStorage.add(user1);
        userStorage.add(user2);

        friendDbStorage.add(user1.getId(), user2.getId());
        friendDbStorage.remove(user1.getId(), user2.getId());

        assertTrue(friendDbStorage.findAll(user1.getId()).isEmpty());

    }

    @Test
    public void addEventWhenAddFriend() {
        userStorage.add(user1);
        userStorage.add(user2);

        eventDbStorage.add(event1);

        assertEquals(1, eventDbStorage.findAll(user1.getId()).size());
    }

    @Test
    public void addEventWhenRemoveFriend() {
        userStorage.add(user1);
        userStorage.add(user2);

        eventDbStorage.add(event1);
        assertEquals(1, eventDbStorage.findAll(user1.getId()).size());

        eventDbStorage.add(event1);
        assertEquals(2, eventDbStorage.findAll(user1.getId()).size());
    }
}