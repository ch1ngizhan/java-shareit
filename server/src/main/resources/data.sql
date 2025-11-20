-- Тестовые пользователи
INSERT INTO users (name, email)
VALUES ('John Doe', 'john@example.com'),
       ('Jane Smith', 'jane@example.com'),
       ('Bob Johnson', 'bob@example.com');

-- Тестовые запросы
INSERT INTO requests (description, requestor_id, created)
VALUES ('Нужна дрель', 1, '2024-01-15 10:00:00'),
       ('Ищу палатку для похода', 2, '2024-01-16 14:30:00');

-- Тестовые вещи
INSERT INTO items (name, description, available, owner_id, request_id)
VALUES ('Дрель', 'Мощная дрель с набором сверл', true, 2, 1),
       ('Палатка', '4-х местная палатка', true, 3, 2),
       ('Молоток', 'Строительный молоток', true, 1, NULL);

-- Тестовые бронирования
INSERT INTO bookings (start_date, end_date, item_id, booker_id, status)
VALUES ('2024-01-20 10:00:00', '2024-01-25 18:00:00', 1, 3, 'APPROVED'),
       ('2024-02-01 09:00:00', '2024-02-05 20:00:00', 2, 1, 'WAITING');