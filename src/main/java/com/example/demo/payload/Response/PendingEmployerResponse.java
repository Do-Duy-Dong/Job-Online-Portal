package com.example.demo.payload.Response;

import com.example.demo.entity.Enum.EnumApprovalStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class PendingEmployerResponse {
    private UUID userId;
    private String fullName;
    private String email;
    private EnumApprovalStatus approvalStatus;
    private String companyTitle;
    private String companyEmail;
    private String companyPhone;
    private String companyAddress;
}
