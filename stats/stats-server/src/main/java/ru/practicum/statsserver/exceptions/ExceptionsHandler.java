package ru.practicum.statsserver.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ExceptionsHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> issingParamException(MissingServletRequestParameterException e) {
        log.warn("Некорректные данные: {}", e.getMessage());
        return Map.of(
                "error", "Некорректные данные",
                "errorMessage", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequestException(BadRequestException e) {
        log.warn("Некорректные данные: {}", e.getMessage());
        return Map.of(
                "error", "Некорректные данные",
                "errorMessage", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> methodNitValidException(MethodArgumentNotValidException e) {
        log.warn("Некорректные данные: {}", e.getMessage());
        return Map.of(
                "error", "Некорректные данные",
                "errorMessage", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> exception(final Exception e) {
        log.warn("Ошибка сервера: {}", e.getMessage());
        return Map.of(
                "error", "Ошибка сервера",
                "errorMessage", e.getMessage()
        );
    }

}
