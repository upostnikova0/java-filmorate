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
        assertEquals(8841, updatedReview.getUseful(), "This field shouldn't be updated!");
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

        reviewService.addOrDeleteLikeOrDislike(1L, 2L, "like", "put");
        Review foundReview = reviewService.findById(1L);

        assertEquals(1, foundReview.getUseful(), "Incorrect Useful updated");

        assertThrows(DuplicateKeyException.class,
                () -> reviewService.addOrDeleteLikeOrDislike(1L, 2L, "like", "put"));
    }

    @Test
    void addDislike() {
        reviewService.create(review1);

        reviewService.addOrDeleteLikeOrDislike(1L, 2L, "dislike", "put");
        Review foundReview = reviewService.findById(1L);

        assertEquals(-1, foundReview.getUseful(), "Incorrect Useful updated");

        assertThrows(DuplicateKeyException.class,
                () -> reviewService.addOrDeleteLikeOrDislike(1L, 2L, "dislike", "put"));
    }

    @Test
    void deleteLike() {
        reviewService.create(review1);

        reviewService.addOrDeleteLikeOrDislike(1L, 2L, "like", "delete");
        Review foundReview = reviewService.findById(1L);

        assertEquals(0, foundReview.getUseful(), "Incorrect Useful updated");

        reviewService.addOrDeleteLikeOrDislike(1L, 2L, "like", "put");
        foundReview = reviewService.findById(1L);

        assertEquals(1, foundReview.getUseful(), "Incorrect Useful updated");

        reviewService.addOrDeleteLikeOrDislike(1L, 2L, "like", "delete");
        foundReview = reviewService.findById(1L);

        assertEquals(0, foundReview.getUseful(), "Incorrect Useful updated");
    }

    @Test
    void deleteDislike() {
        reviewService.create(review1);

        reviewService.addOrDeleteLikeOrDislike(1L, 2L, "dislike", "delete");
        Review foundReview = reviewService.findById(1L);

        assertEquals(0, foundReview.getUseful(), "Incorrect Useful updated");

        reviewService.addOrDeleteLikeOrDislike(1L, 2L, "dislike", "put");
        foundReview = reviewService.findById(1L);

        assertEquals(-1, foundReview.getUseful(), "Incorrect Useful updated");

        reviewService.addOrDeleteLikeOrDislike(1L, 2L, "dislike", "delete");
        foundReview = reviewService.findById(1L);

        assertEquals(0, foundReview.getUseful(), "Incorrect Useful updated");
    }

    @Test
    void findByFilmIdOrAll() {
        reviewService.create(review1);
        reviewService.addOrDeleteLikeOrDislike(1L, 2L, "like", "put");
        reviewService.addOrDeleteLikeOrDislike(1L, 3L, "dislike", "put");

        reviewService.create(review2);
        reviewService.addOrDeleteLikeOrDislike(2L, 2L, "like", "put");

        reviewService.create(review3);
        reviewService.addOrDeleteLikeOrDislike(3L, 3L, "dislike", "put");
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
