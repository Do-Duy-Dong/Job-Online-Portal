package com.example.demo.payload.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class KeysetPageResponse<T> {
    private List<T> content;
    private int size;
    private boolean hasNext;
    private String nextCursorTime;
    private String nextCursorId;
}
