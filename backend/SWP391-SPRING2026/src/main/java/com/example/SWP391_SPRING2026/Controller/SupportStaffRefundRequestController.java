package com.example.SWP391_SPRING2026.Controller;

import com.example.SWP391_SPRING2026.DTO.Request.RefundActionRequestDTO;
import com.example.SWP391_SPRING2026.DTO.Response.RefundRequestResponseDTO;
import com.example.SWP391_SPRING2026.Entity.UserPrincipal;
import com.example.SWP391_SPRING2026.Enum.RefundRequestStatus;
import com.example.SWP391_SPRING2026.Service.RefundRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support_staff/refund-requests")
@RequiredArgsConstructor
public class SupportStaffRefundRequestController {

    private final RefundRequestService refundRequestService;

    @GetMapping("/requested")
    @ResponseStatus(HttpStatus.OK)
    public List<RefundRequestResponseDTO> requested() {
        return refundRequestService.getByStatus(RefundRequestStatus.REQUESTED);
    }

    @PostMapping("/{id}/approve")
    @ResponseStatus(HttpStatus.OK)
    public RefundRequestResponseDTO approve(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody(required = false) RefundActionRequestDTO body
    ) {
        String note = body == null ? null : body.getNote();
        return refundRequestService.approve(principal.getUserId(), id, note);
    }

    @PostMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.OK)
    public RefundRequestResponseDTO reject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody(required = false) RefundActionRequestDTO body
    ) {
        String note = body == null ? null : body.getNote();
        return refundRequestService.reject(principal.getUserId(), id, note);
    }
}