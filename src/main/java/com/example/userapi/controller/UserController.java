package com.example.userapi.controller;

import com.example.userapi.dto.request.CreateUserRequestDTO;
import com.example.userapi.dto.request.UpdateUserRequestDTO;
import com.example.userapi.dto.response.SuccessResponseDTO;
import com.example.userapi.dto.response.UserResponseDTO;
import com.example.userapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/createNewUser")
    public ResponseEntity<SuccessResponseDTO> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        UserResponseDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDTO.withData("Пользователь успешно создан", user));
    }

    @GetMapping("/users")
    public ResponseEntity<SuccessResponseDTO> getUser(@RequestParam UUID userID) {
        UserResponseDTO user = userService.getUserById(userID);
        return ResponseEntity.ok(SuccessResponseDTO.withData("Пользователь найден", user));
    }

    @PutMapping("/userDetailsUpdate")
    public ResponseEntity<SuccessResponseDTO> updateUser(@Valid @RequestBody UpdateUserRequestDTO request) {
        UserResponseDTO user = userService.updateUser(request);
        return ResponseEntity.ok(SuccessResponseDTO.withData("Пользователь успешно обновлен", user));
    }

    @DeleteMapping("/users")
    public ResponseEntity<SuccessResponseDTO> deleteUser(@RequestParam UUID userID) {
        userService.deleteUser(userID);
        return ResponseEntity.ok(SuccessResponseDTO.withMessage("Пользователь успешно удален"));
    }
}
