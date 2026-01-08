package com.example.demo.payload.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class MailResponse {
    private String userName;
    private String jobTitle;
    private String jobLink;
}
