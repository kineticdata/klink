package com.kd.klink.control;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *
 */
public abstract class StructureController extends Controller {
    public abstract ActionForward structures(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response);
    public abstract ActionForward structure(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response);
}
