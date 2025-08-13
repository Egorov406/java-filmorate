package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new UserDbStorage(jdbcTemplate);
    }

    @Test
    void shouldCreateAndFindUser() {
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setLogin("newLogin");
        newUser.setName("New User");
        newUser.setBirthday(LocalDate.of(1995, 5, 5));

        User createdUser = userStorage.create(newUser);

        assertThat(createdUser.getId()).isNotNull();
        assertThat(userStorage.find(createdUser.getId()).getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void shouldUpdateUser() {
        User userToUpdate = userStorage.find(1L);
        userToUpdate.setName("Updated Name");

        User updatedUser = userStorage.amend(userToUpdate);

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(userStorage.find(1L).getName()).isEqualTo("Updated Name");
    }

    @Test
    void shouldFindAllUsers() {
        List<User> users = userStorage.findAll();
        assertThat(users).hasSize(1); // Согласно test-data.sql
    }

    @Test
    void shouldAddAndFindFriends() {
        User user1 = userStorage.find(1L);
        User user2 = new User();
        user2.setEmail("friend@example.com");
        user2.setLogin("friendLogin");
        user2.setName("Friend");
        user2.setBirthday(LocalDate.of(1990, 1, 1));
        User createdFriend = userStorage.create(user2);

        user1.getFriends().add(createdFriend.getId());
        userStorage.amend(user1);

        assertThat(userStorage.find(1L).getFriends()).contains(createdFriend.getId());
    }

    @Test
    void shouldFindUsersByIds() {
        User user1 = userStorage.find(1L);
        User user2 = new User();
        user2.setEmail("second@example.com");
        user2.setLogin("secondLogin");
        user2.setName("Second User");
        user2.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser2 = userStorage.create(user2);

        List<User> users = userStorage.getUsersByIds(Set.of(user1.getId(), createdUser2.getId()));
        assertThat(users).hasSize(2);
    }
}