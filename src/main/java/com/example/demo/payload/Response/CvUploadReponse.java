package com.example.demo.payload.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CvUploadReponse {
    private String url;
    private String s3Key;
}
