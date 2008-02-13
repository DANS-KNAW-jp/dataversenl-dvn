/*
 * Dataverse Network - A web application to distribute, share and analyze quantitative data.
 * Copyright (C) 2007
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation,Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

/*
 * EditBannerFooterPage.java
 *
 * Created on October 10, 2006, 10:46 AM
 */
package edu.harvard.hmdc.vdcnet.web.admin;

import edu.harvard.hmdc.vdcnet.util.ExceptionMessageWriter;
import edu.harvard.hmdc.vdcnet.vdc.VDC;
import edu.harvard.hmdc.vdcnet.vdc.VDCNetwork;
import edu.harvard.hmdc.vdcnet.vdc.VDCNetworkServiceLocal;
import edu.harvard.hmdc.vdcnet.vdc.VDCServiceLocal;
import edu.harvard.hmdc.vdcnet.web.common.VDCBaseBean;
import javax.ejb.EJB;
import javax.faces.component.html.HtmlCommandButton;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class EditBannerFooterPage extends VDCBaseBean  implements java.io.Serializable {
    @EJB VDCServiceLocal vdcService;
    @EJB VDCNetworkServiceLocal vdcNetworkService;
    // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Definition">
    private int __placeholder;
    
    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    public void init() {
        super.init();
        success = false;
        if (this.getBanner() == null){
            setBanner( (getVDCRequestBean().getCurrentVDCId() == null) ? getVDCRequestBean().getVdcNetwork().getNetworkPageHeader(): getVDCRequestBean().getCurrentVDC().getHeader());
            setFooter( (getVDCRequestBean().getCurrentVDCId() == null) ? getVDCRequestBean().getVdcNetwork().getNetworkPageFooter(): getVDCRequestBean().getCurrentVDC().getFooter());
        }
    }
    
    
    private HtmlCommandButton btnSave = new HtmlCommandButton();

    public HtmlCommandButton getBtnSave() {
        return btnSave;
    }

    public void setBtnSave(HtmlCommandButton hcb) {
        this.btnSave = hcb;
    }

    private HtmlCommandButton btnCancel = new HtmlCommandButton();

    public HtmlCommandButton getBtnCancel() {
        return btnCancel;
    }

    public void setBtnCancel(HtmlCommandButton hcb) {
        this.btnCancel = hcb;
    }
    
    // </editor-fold>

    private String ERROR_MESSAGE   = new String("An Error Occurred.");
    private String SUCCESS_MESSAGE = new String("Update Successful!");
    /** 
     * <p>Construct a new Page bean instance.</p>
     */
    public EditBannerFooterPage() {
    }



    /** 
     * <p>Callback method that is called after the component tree has been
     * restored, but before any event processing takes place.  This method
     * will <strong>only</strong> be called on a postback request that
     * is processing a form submit.  Customize this method to allocate
     * resources that will be required in your event handlers.</p>
     */
    public void preprocess() {
    }

    /** 
     * <p>Callback method that is called just before rendering takes place.
     * This method will <strong>only</strong> be called for the page that
     * will actually be rendered (and not, for example, on a page that
     * handled a postback and then navigated to a different page).  Customize
     * this method to allocate resources that will be required for rendering
     * this page.</p>
     */
    public void prerender() {
    }

    /** 
     * <p>Callback method that is called after rendering is completed for
     * this request, if <code>init()</code> was called (regardless of whether
     * or not this was the page that was actually rendered).  Customize this
     * method to release resources acquired in the <code>init()</code>,
     * <code>preprocess()</code>, or <code>prerender()</code> methods (or
     * acquired during execution of an event handler).</p>
     */
    public void destroy() {
    }
    
    private String banner;
    
    public String getBanner(){
        return banner;
    }
    
    public void setBanner(String banner) {
        this.banner = banner;
    }
    
    private String footer;
    
    public String getFooter() {
        return footer;
    }
    
    public void setFooter(String footer) {
        this.footer = footer;
    }
    
     /**
     * Holds value of property success.
     */
    private boolean success;

    /**
     * Getter for property success.
     * @return Value of property success.
     */
    public boolean isSuccess() {
        return this.success;
    }

    /**
     * Setter for property success.
     * @param success New value of property success.
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    // ACTION METHODS
    public String save_action() {
        String msg  = SUCCESS_MESSAGE;
        success = true;
        //add the success message to the queue for global messages
        ExceptionMessageWriter.addGlobalMessage(msg);
        try {
            if (getVDCRequestBean().getCurrentVDCId() == null) {
                // this is a save against the network
                VDCNetwork vdcnetwork = getVDCRequestBean().getVdcNetwork();
                vdcnetwork.setNetworkPageHeader(banner);
                vdcnetwork.setNetworkPageFooter(footer);
                vdcNetworkService.edit(vdcnetwork);
                getVDCRequestBean().getVdcNetwork().setNetworkPageHeader(banner);
                getVDCRequestBean().getVdcNetwork().setNetworkPageFooter(footer);
             } else {
                VDC vdc = vdcService.find(new Long(getVDCRequestBean().getCurrentVDC().getId()));
                vdc.setHeader(banner);
                vdc.setFooter(footer);
                vdcService.edit(vdc);
                getVDCRequestBean().getCurrentVDC().setHeader(banner);
                getVDCRequestBean().getCurrentVDC().setFooter(footer);
             }
        } catch (Exception e) {
            success = false;
            // remove the queued message from the FacesContext to avoid default FacesMessage logging
            ExceptionMessageWriter.removeGlobalMessage(SUCCESS_MESSAGE);
            ExceptionMessageWriter.logException(e);
            System.out.println("an exception occurred.");
        } finally {
            return "result";
        }
    }

    public String cancel_action(){
        if (getVDCRequestBean().getCurrentVDCId() == null) {
            setBanner(getVDCRequestBean().getVdcNetwork().getNetworkPageHeader());
            setFooter(getVDCRequestBean().getVdcNetwork().getNetworkPageFooter());
            return "cancelNetwork";
        } else {
                setBanner(getVDCRequestBean().getCurrentVDC().getHeader());
                setFooter(getVDCRequestBean().getCurrentVDC().getFooter());
            return "cancelVDC";
        }
    }
}

