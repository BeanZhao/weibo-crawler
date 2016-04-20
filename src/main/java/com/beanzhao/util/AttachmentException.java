package com.beanzhao.util;

public class AttachmentException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private AttachmentErrorCode errorCode;

    public AttachmentException() {
        super();
    }

    public AttachmentException(String message) {
        super(message);
    }

    public AttachmentException(AttachmentErrorCode error) {
        super();
        this.errorCode = error;
    }

    public AttachmentException(AttachmentErrorCode error, Exception e) {
        super(e);
        this.errorCode = error;
    }

    public AttachmentErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(AttachmentErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}