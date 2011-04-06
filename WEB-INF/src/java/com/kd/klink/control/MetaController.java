package com.kd.klink.control;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *
 */
public abstract class MetaController extends Controller {
    public abstract ActionForward configurations(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response);
    public abstract ActionForward permissions(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response);
    public abstract ActionForward statistics(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response);
    public abstract ActionForward usercheck(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response);
}
