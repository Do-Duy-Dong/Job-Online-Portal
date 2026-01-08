package com.example.demo.payload.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ResponsePageBase<T> {
    private List<T> content;
    private int totalPage;
    private int currentPage;
}
