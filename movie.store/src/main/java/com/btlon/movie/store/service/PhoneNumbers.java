package com.btlon.movie.store.service;

import com.btlon.movie.store.web.BadRequestException;

import java.util.regex.Pattern;

/** Vietnamese mobile numbers, stored as 0xxxxxxxxx. */
public final class PhoneNumbers {
    private static final Pattern VN_MOBILE = Pattern.compile("0[35789]\\d{8}");

    private PhoneNumbers() {}

    /** Accepts 0xxxxxxxxx or +84xxxxxxxxx with optional spaces, dots or dashes; returns null if blank. */
    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String phone = raw.replaceAll("[\\s.-]", "");
        if (phone.startsWith("+84")) phone = "0" + phone.substring(3);
        if (!VN_MOBILE.matcher(phone).matches()) {
            throw new BadRequestException("Phone number must be a valid Vietnamese mobile number");
        }
        return phone;
    }
}
