package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Запрос на получение всех пользователей. Количество пользователей: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        user.validate();
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя не указано, используется логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @PutMapping()
    public User update(@RequestBody User user) {
        user.validate();
        User userInMemory = users.get(user.getId());
        userInMemory.setEmail(user.getEmail());
        userInMemory.setLogin(user.getLogin());
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя не указано при обновлении, используется логин: {}", user.getLogin());
            userInMemory.setName(user.getLogin());
        } else {
            userInMemory.setName(user.getName());
        }
        userInMemory.setBirthday(user.getBirthday());
        log.info("Обновлён пользователь: {}", userInMemory);
        return user;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long nextId = ++currentMaxId;
        log.debug("Сгенерирован новый ID для пользователя: {}", nextId);
        return nextId;
    }
}
