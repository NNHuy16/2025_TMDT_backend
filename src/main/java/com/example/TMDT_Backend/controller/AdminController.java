package com.example.TMDT_Backend.controller;

import com.example.TMDT_Backend.dto.response.UserDTO;
import com.example.TMDT_Backend.entity.User;
import com.example.TMDT_Backend.entity.enums.Role;
import com.example.TMDT_Backend.mapper.UserMapper;
import com.example.TMDT_Backend.repository.OrderRepository;
import com.example.TMDT_Backend.service.AdminService;
import com.example.TMDT_Backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;
    private final UserMapper userMapper;


    public AdminController(UserService userService, AdminService adminService, UserMapper userMapper) {
        this.userService = userService;
        this.adminService = adminService;
        this.userMapper = userMapper;
    }

    /**
     * Tạo mới một người dùng (chỉ Admin có quyền).
     *
     * @param user Đối tượng người dùng cần tạo.
     * @return Trả về người dùng sau khi được lưu thành công.
     */

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(adminService.createUsers(user));
    }

    /**
     * Lấy thông tin chi tiết người dùng theo ID (chỉ Admin).
     *
     * @param id ID của người dùng.
     * @return Trả về thông tin người dùng dưới dạng DTO.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(userMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy danh sách tất cả người dùng. Có thể lọc theo vai trò (role).
     *
     * @param role (Tùy chọn) Vai trò cần lọc (USER, EMPLOYER, ADMIN).
     * @return Danh sách người dùng dưới dạng DTO.
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getUsers(@RequestParam(required = false) Role role) {
        List<User> users = userService.getAllUsers();
        if (role != null) {
            users = users.stream()
                    .filter(u -> u.getRole() == role)
                    .collect(Collectors.toList());
        }

        List<UserDTO> dtos = users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }


    /**
     * Xóa người dùng theo ID (chỉ Admin).
     *
     * @param id ID của người dùng cần xóa.
     * @return Trả về mã phản hồi HTTP 204 No Content nếu thành công.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
