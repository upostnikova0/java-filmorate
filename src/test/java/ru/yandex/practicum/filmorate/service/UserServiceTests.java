package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friends.FriendDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceTests {
    private final UserDbStorage userStorage;
    private final FriendDbStorage friendDbStorage;
    User user1;
    User user2;
    User user3;


    @BeforeEach
    void beforeEach() {
        userStorage.findAll().forEach(x -> friendDbStorage.removeAll(x.getId()));
        userStorage.findAll().forEach(userStorage::remove);

        user1 = new User();
        user1.setEmail("francesy@gmail.com");
        user1.setLogin("francesy");
        user1.setName("Frances");
        user1.setBirthday(LocalDate.of(1990, Month.JANUARY, 15));

        user2 = new User();
        user2.setEmail("petrov@yandex.ru");
        user2.setLogin("vasyapetrov");
        user2.setName("Vasya");
        user2.setBirthday(LocalDate.of(1997, Month.OCTOBER, 8));

        user3 = new User();
        user3.setEmail("dyadyaStyopa@yandex.ru");
        user3.setLogin("yatebenedyadya");
        user3.setName("Styopa");
        user3.setBirthday(LocalDate.of(1980, Month.APRIL, 1));
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

        assertTrue(userStorage.findUser(user1.getId()).getFriends().isEmpty());

    }
}