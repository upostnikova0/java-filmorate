package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.Duration;
import java.time.LocalDate;

@Data
public class Film {
    private int id;

    @NotNull
    @NotBlank
    private String name;

    @Size(max = 200)
    private String description;
    private LocalDate releaseDate;

    @Positive
    private Long duration;
}
