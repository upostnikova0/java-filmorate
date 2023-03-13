# java-filmorate
Template repository for Filmorate project.

![DB diagram](src/main/resources/diagram/dbdiagram.pdf)

Запрос на получение id всех пользователей:
SELECT id
FROM user

Запрос на получение всех названий фильмов с рейтингом 'NC-17'
SELECT name
FROM film
WHERE rating = 'NC-17'

Запрос на получение ТОП-5 лучших фильмов:
SELECT name
FROM film
ORDER BY likes DESC
LIMIT 5