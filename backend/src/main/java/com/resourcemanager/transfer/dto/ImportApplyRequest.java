package com.resourcemanager.transfer.dto;

import jakarta.validation.constraints.NotBlank;

public record ImportApplyRequest(
        @NotBlank String conflictStrategy
) {}
