package org.example.video_hosting.payload.constants;

import lombok.Getter;

/**
 * Standard structure for API success responses with codes and messages.
 */
@Getter
public class ResponseSuccess {

    private final int code;
    private final String message;

    private ResponseSuccess(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ResponseSuccess saved(Object data) {
        return new ResponseSuccess(1, String.format("%s muvaffaqiyatli saqlandi.", data));
    }

    public static ResponseSuccess updated(Object data) {
        return new ResponseSuccess(2, String.format("%s muvaffaqiyatli yangilandi.", data));
    }

    public static ResponseSuccess deleted(Object data) {
        return new ResponseSuccess(3, String.format("%s muvaffaqiyatli o‘chirildi.", data));
    }

    public static ResponseSuccess fetched(Object data) {
        return new ResponseSuccess(4, String.format("%s muvaffaqiyatli olindi.", data));
    }

    public static ResponseSuccess uploaded(){
        return new ResponseSuccess(5, "Kontent muvaffaqiyatli yuklandi.");
    }

    public static ResponseSuccess login() {
        return new ResponseSuccess(6, "Tizimga muvaffaqiyatli kirildi.");
    }

    public static ResponseSuccess logout() {
        return new ResponseSuccess(7, "Tizimdan muvaffaqiyatli chiqildi.");
    }

    public static ResponseSuccess registered() {
        return new ResponseSuccess(8, "Ro‘yxatdan muvaffaqiyatli o‘tildi.");
    }

    public static ResponseSuccess passwordChanged() {
        return new ResponseSuccess(9, "Parol muvaffaqiyatli o‘zgartirildi.");
    }

    public static ResponseSuccess emailSent() {
        return new ResponseSuccess(10, "Email muvaffaqiyatli yuborildi.");
    }

    public static ResponseSuccess operationDone(String message) {
        return new ResponseSuccess(11, message);
    }
}
