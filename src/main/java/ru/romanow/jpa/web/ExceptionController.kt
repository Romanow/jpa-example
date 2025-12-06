package ru.romanow.jpa

import jakarta.persistence.EntityNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.romanow.jpa.model.ErrorResponse

@RestControllerAdvice(annotations = [RestController::class])
class ExceptionController {
    private final val logger: Logger = LoggerFactory.getLogger(ExceptionController::class.java)

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException::class)
    fun error(exception: EntityNotFoundException) = ErrorResponse(exception.message)

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException::class)
    fun error(exception: RuntimeException): ErrorResponse {
        logger.error("", exception)
        return ErrorResponse(exception.message)
    }
}
