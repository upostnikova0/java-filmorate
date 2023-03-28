package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
public class User {
    private Long id;
    @Email
    private String email;
    @NotBlank
    private String login;
    private String name;
    @Past
    private LocalDate birthday;

    private Set<Long> friends = new HashSet<>();

    public User(long id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name == null || name.isBlank() ? login : name;
        this.birthday = birthday;
    }


    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("email", email);
        values.put("login", login);
        values.put("name", name);
        values.put("birthday", birthday);
        return values;
    }
}
