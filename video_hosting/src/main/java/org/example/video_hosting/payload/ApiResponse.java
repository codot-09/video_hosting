package org.example.video_hosting.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.example.video_hosting.payload.constants.ResponseError;
import org.example.video_hosting.payload.constants.ResponseSuccess;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * Standard API response wrapper for consistent response structures.
 *
 * @param <T> the type of the response data
 */
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> implements Serializable {

    private final boolean success;
    private final ResponseSuccess successMessage;
    private final ResponseError errorMessage;
    private final HttpStatus status;
    private final T data;

    @Builder
    private ApiResponse(boolean success,
                        ResponseSuccess successMessage,
                        ResponseError errorMessage,
                        HttpStatus status,
                        T data) {
        this.success = success;
        this.successMessage = successMessage;
        this.errorMessage = errorMessage;
        this.status = status != null ? status : (success ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
        this.data = data;
    }

    /**
     * Build success response with data and message.
     */
    public static <T> ApiResponse<T> ok(ResponseSuccess successMessage, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .successMessage(successMessage)
                .status(HttpStatus.OK)
                .data(data)
                .build();
    }

    /**
     * Build success response without data.
     */
    public static <T> ApiResponse<T> ok(ResponseSuccess successMessage) {
        return ApiResponse.<T>builder()
                .success(true)
                .successMessage(successMessage)
                .status(HttpStatus.OK)
                .build();
    }

    /**
     * Build error response.
     */
    public static <T> ApiResponse<T> error(ResponseError errorMessage) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorMessage(errorMessage)
                .status(HttpStatus.BAD_REQUEST)
                .build();
    }

    /**
     * Build error response with custom status.
     */
    public static <T> ApiResponse<T> error(ResponseError errorMessage, HttpStatus status) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorMessage(errorMessage)
                .status(status)
                .build();
    }
}
