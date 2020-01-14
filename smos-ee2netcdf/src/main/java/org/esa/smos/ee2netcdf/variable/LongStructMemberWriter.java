package org.esa.smos.ee2netcdf.variable;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.snap.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.io.IOException;

class LongStructMemberWriter extends AbstractVariableWriter {

    private final int memberIndex;

    LongStructMemberWriter(NVariable variable, int memberIndex, int arraySize, long fillValue) {
        this.memberIndex = memberIndex;
        final long[] longVector = VariableHelper.getLongVector(arraySize, fillValue);
        array = Array.factory(DataType.LONG, new int[]{longVector.length}, longVector);
        this.variable = variable;
    }

    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final long gpLong = gridPointData.getLong(memberIndex);
        array.setLong(index, gpLong);
    }
}
