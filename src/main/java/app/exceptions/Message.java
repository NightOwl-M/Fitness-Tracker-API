package app.exceptions;

// Simpel JSON response objekt for beskeder og errors.

public record Message(int status, String message) {}