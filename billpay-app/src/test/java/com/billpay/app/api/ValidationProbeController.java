package com.billpay.app.api;

import com.billpay.common.api.ApiException;
import com.billpay.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/_probe")
class ValidationProbeController {

    record ProbeRequest(@NotBlank String name) {}

    @PostMapping("/validation")
    public ApiResponse<Void> validate(@Valid @RequestBody ProbeRequest request) {
        return ApiResponse.ok(null);
    }

    @GetMapping("/api-error")
    public void apiError() {
        throw new ApiException(HttpStatus.NOT_FOUND, "Resource not found");
    }
}
