package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final EventStorage eventStorage;
    private final FilmService filmService;
    private final UserService userService;

    public Review create(Review review) {
        checkUserAndFilmIsExists(review);
        review.setUseful(0);
        reviewStorage.create(review);

        eventStorage.add(Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(review.getUserId())
                .eventType(EventType.REVIEW)
                .operation(OperationType.ADD)
                .entityId(review.getReviewId())
                .build());
        return review;
    }

    public Review findById(long reviewId) {
        return reviewStorage.findById(reviewId).orElseThrow(
                () -> new ReviewNotFoundException("Отзыв с ID " + reviewId + " не найден."));
    }

    public Review update(Review review) {
        checkUserAndFilmIsExists(review);
        Review review1 = findById(review.getReviewId());

        if (review1 != null) {
            eventStorage.add(Event.builder()
                    .timestamp(System.currentTimeMillis())
                    .userId(review1.getUserId())
                    .eventType(EventType.REVIEW)
                    .operation(OperationType.UPDATE)
                    .entityId(review1.getReviewId())
                    .build());
        }
        return reviewStorage.update(review);
    }

    public void delete(long reviewId) {
        Review review = findById(reviewId);
        if (review != null) {
            eventStorage.add(Event.builder()
                    .timestamp(System.currentTimeMillis())
                    .userId(review.getUserId())
                    .eventType(EventType.REVIEW)
                    .operation(OperationType.REMOVE)
                    .entityId(review.getReviewId())
                    .build());

            reviewStorage.delete(reviewId);
        }
    }

    public void addLike(long reviewId, long userId) {
        findById(reviewId);
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(long reviewId, long userId) {
        findById(reviewId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void deleteLike(long reviewId, long userId) {
        findById(reviewId);
        reviewStorage.deleteLike(reviewId, userId);
    }

    public void deleteDislike(long reviewId, long userId) {
        findById(reviewId);
        reviewStorage.deleteDislike(reviewId, userId);
    }

    public List<Review> findByFilmIdOrAll(long filmId, int count) {
        if (filmId > 0) filmService.findFilm(filmId);
        return reviewStorage.findByFilmIdOrAll(filmId, count);
    }

    private void checkUserAndFilmIsExists(Review review) {
        filmService.findFilm(review.getFilmId());
        userService.findUser(review.getUserId());
    }
}