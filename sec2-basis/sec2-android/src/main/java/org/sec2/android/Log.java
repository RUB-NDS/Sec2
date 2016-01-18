/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.android;

/**
 *  This is a Variable Logging Class for Filtering the log Option for Android
 *  It is for Life Debugging
 *
 * @author hiphop-dave
 */


public class Log {
    private static String BLACKLIST =  ""
         + "org.sec2.research.core.middleware.xml.XMLProcessor.processXMLStream"
         + "org.sec2.research.core.middleware.xml.XMLProcessor.cacheAndReplay"
        + "org.sec2.research.core.middleware.servers.ConnectionHandler.cacheAndReplay"
         + "org.sec2.research.core.middleware.xml.handlers.EncryptionHandler.characters"
         + "org.sec2.research.core.middleware.xml.handlers.SerializationHandler.endElement"
        + "org.sec2.research.core.middleware.xml.handlers.DecryptionHandler.endElement"
         //+ "encryptBytes"
                                        ;

    public static String TAG = "sec2middleware";
    
    public static int d(String message){
        
         String classTag;
       
        String addTag =Thread.currentThread()
                                 .getStackTrace()[3]
                                        .getMethodName();
    
        if (addTag.equals("LogOutByteArray")
            ||
                
            addTag.equals("LogOutByteArrayCont") )
                   {
                       addTag =Thread.currentThread()
                                 .getStackTrace()[4]
                                        .getMethodName();
                       classTag =Thread.currentThread()
                                 .getStackTrace()[4]
                                        .getClassName();
                       
                   }
            else
            {
            classTag =Thread.currentThread()
                                    .getStackTrace()[3]
                                            .getClassName();
            }
       

         if (BLACKLIST.contains(classTag+".*"))
            return 0;
        if (BLACKLIST.contains(classTag+"."+addTag))
            return 0;
      // Das Paket ausblenden, da es Zu Redundant ist.
      classTag =  classTag.replace("org.sec2.research.core.", "");
       
       
    return android.util.Log.d(TAG, classTag + "." + addTag  + "(): " +  message);
       
    };
    
       public static void LogOutByteArray(byte[] bytes) {
        
            //DEBUG
                byte[] b = bytes;
             for (int i = 0; i < b.length; i++ )
           if ( b[i] != 0)
                 org.sec2.android.Log.d( 
                         
                     "(" +    (char) b[i] + ") = " + 
                         java.lang.Integer.toHexString((int) b[i]));
            else
           {
                org.sec2.android.Log.d( "Found a 0-Byte, aborting");
                return;
           }
             
              //DEBUG
             
    };

        public static void LogOutByteArrayCont(byte[] bytes) {
        
            //DEBUG
                byte[] b = bytes;
             for (int i = 0; i < b.length; i++ )
           if ( b[i] != 0)
                 org.sec2.android.Log.d( 
               "(" +    (char) b[i] + ") = " + 
                         java.lang.Integer.toHexString((int) b[i]));
            else
           {
                org.sec2.android.Log.d( "Found a 0-Byte, aborting");
                return;
           }
             
              //DEBUG
             
    };
    
}
