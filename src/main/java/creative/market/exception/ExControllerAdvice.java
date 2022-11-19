package creative.market.exception;

import creative.market.util.ValidatorMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ExControllerAdvice {

    private final ValidatorMessageUtils validatorMessageUtils;

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ErrorRes BindException(BindException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        log.error("error{}",bindingResult.toString());
        return new ErrorRes(HttpStatus.BAD_REQUEST.value(), validatorMessageUtils.getValidationMessage(bindingResult));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(FileSaveException.class)
    public ErrorRes FileSaveException(FileSaveException ex) { // 파일 저장 오
        return new ErrorRes(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(LoginAuthenticationException.class)
    public ErrorRes loginAuthExHandle(LoginAuthenticationException ex) {
        return new ErrorRes(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NoSuchElementException.class)
    public ErrorRes noSuchExHandle(NoSuchElementException ex) {
        return new ErrorRes(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalStateException.class)
    public ErrorRes illegalStateExHandle(IllegalStateException ex) {
        return new ErrorRes(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorRes illegalArgsExHandle(IllegalArgumentException ex) {
        return new ErrorRes(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DuplicateException.class)
    public ErrorRes duplicateExHandle(DuplicateException ex) {
        return new ErrorRes(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NotExistOrder.class)
    public ErrorRes NotExistOrderExHandle(NotExistOrder ex) {
        return new ErrorRes(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }


}
