package com.example.demo.payload.Response;

import com.example.demo.utils.statusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListJobApplication {
    private UUID id;
    private statusEnum status;
    private UUID jobId;
    private UUID cvId;
    private String jobTitle;
    private String companyName;
}
