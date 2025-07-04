package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        user.validate();
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping()
    public User update(@RequestBody User user) {
        user.validate();
        User userInMemory = users.get(user.getId());
        userInMemory.setEmail(user.getEmail());
        userInMemory.setLogin(user.getLogin());
        if (user.getName() == null || user.getName().isBlank()) {
            userInMemory.setName(user.getLogin());
        } else {
            userInMemory.setName(user.getName());
        }
        userInMemory.setBirthday(user.getBirthday());

        return user;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
