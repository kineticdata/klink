Kinetic Link 1.0.1, README.txt
Last Updated: 2006-08-25

TABLE OF CONTENTS
=================
1.0 What is Kinetic Link?
2.0 What is in store for Klink?
3.0 Frequently Asked Questions
4.0 Known Issues
5.0 Document Changelog
 

1.0 WHAT IS KINETIC LINK?
=========================
Kinetic Link, or Klink, is a web framework that provides a consistent XML over
HTTP interface for interacting with the Action Request System.  The framework
itself is written using Java Servlets and Apache Struts, however once Klink is
running any language that can make HTTP requests and parse the xml results can
be used to consume the framework.


2.0 WHAT IS IN STORE FOR KLINK?
===============================

The development team is activly developing a Klink homepage, including a Bug 
Tracking and RFE system, a message board, and enhanced documentation.  
Additionally, development for utility libraries and a series of sample 
applications are in the final stages for Java, and development has been started
in languages such as Ruby, Python, and Javascript.  A detailed list of planned 
future enhancements is available in the attached ROADMAP.txt.


3.0 FREQUENTLY ASKED QUESTIONS
==============================

3.1 WHERE CAN I GET MORE INFORMATION
New documentation is released at the Klink homepage as it becomes available.
The homepage is currently at http://www.kineticdata.com/products/klink/.
Additionally, as Kinetic Link is an open source project, the development team
is very willing to work with anyone to get questions answered and problems
worked out.  We can be reached at klink@kineticdata.com.

3.2 A KLINK CALL RETURNS "UNABLE TO PROCESS REQUEST, THERE IS NO FRAMEWORK 
    CALL MATCHING THE REQUEST" EVEN THOUGH I KNOW THERE IS A MATCHING CALL
Check to make sure that the framework call does not end with a trailing "/".
The Klink framework is unable to determine the call matching the request
because of the way that requests are mapped.  A trailing "/" actually means
that the last method parameter is null.  Because few of the framework calls
have optional method parameters, this changes the signature of the method
to one that does not exist in the mapping file.


4.0 KNOWN ISSUES
================

4.1 INCOMPATILIBITY WITH SERVLETEXEC RUNNING MID-TIER
Some significant time was put into attempting to overcome this problem, however
because Mid-tier and Klink both require the the Ars system library this may be
a perminant limitation of ServletExec.


5.DOCUMENT CHANGELOG
====================
Kinetic Link 1.0.1, 1006-09-15
 - Removed notes version 7.0 incompatibility.

Kinetic Link 1.0.0, 2006-08-25
 - Added the frequently asked questions section
 - Added the known issues section
 - Removed the beta specific sections

Kinetic Link 1.0.0b, 2006-07-13
 - initial release
