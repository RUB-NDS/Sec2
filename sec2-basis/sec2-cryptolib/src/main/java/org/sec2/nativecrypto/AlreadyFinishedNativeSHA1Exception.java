package org.sec2.nativecrypto;

public class AlreadyFinishedNativeSHA1Exception extends RuntimeException {
	private static final long serialVersionUID = 7530391580616900175L;

	public AlreadyFinishedNativeSHA1Exception(){
		
	}
	
	public AlreadyFinishedNativeSHA1Exception(String s){
		super(s);
	}
}
