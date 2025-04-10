package org.example.video_hosting.payload.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

@Getter
@Setter
public class ResFile {
    private HttpHeaders headers;
    private Resource resource;
    private String fillName;
}