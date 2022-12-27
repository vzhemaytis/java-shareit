package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getUsers();

    UserDto findUser(Long id);

    UserDto addNewUser(UserDto userDto);

    UserDto updateUser(UserDto userDto);

    void deleteUser(Long id);
}
