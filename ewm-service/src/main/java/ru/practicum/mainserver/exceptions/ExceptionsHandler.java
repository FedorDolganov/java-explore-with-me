package ru.practicum.mainserver.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ExceptionsHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFoundException(NotFoundException e) {
        ApiError error = new ApiError(
                e.getStackTrace(),
                e.getMessage(),
                "The required object was not found.",
                HttpStatus.NOT_FOUND,
                LocalDateTime.now()
        );

        log.info(error.toString());

        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError dbConflictException(DataIntegrityViolationException e) {
        ApiError error = new ApiError(
                e.getStackTrace(),
                e.getMessage(),
                "Integrity constraint has been violated.",
                HttpStatus.CONFLICT,
                LocalDateTime.now()
        );

        log.info(error.toString());

        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequestException(BadRequestException e) {
        ApiError error = new ApiError(
                e.getStackTrace(),
                e.getMessage(),
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );

        log.info(error.toString());

        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError conflictException(ConflictException e) {
        ApiError error = new ApiError(
                e.getStackTrace(),
                e.getMessage(),
                "For the requested operation the conditions are not met.",
                HttpStatus.CONFLICT,
                LocalDateTime.now()
        );

        log.info(error.toString());

        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError forbiddenException(ForbiddenException e) {
        ApiError error = new ApiError(
                e.getStackTrace(),
                e.getMessage(),
                "For the requested operation the conditions are not met.",
                HttpStatus.FORBIDDEN,
                LocalDateTime.now()
        );

        log.info(error.toString());

        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError methodNitValidException(MethodArgumentNotValidException e) {
        ApiError error = new ApiError(
                e.getStackTrace(),
                e.getMessage(),
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );

        log.info(error.toString());

        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError exception(final Exception e) {
        ApiError error = new ApiError(
                e.getStackTrace(),
                e.getMessage(),
                "Server error",
                HttpStatus.INTERNAL_SERVER_ERROR,
                LocalDateTime.now()
        );

        log.info(error.toString());

        return error;
    }

}
