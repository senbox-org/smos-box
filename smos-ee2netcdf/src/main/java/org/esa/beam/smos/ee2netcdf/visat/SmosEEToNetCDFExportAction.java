package org.esa.beam.smos.ee2netcdf.visat;

import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.visat.actions.AbstractVisatAction;

public class SmosEEToNetCDFExportAction extends AbstractVisatAction {

    @Override
    public void actionPerformed(CommandEvent event) {
        new SmosEEToNetCDFExportDialog(getAppContext(), event.getCommand().getHelpId()).show();
    }
}
