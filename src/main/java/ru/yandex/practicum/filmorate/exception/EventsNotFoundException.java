package ru.yandex.practicum.filmorate.exception;

public class EventsNotFoundException extends RuntimeException {
    public EventsNotFoundException(String message) {
        super(message);
    }
}
