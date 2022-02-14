package ru.romanow.jpa.web;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.romanow.jpa.model.ErrorResponse;

import javax.persistence.EntityNotFoundException;

import static org.slf4j.LoggerFactory.getLogger;

@RestControllerAdvice(annotations = RestController.class)
public class ExceptionController {
    private static final Logger logger = getLogger(ExceptionController.class);

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public ErrorResponse error(EntityNotFoundException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public ErrorResponse error(RuntimeException exception) {
        logger.error("", exception);
        return new ErrorResponse(exception.getMessage());
    }
}