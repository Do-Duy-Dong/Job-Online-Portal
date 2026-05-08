package com.example.demo.payload.Response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeysetPageResponse<T> {
    private List<T> content;
    private int size;
    private boolean hasNext;
    private String nextCursorTime;
    private String nextCursorId;
    private long totalCount;
}
