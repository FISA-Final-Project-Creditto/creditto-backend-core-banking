package org.creditto.core_banking.global.response.exception;

import lombok.Getter;
import org.creditto.core_banking.global.response.error.ErrorCode;

/**
 * 예측 가능하고 정형화된 에러를 처리하기 위한 비즈니스 예외 클래스
 * 이 예외는 내부에 {@link ErrorCode}를 포함하고 있어, 클라이언트에게 일관된 에러 응답을 제공
 *
 * @see GlobalExceptionHandler#handleCustomBaseException(CustomBaseException)
 */
@Getter
public class CustomBaseException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomBaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
