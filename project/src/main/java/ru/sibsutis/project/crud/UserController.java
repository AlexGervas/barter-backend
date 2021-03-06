package ru.sibsutis.project.crud;

import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import ru.sibsutis.project.AuthorizationRow;
import ru.sibsutis.project.databases.User;
import ru.sibsutis.project.dto.UserDto;
import ru.sibsutis.project.dto.UserDtoWithId;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    private UserDtoWithId copyToUserDtoWithId(User user) {
        UserDtoWithId userDtoWithId = new UserDtoWithId();
        BeanUtils.copyProperties(user, userDtoWithId, "favorites");
        return userDtoWithId;
    }

    private UserDto copyToDto(User user) {
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto, "id", "favorites");
        return userDto;
    }

    @PostMapping("/reg")
    public UserDtoWithId registrationUser(@RequestBody UserDto userDto) {
        return copyToUserDtoWithId(service.create(userDto));
    }


    @PostMapping("/auth")
    public UserDtoWithId authorizationUser(@RequestBody AuthorizationRow row) {
        return copyToUserDtoWithId(service.authorization(row.getEmail(), row.getPassword()));
    }

    @PostMapping("/info")
    public UserDto userInfo(@RequestParam Long userId) {
        return copyToDto(service.getUserInfo(userId));
    }

    @PostMapping("/add_faves")
    public void addFaves(@RequestParam Long productId, @RequestParam Long userId) {
        service.addFaves(productId, userId);
    }

    @PostMapping("/delete_faves")
    public void deleteFaves(@RequestParam Long productId, @RequestParam Long userId) {
        service.deleteFaves(productId, userId);
    }
}
