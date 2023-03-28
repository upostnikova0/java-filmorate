package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class Film implements Comparable<Film> {
    private Long id;
    @NotBlank
    private String name;
    @Size(max = 200)
    private String description;
    private LocalDate releaseDate;
    @Positive
    private Long duration;
    private Mpa mpa;
    private List<Genre> genres;
    private Set<Long> likes;

    @Override
    public int compareTo(Film o) {
        return this.likes.size() - o.getLikes().size();
    }

    public int likesAmount() {
        return likes.size();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("name", name);
        values.put("description", description);
        values.put("release_date", releaseDate);
        values.put("duration", duration);
        values.put("mpa_rating_id", mpa.getId());
        return values;
    }
}
