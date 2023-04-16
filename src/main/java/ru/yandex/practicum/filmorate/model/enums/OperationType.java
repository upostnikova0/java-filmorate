package ru.yandex.practicum.filmorate.model.enums;

public enum OperationType {
    ADD("ADD"),
    REMOVE("REMOVE"),
    UPDATE("UPDATE");

    private String name;

    OperationType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
