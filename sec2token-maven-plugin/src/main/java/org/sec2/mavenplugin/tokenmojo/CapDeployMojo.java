/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.mavenplugin.tokenmojo;

import java.io.*;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import sun.util.logging.resources.logging;


/**
 * Java Card Mojo
 *
 * @goal deploy
 *
 * @phase deploy
 * 
 * @author Thorsten Schreiber, HGI Bochum, for sec2 Project
 * 
 * This Mojo just copies the settings from the pom.xml and 
 * calls a service called gpshell to setup the card.
 */
public class CapDeployMojo 
                extends AbstractMojo {
    
   /**
     * Parameter of CAP Converter Applet Packagename
     * 
     * @parameter expression="${appletPacket}"
     */
    private String appletPacket;
   
     /**
     * Parameter of CAP Converter Applet AID
     * 
     * @parameter expression="${appletAID}"
     */
    private String appletAID;
    
   /**
     * Parameter of CAP Converter Applet Package AID
     * 
     * @parameter expression="${appletPackageAID}"
     */
    private String appletPackageAID;
    
   /**
     * Parameter of CAP Converter Applet Classname
     * 
     * @parameter expression="${appletClass}"
     */    
    private String appletClass;
    
    /**
     * Parameter of CAP Deployer Card REader Name
     * 
     * @parameter expression="${cardReaderName}"
     */    
    private String cardReaderName;
    
    /**
     * Parameter of CAP Deployer Card MAC
     * 
     * @parameter expression="${cardMAC}"
     */    
    private String cardMAC;
    
    /**
     * Parameter of CAP Deployer Card KEy
     * 
     * @parameter expression="${cardKey}"
     */    
    private String cardKey;
    
    /**
     * Parameter of CAP Deployer CardAID
     * 
     * @parameter expression="${cardAID}"
     */    
    private String cardAID;
    
        /**
     * The maven project.
     *
     * @parameter expression="${project}" @required @readonly
     */
    private MavenProject project;
    /**
     * The artifact handler.
     *
     * @parameter
     * expression="${component.org.apache.maven.artifact.handler.ArtifactHandler#cap}"
     * @required @readonly
     */
    private ArtifactHandler artifactHandler;
    
    
    private String stripAID(String aid) throws MojoExecutionException
    {
       if (aid != null){
           return aid.replace("0x","").replace(":", "");
           
       }
       else
           throw new MojoExecutionException("AID to Strip is not set");
           
    }
    
    private String buildGPShellScript( String cardReaderName,
                       String cardMAC, String cardKey, 
                       String appletAID, String appletPackageAID,
                       String fileName) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("mode_211\n");
        buffer.append("enable_trace\nestablish_context\n");
        buffer.append("card_connect -reader \"");
        buffer.append(cardReaderName);
        buffer.append("\"\n");
        buffer.append("select -AID ");
        buffer.append("a000000003000000");//Java default card manager
        buffer.append("\n");
        buffer.append("open_sc -scp 2 -scpimpl 0x15 -security 1 -keyind 0 -keyver 1 -mac_key ");
        buffer.append(cardMAC);
        buffer.append(" -enc_key ");
        buffer.append(cardKey);
        buffer.append("\n");
        buffer.append("delete -AID ");
        buffer.append(appletAID);
        buffer.append("\n");
        buffer.append("delete -AID ");
        buffer.append(appletPackageAID);
        buffer.append("\n");
        
        buffer.append("install -file ");
        buffer.append(fileName);
        buffer.append(" -instParam C90145 -priv 2\n");
         buffer.append("card_disconnect \nrelease_context");

        return buffer.toString();
    }
    
        private String inputStreamToString(InputStream in) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
    StringBuilder stringBuilder = new StringBuilder();
    String line = null;

    while ((line = bufferedReader.readLine()) != null) {
    stringBuilder.append(line + "\n");
    }

    bufferedReader.close();
    return stringBuilder.toString();
    }
        
    private void connectStreams(InputStream a,OutputStream b) throws Exception{
        BufferedReader r = new BufferedReader (new InputStreamReader(a));
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(b));
        int cbuf;
        
        

        
        while (  (cbuf = r.read()) != -1 )
        {
           w.append((char) cbuf);           
        }
        
        w.close();
    }

    public void execute()
            throws MojoExecutionException {
        String gpshellscript =  buildGPShellScript(cardReaderName,
                                    cardMAC, 
                                    cardKey, 
                                    stripAID(appletAID), 
                                    stripAID(appletPackageAID),
                                    project.getArtifact().getFile().getAbsolutePath()
                                    );
        
        
               getLog().info("Debuginformation \n"+gpshellscript);
        
    
    
    try
    {
  //  Process p2 = Runtime.getRuntime().exec("echo \"get_status\"");
  //  p2.waitFor(); // p2 is ready.
    
    getLog().info("Invoking GPSHELL...");
    
    String [] envVars =  new String [] {"LD_LIBRARY_PATH=/usr/local/lib"};
    Process p = Runtime.getRuntime().exec("gpshell",envVars);    

    connectStreams(new ByteArrayInputStream(gpshellscript.getBytes())
                , p.getOutputStream());

    p.waitFor();
    
    getLog().info("Exit Value was"+ p.exitValue());
 
    getLog().info("StdOut: " + inputStreamToString( p.getInputStream()));
    
    getLog().info("ErrorOut: " + inputStreamToString(p.getErrorStream())
                );
    }
    catch (Exception ex)
    {
        getLog().error((ex.getMessage()));
    }
    }     
}