package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService userService;
    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(new InMemoryUserStorage());

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setLogin("testLogin");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void createUser_ShouldAddUserToStorage() {
        User createdUser = userService.createUser(testUser);

        assertNotNull(createdUser.getId());
        assertEquals(1, userService.getAllUsers().size());
        assertEquals("test@example.com", userService.getAllUsers().get(0).getEmail());
    }

    @Test
    void createUser_ShouldSetLoginAsName_WhenNameIsEmpty() {
        testUser.setName("");

        User createdUser = userService.createUser(testUser);

        assertEquals("testLogin", createdUser.getName());
    }

    @Test
    void updateUser_ShouldUpdateExistingUser() {
        User createdUser = userService.createUser(testUser);
        createdUser.setEmail("updated@example.com");

        User updatedUser = userService.updateUser(createdUser);

        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals(1, userService.getAllUsers().size());
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        testUser.setId(999L);

        assertThrows(NotFoundException.class, () -> userService.updateUser(testUser));
    }

    @Test
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsersAdded() {
        List<User> users = userService.getAllUsers();

        assertTrue(users.isEmpty());
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        userService.createUser(testUser);
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setLogin("anotherLogin");
        userService.createUser(anotherUser);

        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
    }

    @Test
    void addFriends_ShouldAddFriendToBothUsers() {
        User user1 = userService.createUser(testUser);
        User user2 = new User();
        user2.setEmail("friend@example.com");
        user2.setLogin("friendLogin");
        user2 = userService.createUser(user2);

        userService.addFriends(user1.getId(), user2.getId());

        assertTrue(userService.getUserById(user1.getId()).getFriends().contains(user2.getId()));
        assertTrue(userService.getUserById(user2.getId()).getFriends().contains(user1.getId()));
    }

    @Test
    void addFriends_ShouldThrowException_WhenUserNotFound() {
        User user = userService.createUser(testUser);
        assertThrows(NotFoundException.class, () -> userService.addFriends(user.getId(), 999L));
    }

    @Test
    void deleteFriends_ShouldRemoveFriendFromBothUsers() {
        User user1 = userService.createUser(testUser);
        User user2 = new User();
        user2.setEmail("friend@example.com");
        user2.setLogin("friendLogin");
        user2 = userService.createUser(user2);

        userService.addFriends(user1.getId(), user2.getId());
        userService.deleteFriends(user1.getId(), user2.getId());

        assertFalse(userService.getUserById(user1.getId()).getFriends().contains(user2.getId()));
        assertFalse(userService.getUserById(user2.getId()).getFriends().contains(user1.getId()));
    }

    @Test
    void getFriends_ShouldReturnUserFriends() {
        User user = userService.createUser(testUser);
        User friend1 = new User();
        friend1.setEmail("friend1@example.com");
        friend1.setLogin("friend1Login");
        friend1 = userService.createUser(friend1);
        User friend2 = new User();
        friend2.setEmail("friend2@example.com");
        friend2.setLogin("friend2Login");
        friend2 = userService.createUser(friend2);

        userService.addFriends(user.getId(), friend1.getId());
        userService.addFriends(user.getId(), friend2.getId());

        List<User> friends = userService.getFriends(user.getId());
        assertEquals(2, friends.size());
    }

    @Test
    void getMutualFriends_ShouldReturnCommonFriends() {
        User user1 = userService.createUser(testUser);
        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2Login");
        user2 = userService.createUser(user2);
        User commonFriend = new User();
        commonFriend.setEmail("common@example.com");
        commonFriend.setLogin("commonLogin");
        commonFriend = userService.createUser(commonFriend);

        userService.addFriends(user1.getId(), commonFriend.getId());
        userService.addFriends(user2.getId(), commonFriend.getId());

        List<User> mutualFriends = userService.getMutualFriends(user1.getId(), user2.getId());
        assertEquals(1, mutualFriends.size());
        assertEquals(commonFriend.getId(), mutualFriends.get(0).getId());
    }

    @Test
    void getMutualFriends_ShouldReturnEmptyList_WhenNoCommonFriends() {
        User user1 = userService.createUser(testUser);
        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2Login");
        user2 = userService.createUser(user2);

        List<User> mutualFriends = userService.getMutualFriends(user1.getId(), user2.getId());
        assertTrue(mutualFriends.isEmpty());
    }

    @Test
    void deleteUser_ShouldRemoveUserFromStorage() {
        User createdUser = userService.createUser(testUser);
        assertEquals(1, userService.getAllUsers().size());

        userService.deleteUser(createdUser.getId());

        assertEquals(0, userService.getAllUsers().size());
    }

    @Test
    void deleteUser_ShouldRemoveUserFromFriendsLists() {
        User user1 = userService.createUser(testUser);
        User user2 = new User();
        user2.setEmail("friend@example.com");
        user2.setLogin("friendLogin");
        user2.setBirthday(LocalDate.of(1995, 5, 15));
        user2 = userService.createUser(user2);

        userService.addFriends(user1.getId(), user2.getId());

        // Проверяем, что дружба установлена в обе стороны
        assertTrue(userService.getUserById(user1.getId()).getFriends().contains(user2.getId()));
        assertTrue(userService.getUserById(user2.getId()).getFriends().contains(user1.getId()));

        userService.deleteUser(user1.getId());

        // Проверяем, что user1 удален из друзей user2
        User remainingUser = userService.getUserById(user2.getId());
        assertFalse(remainingUser.getFriends().contains(user1.getId()),
                "Удаленный пользователь должен быть удален из списка друзей");

        // Проверяем, что user1 действительно удален
        assertThrows(NotFoundException.class, () -> userService.getUserById(user1.getId()));
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        assertThrows(NotFoundException.class, () -> userService.deleteUser(999L));
    }
}