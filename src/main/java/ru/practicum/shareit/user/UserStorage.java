package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    List<User> getUsers();

    User findUser(Long id);

    User addNewUser(User user);

    User updateUser(User user);

    void deleteUser(Long id);
}
