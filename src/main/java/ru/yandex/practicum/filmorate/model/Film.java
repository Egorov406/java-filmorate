package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
@Slf4j
@Data
@AllArgsConstructor
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;

    public void validate() {
        if (getName() == null || getName().isBlank()) {
            log.error("Валидация не пройдена: название фильма не может быть пустым, name={}", name);
            throw new ValidationException("Название не может быть пустым");
        }

        if (getDescription().length() > 200) {
            log.error("Валидация не пройдена: длина описания превышает 200 символов, description.length={}", description.length());
            throw new ValidationException("Максимальная длина описания не может быть более 200 символов");
        }

        if (getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Валидация не пройдена: дата релиза не может быть ранее 28 декабря 1895 года или null, releaseDate={}", releaseDate);
            throw new ValidationException("Дата релиза не может быть ранее 28 декабря 1895 года");
        }

        if (getDuration() < 0) {
            log.error("Валидация не пройдена: продолжительность фильма должна быть положительным числом, duration={}", duration);
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

    }
}