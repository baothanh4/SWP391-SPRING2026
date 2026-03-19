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
@RequestMapping("/api/manager/refund-requests")
@RequiredArgsConstructor
public class ManagerRefundRequestController {

    private final RefundRequestService refundRequestService;

    @GetMapping("/approved")
    @ResponseStatus(HttpStatus.OK)
    public List<RefundRequestResponseDTO> approved() {
        return refundRequestService.getByStatus(RefundRequestStatus.APPROVED);
    }

    @GetMapping("/done")
    @ResponseStatus(HttpStatus.OK)
    public List<RefundRequestResponseDTO> done() {
        return refundRequestService.getByStatus(RefundRequestStatus.DONE);
    }

    @PostMapping("/{id}/done")
    @ResponseStatus(HttpStatus.OK)
    public RefundRequestResponseDTO markDone(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody(required = false) RefundActionRequestDTO body
    ) {
        String note = body == null ? null : body.getNote();
        return refundRequestService.markDone(principal.getUserId(), id, note);
    }
}