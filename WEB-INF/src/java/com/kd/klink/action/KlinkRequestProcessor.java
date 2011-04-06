package com.kd.klink.action;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This abstract class describes the first point of control in the Klink
 * processing chain, classes extending the KlinkRequestProcessor are responsible
 * for deciding if the request is for static content, Klink management, or an 
 * actual framework request.  If the request is for static content this class 
 * will attempt to locate the file in the root directory for the klink web 
 * application and write back the contents.  If the request is to display the 
 * about page this class will call the abstract method "printAboutPage".  If the
 * request is for Klink management this class will call the abstract method 
 * "manage".  If the request is for an actual framework request the request will
 * be forwarded on the the second point of control in the Klink processing 
 * chain, the KlinkFrameworkRequestProcessor.
 */
public abstract class KlinkRequestProcessor extends HttpServlet {
    protected ServletConfig config = null;
    
    public KlinkRequestProcessor() { super(); }
    public KlinkRequestProcessor(ServletConfig config) throws ServletException { this.init(config); }
    
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        super.init(config);
    }
    
    /**
     * This method is called whenever the a request is made to the framework in
     * the format of /klink/manage or /klink/manage/.  No processing is done
     * outside of that within the overriden method.
     */
    public abstract void manage(HttpServletRequest request, HttpServletResponse response);
    /**
     * This method should be overriden to return an HTML page that shows version
     * information, present and missing requirements, and anything else helpful
     * in debugging issues with the framework.
     */
    public abstract void printAboutPage(HttpServletResponse response);
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) { doProcess(request, response); }
    public void doPost(HttpServletRequest request, HttpServletResponse response) { doProcess(request, response); }
    
    /**
     * The generic method called when either a GET or POST http request is made.
     * This method parses the request and attempts to determine the type of
     * klink request made (static content, about, mangement, or framework).
     */ 
    private void doProcess(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            String requestUrl = new String(httpRequest.getRequestURL());
            String requestTarget = getRequestTarget(requestUrl);
            
            // If we couldn't find /klink/ on the request Url
            if (requestTarget == null) {
                // Redirect to /klink/ (this makes things nicer for determining the real path of static content
                httpResponse.sendRedirect("/klink/");
            }
            // If we could find /klink/ on the request Url
            else {
                // If the target is ""
                if (requestTarget.equals("")) {
                    // Make the target index.html
                    requestTarget = "index.html";
                }
                // If the target is for static content
                if (requestTarget.endsWith(".htm") || requestTarget.endsWith(".html") || requestTarget.endsWith(".txt")) {
                    printStaticContent(httpRequest, httpResponse, requestTarget);
                }
                // If the target is for the about page
                else if (requestTarget.equals("about")) {
                    printAboutPage(httpResponse);
                }
                // If the target is for the manage page
                else if (requestTarget.equals("manage")) {
                    manage(httpRequest, httpResponse);
                }
                // If the target is a framework call
                else {
                    // Process the request
                    KlinkFrameworkRequestProcessor wrapper = new KlinkFrameworkRequestProcessor(config);
                    wrapper.process(httpRequest, httpResponse, requestTarget);
                }
            }
        }
        // If there was a problem processing the request
        catch (Exception e) {
            // Write the exception to standard error (likely the Servlet Container log files)
            System.out.println(e.toString());
            e.printStackTrace(System.out);
        }
    }
    
    /**
     * Private method used to determine the target of the Klink request (this
     * will differentiate between a request for static content, the about page,
     * management, or an actual framework request.
     */
    private String getRequestTarget(String requestUrl) {
        // Set the default requestTarget
        String requestTarget = null;

        // Get the Framework Calls Method Name
        String klinkString = "/klink/";
        int klinkLoc = requestUrl.indexOf(klinkString);

        // If /klink/ was found 
        if (klinkLoc != -1) {
            // Get the index of the first character after the /klink/ string
            int afterKlink = klinkLoc + klinkString.length();
            // Get the index of the first "/" after the /klink/ string
            int afterMethodName = requestUrl.indexOf("/", afterKlink);

            // If there isn't a / after the /klink/ string
            if (afterMethodName == -1) {
                // The target is whatever is after the /klink/ string
                requestTarget = requestUrl.substring(afterKlink);
            }
            // If there is a / after the /klink/string
            else {
                // The target is whatever is between the /klink/ string and the next /
                requestTarget = requestUrl.substring(afterKlink, afterMethodName);
            }
        }

        // Return the requestTarget
        return requestTarget;
    }
    
    /**
     * Private method that attempts to respond with static content (such as .txt
     * files, or html).  This method is only able to display static content of
     * files in the root Klink web application directory.  If the file does not
     * exist in that directory an Http File Not Found response is returned, if
     * this servlet does not have enough privlidges to access the file an Http
     * Forbidden response is returned, and if there is a problem generating the
     * response an Http Internal Server Error response is returned.
     *
     * throws   IOException If unable to send an Http Internal Server Error back
     *                      to the requester.
     */
    private void printStaticContent(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String target) throws IOException {
        try {
            // Set the content type expected
            if (target.endsWith(".htm") || target.endsWith(".html")) {
                httpResponse.setContentType("text/html");
            } else if (target.endsWith(".txt")) {
                httpResponse.setContentType("text/plain");
            }
            
            // Get the printwriter
            PrintWriter out = httpResponse.getWriter();

            // Get the file associated with our target
            File file = new File(config.getServletContext().getRealPath(target));

            // Return a response
            if (!file.exists()) {
                httpResponse.sendError(httpResponse.SC_NOT_FOUND);
            } else if (!file.canRead()) {
                httpResponse.sendError(httpResponse.SC_FORBIDDEN);
            } else {
                // Print the contents of the file
                String str;
                BufferedReader in = new BufferedReader(new FileReader(file));
                while ((str = in.readLine()) != null) { out.println(str); }
                in.close();
            }

            // Flush the printwriter
            out.flush();
        }
        // If there was a problem printing the static content
        catch (Exception e) {
            // Write the exception to standard error (likely the Servlet Container log files)
            System.out.println(e.toString());
            e.printStackTrace(System.out);
            
            // Send an internal server error response
            httpResponse.sendError(httpResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
