package ru.yandex.practicum.filmorate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private FilmDbStorage filmStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new FilmDbStorage(jdbcTemplate);
    }

    @Test
    void shouldCreateAndFindFilm() {
        Film newFilm = new Film();
        newFilm.setName("New Film");
        newFilm.setDescription("New Description");
        newFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        newFilm.setDuration(120);
        newFilm.setMpa(new MpaRating(1L, "G", "General Audiences"));

        Film createdFilm = filmStorage.create(newFilm);

        assertThat(createdFilm.getId()).isNotNull();

        Film foundFilm = filmStorage.find(createdFilm.getId());
        assertThat(foundFilm.getName()).isEqualTo("New Film");
    }

    @Test
    void shouldUpdateFilm() {
        Film filmToUpdate = filmStorage.find(1L);
        filmToUpdate.setName("Updated Film Name");

        Film updatedFilm = filmStorage.amend(filmToUpdate);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film Name");
        assertThat(filmStorage.find(1L).getName()).isEqualTo("Updated Film Name");
    }

    @Test
    void shouldFindAllFilms() {
        List<Film> films = filmStorage.findAll();
        assertThat(films).hasSize(1); // Согласно test-data.sql
    }

    @Test
    void shouldAddAndFindFilmWithGenres() {
        Film film = filmStorage.find(1L);
        film.getGenres().add(new Genre(1L, "Комедия"));

        Film updatedFilm = filmStorage.amend(film);

        assertThat(updatedFilm.getGenres()).hasSize(1);
        assertThat(updatedFilm.getGenres().get(0).getName()).isEqualTo("Комедия");
    }
}
