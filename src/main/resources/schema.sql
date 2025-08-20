-- Удаление таблиц в случае, если созданы ранее
DROP TABLE IF EXISTS friendships CASCADE;
DROP TABLE IF EXISTS film_likes CASCADE;
DROP TABLE IF EXISTS film_genres CASCADE;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS mpa_ratings CASCADE;

-- Создание таблицы возрастных рейтингов (MPA)
CREATE TABLE IF NOT EXISTS mpa_ratings (
    mpa_id SERIAL PRIMARY KEY,
    mpa_name VARCHAR(10) NOT NULL UNIQUE,
    description VARCHAR(200)
);

-- Создание таблицы жанров
CREATE TABLE IF NOT EXISTS genres (
    genre_id SERIAL PRIMARY KEY,
    genre_name VARCHAR(20) NOT NULL UNIQUE
);

-- Создание таблицы фильмов
CREATE TABLE IF NOT EXISTS films (
    film_id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL CHECK (duration > 0),
    mpa_id INTEGER NOT NULL REFERENCES mpa_ratings(mpa_id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Создание индексов для таблицы фильмов
CREATE INDEX idx_films_release_date ON films(release_date);
CREATE INDEX idx_films_mpa_id ON films(mpa_id);

-- Создание таблицы связи фильмов и жанров
CREATE TABLE IF NOT EXISTS film_genres (
    film_id INTEGER NOT NULL REFERENCES films(film_id) ON DELETE CASCADE,
    genre_id INTEGER NOT NULL REFERENCES genres(genre_id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id)
);

-- Индекс для поиска фильмов по жанру
CREATE INDEX idx_film_genres_genre_id ON film_genres(genre_id);

-- Создание таблицы пользователей
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE CHECK (email ~* '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$'),
    login VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100),
    birthday DATE NOT NULL CHECK (birthday <= CURRENT_DATE),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для таблицы пользователей
CREATE INDEX idx_users_birthday ON users(birthday);
CREATE INDEX idx_users_login ON users(login);

-- Создание таблицы лайков фильмов
CREATE TABLE IF NOT EXISTS film_likes (
    film_id INTEGER NOT NULL REFERENCES films(film_id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (film_id, user_id)
);

-- Индексы для таблицы лайков
CREATE INDEX idx_film_likes_user_id ON film_likes(user_id);
CREATE INDEX idx_film_likes_created_at ON film_likes(created_at);

-- Создание таблицы дружеских связей
CREATE TABLE IF NOT EXISTS friendships (
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    friend_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    status VARCHAR(10) DEFAULT 'pending' NOT NULL CHECK (status IN ('pending', 'confirmed')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, friend_id),
    CHECK (user_id < friend_id)
);

-- Индексы для таблицы друзей
CREATE INDEX idx_friendships_friend_id ON friendships(friend_id);
CREATE INDEX idx_friendships_status ON friendships(status);