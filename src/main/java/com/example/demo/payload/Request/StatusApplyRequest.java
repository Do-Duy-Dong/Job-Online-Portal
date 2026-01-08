package com.example.demo.payload.Request;

import com.example.demo.utils.statusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusApplyRequest {
    private String applicationId;
    private statusEnum status;
}
