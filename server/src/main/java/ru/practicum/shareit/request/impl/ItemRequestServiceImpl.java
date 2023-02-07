package ru.practicum.shareit.request.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exeption.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Transactional
    @Override
    public ItemRequestDto createNewRequest(ItemRequestDto itemRequestDto, Long requestorId) {
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        User requestor = userService.checkIfUserExist(requestorId);
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());
        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Transactional
    @Override
    public List<ItemRequestDto> getUsersRequests(Long requestorId) {
        User requestor = userService.checkIfUserExist(requestorId);
        List<ItemRequestDto> requests = itemRequestRepository
                .findAllByRequestorIdOrderByCreatedDesc(requestor.getId())
                .stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        List<Long> requestIds = requests.stream().map(ItemRequestDto::getId).collect(Collectors.toList());
        List<Item> items = itemService.getItemsByRequestIdIn(requestIds);
        return requests.stream().map(r -> findItems(r, items)).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<ItemRequestDto> getAllRequestsPageable(Long userId, Long from, Integer size) {
        userService.checkIfUserExist(userId);
        int startPage = Math.toIntExact(from / size);
        List<ItemRequestDto> requests = itemRequestRepository
                .findAllByRequestorIdIsNot(userId,
                        PageRequest.of(startPage, size, Sort.by("created").descending()))
                .stream().map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        List<Long> requestIds = requests.stream().map(ItemRequestDto::getId).collect(Collectors.toList());
        List<Item> items = itemService.getItemsByRequestIdIn(requestIds);
        return requests.stream().map(r -> findItems(r, items)).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ItemRequestDto getRequestById(Long requestId, Long requestorId) {
        userService.checkIfUserExist(requestorId);
        Optional<ItemRequest> itemRequest = itemRequestRepository.findById(requestId);
        if (itemRequest.isEmpty()) {
            throw new NotFoundException(String.format("request with id = %s not found", requestId));
        }
        List<Item> items = itemService.getItemsByRequestId(requestId);
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest.get());
        return findItems(itemRequestDto, items);
    }

    private ItemRequestDto findItems(ItemRequestDto itemRequestDto, List<Item> items) {
        List<ItemDto> itemDtos = items.stream()
                .filter(item -> item.getRequest().getId().equals(itemRequestDto.getId()))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        itemRequestDto.setItems(itemDtos);
        return itemRequestDto;
    }
}
