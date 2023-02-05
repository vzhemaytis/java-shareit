package ru.practicum.shareit.user.dto;


import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

public class UserDto {
    private Long id;
    @NotBlank (message = "user name should be not blank or null")
    private String name;
    @NotBlank (message = "user email should be not blank or null")
    @Email (message = "wrong email format")
    private String email;

    public UserDto(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDto userDto = (UserDto) o;
        return id.equals(userDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
