package ru.yandex.practicum.filmorate.storage.genre;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

public interface GenreStorage {
    Genre findGenre(int genreId);

    Collection<Genre> findAll();
}
