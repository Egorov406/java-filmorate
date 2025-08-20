package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreService genreService;
    private final MpaService mpaService;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, UserService userService,
                       GenreService genreService,
                       MpaService mpaService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.genreService = genreService;
        this.mpaService = mpaService;
    }

    public Film createFilm(Film film) {
        validateFilm(film);
        validateMpa(film.getMpa());
        validateGenres(film.getGenres());
        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {
        validateFilm(film);
        validateMpa(film.getMpa());
        validateGenres(film.getGenres());
        return filmStorage.amend(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.findAll();
    }

    public Film getFilmById(Long id) {
        return filmStorage.find(id);
    }

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.find(filmId);
        userService.getUserById(userId); // Проверка существования пользователя
        film.getLikes().add(userId);
        filmStorage.amend(film);
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = filmStorage.find(filmId);
        if (!film.getLikes().remove(userId)) {
            throw new NotFoundException("Лайк не найден");
        }
        filmStorage.amend(film);
    }

    public void deleteFilm(Long id) {
        Film film = filmStorage.find(id);
        filmStorage.delete(film);
    }

    public List<Film> getPopularFilms(Integer count) {
        int filmsCount = count == null ? 10 : count;
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(f -> -f.getLikes().size()))
                .limit(filmsCount)
                .collect(Collectors.toList());
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    private void validateMpa(MpaRating mpa) {
        if (mpa == null || mpa.getId() == null) {
            throw new ValidationException("MPA рейтинг обязателен");
        }
        try {
            mpaService.getMpaRatingById(mpa.getId());
        } catch (NotFoundException e) {
            throw new NotFoundException("Указан несуществующий MPA рейтинг");
        }
    }

    private void validateGenres(List<Genre> genres) {
        if (genres != null) {
            List<Genre> uniqueGenres = genres.stream()
                    .distinct()
                    .collect(Collectors.toList());

            genres.clear();
            genres.addAll(uniqueGenres);

            for (Genre genre : genres) {
                try {
                    genreService.getGenreById(genre.getId());
                } catch (NotFoundException e) {
                    throw new NotFoundException("Указан несуществующий жанр с id " + genre.getId());
                }
            }
        }
    }
}
