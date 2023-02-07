package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createNewRequest(ItemRequestDto itemRequestDto, Long requestorId);

    List<ItemRequestDto> getUsersRequests(Long userId);

    List<ItemRequestDto> getAllRequestsPageable(Long userId, Long from, Integer size);

    ItemRequestDto getRequestById(Long requestId, Long requestorId);
}
