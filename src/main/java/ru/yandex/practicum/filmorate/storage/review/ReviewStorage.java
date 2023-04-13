package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    void delete(long id);

    Optional<Review> findById(long id);

    void addOrDeleteLikeOrDislike(long reviewId, long userId, String likeOrDislike, String requestMethod);

    List<Review> findByFilmIdOrAll(long filmId, int count);
}
