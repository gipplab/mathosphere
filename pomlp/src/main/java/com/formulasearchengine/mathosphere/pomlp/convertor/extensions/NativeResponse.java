package com.formulasearchengine.mathosphere.pomlp.convertor.extensions;

/**
 * @author Andre Greiner-Petter
 */
public class NativeResponse {
    private int responseCode;
    private String result;
    private String message;
    private Throwable exception;

    public NativeResponse( String result ){
        this.responseCode = 0;
        this.result = result;
    }

    public NativeResponse( int responseCode, String error_message, Throwable exception ){
        this.responseCode = responseCode;
        this.message = error_message;
        this.exception = exception;
    }

    public int getStatusCode() {
        return responseCode;
    }

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getThrowedException(){
        return exception;
    }
}
