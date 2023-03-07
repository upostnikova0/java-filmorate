package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserServiceTests {
    User user1;
    User user2;
    User user3;
    UserService userService = new UserService(new InMemoryUserStorage());


    @BeforeEach
    void beforeEach() {
        user1 = new User();
        user1.setEmail("francesy@gmail.com");
        user1.setLogin("francesy");
        user1.setName("Frances");
        user1.setBirthday(LocalDate.of(1990, Month.JANUARY, 15));

        user2 = new User();
        user2.setEmail("petrov@yandex.ru");
        user2.setLogin("vasyapetrov");
        user2.setName("Vasya");
        user2.setBirthday(LocalDate.of(1997, Month.OCTOBER,8));

        user3 = new User();
        user3.setEmail("dyadyaStyopa@yandex.ru");
        user3.setLogin("yatebenedyadya");
        user3.setName("Styopa");
        user3.setBirthday(LocalDate.of(1980, Month.APRIL,1));
    }

    @Test
    public void create_shouldReturnValidIdWhenCreate() {
        userService.create(user1);
        userService.create(user2);

        assertEquals(user1, userService.getUser(user1.getId()));
        assertEquals(user2, userService.getUser(user2.getId()));
    }

    @Test
    public void getUser_shouldReturnValidUserWhenCreate() {
        userService.create(user1);
        userService.create(user2);

        assertEquals(user2, userService.getUser(user2.getId()));
        assertEquals(user1, userService.getUser(user1.getId()));
    }

    @Test
    public void update_shouldReturnValidUserWhenUpdate() {
        userService.create(user1);

        user1.setName("Акакий");

        assertEquals("Акакий", userService.getUser(user1.getId()).getName());
    }

    @Test
    public void findAll_shouldReturnRightUsersSize() {
        assertEquals(0, userService.findAll().size());

        userService.create(user1);

        assertEquals(1, userService.findAll().size());

        userService.create(user2);

        assertEquals(2, userService.findAll().size());
    }

    @Test
    public void addFriend_shouldAddFriend() {
        userService.create(user1);
        userService.create(user2);

        userService.addFriend(user1.getId(), user2.getId());

        assertTrue(userService.getUser(user1.getId()).getFriends().contains(user2.getId()));
        assertTrue(userService.getUser(user2.getId()).getFriends().contains(user1.getId()));
    }

    @Test
    public void deleteFriend_shouldDeleteFriend() {
        userService.create(user1);
        userService.create(user2);

        userService.addFriend(user1.getId(), user2.getId());
        userService.deleteFriend(user1.getId(), user2.getId());

        assertTrue(userService.getUser(user1.getId()).getFriends().isEmpty());
        assertTrue(userService.getUser(user2.getId()).getFriends().isEmpty());
    }

    @Test
    public void getAllFriend_shouldReturnAllFriends() {
        userService.create(user1);
        userService.create(user2);

        userService.addFriend(user1.getId(), user2.getId());

        assertEquals(user2, userService.getAllFriends(user1.getId()).toArray()[0]);
        assertEquals(user1, userService.getAllFriends(user2.getId()).toArray()[0]);
    }

    @Test
    public void getCommonFriends_shouldReturnCommonFriends() {
        userService.create(user1);
        userService.create(user2);
        userService.create(user3);

        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user1.getId(), user3.getId());
        userService.addFriend(user2.getId(), user3.getId());

        assertEquals(user2, userService.getCommonFriends(user1.getId(), user3.getId()).toArray()[0]);
        assertEquals(user1, userService.getCommonFriends(user2.getId(), user3.getId()).toArray()[0]);
        assertEquals(user3, userService.getCommonFriends(user1.getId(), user2.getId()).toArray()[0]);
    }
}
