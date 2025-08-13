package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Profile("memory")
@Component
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Long, User> users = new HashMap<>();
    private Long id = 1L;
    private static final String WRONG_ID = "неверный номер ID";

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public List<User> getUsersByIds(Set<Long> ids) {
        return ids.stream()
                .filter(users::containsKey)
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public User create(User user) {
        user.setId(id++);
        user.setName(checkAndReturnName(user));
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User amend(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException(WRONG_ID);
        }
        user.setName(checkAndReturnName(user));
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException(WRONG_ID);
        }

        // Удаляем пользователя из списков друзей всех других пользователей
        for (User u : users.values()) {
            u.getFriends().remove(user.getId());
        }

        users.remove(user.getId());
    }

    @Override
    public User find(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException(WRONG_ID);
        }
        return users.get(id);
    }

    private String checkAndReturnName(User user) {
        return (user.getName() == null || user.getName().isBlank()) ? user.getLogin() : user.getName();
    }
}