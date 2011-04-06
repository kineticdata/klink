package com.kd.klink.control;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * 
 */
public abstract class DataController extends Controller {
       public abstract ActionForward entries(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response);
       public abstract ActionForward entry(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response);
       public abstract ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response);
       public abstract ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response);
       public abstract ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response);
}
