package org.example.video_hosting.payload.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponsePageable<T> {
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private List<T> content;
}
