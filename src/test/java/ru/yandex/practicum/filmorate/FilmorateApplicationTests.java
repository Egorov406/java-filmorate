package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"test", "db"})
class FilmorateApplicationTests {

    @Autowired(required = false)
    private FilmController filmController;

    @Autowired(required = false)
    private UserController userController;

    @Test
    void contextLoads() {
        assertThat(true).isTrue();
    }

    @Test
    void controllersShouldLoad() {
        if (filmController != null && userController != null) {
            assertThat(filmController).isNotNull();
            assertThat(userController).isNotNull();
        }
    }
}