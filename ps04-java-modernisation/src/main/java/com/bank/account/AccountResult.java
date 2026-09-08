package com.bank.account;

/**
 * Wraps the result of an account operation.
 *
 * Carries either a successful value or a failure with an error code and message.
 * Callers must check {@code isSuccess()} before calling {@code getValue()}.
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
     * Returns the wrapped value, or null if this is a failure result.
     * Callers must check {@code isSuccess()} first.
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

    // -------------------------------------------------------------------------
    // equals / hashCode / toString
    // -------------------------------------------------------------------------
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
