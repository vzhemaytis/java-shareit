package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> getUsers() {
        return userService.getUsers();
    }


    @GetMapping("/{id}")
    public UserDto findUser(@PathVariable("id") Long id) {
        log.info("get user with id = {}", id);
        return userService.findUser(id);
    }

    @PostMapping
    public UserDto addNewUser(@RequestBody UserDto userDto) {
        log.info("save new user = {}", userDto);
        return userService.addNewUser(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable("id") Long id,
                              @RequestBody UserDto userDto) {
        userDto.setId(id);
        log.info("update user = {}", userDto);
        return userService.updateUser(userDto);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable("id") Long id) {
        log.info("delete user with id = {}", id);
        userService.deleteUser(id);
    }


}
