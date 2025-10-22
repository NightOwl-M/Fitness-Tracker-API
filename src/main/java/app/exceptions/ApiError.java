package app.exceptions;

// Simpel JSON response objekt for beskeder og errors.

public record ApiError(int status, String message) {}
