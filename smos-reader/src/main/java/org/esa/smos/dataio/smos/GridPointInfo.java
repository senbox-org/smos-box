package org.esa.smos.dataio.smos;

import java.util.Arrays;

public class GridPointInfo {

    private final int minSeqnum;
    private final int maxSeqnum;
    private final int[] indexes;

    public GridPointInfo(int minSeqnum, int maxSeqnum) {
        this.minSeqnum = minSeqnum;
        this.maxSeqnum = maxSeqnum;
        indexes = new int[maxSeqnum - minSeqnum + 1];
        Arrays.fill(indexes, -1);
    }

    public void setSequenceNumbers(int[] sequenceNumbers) {
        for (int i = 0; i < sequenceNumbers.length; i++) {
            indexes[sequenceNumbers[i] - minSeqnum] = i;
        }
    }

    public int getGridPointIndex(int seqnum) {
        if (seqnum < minSeqnum || seqnum > maxSeqnum) {
            return -1;
        }

        return indexes[seqnum - minSeqnum];
    }
}
