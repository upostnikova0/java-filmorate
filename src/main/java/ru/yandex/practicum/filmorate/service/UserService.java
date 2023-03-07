package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    protected final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        return userStorage.add(user);
    }

    public User getUser(long id) {
        return userStorage.findUser(id);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public void addFriend(long id, long friendId) {
        User user = userStorage.findUser(id);
        User friend = userStorage.findUser(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(id);
    }

    public void deleteFriend(long id, long friendId) {
        User user = userStorage.findUser(id);
        User friend = userStorage.findUser(friendId);
        friend.getFriends().remove(user.getId());
        user.getFriends().remove(friend.getId());
    }

    public Set<User> getAllFriends(long id) {
        Set<User> friends = new LinkedHashSet<>();
        User user = userStorage.findUser(id);
        user.getFriends().forEach(x -> friends.add(getUser(x)));
        return friends;
    }

    public Set<User> getCommonFriends(long id, long friendId) {
        User user = userStorage.findUser(id);
        User friend = userStorage.findUser(friendId);
        Set<User> common = new LinkedHashSet<>();
        user.getFriends().stream()
                .filter(friend.getFriends()::contains)
                .forEach(x -> common.add(getUser(x)));
        return common;
    }
}
