package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Slf4j
@Service
public class DirectorService {
    private final DirectorStorage directorStorage;

    public DirectorService(@Qualifier("directorsDbStorage") DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Director create(Director director) {
        return directorStorage.add(director);
    }

    public Collection<Director> getAll() {
        return directorStorage.findAll();
    }

    public Director getDirector(long directorId) {
        Director director = directorStorage.find(directorId);

        log.info("Режиссер найден: {}", director);
        return director;
    }

    public Director update(Director director) {
        directorStorage.find(director.getId());
        return directorStorage.update(director);
    }

    public void remove(long directorId) {
        Director director = directorStorage.find(directorId);

        directorStorage.remove(director);
    }
}
