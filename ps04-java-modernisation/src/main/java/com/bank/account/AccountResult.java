package com.bank.account;

/**
 * Manual result wrapper that carries either a success value or a failure message.
 *
 * Java 8 style: a single class with a boolean flag and two nullable fields.
 * Callers must remember to check {@code isSuccess()} before calling {@code getValue()},
 * and the compiler gives no help if they forget.
 *
 * Modernisation targets (Java 17+):
 *  - Replace with a sealed interface + two record variants:
 *
 *    sealed interface AccountResult<T> permits AccountResult.Success, AccountResult.Failure {
 *        record Success<T>(T value) implements AccountResult<T> {}
 *        record Failure<T>(String errorCode, String message) implements AccountResult<T> {}
 *    }
 *
 *  - Call sites can then use pattern matching switch to handle both cases exhaustively,
 *    eliminating null-pointer risk entirely.
 */
public class AccountResult<T> {

    private final boolean success;
    private final T value;
    private final String errorCode;
    private final String message;

    // Private constructor — use static factory methods below
    private AccountResult(boolean success, T value, String errorCode, String message) {
        this.success = success;
        this.value = value;
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * Creates a successful result wrapping the given value.
     */
    public static <T> AccountResult<T> success(T value) {
        return new AccountResult<T>(true, value, null, null);
    }

    /**
     * Creates a failure result with an error code and human-readable message.
     */
    public static <T> AccountResult<T> failure(String errorCode, String message) {
        return new AccountResult<T>(false, null, errorCode, message);
    }

    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the wrapped value.
     * WARNING: returns null when {@code isSuccess()} is false — callers must guard.
     * A sealed record variant would eliminate this ambiguity at the type level.
     */
    public T getValue() {
        return value;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    // -----------------------------------------------------------------------
    // Manually implemented equals / hashCode / toString
    // -----------------------------------------------------------------------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountResult<?> that = (AccountResult<?>) o;
        if (success != that.success) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (errorCode != null ? !errorCode.equals(that.errorCode) : that.errorCode != null) return false;
        return message != null ? message.equals(that.message) : that.message == null;
    }

    @Override
    public int hashCode() {
        int result = (success ? 1 : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (errorCode != null ? errorCode.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if (success) {
            return "AccountResult{success=true, value=" + value + "}";
        } else {
            return "AccountResult{success=false, errorCode='" + errorCode
                    + "', message='" + message + "'}";
        }
    }
}
