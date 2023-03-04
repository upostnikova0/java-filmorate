package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Component
public class User {
    private Long id;

    @Email
    private String email;

    @NotNull
    @NotBlank
    private String login;
    private String name;

    @Past
    private LocalDate birthday;

    Set<Long> friends = new HashSet<>();
}
