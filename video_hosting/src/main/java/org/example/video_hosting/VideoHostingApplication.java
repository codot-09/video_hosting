package org.example.video_hosting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VideoHostingApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoHostingApplication.class, args);
    }

}