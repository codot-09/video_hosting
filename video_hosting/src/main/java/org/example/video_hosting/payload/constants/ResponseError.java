package org.example.video_hosting.payload.constants;

import lombok.Getter;

/**
 * Standard structure for API error responses with error codes and messages.
 */
@Getter
public class ResponseError {

    private final int code;
    private final String message;

    private ResponseError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ResponseError accessDenied() {
        return new ResponseError(1, "Kirish taqiqlanadi.");
    }

    public static ResponseError notFound(Object data) {
        return new ResponseError(2, String.format("%s topilmadi.", data));
    }

    public static ResponseError passwordDidNotMatch() {
        return new ResponseError(3, "Telefon raqam yoki parol mos kelmadi.");
    }

    public static ResponseError alreadyExists(String data) {
        return new ResponseError(4, String.format("%s allaqachon mavjud.", data));
    }

    public static ResponseError validationFailed(String message) {
        return new ResponseError(5, message);
    }

    public static ResponseError notAnAdmin() {
        return new ResponseError(6, "Siz admin emassiz!");
    }

    public static ResponseError defaultError(String message) {
        return new ResponseError(7, message);
    }

    public static ResponseError serverError(String message) {
        return new ResponseError(8, message);
    }
}
