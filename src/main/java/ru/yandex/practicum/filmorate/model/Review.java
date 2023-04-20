package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class Review {
    @Positive
    private Long reviewId;
    @NotBlank
    @Size(max = 255)
    private String content;
    @NotNull
    private Boolean isPositive;
    @NotNull
    private Long userId;
    @NotNull
    private Long filmId;
    private Integer useful;
    private boolean deleted;
    private Set<Long> likes;
    private Set<Long> dislikes;

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("contents", content);
        values.put("is_positive", isPositive);
        values.put("user_id", userId);
        values.put("film_id", filmId);
        values.put("useful", useful);
        values.put("deleted", deleted);
        return values;
    }
}