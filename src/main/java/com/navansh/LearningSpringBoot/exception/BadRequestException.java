package com.navansh.LearningSpringBoot.exception;

public class BadRequestException extends RuntimeException{
    //so that the message can be passed when the exception is thrown
    public BadRequestException(String message){
        super(message);
    }

}
