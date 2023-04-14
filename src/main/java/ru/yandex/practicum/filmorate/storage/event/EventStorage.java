package ru.yandex.practicum.filmorate.storage.event;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.Collection;

public interface EventStorage {
    Event add(Event event);

    Collection<Event> findAll(long userId);
}
