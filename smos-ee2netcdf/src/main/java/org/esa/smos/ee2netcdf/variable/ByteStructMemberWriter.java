package org.esa.smos.ee2netcdf.variable;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.snap.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;

import java.io.IOException;

class ByteStructMemberWriter extends AbstractVariableWriter {

    private final int memberIndex;

    ByteStructMemberWriter(NVariable variable, int memberIndex, int arraySize, byte fillValue) {
        this.memberIndex = memberIndex;
        final byte[] byteVector = VariableHelper.getByteVector(arraySize, fillValue);
        array = Array.factory(byteVector);
        this.variable = variable;
    }

    @Override
    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final byte gpByte = gridPointData.getByte(memberIndex);
        array.setByte(index, gpByte);
    }
}
