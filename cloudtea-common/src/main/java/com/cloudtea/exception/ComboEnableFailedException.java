package com.cloudtea.exception;

/**
 * 组合套餐启用失败异常
 */
public class ComboEnableFailedException extends BaseException {

    public ComboEnableFailedException(){}

    public ComboEnableFailedException(String msg){
        super(msg);
    }
}
