package ru.yandex.practicum.filmorate.controller;

import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserControllerTests extends UserController {
    UserController userController = new UserController();

    User user1;
    User user2;

    @BeforeEach
    void createValidUser() {
        UserController.counter = 1;

        user1 = new User();
        user1.setEmail("francesy@gmail.com");
        user1.setLogin("francesy");
        user1.setName("Frances");
        user1.setBirthday(LocalDate.of(1990, 1, 15));

        user2 = new User();
        user2.setEmail("petrov@yandex.ru");
        user2.setLogin("vasyapetrov");
        user2.setName("Vasya");
        user2.setBirthday(LocalDate.of(1997, 10,8));
    }

    @AfterEach
    void afterEach() {
        users.clear();
    }

    @Test
    public void create_shouldReturnValidIdWhenCreate() {
        createValidUser();
        userController.create(user1);
        userController.create(user2);

        assertEquals(1, user1.getId());
        assertEquals(2, user2.getId());
    }

    @Test
    public void create_shouldReturnExceptionWhenEmailIsEmpty() {
        createValidUser();
        user1.setEmail("  ");

        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> userController.create(user1));

        assertEquals("Электронная почта не может быть пустой и должна содержать символ @.", thrown.getMessage());
    }

    @Test
    public void create_shouldReturnExceptionWhenLoginIsEmpty() {
        createValidUser();
        user1.setLogin("  ");

        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> userController.create(user1));

        assertEquals("Логин не может быть пустым и содержать пробелы.", thrown.getMessage());
    }

    @Test
    public void create_shouldReturnLoginWhenNameIsEmpty() {
        createValidUser();
        user1.setName("  ");

        userController.create(user1);

        assertEquals("francesy", user1.getLogin());
    }

    @Test
    public void create_shouldReturnExceptionWhenDateOfBirthIsInTheFuture() {
        createValidUser();
        user1.setBirthday(LocalDate.of(2030,10,10));

        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> userController.create(user1));

        assertEquals("Дата рождения не может быть в будущем.", thrown.getMessage());
    }

    @Test
    public void update_shouldReturnValidIdWhenCreate() {
        createValidUser();
        userController.create(user1);

        assertEquals(1, user1.getId());

        user1.setName("Lolita");

        userController.update(user1);

        assertEquals(1, user1.getId());
    }

    @Test
    public void update_shouldReturnExceptionWhenEmailIsEmpty() {
        createValidUser();
        userController.create(user1);

        user1.setEmail("aaa");

        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> userController.update(user1));

        assertEquals("Электронная почта не может быть пустой и должна содержать символ @.", thrown.getMessage());
    }

    @Test
    public void update_shouldReturnExceptionWhenLoginIsEmpty() {
        createValidUser();
        userController.create(user1);

        user1.setLogin("");

        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> userController.update(user1));

        assertEquals("Логин не может быть пустым и содержать пробелы.", thrown.getMessage());
    }

    @Test
    public void update_shouldReturnLoginWhenNameIsEmpty() {
        createValidUser();
        userController.create(user1);

        user1.setName("  ");
        userController.update(user1);

        assertEquals("francesy", user1.getLogin());
    }

    @Test
    public void update_shouldReturnExceptionWhenDateOfBirthIsInTheFuture() {
        createValidUser();
        userController.create(user1);

        user1.setBirthday(LocalDate.of(2030,10,10));

        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> userController.update(user1));

        assertEquals("Дата рождения не может быть в будущем.", thrown.getMessage());
    }

    @Test
    public void findAll_shouldReturnValidSize() {
        createValidUser();
        userController.create(user1);
        userController.create(user2);

        assertEquals(2, userController.users.size());

        user2.setName("   ");
        userController.update(user2);

        assertEquals(2, userController.users.size());
    }
}
