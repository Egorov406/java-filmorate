package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class FilmServiceTest {
    private FilmService filmService;
    private UserService userService;
    private Film testFilm;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        filmService = new FilmService(new InMemoryFilmStorage(), userService);

        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);

        // Мокируем вызовы UserService
        when(userService.getUserById(anyLong())).thenReturn(new User());
    }

    @Test
    void createFilm_ShouldAddFilmToStorage() {
        Film createdFilm = filmService.createFilm(testFilm);

        assertNotNull(createdFilm.getId());
        assertEquals(1, filmService.getAllFilms().size());
        assertEquals("Test Film", filmService.getAllFilms().get(0).getName());
    }

    @Test
    void createFilm_ShouldThrowException_WhenReleaseDateBefore1895() {
        testFilm.setReleaseDate(LocalDate.of(1890, 1, 1));

        assertThrows(ValidationException.class, () -> filmService.createFilm(testFilm));
    }

    @Test
    void updateFilm_ShouldUpdateExistingFilm() {
        Film createdFilm = filmService.createFilm(testFilm);
        createdFilm.setName("Updated Name");

        Film updatedFilm = filmService.updateFilm(createdFilm);

        assertEquals("Updated Name", updatedFilm.getName());
        assertEquals(1, filmService.getAllFilms().size());
    }

    @Test
    void updateFilm_ShouldThrowException_WhenFilmNotFound() {
        testFilm.setId(999L);

        assertThrows(NotFoundException.class, () -> filmService.updateFilm(testFilm));
    }

    @Test
    void getAllFilms_ShouldReturnEmptyList_WhenNoFilmsAdded() {
        List<Film> films = filmService.getAllFilms();

        assertTrue(films.isEmpty());
    }

    @Test
    void getAllFilms_ShouldReturnAllFilms() {
        filmService.createFilm(testFilm);
        Film anotherFilm = new Film();
        anotherFilm.setName("Another Film");
        anotherFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        anotherFilm.setDuration(90);
        filmService.createFilm(anotherFilm);

        List<Film> films = filmService.getAllFilms();

        assertEquals(2, films.size());
    }

    @Test
    void addLike_ShouldAddLikeToFilm() {
        Film createdFilm = filmService.createFilm(testFilm);
        filmService.addLike(createdFilm.getId(), 1L);

        assertEquals(1, filmService.getFilmById(createdFilm.getId()).getLikes().size());
        assertTrue(filmService.getFilmById(createdFilm.getId()).getLikes().contains(1L));
    }

    @Test
    void addLike_ShouldThrowException_WhenFilmNotFound() {
        assertThrows(NotFoundException.class, () -> filmService.addLike(999L, 1L));
    }

    @Test
    void deleteLike_ShouldRemoveLikeFromFilm() {
        Film createdFilm = filmService.createFilm(testFilm);
        filmService.addLike(createdFilm.getId(), 1L);
        filmService.deleteLike(createdFilm.getId(), 1L);

        assertEquals(0, filmService.getFilmById(createdFilm.getId()).getLikes().size());
    }

    @Test
    void deleteLike_ShouldThrowException_WhenLikeNotFound() {
        Film createdFilm = filmService.createFilm(testFilm);
        assertThrows(NotFoundException.class, () -> filmService.deleteLike(createdFilm.getId(), 1L));
    }

    @Test
    void getPopularFilms_ShouldReturnMostLikedFilms() {
        Film film1 = filmService.createFilm(testFilm);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(90);
        Film createdFilm2 = filmService.createFilm(film2);

        filmService.addLike(film1.getId(), 1L);
        filmService.addLike(film1.getId(), 2L);
        filmService.addLike(createdFilm2.getId(), 1L);

        List<Film> popularFilms = filmService.getPopularFilms(1);
        assertEquals(1, popularFilms.size());
        assertEquals(film1.getId(), popularFilms.get(0).getId());
    }

    @Test
    void getPopularFilms_ShouldReturnDefaultCount_WhenCountNotSpecified() {
        for (int i = 0; i < 15; i++) {
            Film film = new Film();
            film.setName("Film " + i);
            film.setReleaseDate(LocalDate.of(2000 + i, 1, 1));
            film.setDuration(90 + i);
            filmService.createFilm(film);
        }

        List<Film> popularFilms = filmService.getPopularFilms(null);
        assertEquals(10, popularFilms.size());
    }

    @Test
    void deleteFilm_ShouldRemoveFilmFromStorage() {
        Film createdFilm = filmService.createFilm(testFilm);
        assertEquals(1, filmService.getAllFilms().size());

        filmService.deleteFilm(createdFilm.getId());

        assertEquals(0, filmService.getAllFilms().size());
    }

    @Test
    void deleteFilm_ShouldRemoveAllLikes() {
        Film createdFilm = filmService.createFilm(testFilm);
        filmService.addLike(createdFilm.getId(), 1L);
        filmService.addLike(createdFilm.getId(), 2L);
        assertEquals(2, filmService.getFilmById(createdFilm.getId()).getLikes().size());

        filmService.deleteFilm(createdFilm.getId());

        assertThrows(NotFoundException.class, () -> filmService.getFilmById(createdFilm.getId()));
    }

    @Test
    void deleteFilm_ShouldThrowException_WhenFilmNotFound() {
        assertThrows(NotFoundException.class, () -> filmService.deleteFilm(999L));
    }

}
