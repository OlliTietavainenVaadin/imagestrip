package org.vaadin.peter.imagestrip.client;

import org.vaadin.peter.imagestrip.ImageStrip;

import com.google.gwt.core.client.GWT;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.Paintable;
import com.vaadin.client.UIDL;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

@Connect(ImageStrip.class)
public class VImageStripConnector extends AbstractComponentConnector implements
		Paintable {

	private static final long serialVersionUID = 7250536809750261868L;

	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		getWidget().updateFromUIDL(uidl, client);
	}

	@Override
	protected VImageStrip createWidget() {
		return (VImageStrip) GWT.create(VImageStrip.class);
	}

	@Override
	public VImageStrip getWidget() {
		return (VImageStrip) super.getWidget();
	}

}
