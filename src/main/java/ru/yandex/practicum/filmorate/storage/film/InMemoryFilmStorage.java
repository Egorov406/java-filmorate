package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Profile("memory")
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final HashMap<Long, Film> films = new HashMap<>();
    private Long id = 1L;
    private static final String WRONG_ID = "нет фильма с таким id";

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film find(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException(WRONG_ID);
        }
        return films.get(id);
    }

    @Override
    public Film create(Film film) {
        film.setId(id++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film amend(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException(WRONG_ID);
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void delete(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException(WRONG_ID);
        }
        films.remove(film.getId());
    }
}
