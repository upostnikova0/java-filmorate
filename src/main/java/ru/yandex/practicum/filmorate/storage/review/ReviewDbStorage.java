package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.*;

@Component("reviewDbStorage")
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Review> rowMapper = ((rs, rowNum) -> Review.builder()
            .reviewId(rs.getLong("review_id"))
            .content(rs.getString("contents"))
            .isPositive(rs.getBoolean("is_positive"))
            .userId(rs.getLong("user_id"))
            .filmId(rs.getLong("film_id"))
            .useful(rs.getInt("useful"))
            .build());

    private final ResultSetExtractor<Map<Long, Set<Long>>> likesDislikesExtractor = (rs) -> {
        Map<Long, Set<Long>> data = new HashMap<>();
        while (rs.next()) {
            long reviewId = rs.getLong("review_id");
            data.putIfAbsent(reviewId, new TreeSet<>());
            long userId = rs.getLong("user_id");
            data.get(reviewId).add(userId);
        }
        return data;
    };

    @Override
    public Review create(Review review) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("review_id");
        review.setReviewId(simpleJdbcInsert.executeAndReturnKey(review.toMap()).longValue());
        return review;
    }

    @Override
    public Optional<Review> findById(long reviewId) {
        String sqlQuery = "SELECT * FROM reviews WHERE review_id = ? AND deleted = false";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, rowMapper, reviewId));
//            review.setLikes(getReviewLikes(reviewId));
//            review.setDislikes(getReviewDislikes(reviewId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Review update(Review review) {
        String sqlQuery = "UPDATE reviews SET contents = ?, is_positive = ?" +
                "WHERE review_id = ?";
//        boolean isUpdated = jdbcTemplate.update(sqlQuery, review.getContent(), review.getIsPositive(),
//                review.getReviewId()) > 0;
//        if (isUpdated) {
        review.setUserId(getReviewUserId(review.getReviewId()));
        review.setFilmId(getReviewFilmId(review.getReviewId()));
////            review.setLikes(getReviewLikes(review.getReviewId()));
////            review.setDislikes(getReviewDislikes(review.getReviewId()));
        //review.setUseful();
//        }

        /////wdfsfsfsfsfsfsrfs
        //// удалить то что сверху
        jdbcTemplate.update(sqlQuery, review.getContent(), review.getIsPositive(), review.getReviewId());
        return review;
    }

    @Override
    public void delete(long reviewId) {
        String sqlQuery = "UPDATE reviews SET deleted = true " +
                "WHERE review_id = ?";
        jdbcTemplate.update(sqlQuery, reviewId);
    }

    @Override
    public void addOrDeleteLikeOrDislike(long reviewId, long userId, String likeOrDislike, String requestMethod) {
        String sqlQuery;
        String sqlQueryForUseful;
        if (requestMethod.equals("put")) {
            if (likeOrDislike.equals("like")) {
                sqlQuery = "INSERT INTO reviews_likes VALUES (?, ?)";
                sqlQueryForUseful = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";
            } else {
                sqlQuery = "INSERT INTO reviews_dislikes VALUES (?, ?)";
                sqlQueryForUseful = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
            }
        } else {
            if (likeOrDislike.equals("like")) {
                sqlQuery = "DELETE FROM reviews_likes WHERE review_id = ? AND user_id = ?";
                sqlQueryForUseful = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
            } else {
                sqlQuery = "DELETE FROM reviews_dislikes WHERE review_id = ? AND user_id = ?";
                sqlQueryForUseful = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";
            }
        }
        if (jdbcTemplate.update(sqlQuery, reviewId, userId) > 0) jdbcTemplate.update(sqlQueryForUseful, reviewId);
    }

    @Override
    public List<Review> findByFilmIdOrAll(long filmId, int count) {
        String sqlQuery;
        List<Review> reviews;
        if (filmId > 0) {
            sqlQuery = "SELECT * FROM reviews " +
                    "JOIN FILMS AS f ON reviews.film_id = f.film_id " +
                    "WHERE reviews.film_id = ? AND f.deleted = false AND reviews.deleted = false " +
                    "ORDER BY reviews.useful DESC, reviews.review_id LIMIT ?";
            reviews = jdbcTemplate.query(sqlQuery, rowMapper, filmId, count);
        } else {
            sqlQuery = "SELECT * FROM reviews " +
                    "JOIN FILMS AS f ON reviews.film_id = f.film_id " +
                    "WHERE f.deleted = false AND reviews.deleted = false " +
                    "ORDER BY reviews.useful DESC, reviews.review_id LIMIT ?";
            reviews = jdbcTemplate.query(sqlQuery, rowMapper, count);
        }

//        sqlQuery = "SELECT * FROM reviews_likes";
//        Map<Long, Set<Long>> likes = jdbcTemplate.query(sqlQuery, likesDislikesExtractor);
//
//        sqlQuery = "SELECT * FROM reviews_dislikes";
//        Map<Long, Set<Long>> dislikes = jdbcTemplate.query(sqlQuery, likesDislikesExtractor);

//        reviews.forEach(review -> {
//            Set<Long> reviewLikes = Optional.ofNullable(likes.get(review.getReviewId())).orElseGet(TreeSet::new);
//            Set<Long> reviewDislikes = Optional.ofNullable(dislikes.get(review.getReviewId())).orElseGet(TreeSet::new);
//            //review.setLikes(reviewLikes);
//            //review.setDislikes(reviewDislikes);
//        });
        return reviews;
    }

//    private Set<Long> getReviewLikes(long reviewId) {
//        String sqlQuery = "SELECT user_id FROM reviews_likes WHERE review_id = ?";
//        return new TreeSet<>(jdbcTemplate.queryForList(sqlQuery, Long.class, reviewId));
//    }
//
//    private Set<Long> getReviewDislikes(long reviewId) {
//        String sqlQuery = "SELECT user_id FROM reviews_dislikes WHERE review_id = ?";
//        return new TreeSet<>(jdbcTemplate.queryForList(sqlQuery, Long.class, reviewId));
//    }

    private Long getReviewUserId(long reviewId) {
        String sqlQuery = "SELECT user_id FROM reviews WHERE review_id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, Long.class, reviewId);
    }

    private Long getReviewFilmId(long reviewId) {
        String sqlQuery = "SELECT film_id FROM reviews WHERE review_id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, Long.class, reviewId);
    }
}
