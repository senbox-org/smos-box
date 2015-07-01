/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.smos.gui.export;


import org.esa.snap.rcp.actions.AbstractSnapAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

@ActionID(category = "File", id = "SmosGridPointExportAction")

@ActionRegistration(
        displayName = "#CTL_SmosGridPointExport_MenuText",
        popupText = "#CTL_SmosGridPointExport_ShortDescription",
        lazy = true
)

@ActionReference(path = "Menu/File/Export")

@NbBundle.Messages({
        "CTL_SmosGridPointExport_MenuText=SMOS Grid Points ...",
        "CTL_SmosGridPointExport_ShortDescription=Export SMOS Grid Points to Clipboard or ASCII File"
})

public class GridPointExportAction extends AbstractSnapAction {

    private static final String HELP_ID = "smosGridPointExport";
    private GridPointExportDialog dialog;

    public GridPointExportAction() {
        putValue(NAME, Bundle.CTL_SmosGridPointExport_MenuText());
        putValue(SHORT_DESCRIPTION, Bundle.CTL_SmosGridPointExport_ShortDescription());
        setHelpId(HELP_ID);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (dialog == null) {
            dialog = new GridPointExportDialog(getAppContext(), HELP_ID);
        }

        dialog.show();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }
}
