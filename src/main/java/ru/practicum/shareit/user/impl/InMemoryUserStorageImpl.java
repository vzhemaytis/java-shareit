package ru.practicum.shareit.user.impl;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exeption.DuplicateEmailException;
import ru.practicum.shareit.exeption.EntityNotFoundException;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryUserStorageImpl implements UserStorage {
    private Long id = 1L;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public List<User> getUsers() {
        return List.copyOf(users.values());
    }

    @Override
    public User findUser(Long id) {
        checkUserExist(id);
        return users.get(id);
    }

    @Override
    public User addNewUser(User user) {
        checkEmailDuplicated(user);
        user.setId(getId());
        users.put(user.getId(), user);
        return users.get(user.getId());
    }

    @Override
    public User updateUser(User user) {
        Long userId = user.getId();
        checkUserExist(userId);
        User userToUpdate = users.get(userId);
        if (user.getName() != null) {
            userToUpdate.setName(user.getName());
        }
        if (user.getEmail() != null) {
            checkEmailDuplicated(user);
            userToUpdate.setEmail(user.getEmail());
        }
        return users.get(userId);
    }

    @Override
    public void deleteUser(Long id) {
        checkUserExist(id);
        users.remove(id);
    }

    private Long getId() {
        return id++;
    }

    private void checkEmailDuplicated (User user) {
        Optional<User> duplicateEmailUser = users.values().stream().
                filter(u -> u.getEmail().equals(user.getEmail())).findFirst();
        if (duplicateEmailUser.isPresent()) {
            throw new DuplicateEmailException(
                    String.format("Email %s is already used by other user", user.getEmail()));
        }
    }

    private void checkUserExist(Long id) {
        if (!users.containsKey(id)) {
            throw new EntityNotFoundException(String.format("%s with id= %s not found", User.class, id));
        }
    }
}
