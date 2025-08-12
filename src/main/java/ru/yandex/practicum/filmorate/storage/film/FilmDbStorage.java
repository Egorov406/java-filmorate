package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Profile("db")
@Component
@Primary
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film create(Film film) {
        String sqlQuery = "INSERT INTO films (title, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId().intValue());
            return stmt;
        }, keyHolder);

        Long filmId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        insertFilmGenres(filmId, film.getGenres());
        return find(filmId);
    }

    @Override
    public void delete(Film film) {
        String sqlQuery = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, film.getId());
    }

    @Override
    public Film amend(Film film) {
        String sqlCheck = "SELECT COUNT(*) FROM films WHERE film_id = ?";
        int count = jdbcTemplate.queryForObject(sqlCheck, Integer.class, film.getId());

        if (count == 0) {
            throw new NotFoundException("Film not found");
        }

        String sqlQuery = "UPDATE films SET title = ?, description = ?, release_date = ?, duration = ?, " +
                "mpa_id = ? WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        updateFilmGenres(film);
        updateFilmLikes(film);
        return find(film.getId());
    }

    @Override
    public List<Film> findAll() {
        String sqlQuery = "SELECT * FROM films";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
    }

    @Override
    public Film find(Long id) {
        String sqlQuery = "SELECT * FROM films WHERE film_id = ?";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Film not found"));
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("title"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setLikes(getLikes(film.getId()));
        film.setGenres(getGenres(film.getId()));
        film.setMpa(getMpaRatingById(rs.getLong("mpa_id")));
        return film;
    }

    private MpaRating getMpaRatingById(Long id) {
        String sqlQuery = "SELECT * FROM mpa_ratings WHERE mpa_id = ?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) ->
                        new MpaRating(rs.getLong("mpa_id"), rs.getString("mpa_name"), rs.getString("description")), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("MPA rating not found"));
    }

    private List<Genre> getGenres(Long filmId) {
        String sqlQuery = "SELECT g.genre_id, g.genre_name FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) ->
                new Genre(rs.getLong("genre_id"), rs.getString("genre_name")), filmId);
    }

    private Set<Long> getLikes(Long filmId) {
        String sqlQuery = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sqlQuery, Long.class, filmId));
    }

    private void insertFilmGenres(Long filmId, List<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        String sqlQuery = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        genres.forEach(genre -> jdbcTemplate.update(sqlQuery, filmId, genre.getId()));
    }

    private void updateFilmGenres(Film film) {
        String deleteQuery = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteQuery, film.getId());

        // Удаляем дубликаты перед вставкой
        List<Genre> uniqueGenres = film.getGenres().stream()
                .distinct()
                .collect(Collectors.toList());

        insertFilmGenres(film.getId(), uniqueGenres);
    }

    private void updateFilmLikes(Film film) {
        String deleteQuery = "DELETE FROM film_likes WHERE film_id = ?";
        jdbcTemplate.update(deleteQuery, film.getId());

        if (film.getLikes() != null && !film.getLikes().isEmpty()) {
            String insertQuery = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
            film.getLikes().forEach(userId -> jdbcTemplate.update(insertQuery, film.getId(), userId));
        }
    }
}
