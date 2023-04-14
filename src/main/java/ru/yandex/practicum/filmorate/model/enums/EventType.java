package ru.yandex.practicum.filmorate.model.enums;

public enum EventType {
    LIKE("LIKE"),
    REVIEW("REVIEW"),
    FRIEND("FRIEND");

    private String name;
    EventType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
