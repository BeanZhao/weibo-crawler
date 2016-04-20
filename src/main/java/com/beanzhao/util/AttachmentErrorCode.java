package com.beanzhao.util;

public enum AttachmentErrorCode {

    // 附件有关的错误105开头
    ATTACHMENT_UNDEFINED_ERROR(10500, "attachment undefined error"), ATTACHMENT_IO_ERROR(10501,
            "attachment upload error"), ATTACHMENT_SCS_ERROR(10502, "attachment scs service error"), ATTACHMENT_SCS_CLIENT_ERROR(
            10503, "attachment scs client error"), ATTACHMENT_FILETYPE_ERROR(10504, "attachment type not supported"), ATTACHMENT_FILESIZE_ERROR(
            10505, "attachment size inncorrect"), ATTACHMENT_DESCRIPTION_TOO_LONG(10506,
            "attachment description is too long");

    private Integer code;
    private String message;

    private AttachmentErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
