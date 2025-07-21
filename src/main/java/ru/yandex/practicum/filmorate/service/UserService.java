package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.amend(user);
    }

    public List<User> getAllUsers() {
        return userStorage.findAll();
    }

    public User getUserById(Long id) {
        return userStorage.find(id);
    }

    public void addFriends(Long userId, Long friendId) {
        User user = userStorage.find(userId);
        User friend = userStorage.find(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        userStorage.amend(user);
        userStorage.amend(friend);
    }

    public void deleteFriends(Long userId, Long friendId) {
        User user = userStorage.find(userId);
        User friend = userStorage.find(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        userStorage.amend(user);
        userStorage.amend(friend);
    }

    public void deleteUser(Long id) {
        User user = userStorage.find(id);
        userStorage.delete(user);
    }

    public List<User> getFriends(Long userId) {
        User user = userStorage.find(userId);
        return userStorage.getUsersByIds(user.getFriends());
    }

    public List<User> getMutualFriends(Long userId, Long otherId) {
        User user = userStorage.find(userId);
        User otherUser = userStorage.find(otherId);

        Set<Long> mutualFriendsIds = new HashSet<>(user.getFriends());
        mutualFriendsIds.retainAll(otherUser.getFriends());

        return userStorage.getUsersByIds(mutualFriendsIds);
    }
}
