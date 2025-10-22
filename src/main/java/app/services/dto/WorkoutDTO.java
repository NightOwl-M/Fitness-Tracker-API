package app.services.dto;

import java.time.LocalDate;

public record WorkoutDTO(Integer id, Integer userId, LocalDate date, String notes) {}
