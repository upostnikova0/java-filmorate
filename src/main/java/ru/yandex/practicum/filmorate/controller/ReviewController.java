package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        return reviewService.create(review);
    }

    @GetMapping("/{id}")
    public Review findById(@Valid @PathVariable("id") long reviewId) {
        return reviewService.findById(reviewId);
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review review) {
        return reviewService.update(review);
    }

    @DeleteMapping("/{reviewId}")
    public void delete(@PathVariable("reviewId") long reviewId) {
        reviewService.delete(reviewId);
    }

    @PutMapping("/{reviewId}/{likeOrDislike}/{userId}")
    public void addLikeOrDislike(@PathVariable("reviewId") long reviewId, @PathVariable("userId") long userId,
                                 @PathVariable("likeOrDislike") String likeOrDislike) {
        if (likeOrDislike.equals("like")) {
            reviewService.addLike(reviewId, userId);
        }
        if (likeOrDislike.equals("dislike")) {
            reviewService.addDislike(reviewId, userId);
        }
    }

    @DeleteMapping("/{reviewId}/{likeOrDislike}/{userId}")
    public void deleteLikeOrDislike(@PathVariable("reviewId") long reviewId, @PathVariable("userId") long userId,
                                    @PathVariable("likeOrDislike") String likeOrDislike) {
        if (likeOrDislike.equals("like")) {
            reviewService.deleteLike(reviewId, userId);
        }
        if (likeOrDislike.equals("dislike")) {
            reviewService.deleteDislike(reviewId, userId);
        }
    }

    @GetMapping
    public List<Review> findByFilmIdOrAll(@RequestParam(required = false, defaultValue = "0") long filmId,
                                          @Positive @RequestParam(required = false, defaultValue = "10") int count) {
        return reviewService.findByFilmIdOrAll(filmId, count);
    }
}