package com.example.demo.payload.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SelectedTagRequest {
    private String jobId;
    private String tagId;
}
