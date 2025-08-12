package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Profile("db")
@Component
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        String name = (user.getName() == null || user.getName().isBlank()) ? user.getLogin() : user.getName();

        String sqlQuery = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, name);
            stmt.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        Long userId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return find(userId);
    }

    @Override
    public User amend(User user) {
        String sqlCheck = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        int count = jdbcTemplate.queryForObject(sqlCheck, Integer.class, user.getId());

        if (count == 0) {
            throw new NotFoundException("User not found");
        }

        String name = (user.getName() == null || user.getName().isBlank()) ? user.getLogin() : user.getName();

        String sqlQuery = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                name,
                user.getBirthday(),
                user.getId());

        updateFriendships(user);
        return find(user.getId());
    }

    @Override
    public void delete(User user) {
        String sqlQuery = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(sqlQuery, user.getId());
    }

    @Override
    public User find(Long id) {
        String sqlQuery = "SELECT * FROM users WHERE user_id = ?";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public List<User> findAll() {
        String sqlQuery = "SELECT * FROM users";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser);
    }

    @Override
    public List<User> getUsersByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        String sqlQuery = "SELECT * FROM users WHERE user_id IN (" +
                String.join(",", Collections.nCopies(ids.size(), "?")) + ")";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser, ids.toArray());
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        user.setFriends(getFriends(user.getId()));
        return user;
    }

    private Set<Long> getFriends(Long userId) {
        String sqlQuery = "SELECT friend_id FROM friendships WHERE user_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sqlQuery, Long.class, userId));
    }

    private void updateFriendships(User user) {
        String deleteQuery = "DELETE FROM friendships WHERE user_id = ?";
        jdbcTemplate.update(deleteQuery, user.getId());

        if (user.getFriends() != null && !user.getFriends().isEmpty()) {
            String insertQuery = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
            user.getFriends().forEach(friendId -> {
                jdbcTemplate.update(insertQuery, user.getId(), friendId, "pending");
            });
        }
    }
}
