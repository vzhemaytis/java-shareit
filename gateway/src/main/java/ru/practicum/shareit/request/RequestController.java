package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> createNewRequest(@Valid @RequestBody @NotNull ItemRequestDto itemRequestDto,
                                                   @RequestHeader("X-Sharer-User-Id") Long requestorId) {
        log.info("create new item request = {} by user with id = {}", itemRequestDto, requestorId);
        return requestClient.createNewRequest(requestorId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getUsersRequests(@RequestHeader("X-Sharer-User-Id") Long requestorId) {
        log.info("get all requests of user with id = {}", requestorId);
        return requestClient.getUsersRequests(requestorId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequestsPageable(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(name = "from", required = false, defaultValue = "0") @Min(value = 0) Long from,
            @RequestParam(name = "size", required = false, defaultValue = "10") @Min(value = 1) Integer size) {
        log.info("get all requests from id = {} page size = {}", from, size);
        return requestClient.getAllRequestsPageable(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") Long requestorId,
                                         @PathVariable("requestId") Long requestId) {
        log.info("get request with id = {}", requestId);
        return requestClient.getRequestById(requestId, requestorId);
    }
}
