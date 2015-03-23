package org.esa.beam.smos.ee2netcdf.visat;


import org.esa.snap.rcp.actions.AbstractSnapAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@ActionID(
        category = "File",
        id = "Ee2NetCDFExportAction"
)

@ActionRegistration(
        displayName = "#CTL_Ee2NetCDFExport_MenuText",
        popupText = "#CTL_Ee2NetCDFExport_ShortDescription",
        lazy = true
)

@ActionReferences({
        @ActionReference(
                path = "File/Export",
                position = 110
        ),
        @ActionReference(
                path = "Shortcuts",
                name = "D-N"
        ),
})

@NbBundle.Messages({
        "CTL_Ee2NetCDFExport_MenuText=Export SMOS EE Files to NetCDF...",
        "CTL_Ee2NetCDFExport_ShortDescription=Export SMOS EE Files to linear NetCDF format, preserving the Snapshot/Gridpoint structures"
})
public class EeNetCDFExportAction extends AbstractSnapAction implements HelpCtx.Provider {

    public EeNetCDFExportAction() {
        putValue(NAME, Bundle.CTL_Ee2NetCDFExport_MenuText());
        putValue(SHORT_DESCRIPTION, Bundle.CTL_Ee2NetCDFExport_ShortDescription());
        setHelpId("smosNetcdfExport");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
