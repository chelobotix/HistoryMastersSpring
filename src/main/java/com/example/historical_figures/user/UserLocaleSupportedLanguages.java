package com.example.historical_figures.user;

import com.example.historical_figures.shared.exceptions.DomainException;

public enum UserLocaleSupportedLanguages {
    EN,
    ES,
    PT;

    public static UserLocaleSupportedLanguages validate(String code) {
        try {
            return valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new DomainException(UserErrorCodes.USER_INVALID_LOCALE);
        }
    }
}
