package app.util;

public final class EmailUtil {
    private EmailUtil() {}

    public static boolean isEmailLogin(String login) {
        return login.contains("@");
    }

}
