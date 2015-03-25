package org.esa.beam.smos.ee2netcdf.ui;


import org.esa.snap.rcp.actions.AbstractSnapAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

import java.awt.event.ActionEvent;

@ActionID(
        category = "File",
        id = "FakeAction"
)

@ActionRegistration(
        displayName = "FakeAction",
        popupText = "FakeAction_description"
)

@ActionReference(
        path = "Menu/File/Export"
)

public class FakeAction extends AbstractSnapAction {

    public FakeAction() {
        System.out.println("================================================" + enabled);
        putValue(NAME, "FakeName");
        putValue(SHORT_DESCRIPTION, "FakeDescription");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
