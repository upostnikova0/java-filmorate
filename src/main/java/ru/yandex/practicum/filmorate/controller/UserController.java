package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    protected static Integer counter = 1;
    protected final Map<Integer, User> users = new HashMap<>();

    @PostMapping
    public User create(@RequestBody User user) {
        validityCheck(user);

        user.setId(counter++);

        for (Map.Entry<Integer, User> entry : users.entrySet()) {
            User temp = entry.getValue();

            if (user.getEmail().equals(temp.getEmail())) {
                log.debug("Пользователь с электронной почтой " + user.getEmail() + " уже зарегистрирован.");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Пользователь с электронной почтой " + user.getEmail() + " уже зарегистрирован."
                );
            }
        }

        log.info("Добавлен новый пользователь c ID: " + user.getId());
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        validityCheck(user);

        if (!users.containsKey(user.getId())) {
            log.debug("Пользователя с таким ID не существует");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователя с таким ID не существует");
        }

        log.info("Обновлен пользователь c ID: " + user.getId());
        users.put(user.getId(), user);
        return user;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Количество пользователей: " + users.size());
        return users.values();
    }

    private ResponseEntity<String> validityCheck(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.debug("Электронная почта не может быть пустой и должна содержать символ @.");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @.");
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.debug("Логин не может быть пустым и содержать пробелы.");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы.");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if(user.getBirthday().isAfter(LocalDate.now())) {
            log.debug("Дата рождения не может быть в будущем.");
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
        return ResponseEntity.ok("valid");
    }
}
