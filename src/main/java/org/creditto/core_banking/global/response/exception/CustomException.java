package org.creditto.core_banking.global.response.exception;

/**
 * 예측하기 어렵거나, 일회성으로 발생하는 에러를 처리하기 위한 예외 클래스
 * {@link org.creditto.core_banking.global.response.error.ErrorCode}를 정의하지 않고,
 * 동적인 에러 메시지를 직접 전달하여 간단하게 예외를 생성할 때 사용
 *
 * @see GlobalExceptionHandler#handleCustomException(CustomException)
 */
public class CustomException extends RuntimeException {

    public CustomException(String message) {
        super(message);
    }
}
