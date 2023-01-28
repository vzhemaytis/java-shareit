package ru.practicum.shareit.request.model;

import lombok.Data;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Data
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "requestor_id")
    private User requestor;
    private LocalDateTime created;

    public ItemRequest(String description) {
        this.description = description;
    }

    public ItemRequest() {
    }
}