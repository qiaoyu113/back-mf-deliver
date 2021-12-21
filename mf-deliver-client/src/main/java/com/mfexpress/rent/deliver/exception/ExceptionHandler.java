/*
package com.mfexpress.rent.deliver.exception;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.response.Result;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ExceptionHandler {

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler()
    public Result<Object> exceptionHandle(Exception e) {
        e.printStackTrace();
        if (e instanceof CommonException){
            CommonException commonException = (CommonException) e;
            return Result.getInstance(null).fail(commonException.getCode(), commonException.getMsg());
        } else if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException methodArgumentNotValidException = (MethodArgumentNotValidException) e;
            BindingResult bindingResult = methodArgumentNotValidException.getBindingResult();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid request: ");
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (int i = 0; i < fieldErrors.size(); i++) {
                FieldError fieldError = fieldErrors.get(i);
                if(i == 0){
                    stringBuilder.append(fieldError.getField()).append(":").append(fieldError.getDefaultMessage());
                } else {
                    stringBuilder.append(",").append(fieldError.getField()).append(":").append(fieldError.getDefaultMessage());
                }
            }
            return Result.getInstance(null).fail(ResultErrorEnum.VILAD_ERROR.getCode(), stringBuilder.toString());
        } else {
            return Result.getInstance(null).fail(ResultErrorEnum.SERRVER_ERROR.getCode(), ResultErrorEnum.SERRVER_ERROR.getName());
        }
    }
}
*/
