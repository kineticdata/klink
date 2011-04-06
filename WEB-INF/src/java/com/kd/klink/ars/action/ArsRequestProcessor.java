package com.kd.klink.ars.action;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.kd.klink.action.*;
import com.kd.klink.verification.*;
import com.kd.klink.ars.verification.*;

/**
 * This class is an implementation of the abstract KlinkRequestProcessor and
 * should be used as the first point of control in the Klink processing chain
 * (by adding a mapping rule from /* to the ArsRequestProcessor to the web
 * applications web.xml file).
 */
public class ArsRequestProcessor extends KlinkRequestProcessor {
    /**
     * This method implements the abstract method defined in the 
     * KlinkRequestProcessor class, and returns a simple HTML page stating that
     * there are no Ars specific management configurations.
     */
    public void manage(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Set the content type
            response.setContentType("text/html");

            // Get the writer
            PrintWriter out = response.getWriter();

            // Write the Page
            out.println("<html>");
            out.println("   <head>");
            out.println("      <title>Klink-Ars Management Page</title>");
            out.println("   </head>");
            out.println("   <body>");
            out.println("      <table height=\"100%\" width=\"100%\">");
            out.println("         <tr valign=\"top\">");
            out.println("            <td>There are no Ars specific managment configurations.</td>");
            out.println("         </tr>");
            out.println("         <tr valign=\"bottom\">");
            out.println("         <td>");
            out.println("               <hr/>");
            out.println("               Kinetic Link is sponsored by <a href=\"http://www.kineticdata.com\">Kinetic Data Inc.</a>, a BMC Software Solution Partner.<br/>");
            out.println("               Kinetic Link is licensed on the BSD Copyright, <a href=\"LICENSE.txt\">click here</a> to view the licence.<br/>");
            out.println("            </td>");
            out.println("         </tr>");
            out.println("      </table>");
            out.println("   </body>");
            out.println("</html>");

            // Close the writer
            out.flush();
        } catch (Exception e) {
            // Write the exception information to the servlet logs.
            System.err.println(e.toString());
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * This method implements the abstract method defined in the
     * KlinkRequestProcessor class, and returns a simple HTML page that includes
     * version and requirement information for Klink-Ars and Klink-Commons as
     * well as environment information useful in tracking framework problems.
     */
    public void printAboutPage(HttpServletResponse response) {
        try {
            // Set the content type
            response.setContentType("text/html");

            // Get the writer
            PrintWriter out = response.getWriter();

            // Start the Page
            out.println("<html>");
            out.println("   <head>");
            out.println("      <title>About Klink-Ars</title>");
            out.println("   </head>");
            out.println("   <body>");
            out.println("      <table height=\"100%\" width=\"100%\">");
            out.println("         <tr valign=\"top\">");
            out.println("            <td>");
            
            // Write the Klink-Ars Header
            out.println("               <font size=\"5\"><b>Klink-Ars</b></font><br/>");
            
            // Write the Klink-Ars Required Ars System Libraries
            out.println("               <font size=\"4\"><b>Required Ars Libraries</b></font><br/>");
            try {
                String[] libraries = ArsApiVerifier.getRequiredSystemLibraries();
                out.println("               <table border=\"1\" cellpadding=\"2\" cellspacing=\"0\" width=\"100%\">");
                out.println("                  <tr align=\"center\">");
                out.println("                     <th width=\"75\">Presence</th>");
                out.println("                     <th width=\"150\">Library</th>");
                out.println("                     <th>Location or Message</th>");
                out.println("                  </tr>");
                for (int i=0;i<libraries.length;i++) {
                    out.println("                  <tr>");
                    try {
                        VerificationUtils.loadSystemLibrary(libraries[i]);
                        String location = VerificationUtils.getSystemLibraryLocation(libraries[i]);
                        out.println("                     <td align=\"center\"><font color=\"green\"><b>Found</b></td>");
                        out.println("                     <td align=\"center\">" + libraries[i] + "</td>");
                        out.println("                     <td>" + location + "</td>");
                    } catch (FileNotFoundException e) {
                        out.println("                     <td align=\"center\"><font color=\"green\"><b>Found</b></td>");
                        out.println("                     <td align=\"center\">" + libraries[i] + "</td>");
                        out.println("                     <td>Unable to determine location.</td>");
                    } catch (Exception e) {
                        out.println("                     <td align=\"center\"><font color=\"red\"><b>Missing</b></td>");
                        out.println("                     <td align=\"center\">" + libraries[i] + "</td>");
                        out.println("                     <td>" + e.toString() + "</td>");
                    } catch (Error e) {
                        out.println("                     <td align=\"center\"><font color=\"red\"><b>Missing</b></td>");
                        out.println("                     <td align=\"center\">" + libraries[i] + "</td>");
                        out.println("                     <td>" + e.toString() + "</td>");
                    }
                    out.println("                  </tr>");
                }
                out.println("               </table>");
            } catch (MissingApiException e) {
                out.println("               <font color=\"red\"><b>Error</b></font>: Could not determine required system libraries, unable to locate the arapi java library.<br/>");
            } catch (UnsupportedVersionException e) {
                out.println("               <font color=\"red\"><b>Error</b></font>: Could not determine required system libraries, unsupported arapi java library version.<br/>");
            }
            out.println("               <br/>");
            out.println();
            
            // Write the Klink-Ars Required Java Libraries
            out.println("               <font size=\"4\"><b>Required Java Libraries</b></font><br/>");
            out.println("               <table border=\"1\" cellpadding=\"2\" cellspacing=\"0\" width=\"100%\">");
            out.println("                  <tr align=\"center\">");
            out.println("                     <th width=\"75\">Presence</th>");
            out.println("                     <th width=\"150\">Library</th>");
            out.println("                     <th width=\"200\">");
            out.println("                        Specification Version<br/>");
            out.println("                        Implementation Version");
            out.println("                     </th>");
            out.println("                     <th>");
            out.println("                        Classname<br/>");
            out.println("                        Location");
            out.println("                     </th>");
            out.println("                  </tr>");
            out.println("                  <tr>");
            String arsClassName = "com.remedy.arsys.api.Util";
            String arsLibraryName = "arapi";
            try {
                String arsapiVersion = ArsApiVerifier.getApiVersion();
                
                out.println("                     <td align=\"center\"><font color=\"green\"><b>Found</b></font></td>");
                out.println("                     <td align=\"center\">" + arsLibraryName + "</td>");
                out.println("                     <td>" + arsapiVersion + "</td>");
                out.println("                     <td>");
                out.println("                        " + arsClassName + "<br/>");
                out.println("                        " + VerificationUtils.getClassLocation(arsClassName));
                out.println("                     </td>");
            } catch (MissingApiException e) {
                out.println("                     <td align=\"center\"><font color=\"red\"><b>Missing</b></font></td>");
                out.println("                     <td align=\"center\">" + arsLibraryName + "</td>");
                out.println("                     <td>&nbsp;</td>");
                out.println("                     <td>");
                out.println("                        " + arsClassName);
                out.println("                     </td>");
            }
            out.println("                  </tr>");
            out.println("                  <tr>");

            String klinkCommonsLibraryClass = "com.kd.klink.control.Controller";
            String klinkCommonsLibraryName = "klink-commons";
            if (VerificationUtils.classExists(klinkCommonsLibraryClass)) {
                out.println("                     <td align=\"center\"><font color=\"green\"><b>Found</b></font></td>");
                out.println("                     <td align=\"center\">" + klinkCommonsLibraryName + "</td>");
                out.println("                     <td>");
                out.println("                        " + VerificationUtils.getLibrarySpecificationVersion(klinkCommonsLibraryClass) + "<br/>");
                out.println("                        " + VerificationUtils.getLibraryImplementationVersion(klinkCommonsLibraryClass));
                out.println("                     </td>");
                out.println("                     <td>");
                out.println("                        " + klinkCommonsLibraryClass + "<br/>");
                out.println("                        " + VerificationUtils.getClassLocation(klinkCommonsLibraryClass));
                out.println("                     </td>");
            } else {
                out.println("                     <td align=\"center\"><font color=\"red\"><b>Missing</b></font></td>");
                out.println("                     <td align=\"center\">" + klinkCommonsLibraryName + "</td>");
                out.println("                     <td>&nbsp;</td>");
                out.println("                     <td>");
                out.println("                        " + klinkCommonsLibraryClass);
                out.println("                     </td>");
            }
            out.println("            </tr>");
            out.println("         </table>");
            out.println("         <br/>");
            out.println();
            
            // Write the Klink-Ars Commons Header
            out.println("               <font size=\"5\"><b>Klink-Commons</b></font><br/>");
            
            // Write the Klink-Ars Commons Requirements
            out.println("               <font size=\"4\"><b>Required Java Libraries</b></font><br/>");
            out.println("               <table border=\"1\" cellpadding=\"2\" cellspacing=\"0\" width=\"100%\">");
            out.println("                  <tr align=\"center\">");
            out.println("                     <th width=\"75\">Presence</th>");
            out.println("                     <th width=\"150\">Library</th>");
            out.println("                     <th width=\"200\">");
            out.println("                        Specification Version<br/>");
            out.println("                        Implementation Version");
            out.println("                     </th>");
            out.println("                     <th>");
            out.println("                        Classname<br/>");
            out.println("                        Location");
            out.println("                     </th>");
            out.println("                  </tr>");
            Library[] requiredLibraries = VerificationUtils.getRequiredLibraries();
            for (int i=0;i<requiredLibraries.length;i++) {
                String libraryClass = requiredLibraries[i].getLibraryIndicatorClassName();
                String libraryName = requiredLibraries[i].getName();
                String specVersion = VerificationUtils.getLibrarySpecificationVersion(libraryClass);
                if (specVersion == null) { specVersion = ""; }
                String implVersion = VerificationUtils.getLibraryImplementationVersion(libraryClass);
                if (implVersion == null) { implVersion = ""; }
                
                out.println("                  <tr>");
                if (VerificationUtils.classExists(requiredLibraries[i].getLibraryIndicatorClassName())) {
                    out.println("                     <td align=\"center\"><font color=\"green\"><b>Found</b></font></td>");
                    out.println("                     <td align=\"center\">" + libraryName + "</td>");
                    out.println("                     <td>");
                    out.println("                        " + specVersion + "<br/>");
                    out.println("                        " + implVersion);
                    out.println("                     </td>");
                    out.println("                     <td>");
                    out.println("                        " + libraryClass + "<br/>");
                    out.println("                        <font size=\"3\">" + VerificationUtils.getClassLocation(libraryClass) + "</font>");
                    out.println("                     </td>");
                } else {
                    out.println("                     <td align=\"center\"><font color=\"red\"><b>Missing</b></font></td>");
                    out.println("                     <td align=\"center\">" + libraryName + "</td>");
                    out.println("                     <td>&nbsp;</td>");
                    out.println("                     <td>");
                    out.println("                        " + libraryClass);
                    out.println("                     </td>");
                }
                out.println("                  </tr>");
            }
            out.println("               </table>");
            out.println("               <br/>");
            
            // Write the Environment Information
            out.println("               <font size=\"5\"><b>Environment</b></font><br/>");
            out.println("               <table>");
            out.println("                  <tr>");
            out.println("                     <td><u>Java Version</u>:</td>");
            out.println("                     <td>" + VerificationUtils.getJavaVersion() + "</td>");
            out.println("                  </tr>");
            out.println("                  <tr>");
            out.println("                     <td width=\"200\"><u>Operating System</u>:</td>");
            out.println("                     <td>" + VerificationUtils.getOperationSystem() + "</td>");
            out.println("                  </tr>");
            out.println("                  <tr>");
            out.println("                     <td><u>Servlet Container</u>:</td>");
            out.println("                     <td>" + this.getServletContext().getServerInfo() + "</td>");
            out.println("                  </tr>");
            out.println("                  <tr valign=\"top\">");
            out.println("                     <td><u>java.library.path</u>:</td>");
            out.println("                     <td>");
            String[] libraryPaths = VerificationUtils.getJavaLibraryPath();
            for (int i=0;i<libraryPaths.length;i++) {
                out.println("                                    " + libraryPaths[i] + "<br/>");
            }
            out.println("                     </td>");
            out.println("                  </tr>");
            out.println("               </table>");
            
            // Finish the Page
            out.println("            </td>");
            out.println("         </tr>");
            out.println("         <tr valign=\"bottom\">");
            out.println("            <td>");
            out.println("            <hr/>");
            out.println("            Kinetic Link is sponsored by <a href=\"http://www.kineticdata.com\">Kinetic Data Inc.</a>, a BMC Software Solution Partner.<br/>");
            out.println("            Kinetic Link is licensed on the BSD Copyright, <a href=\"LICENSE.txt\">click here</a> to view the licence.<br/>");
            out.println("            </td>");
            out.println("         </tr>");
            out.println("      </table>");
            out.println("   </body>");
            out.println("</html>");

            // Close the writer
            out.flush();
        } catch (Exception e) {
            // Write the exception information to the servlet logs.
            System.err.println(e.toString());
            e.printStackTrace(System.err);
        }
    }
}
