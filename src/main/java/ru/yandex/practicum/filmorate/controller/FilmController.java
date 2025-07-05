package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {

        film.validate();
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Создан фильм: {}", film);
        return film;
    }

    @PutMapping()
    public Film update(@RequestBody Film film) {

        film.validate();

        Film filmInMemory = films.get(film.getId());
        filmInMemory.setName(film.getName());
        filmInMemory.setDescription(film.getDescription());
        filmInMemory.setReleaseDate(film.getReleaseDate());
        filmInMemory.setDuration(film.getDuration());
        log.info("Обновлён фильм: {}", filmInMemory);
        return filmInMemory;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long nextId = ++ currentMaxId;
        log.debug("Сгенерирован новый ID для фильма: {}", nextId);
        return nextId;
    }
}
