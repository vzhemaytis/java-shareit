package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@Validated
@Slf4j
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createNewRequest(@Valid @RequestBody @NotNull ItemRequestDto itemRequestDto,
                                           @RequestHeader("X-Sharer-User-Id") Long requestorId) {
        log.info("create new item request = {} by user with id = {}", itemRequestDto, requestorId);
        return itemRequestService.createNewRequest(itemRequestDto, requestorId);
    }

    @GetMapping
    public List<ItemRequestDto> getUsersRequests(@RequestHeader("X-Sharer-User-Id") Long requestorId) {
        log.info("get all requests of user with id = {}", requestorId);
        return itemRequestService.getUsersRequests(requestorId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequestsPageable(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(name = "from", required = false, defaultValue = "0") @Min(value = 0) Long from,
            @RequestParam(name = "size", required = false, defaultValue = "10") @Min(value = 1) Integer size) {
        log.info("get all requests from id = {} page size = {}", from, size);
        return itemRequestService.getAllRequestsPageable(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader("X-Sharer-User-Id") Long requestorId,
                                          @PathVariable("requestId") Long requestId) {
        log.info("get request with id = {}", requestId);
        return itemRequestService.getRequestById(requestId, requestorId);
    }
}
