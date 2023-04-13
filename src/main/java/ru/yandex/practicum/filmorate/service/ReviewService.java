package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;
import java.util.TreeSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final FilmService filmService;
    private final UserService userService;

    public Review create(Review review) {
        checkUserAndFilmIsExists(review);
        review.setUseful(0);
        reviewStorage.create(review);
        review.setLikes(new TreeSet<>());
        review.setDislikes(new TreeSet<>());
        return review;
    }

    public Review findById(long reviewId) {
        return reviewStorage.findById(reviewId).orElseThrow(
                () -> new ReviewNotFoundException("Отзыв с ID " + reviewId + " не найден."));
    }

    public Review update(Review review) {
        checkUserAndFilmIsExists(review);
        findById(review.getReviewId());
        return reviewStorage.update(review);
    }

    public void delete(long reviewId) {
        findById(reviewId);
        reviewStorage.delete(reviewId);
    }

    public void addOrDeleteLikeOrDislike(long reviewId, long userId, String likeOrDislike, String requestMethod) {
        findById(reviewId);
        reviewStorage.addOrDeleteLikeOrDislike(reviewId, userId, likeOrDislike, requestMethod);
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