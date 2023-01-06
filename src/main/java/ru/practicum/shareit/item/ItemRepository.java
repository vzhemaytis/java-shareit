package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Set;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerIdIs(Long ownerId);
    Set<Item> findByNameContainingIgnoreCase(String text);
    Set<Item> findByDescriptionContainingIgnoreCase(String text);
}
