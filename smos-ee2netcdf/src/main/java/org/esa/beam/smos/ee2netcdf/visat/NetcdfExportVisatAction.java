package org.esa.beam.smos.ee2netcdf.visat;

import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.visat.actions.AbstractVisatAction;

public class NetcdfExportVisatAction extends AbstractVisatAction {

    @Override
    public void actionPerformed(CommandEvent event) {
        new NetcdfExportDialog(getAppContext(), event.getCommand().getHelpId()).show();
    }
}
