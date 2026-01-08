-- Таблица пользователей
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE, -- NULL для студентов, обязателен для учителей
    login VARCHAR(255) UNIQUE, -- NULL для учителей, обязателен для студентов (уникальный логин)
    first_name VARCHAR(255) NOT NULL, -- Имя
    last_name VARCHAR(255) NOT NULL, -- Фамилия
    role VARCHAR(20) NOT NULL CHECK (role IN ('ROLE_STUDENT', 'ROLE_TEACHER')),
    password_hash VARCHAR(255) NOT NULL, -- Обязателен для всех пользователей
    class_id INTEGER, -- NULL для учителей, обязателен для студентов (выбирается при регистрации)
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица классов
CREATE TABLE classes (
    id SERIAL PRIMARY KEY,
    teacher_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    invitation_code VARCHAR(20) UNIQUE NOT NULL,
    active_dictionary_id INTEGER, -- NULL если не назначен
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Добавляем внешний ключ для class_id в users после создания таблицы classes
ALTER TABLE users ADD CONSTRAINT fk_users_class 
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE SET NULL;

-- Ограничения безопасности для ролей
-- Учитель НЕ может быть в классе (class_id должен быть NULL)
ALTER TABLE users ADD CONSTRAINT check_teacher_no_class 
    CHECK (role != 'ROLE_TEACHER' OR class_id IS NULL);

-- Учитель ДОЛЖЕН иметь email
ALTER TABLE users ADD CONSTRAINT check_teacher_has_email 
    CHECK (role != 'ROLE_TEACHER' OR email IS NOT NULL);

-- Учитель НЕ должен иметь login
ALTER TABLE users ADD CONSTRAINT check_teacher_no_login 
    CHECK (role != 'ROLE_TEACHER' OR login IS NULL);

-- Студент ДОЛЖЕН иметь login
ALTER TABLE users ADD CONSTRAINT check_student_has_login 
    CHECK (role != 'ROLE_STUDENT' OR login IS NOT NULL);

-- Студент ДОЛЖЕН быть привязан к классу (class_id обязателен)
ALTER TABLE users ADD CONSTRAINT check_student_has_class 
    CHECK (role != 'ROLE_STUDENT' OR class_id IS NOT NULL);

-- Таблица словарей
CREATE TABLE dictionaries (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    theme VARCHAR(100), -- природа, литература, и т.д.
    created_by INTEGER REFERENCES users(id) ON DELETE SET NULL, -- учитель, создавший словарь
    is_global BOOLEAN DEFAULT FALSE, -- глобальный словарь или пользовательский
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица слов в словарях
CREATE TABLE dictionary_words (
    id SERIAL PRIMARY KEY,
    dictionary_id INTEGER NOT NULL REFERENCES dictionaries(id) ON DELETE CASCADE,
    word VARCHAR(50) NOT NULL CHECK (LENGTH(word) = 5), -- только 5-буквенные слова
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(dictionary_id, word)
);

-- Таблица ежедневных вызовов
CREATE TABLE daily_challenges (
    id SERIAL PRIMARY KEY,
    date DATE NOT NULL,
    word VARCHAR(50) NOT NULL CHECK (LENGTH(word) = 5),
    dictionary_id INTEGER NOT NULL REFERENCES dictionaries(id) ON DELETE RESTRICT,
    class_id INTEGER REFERENCES classes(id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'completed', 'expired')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Ограничение: один вызов на пользователя в день (если user_id указан)
    CONSTRAINT uq_date_user UNIQUE(date, user_id)
);

-- Ограничение: один вызов на класс в день (если class_id указан и user_id NULL)
CREATE UNIQUE INDEX uq_date_class_null_user
ON daily_challenges (date, class_id)
WHERE user_id IS NULL;

-- Таблица попыток угадывания
CREATE TABLE attempts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    challenge_id INTEGER NOT NULL REFERENCES daily_challenges(id) ON DELETE CASCADE,
    attempt_number INTEGER NOT NULL CHECK (attempt_number >= 1 AND attempt_number <= 6),
    guessed_word VARCHAR(50) NOT NULL CHECK (LENGTH(guessed_word) = 5),
    result JSONB NOT NULL, -- {"positions": [{"letter": "A", "color": "green", "position": 0}, ...]}
    points INTEGER DEFAULT 0, -- очки за эту попытку
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Ограничение: максимум 6 попыток на вызов для пользователя
    UNIQUE(user_id, challenge_id, attempt_number)
);

-- Таблица рейтингов
CREATE TABLE rankings (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    class_id INTEGER REFERENCES classes(id) ON DELETE CASCADE, -- NULL для глобального рейтинга
    date DATE NOT NULL,
    daily_points INTEGER DEFAULT 0,
    total_points INTEGER DEFAULT 0, -- накопительные очки
    current_streak INTEGER DEFAULT 0, -- текущая серия дней подряд
    longest_streak INTEGER DEFAULT 0, -- максимальная серия
    global_rank INTEGER, -- глобальное место
    class_rank INTEGER, -- место в классе
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, date)
);

-- Таблица статистики по словам (для анализа учителем)
CREATE TABLE word_statistics (
    id SERIAL PRIMARY KEY,
    word VARCHAR(50) NOT NULL,
    class_id INTEGER REFERENCES classes(id) ON DELETE CASCADE,
    total_attempts INTEGER DEFAULT 0,
    successful_attempts INTEGER DEFAULT 0,
    average_attempts_to_solve DECIMAL(4,2), -- среднее количество попыток до решения
    date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для оптимизации запросов
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_login ON users(login); -- Для быстрого поиска студентов по логину
CREATE INDEX idx_users_class_id ON users(class_id);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_first_last_name ON users(first_name, last_name); -- Для поиска студентов по имени и фамилии
CREATE INDEX idx_classes_teacher_id ON classes(teacher_id);
CREATE INDEX idx_classes_invitation_code ON classes(invitation_code);
CREATE INDEX idx_daily_challenges_date ON daily_challenges(date);
CREATE INDEX idx_daily_challenges_class_id ON daily_challenges(class_id);
CREATE INDEX idx_daily_challenges_user_id ON daily_challenges(user_id);
CREATE INDEX idx_attempts_user_id ON attempts(user_id);
CREATE INDEX idx_attempts_challenge_id ON attempts(challenge_id);
CREATE INDEX idx_rankings_user_id ON rankings(user_id);
CREATE INDEX idx_rankings_class_id ON rankings(class_id);
CREATE INDEX idx_rankings_date ON rankings(date);
CREATE INDEX idx_dictionary_words_dictionary_id ON dictionary_words(dictionary_id);

