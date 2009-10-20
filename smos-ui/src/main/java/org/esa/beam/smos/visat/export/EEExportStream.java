package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.DataFormat;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.util.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

class EEExportStream implements GridPointFilterStream {

    private final File targetDirectory;

    private DataContext targetContext;
    private EEExportGridPointHandler targetGridPointHandler;
    private File targetDblFile;
    private File targetHdrFile;

    public EEExportStream(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    @Override
    public void startFile(SmosFile sourceFile) throws FileNotFoundException {
        final File sourceDblFile = sourceFile.getFile();
        targetDblFile = getTargetDblFile(sourceDblFile);
        final DataFormat targetFormat = sourceFile.getFormat();

        targetContext = targetFormat.createContext(targetDblFile, "rw");
        targetGridPointHandler = new EEExportGridPointHandler(targetContext);
    }

    @Override
    public void stopFile(SmosFile sourceFile) throws IOException {
        try {
            final File sourceHdrFile = getSourceHdrFile(sourceFile.getFile());
            targetHdrFile = getTargetHdrFile(sourceHdrFile);

            final EEHdrFilePatcher patcher = new EEHdrFilePatcher();
            if (targetGridPointHandler.hasValidPeriod()) {
                final FileNamePatcher fileNamePatcher = createFileNamePatcher();
                patcher.setFileName(fileNamePatcher.getFileNameWithoutExtension());
                patcher.setSensingPeriod(targetGridPointHandler.getSensingStart(), targetGridPointHandler.getSensingStop());
            }
            patcher.patch(sourceHdrFile, targetHdrFile);
        } finally {
            dispose();
        }
    }

    @Override
    public void handleGridPoint(int id, CompoundData gridPointData) throws IOException {
        targetGridPointHandler.handleGridPoint(id, gridPointData);
    }

    @Override
    public void close() throws IOException {
        dispose();
    }

    private void dispose() {
        if (targetContext != null) {
            targetContext.dispose();
        }

        final FileNamePatcher fileNamePatcher = createFileNamePatcher();
        targetGridPointHandler = null;
        final File targetDir = targetHdrFile.getParentFile();
        targetHdrFile.renameTo(new File(targetDir, fileNamePatcher.getHdrFileName()));
        targetDblFile.renameTo(new File(targetDir, fileNamePatcher.getDblFileName()));

    }

    private FileNamePatcher createFileNamePatcher() {
        final String filenameWOExtension = FileUtils.getFilenameWithoutExtension(targetDblFile);
        final FileNamePatcher fileNamePatcher = new FileNamePatcher(filenameWOExtension);
        if (targetGridPointHandler.hasValidPeriod()) {
            fileNamePatcher.setStartDate(targetGridPointHandler.getSensingStart());
            fileNamePatcher.setStopDate(targetGridPointHandler.getSensingStop());
        }
        return fileNamePatcher;
    }

    private File getSourceHdrFile(File sourceDblFile) {
        return FileUtils.exchangeExtension(sourceDblFile, ".HDR");
    }

    private File getTargetDblFile(File sourceDblFile) {
        return new File(targetDirectory, sourceDblFile.getName());
    }

    private File getTargetHdrFile(File sourceHdrFile) {
        return new File(targetDirectory, sourceHdrFile.getName());
    }
}