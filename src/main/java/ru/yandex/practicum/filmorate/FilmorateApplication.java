package ru.yandex.practicum.filmorate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.yandex.practicum.filmorate.model.User;

@SpringBootApplication
public class FilmorateApplication {
	/**
	 * Разобраться с зависимостями
	 * чтобы filmService получал данные о пользователе через userService
	 * */
	public static void main(String[] args) {
		SpringApplication.run(FilmorateApplication.class, args);
	}

}
