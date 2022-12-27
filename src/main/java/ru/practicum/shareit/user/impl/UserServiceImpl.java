package ru.practicum.shareit.user.impl;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public List<UserDto> getUsers() {
        return userStorage.getUsers()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto findUser(Long id) {
        return UserMapper.toUserDto(userStorage.findUser(id));
    }

    @Override
    public UserDto addNewUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userStorage.addNewUser(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userStorage.updateUser(user));
    }

    @Override
    public void deleteUser(Long id) {
        userStorage.deleteUser(id);
    }
}
