/*
 * Copyright 2011 Ruhr-University Bochum, Chair for Network and Data Security
 * 
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 * 
 *        http://nds.rub.de/research/projects/sec2/
 */

package org.sec2.desktop;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * <DESCRIPTION>
 * @author  Juraj Somorovsky - juraj.somorovsky@rub.de
 * @date    Jul 17, 2012
 * @version 0.1
 *
 */
public class ConsoleInterface {

    public static void main(String[] args) {
        Sec2Middleware sm = Sec2Middleware.getSec2Middleware();

        try {
            if(args.length != 2) {
                sm.startMiddlewareServer("*", 50001);
                System.out.print("Sec2 Middleware started on *, " +
                        "port 50001. \nTo stop it, please type 'stop'.\n");
            } else {
                sm.startMiddlewareServer(args[0], Integer.parseInt(args[1]));
                System.out.print("Sec2 Middleware started on " + args[0] +
                        ", port " + args[1] + ". \nTo stop it, please type 'stop'.\n");
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while(true) {
                String line = br.readLine();
                if (line.trim().equalsIgnoreCase("stop")) {
                    System.out.println("Stopping Sec2 Middleware");
                    break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                sm.stopMiddlewareServer();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
