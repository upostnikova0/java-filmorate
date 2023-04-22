package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.event.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.filmdirectors.FilmDirectorsDbStorage;
import ru.yandex.practicum.filmorate.storage.filmgenres.FilmGenresDbStorage;
import ru.yandex.practicum.filmorate.storage.friends.FriendDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.likes.LikesDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReviewServiceTest {
    private EmbeddedDatabase embeddedDatabase;
    private ReviewService reviewService;
    Review review1;
    Review review2;
    Review review3;
    Review review4;

    @BeforeEach
    void beforeEach() {
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .addScripts("schema.sql")
                .addScript("dataForTests.sql")
                .setType(EmbeddedDatabaseType.H2)
                .build();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(embeddedDatabase);
        ReviewStorage reviewDbStorage = new ReviewDbStorage(jdbcTemplate);
        FriendDbStorage friendDbStorage = new FriendDbStorage(jdbcTemplate);
        UserDbStorage userDbStorage = new UserDbStorage(jdbcTemplate);
        FilmDbStorage filmDbStorage = new FilmDbStorage(jdbcTemplate);
        GenreDbStorage genreDbStorage = new GenreDbStorage(jdbcTemplate);
        FilmGenresDbStorage filmGenresDbStorage = new FilmGenresDbStorage(jdbcTemplate);
        LikesDbStorage likesDbStorage = new LikesDbStorage(jdbcTemplate);
        FilmDirectorsDbStorage filmDirectorsDbStorage = new FilmDirectorsDbStorage(jdbcTemplate);
        DirectorDbStorage directorDbStorage = new DirectorDbStorage(jdbcTemplate);
        EventDbStorage eventDbStorage = new EventDbStorage(jdbcTemplate);

        GenreService genreService = new GenreService(genreDbStorage);
        DirectorService directorService = new DirectorService(directorDbStorage);
        UserService userService = new UserService(userDbStorage, friendDbStorage, eventDbStorage, filmGenresDbStorage);
        FilmService filmService = new FilmService(filmDbStorage, filmGenresDbStorage, likesDbStorage, filmDirectorsDbStorage, eventDbStorage,
                userService, genreService, directorService);
        reviewService = new ReviewService(reviewDbStorage, eventDbStorage, filmService, userService);

        review1 = Review.builder()
                .content("Тупое Г тупого Г-НА!")
                .isPositive(false)
                .userId(1L)
                .filmId(3L)
                .build();

        review2 = Review.builder()
                .content("Честно говоря, потраченного времени жаль")
                .isPositive(false)
                .userId(1L)
                .filmId(2L)
                .build();

        review3 = Review.builder()
                .content("10 из 10! На кончиках пальцев!!!")
                .isPositive(true)
                .userId(2L)
                .filmId(3L)
                .build();

        review4 = Review.builder()
                .content("Best movie i ever seen")
                .isPositive(true)
                .userId(3L)
                .filmId(1L)
                .build();
    }

    @AfterEach
    void afterEach() {
        embeddedDatabase.shutdown();
    }

    @Test
    void create() {
        Review createdReview = reviewService.create(review1);

        assertEquals(1L, createdReview.getReviewId(), "Incorrect ID assigned");
        assertEquals("Тупое Г тупого Г-НА!", createdReview.getContent(), "Incorrect Content written");
        assertEquals(false, createdReview.getIsPositive(), "Incorrect isPositive written");
        assertEquals(3L, createdReview.getFilmId(), "Incorrect FilmId written");
        assertEquals(1L, createdReview.getUserId(), "Incorrect UserId written");
        assertEquals(0, createdReview.getUseful(), "Incorrect Useful assigned");
    }

    @Test
    void findById() {
        reviewService.create(review1);

        assertThrows(ReviewNotFoundException.class, () -> reviewService.findById(9999));

        Review foundReview = reviewService.findById(1L);

        assertEquals(1L, foundReview.getReviewId(), "Incorrect ID assigned");
        assertEquals("Тупое Г тупого Г-НА!", foundReview.getContent(), "Incorrect Content written");
        assertEquals(false, foundReview.getIsPositive(), "Incorrect isPositive written");
        assertEquals(3L, foundReview.getFilmId(), "Incorrect FilmId written");
        assertEquals(1L, foundReview.getUserId(), "Incorrect UserId written");
        assertEquals(0, foundReview.getUseful(), "Incorrect Useful assigned");
    }

    @Test
    void update() {
        reviewService.create(review1);

        Review forUpdate = Review.builder()
                .reviewId(1L)
                .content("UPDATED KLIM SSANICH")
                .isPositive(true)
                .userId(2L)
                .filmId(2L)
                .useful(8841)
                .build();

        Review updatedReview = reviewService.update(forUpdate);

        assertEquals(1L, updatedReview.getReviewId(), "Incorrect ID");
        assertEquals("UPDATED KLIM SSANICH", updatedReview.getContent(), "Incorrect Content update");
        assertEquals(true, updatedReview.getIsPositive(), "Incorrect isPositive update");
        assertEquals(3L, updatedReview.getFilmId(), "This field shouldn't be updated!");
        assertEquals(1L, updatedReview.getUserId(), "This field shouldn't be updated!");
        assertEquals(0, updatedReview.getUseful(), "This field shouldn't be updated!");
    }

    @Test
    void delete() {
        reviewService.create(review1);
        reviewService.delete(1L);
        assertThrows(ReviewNotFoundException.class, () -> reviewService.findById(1L));
    }

    @Test
    void addLike() {
        reviewService.create(review1);

        reviewService.addLike(1L, 2L);
        Review foundReview = reviewService.findById(1L);

        assertEquals(1, foundReview.getUseful(), "Incorrect Useful updated");

        assertThrows(DuplicateKeyException.class,
                () -> reviewService.addLike(1L, 2L));
    }

    @Test
    void addDislike() {
        reviewService.create(review1);

        reviewService.addDislike(1L, 2L);
        Review foundReview = reviewService.findById(1L);

        assertEquals(-1, foundReview.getUseful(), "Incorrect Useful updated");

        assertThrows(DuplicateKeyException.class,
                () -> reviewService.addDislike(1L, 2L));
    }

    @Test
    void deleteLike() {
        reviewService.create(review1);

        reviewService.deleteLike(1L, 2L);
        Review foundReview = reviewService.findById(1L);

        assertEquals(0, foundReview.getUseful(), "Incorrect Useful updated");

        reviewService.addLike(1L, 2L);
        foundReview = reviewService.findById(1L);

        assertEquals(1, foundReview.getUseful(), "Incorrect Useful updated");

        reviewService.deleteLike(1L, 2L);
        foundReview = reviewService.findById(1L);

        assertEquals(0, foundReview.getUseful(), "Incorrect Useful updated");
    }

    @Test
    void deleteDislike() {
        reviewService.create(review1);

        reviewService.deleteDislike(1L, 2L);
        Review foundReview = reviewService.findById(1L);

        assertEquals(0, foundReview.getUseful(), "Incorrect Useful updated");

        reviewService.addDislike(1L, 2L);
        foundReview = reviewService.findById(1L);

        assertEquals(-1, foundReview.getUseful(), "Incorrect Useful updated");

        reviewService.deleteDislike(1L, 2L);
        foundReview = reviewService.findById(1L);

        assertEquals(0, foundReview.getUseful(), "Incorrect Useful updated");
    }

    @Test
    void findByFilmIdOrAll() {
        reviewService.create(review1);
        reviewService.addLike(1L, 2L);
        reviewService.addDislike(1L, 3L);

        reviewService.create(review2);
        reviewService.addLike(2L, 2L);

        reviewService.create(review3);
        reviewService.addDislike(3L, 3L);
        reviewService.create(review4);

        List<Review> expected = new ArrayList<>();

        expected.add(reviewService.findById(1L));
        expected.add(reviewService.findById(3L));
        assertEquals(expected, reviewService.findByFilmIdOrAll(3, 10));

        expected.clear();
        expected.add(reviewService.findById(1L));
        assertEquals(expected, reviewService.findByFilmIdOrAll(3, 1));

        expected.clear();
        assertEquals(expected, reviewService.findByFilmIdOrAll(4, 10));

        expected.add(reviewService.findById(2L));
        expected.add(reviewService.findById(1L));
        expected.add(reviewService.findById(4L));
        expected.add(reviewService.findById(3L));
        assertEquals(expected, reviewService.findByFilmIdOrAll(0, 10));
    }
}