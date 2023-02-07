package ru.practicum.shareit.comment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
class CommentRepositoryTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    CommentRepository commentRepository;

    User author;
    User owner;
    Item itemToSave;
    Item item1;
    Item item2;
    Comment commentToSave;
    Comment comment1;
    Comment comment2;

    @BeforeEach
    void setup() {
        author = userRepository.save(new User(null, "user 1", "user1@email"));
        owner = userRepository.save(new User(null, "user 2", "user2@email"));

        itemToSave = new Item("name1", "description1", true);
        itemToSave.setOwner(owner);
        item1 = itemRepository.save(itemToSave);

        itemToSave = new Item("name2", "description2", true);
        itemToSave.setOwner(owner);
        item2 = itemRepository.save(itemToSave);

        commentToSave = new Comment();
        commentToSave.setAuthor(author);
        commentToSave.setItem(item1);
        commentToSave.setText("text");
        commentToSave.setCreated(LocalDateTime.now());
        comment1 = commentRepository.save(commentToSave);
    }

    @AfterEach
    void afterEach() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
        commentRepository.deleteAll();
    }

    @Test
    void findAllByItemIdIs_whenFound_thenReturnListOfComments() {
        List<Comment> comments = commentRepository.findAllByItemIdIs(item1.getId());

        assertEquals(1, comments.size());
    }

    @Test
    void findAllByItemIdIs_whenNotFound_thenReturnEmptyList() {
        List<Comment> comments = commentRepository.findAllByItemIdIs(item2.getId());

        assertTrue(comments.isEmpty());
    }

    @Test
    void findAllByItemIdIn_whenFound_thenReturnListOfComments() {
        List<Comment> comments = commentRepository
                .findAllByItemIdIn(List.of(item1.getId(), item2.getId()));
        assertEquals(1, comments.size());

        commentToSave = new Comment();
        commentToSave.setAuthor(author);
        commentToSave.setItem(item2);
        commentToSave.setText("text2");
        commentToSave.setCreated(LocalDateTime.now());
        comment2 = commentRepository.save(commentToSave);
        comments = commentRepository
                .findAllByItemIdIn(List.of(item1.getId(), item2.getId()));
        assertEquals(2, comments.size());
    }

}