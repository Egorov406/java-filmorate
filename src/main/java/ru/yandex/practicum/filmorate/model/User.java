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
public class User {
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;

    public void validate() {
        if (getEmail() == null || getEmail().isBlank() || !getEmail().contains("@")) {
            log.error("Валидация не пройдена: электронная почта не может быть пустой и должна содержать символ @, email={}", email);
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }

        if (getLogin() == null || getLogin().isBlank() || getLogin().contains(" ")) {
            log.error("Валидация не пройдена: логин не может быть пустым и содержать пробелы, login={}", login);
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        if (name == null || name.isBlank()) {
            log.info("Имя пользователя пустое или null, используется логин: {}", login);
            name = login;
        }

        if (getBirthday().isAfter(LocalDate.now())) {
            log.error("Валидация не пройдена: дата рождения не может быть в будущем, birthday={}", birthday);
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
    }
}
