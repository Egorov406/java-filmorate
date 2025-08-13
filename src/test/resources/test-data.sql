-- Очистка всех данных
DELETE FROM film_likes;
DELETE FROM film_genres;
DELETE FROM films;
DELETE FROM friendships;
DELETE FROM users;
DELETE FROM genres;
DELETE FROM mpa_ratings;

-- Сброс sequence
ALTER TABLE mpa_ratings ALTER COLUMN mpa_id RESTART WITH 1;
ALTER TABLE genres ALTER COLUMN genre_id RESTART WITH 1;
ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1;
ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1;

-- MPA рейтинги
INSERT INTO mpa_ratings (mpa_name, description) VALUES
('G', 'General Audiences'),
('PG', 'Parental Guidance Suggested');

-- Жанры
INSERT INTO genres (genre_name) VALUES
('Комедия'),
('Драма'),
('Мультфильм');

-- Пользователи
INSERT INTO users (email, login, name, birthday) VALUES
('user@example.com', 'user1', 'User One', '1990-01-01');

-- Фильмы
INSERT INTO films (title, description, release_date, duration, mpa_id) VALUES
('Test Film', 'Test Description', '2000-01-01', 120, 1);