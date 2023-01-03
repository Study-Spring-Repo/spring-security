package com.example.hyena.jwt;

public class JwtAuthentication {

    public final String token;

    public final String username;

    public JwtAuthentication(String token, String username) {
        checkArgument(!token.isEmpty(), "token must be provided");
        checkArgument(!username.isEmpty(), "username must be provided");


        this.token = token;
        this.username = username;
    }

    private void checkArgument(boolean isTrue, String message) {
        if (!isTrue) {
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public String toString() {
        return "JwtAuthentication{" +
                "token='" + token + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
