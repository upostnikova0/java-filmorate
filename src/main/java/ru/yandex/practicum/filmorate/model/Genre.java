package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Genre {
    private Integer id;
    private String name;

    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
