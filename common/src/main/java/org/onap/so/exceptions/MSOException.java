package org.onap.so.exceptions;


public class MSOException extends Exception{
    /**
     * 
     */
    private static final long serialVersionUID = 4563920496855255206L;
    private Integer errorCode;

    public MSOException(String msg){
        super(msg);
    }
    
    public MSOException (Throwable e) {
        super(e);
    }
    
    public MSOException (String msg, Throwable e) {
        super (msg, e);
    }
    
    public MSOException(String msg, int errorCode){
        super(msg);
        this.errorCode=errorCode;
    }
    
    public MSOException(String msg, int errorCode, Throwable t){
        super(msg,t);
        this.errorCode=errorCode;
    }
    
    public Integer getErrorCode(){
        return errorCode;
    }
}
