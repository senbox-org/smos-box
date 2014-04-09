package org.esa.beam.smos.ee2netcdf;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;
import ucar.ma2.Index;

import java.io.IOException;

class ShortVariableSequence2DWriter implements VariableWriter {

    private final Array array;
    private final NVariable variable;
    private final int memberIndex;
    private int height;

    ShortVariableSequence2DWriter(NVariable variable, int width, int height, int memberIndex) {
        this.variable = variable;
        this.memberIndex = memberIndex;
        this.height = height;
        array = Array.factory(new short[width][height]);
    }

    @Override
    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final Index arrayIndex = array.getIndex();
        for (int i = 0; i < height; i++) {
            final short data = btDataList.getCompound(i).getShort(memberIndex);
            arrayIndex.set(index, i);
            array.setShort(arrayIndex, data);
        }
    }

    @Override
    public void close() throws IOException {
        variable.writeFully(array);
    }
}
