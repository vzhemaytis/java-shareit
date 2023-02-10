package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exeption.NotFoundException;
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
        return UserMapper.toUserDto(checkIfUserExist(id));
    }

    @Transactional
    @Override
    public UserDto addNewUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(repository.saveAndFlush(user));
    }

    @Transactional
    @Override
    public UserDto updateUser(UserDto userDto) {
        Long userId = userDto.getId();
        User userToUpdate = checkIfUserExist(userId);
        if (userDto.getName() != null) {
            userToUpdate.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            userToUpdate.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(repository.saveAndFlush(userToUpdate));
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        User userToDelete = checkIfUserExist(id);
        repository.deleteById(userToDelete.getId());
    }

    @Transactional
    @Override
    public User checkIfUserExist(Long id) {
        Optional<User> user = repository.findById(id);
        if (user.isEmpty()) {
            throw new NotFoundException(
                    String.format("%s with id= %s not found", User.class.getSimpleName(), id));
        }
        return user.get();
    }
}
