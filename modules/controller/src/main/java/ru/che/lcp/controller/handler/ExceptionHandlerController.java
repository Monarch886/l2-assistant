package ru.che.lcp.controller.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.che.lcp.client.exception.PromptInjectionException;
import ru.che.lcp.client.view.ErrorView;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorView handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        return ErrorView.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .fieldErrors(fieldErrors)
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorView handleNotReadable(HttpMessageNotReadableException e) {
        return ErrorView.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("BAD_REQUEST")
                .message("Request body is missing or malformed")
                .build();
    }

    @ExceptionHandler(PromptInjectionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorView handlePromptInjection(PromptInjectionException e) {
        log.warn("Prompt injection attempt blocked: {}", e.getDetectedPattern());
        return ErrorView.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("PROMPT_INJECTION")
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorView handleGeneral(Exception e) {
        log.error("Unhandled exception", e);
        return ErrorView.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .build();
    }
}