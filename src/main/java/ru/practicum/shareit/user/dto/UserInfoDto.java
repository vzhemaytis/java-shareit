package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UserInfoDto {
    private Long id;

    public UserInfoDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
