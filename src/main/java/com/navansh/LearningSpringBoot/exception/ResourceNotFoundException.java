package com.navansh.LearningSpringBoot.exception;

public class ResourceNotFoundException extends RuntimeException{
    //so that the message can be passed when the exception is thrown
    public ResourceNotFoundException(String message){
        super(message);
    }
}
