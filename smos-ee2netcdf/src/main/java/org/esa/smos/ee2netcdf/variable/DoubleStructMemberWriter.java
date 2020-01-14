package org.esa.smos.ee2netcdf.variable;


import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.snap.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.io.IOException;

class DoubleStructMemberWriter extends AbstractVariableWriter {

    private final int memberIndex;

    DoubleStructMemberWriter(NVariable variable, int memberIndex, int arraySize, double fillValue) {
        this.memberIndex = memberIndex;
        final double[] doubleVector = VariableHelper.getDoubleVector(arraySize, fillValue);
        array = Array.factory(DataType.DOUBLE, new int[]{doubleVector.length}, doubleVector);
        this.variable = variable;
    }

    @Override
    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final double gpDouble = gridPointData.getDouble(memberIndex);
        array.setDouble(index, gpDouble);
    }
}
