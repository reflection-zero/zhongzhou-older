package com.zzyl.common.exception;

public final class AIException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private Integer code;

    private String message;

    public AIException() {}

    public AIException(String message)
    {
        this.message = message;
    }

    public AIException(String message, Integer code)
    {
        this.message = message;
        this.code = code;
    }

    @Override
    public String getMessage()
    {
        return message;
    }

    public Integer getCode()
    {
        return code;
    }

    public AIException setMessage(String message)
    {
        this.message = message;
        return this;
    }
}
