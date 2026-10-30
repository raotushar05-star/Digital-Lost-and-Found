package com.lostandfound.util;

public final class ValidationPatterns {

    private ValidationPatterns() {}

    private static final String EMAIL_CORE =
            "^(?!.*\\.\\.)[A-Za-z0-9](?:[A-Za-z0-9._%+-]{0,62}[A-Za-z0-9])?@"
          + "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?"
          + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$";

    public static final String EMAIL = EMAIL_CORE;
    public static final String EMAIL_OPTIONAL = "^$|" + EMAIL_CORE;
    public static final String PERSON_NAME = "^[\\p{L}][\\p{L} .'-]{1,99}$";
    public static final String PHONE_10 = "^[0-9]{10}$";
    public static final String PASSWORD =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$";
    public static final String BRAND = "^$|^[\\p{L}\\p{N}][\\p{L}\\p{N} .&'/-]{0,99}$";
    public static final String COLOR = "^$|^[\\p{L}][\\p{L} /,-]{0,49}$";

    public static final String PERSON_NAME_MSG = "Name can contain only letters and spaces";
    public static final String EMAIL_MSG = "Enter a valid email like name@example.com";
    public static final String PHONE_MSG = "Phone must be exactly 10 digits";
    public static final String PASSWORD_MSG =
            "Password must be 8 to 64 characters with an uppercase letter, a lowercase letter, a number and a symbol";
}