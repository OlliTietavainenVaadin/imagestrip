package org.vaadin.peter.imagestrip.demo;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.peter.imagestrip.ImageStrip;

@Theme("demo")
@Title("MyComponent Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI
{

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class)
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest request) {

        // Initialize our new UI component
        ImageStrip imageStrip = new ImageStrip();
        imageStrip
            .addImage(new ExternalResource(
                "http://i.imgur.com/8c3m5Wo.jpg"));
        imageStrip
            .addImage(new ExternalResource(
                "http://i.imgur.com/mz6RXKU.jpg"));
        imageStrip
            .addImage(new ExternalResource(
                "http://i.imgur.com/EJnazue.jpg"));
        imageStrip
            .addImage(new ExternalResource(
                "http://farm4.staticflickr.com/3159/5743166291_194d90f356_z.jpg"));

        imageStrip.setSelectable(true);
        // Show it in the middle of the screen
        final VerticalLayout layout = new VerticalLayout();
        layout.setStyleName("demoContentLayout");
        layout.setSizeFull();
        layout.setMargin(false);
        layout.setSpacing(false);
        layout.addComponent(imageStrip);
        layout.setComponentAlignment(imageStrip, Alignment.MIDDLE_CENTER);
        setContent(layout);
    }
}
