package org.sec2.nativecrypto;

public class NativeSHA1 {
	private native void initWithJReturn(SHA1Context sha1context);
	private native void init();
	private native byte[] finishContextWithJReturn(SHA1Context sha1context);
	private native byte[] finishContext();
	private native int nativeInput(byte[] input);
	private native int nativeInputWithJReturn(SHA1Context sha1context, 
			byte[] input);
	
	
	private SHA1Context sha1Context;
	private byte[] hashValue;
	private boolean copyContextToJava = false;
	private boolean finished = false;
	
	/**
	 * Erstellt ein neues Objekt von NativeSHA1
	 */
	public NativeSHA1(){
		this.copyContextToJava = false;
		initialize();
	}
	
	/**
	 * Erstellt ein neues Objekt der Klasse NativeSHA1 und es kann
	 * copyContextToJava aktiviert werden. Dadurch werden die gesamten
	 * Hashdaten ebenfalls in java gespeichert. Multi- / Parallelhashing 
	 * wird dadurch ermöglicht. Sonst ist nur ein Hashwert möglich.  
	 * @param copyContextToJava
	 */
	public NativeSHA1(boolean copyContextToJava){
		this.copyContextToJava = copyContextToJava;
		initialize();
	}
	
	/**
	 * Initialisiert die Hashklasse und resetet alle Informationen.
	 */
	public void initialize(){
		if(this.copyContextToJava){
			this.sha1Context = new SHA1Context();
			initWithJReturn(this.sha1Context);
		}else{
			init();	
		}
		this.finished = false;
	}
	
	/**
	 * Hasht den Aktuell übergebenen Block. Alle Blöcke müssen die Länge 64
	 * byte haben. Der letzte Block darf abweichen.
	 * @param value 1 Block
	 * @throws Exception
	 */
	public void inputBlock(byte[] value){ //throws Exception{
		NativeSHA1State state;
		if(finished){
			//TODO anpassen
			//throw new Exception("Hashwert wurde bereits berechnet - bitte " +
			//		"initialize aufrufen");
		}
		if(copyContextToJava){
			state = NativeSHA1State.values()[nativeInputWithJReturn(sha1Context, value)];
		}else{
			state = NativeSHA1State.values()[nativeInput(value)];
		}
		if (state == NativeSHA1State.inputToLong){
			throw new IllegalArgumentException("Input-Länge ist größer als 64!");
		}
	}
		 
	/**
	 * Hasht das gesamte Byte-Array.
	 * @param value Byte-Array das gehasht werden soll
	 * @throws Exception
	 */
	public void inputEntrieData(byte[] value)
			throws AlreadyFinishedNativeSHA1Exception
	{
		
		if(finished){
			throw new AlreadyFinishedNativeSHA1Exception(
					"Hashwert wurde bereits berechnet - bitte " +
					"initialize aufrufen");
		}
		byte tmp[] = new byte[64];
		int Anzahlbloecke = (int) Math.ceil(((double)value.length / 64));
		for(int i = 0; i < Anzahlbloecke; i++){
			if(i == Anzahlbloecke - 1){
				tmp = new byte[value.length % 64];
				System.arraycopy(value, i*64, tmp, 0, value.length % 64);
			}else{
				System.arraycopy(value, i*64, tmp, 0, 64);
			}
			inputBlock(tmp);
		}		
	}
	
	/**
	 * Erstellt den Hashwert und stellt diesen zur Verfügung.
	 */
	public void finish(){
		if(finished) return;
		finished = true;
		if(this.copyContextToJava){
			this.hashValue = finishContextWithJReturn(sha1Context);
		}else{
			this.hashValue = finishContext();
		}	
	}

	/**
	 * Gibt den Hashwert zurück
	 * @return Gibt den berechneten Hashwert zurück
	 */
	public byte[] getHashValue(){
		return this.hashValue;
	}
	
	static {
		System.loadLibrary("MPZ");
	}
}