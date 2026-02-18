package com.example.SWP391_SPRING2026.DTO.Request;

import com.example.SWP391_SPRING2026.Enum.UserRole;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserUpdateReq {
    // Không update email/phone tại đây (đúng đặc tả)
    private String fullName;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dob;

    @Min(value = 0, message = "Giới tính phải trong khoảng 0..2")
    @Max(value = 2, message = "Giới tính phải trong khoảng 0..2")
    private Integer gender;

    private UserRole role;
}
