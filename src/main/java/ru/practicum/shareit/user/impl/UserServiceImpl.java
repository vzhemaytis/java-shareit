package ru.practicum.shareit.user.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exeption.EntityNotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Transactional
    @Override
    public List<UserDto> getUsers() {
        return repository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserDto findUser(Long id) {
        Optional<User> user = repository.findById(id);
        if (user.isEmpty()) {
            throw new EntityNotFoundException(
                    String.format("%s with id= %s not found", User.class.getSimpleName(), id));
        }
        return UserMapper.toUserDto(user.get());
    }

    @Transactional
    @Override
    public UserDto addNewUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(repository.save(user));
    }

    @Transactional
    @Override
    public UserDto updateUser(UserDto userDto) {
        Long userId = userDto.getId();
        Optional<User> userToUpdate = repository.findById(userId);
        if (userToUpdate.isEmpty()) {
            throw new EntityNotFoundException(
                    String.format("%s with id= %s not found", User.class.getSimpleName(), userId));
        }
        User existUser = userToUpdate.get();
        User user = UserMapper.toUser(userDto);
        if (user.getName() != null) {
            existUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            existUser.setEmail(user.getEmail());
        }
        return UserMapper.toUserDto(repository.save(existUser));
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        repository.deleteById(id);
    }
}
