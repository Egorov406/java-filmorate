package ru.yandex.practicum.filmorate;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class FilmorateApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	public void testEmptyFieldValidationFilmName() {
		Film film = new Film(1L, "", "Описание", LocalDate.now(), 1000);

		Exception exception = assertThrows(ValidationException.class, () -> film.validate());

		assertEquals("Название не может быть пустым", exception.getMessage());
	}

	@Test
	public void testEmptyFieldValidationFilmDuration() {
		Film film = new Film(1L, "Наименование", "Описание", LocalDate.now(), -1000);

		Exception exception = assertThrows(ValidationException.class, () -> film.validate());

		assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
	}

	@Test
	public void testEmptyFieldValidationUserLogin() {
		User user = new User(1L, "mail@mail.ru", "", "name", LocalDate.now());

		Exception exception = assertThrows(ValidationException.class, () -> user.validate());

		assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
	}

	@Test
	public void testEmptyFieldValidationUserEmail() {
		User user = new User(1L, "mailmail.ru", "login", "name", LocalDate.now());

		Exception exception = assertThrows(ValidationException.class, () -> user.validate());

		assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
	}

}
