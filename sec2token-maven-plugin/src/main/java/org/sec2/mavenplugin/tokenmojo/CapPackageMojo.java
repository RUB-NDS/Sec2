package org.sec2.mavenplugin.tokenmojo;

/*
 * @author Thorsten Schreiber, HGI Bochum, for sec2.org
 */
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.handler.ArtifactHandler;

/**
 * Java Card Mojo
 *
 * @goal package
 *
 * @phase package
 */
public class CapPackageMojo
        extends AbstractMojo {

    private void copyFile(File from, File to) throws IOException {


        FileReader in = new FileReader(from);
        FileWriter out = new FileWriter(to);
        int c;

        while ((c = in.read()) != -1) {
            out.write(c);
        }

        in.close();
        out.close();
    }

    private String removePrefix(String classpath) {
        String[] splitAppletPacket = classpath.split("\\.");
        return splitAppletPacket[splitAppletPacket.length - 1];

    }
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
    /**
     * @parameter expression="${project.build.outputDirectory}" @required
     */
    private File buildOutputDirectory;
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
     * Parameter of CAP Converter Applet Packagename
     *
     * @parameter expression="${appletPacket}"
     */
    private String appletPacket;
    /**
     * Parameter of CAP Converter
     *
     * @parameter expression="${appletVersion}"
     */
    private String appletVersion;
    /**
     * Location of the JavaCardKit.
     *
     * @parameter expression="${JavaCardKitLocation}" @required
     */
    private String javaCardKitLocation;

    public void execute()
            throws MojoExecutionException {

        /*
         * First Exception Check, the order is relevant!!
         */

        if ((javaCardKitLocation == null)) {
            throw new MojoExecutionException(
                    "Could not read the Java Card Kit Location Variable");
        }



        File jckF = new File(javaCardKitLocation);
        if (!jckF.exists()) {
            throw new MojoExecutionException(
                    "The Java Card Kit Location not found!");
        }



        String javahome = System.getenv("JAVA_HOME");


        //Convert aufrufen
        String[] parameters = {
            "-noverify",
            "-i",
            "-classdir", buildOutputDirectory.getAbsolutePath(),
            "-out", "CAP",
            "-exportpath", javaCardKitLocation + "/api_export_files",
            "-applet", appletAID, appletClass, appletPacket, appletPackageAID, appletVersion
        };


        getLog().info("Will now Try To Convert");


        com.sun.javacard.converter.Converter.main(parameters);

        //get Package name without path, only Last

        try {


            /*
             * copyFile( new File(//This is the expected Location of the
             * converter File buildOutputDirectory.getAbsolutePath() + "/" +
             * appletPacket.replace(".", "/") + "/javacard/"+
             * removePrefix(appletPacket ) +".cap" ),
             *
             * // This is the Final Location of Cap new File(
             * outputDirectory.getAbsolutePath() + "/" + finalName + ".cap")
             *
             */

            project.getArtifact().setFile(
                    new File(//This is the expected Location of the converter File
                    buildOutputDirectory.getAbsolutePath()
                    + "/" + appletPacket.replace(".", "/")
                    + "/javacard/" + removePrefix(appletPacket) + ".cap"));
            project.getArtifact().setArtifactHandler(artifactHandler);



        } catch (Exception ex) {
            throw new MojoExecutionException("Could not copy CAP File: "
                    + ex.getMessage());
        }

    }
}
