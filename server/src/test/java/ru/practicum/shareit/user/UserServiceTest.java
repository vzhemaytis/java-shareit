package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exeption.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @InjectMocks
    UserServiceImpl userService;
    User user1;
    User user2;

    @BeforeEach
    void setup() {
        user1 = new User(
                1L,
                "name1",
                "email1@yandex.ru"
        );
        user2 = new User(
                2L,
                "name2",
                "email2@yandex.ru"
        );
    }

    @Test
    void getUsers_whenInvoked_thenReturnUserDtos() {
        Mockito.when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserDto> actualUsers = userService.getUsers();

        assertAll(
                () -> assertEquals(2, actualUsers.size()),
                () -> assertEquals(user1.getId(), actualUsers.get(0).getId()),
                () -> assertEquals(user2.getId(), actualUsers.get(1).getId())
                );
        verify(userRepository).findAll();
    }

    @Test
    void getUsers_whenUsersTableIsEmpty_thenReturnEmptyList() {
        Mockito.when(userRepository.findAll()).thenReturn(List.of());

        List<UserDto> users = userService.getUsers();

        assertTrue(users.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void findUser_whenUserFound_thenReturnUserDto() {
        Long userId = 1L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user1));

        UserDto actualUser = userService.findUser(userId);

        assertEquals(user1.getId(), actualUser.getId());
        verify(userRepository).findById(userId);
    }

    @Test
    void findUser_whenUserNotFound_thenThrowsNotFoundException() {
        Long userId = 1L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> userService.findUser(userId)
        );
        verify(userRepository).findById(userId);
    }

    @Test
    void addNewUser_whenInvoked_thenSaveNewUserAndReturnCorrectDto() {
        User userToSave = new User();
        userToSave.setName(user1.getName());
        userToSave.setEmail(user1.getEmail());
        UserDto userDtoToSave = UserMapper.toUserDto(userToSave);
        Mockito.when(userRepository.saveAndFlush(any())).thenReturn(user1);

        UserDto savedUserDto = userService.addNewUser(userDtoToSave);

        assertEquals(user1.getId(), savedUserDto.getId());
        verify(userRepository, atMostOnce()).saveAndFlush(any());
    }

    @Test
    void updateUser_whenNewNameAndEmailIsNull_thenReturnUserDtoWithNewName() {
        UserDto userToUpdate = new UserDto(
                1L,
                "new name",
                null
        );
        Mockito.when(userRepository.findById(userToUpdate.getId())).thenReturn(Optional.of(user1));
        user1.setName(userToUpdate.getName());
        Mockito.when(userRepository.saveAndFlush(user1)).thenReturn(user1);


        UserDto updatedUser = userService.updateUser(userToUpdate);

        assertAll(
                () -> assertEquals(user1.getId(), updatedUser.getId()),
                () -> assertEquals(user1.getName(), updatedUser.getName()),
                () -> assertEquals(user1.getEmail(), updatedUser.getEmail())
        );
        verify(userRepository).saveAndFlush(user1);
    }

    @Test
    void updateUser_whenNewEmailAndNameIsNull_thenReturnUserDtoWithNewEmail() {
        UserDto userToUpdate = new UserDto(
                1L,
                null,
                "newEmail@yandex.com"
        );
        Mockito.when(userRepository.findById(userToUpdate.getId())).thenReturn(Optional.of(user1));
        user1.setEmail(userToUpdate.getEmail());
        Mockito.when(userRepository.saveAndFlush(user1)).thenReturn(user1);


        UserDto updatedUser = userService.updateUser(userToUpdate);

        assertAll(
                () -> assertEquals(user1.getId(), updatedUser.getId()),
                () -> assertEquals(user1.getName(), updatedUser.getName()),
                () -> assertEquals(user1.getEmail(), updatedUser.getEmail())
        );
        verify(userRepository).saveAndFlush(user1);
    }

    @Test
    void updateUser_whenUserNotFound_thenThrowNotFoundException() {
        UserDto userToUpdate = new UserDto(
                1L,
                null,
                "newEmail@yandex.com"
        );
        Mockito.when(userRepository.findById(userToUpdate.getId())).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> userService.updateUser(userToUpdate)
                );
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void deleteUser_whenInvoked_thenInvokeUserRepositoryDeleteById() {
        Long userId = 1L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user1));

        userService.deleteUser(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_whenUserNotFound_thenThrowsNotFoundException() {
        Long userId = 1L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> userService.deleteUser(userId)
        );
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void checkIfUserExist_whenUserFound_thenReturnUser() {
        Long userId = 1L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user1));

        User actualUser = userService.checkIfUserExist(userId);

        assertEquals(user1, actualUser);
        verify(userRepository).findById(userId);
    }

    @Test
    void checkIfUserExist_whenUserNotFound_thenThrowsNotFoundException() {
        Long userId = 1L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> userService.checkIfUserExist(userId)
        );
        verify(userRepository).findById(userId);
    }

}