package com.hoangtien2k3.userservice.api;

import com.hoangtien2k3.userservice.exception.wrapper.TokenErrorOrAccessTimeOut;
import com.hoangtien2k3.userservice.http.HeaderGenerator;
import com.hoangtien2k3.userservice.model.dto.request.ChangePasswordRequest;
import com.hoangtien2k3.userservice.model.dto.request.SignUp;
import com.hoangtien2k3.userservice.model.dto.request.UserDto;
import com.hoangtien2k3.userservice.model.dto.response.ResponseMessage;
import com.hoangtien2k3.userservice.security.jwt.JwtProvider;
import com.hoangtien2k3.userservice.service.UserService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/manager")
@Api(value = "User API", description = "Operations related to users")
public class UserManager {

    private final ModelMapper modelMapper;
    private final UserService userService;
    private final HeaderGenerator headerGenerator;
    private final JwtProvider jwtProvider;

    @Autowired
    public UserManager(UserService userService,
                       HeaderGenerator headerGenerator,
                       JwtProvider jwtProvider,
                       ModelMapper modelMapper) {
        this.userService = userService;
        this.headerGenerator = headerGenerator;
        this.jwtProvider = jwtProvider;
        this.modelMapper = modelMapper;
    }

    /* ================= UPDATE ================= */

    @PutMapping("update/{id}")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<ResponseEntity<ResponseMessage>> update(@PathVariable Long id,
                                                        @RequestBody SignUp updateDTO) {
        return userService.update(id, updateDTO)
                .map(user -> new ResponseEntity<>(
                        new ResponseMessage("Update user: " + updateDTO.getUsername() + " successfully."),
                        HttpStatus.OK))
                .onErrorResume(error ->
                        Mono.just(new ResponseEntity<>(
                                new ResponseMessage("Update user failed: " + error.getMessage()),
                                HttpStatus.BAD_REQUEST)));
    }

    /* ================= PASSWORD ================= */

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> changePassword(@RequestBody ChangePasswordRequest request) {
        return userService.changePassword(request);
    }

    /* ================= DELETE ================= */

    @DeleteMapping("delete/{id}")
    @PreAuthorize("isAuthenticated() and (hasAuthority('USER') or hasAuthority('ADMIN'))")
    public Mono<String> delete(@PathVariable Long id) {
        return userService.delete(id);
    }

    /* ================= GET BY USERNAME ================= */

    @GetMapping("/user")
    @PreAuthorize("(isAuthenticated() and (hasAuthority('USER') and principal.username == #username) or hasAuthority('ADMIN'))")
    public Mono<ResponseEntity<UserDto>> getUserByUsername(@RequestParam String username) {
        return userService.findByUsername(username)
                .map(user -> modelMapper.map(user, UserDto.class))
                .map(userDto -> new ResponseEntity<>(
                        userDto,
                        headerGenerator.getHeadersForSuccessGetMethod(),
                        HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(
                        null,
                        headerGenerator.getHeadersForError(),
                        HttpStatus.NOT_FOUND));
    }

    /* ================= GET BY ID ================= */

    @GetMapping("/user/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER') and principal.id == #id")
    public Mono<ResponseEntity<UserDto>> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> modelMapper.map(user, UserDto.class))
                .map(userDto -> new ResponseEntity<>(
                        userDto,
                        headerGenerator.getHeadersForSuccessGetMethod(),
                        HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(
                        null,
                        headerGenerator.getHeadersForError(),
                        HttpStatus.NOT_FOUND));
    }

    /* ================= GET ALL ================= */

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Mono<ResponseEntity<Page<UserDto>>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           @RequestParam(defaultValue = "id") String sortBy,
                                                           @RequestParam(defaultValue = "ASC") String sortOrder) {
        return userService.findAllUsers(page, size, sortBy, sortOrder)
                .map(usersPage -> new ResponseEntity<>(
                        usersPage,
                        headerGenerator.getHeadersForSuccessGetMethod(),
                        HttpStatus.OK));
    }

    /* ================= INFO FROM TOKEN ================= */

    @GetMapping("/info")
    public Mono<ResponseEntity<UserDto>> getUserInfo(@RequestHeader("Authorization") String token) {
        String username = jwtProvider.getUserNameFromToken(token);
        return userService.findByUsername(username)
                .map(user -> modelMapper.map(user, UserDto.class))
                .map(userDto -> new ResponseEntity<>(
                        userDto,
                        headerGenerator.getHeadersForSuccessGetMethod(),
                        HttpStatus.OK))
                .switchIfEmpty(Mono.error(
                        new TokenErrorOrAccessTimeOut("Token error or access timeout")));
    }
}

