package org.esa.beam.dataio.smos.bufr;

import java.util.ArrayList;

class SnapshotObservation {

    SnapshotObservation(int[] data) {
        this.data = data;
    }

    int[] data;

    // data[0] "Number_of_grid_points", unscaled, signed short
    // data[1] "Year", unscaled, signed short
    // data[2] "Month", unscaled, byte
    // data[3] "Day", unscaled, byte
    // data[4] "Hour", unscaled, byte
    // data[5] "Minute", unscaled, byte
    // data[6] "Second", unscaled, byte
    // data[7] "Total_electron_count_per_square_metre", *scaled*, byte
    // data[8] "Direct_sun_brightness_temperature", unscaled, int
    // data[9] "Snapshot_accuracy",  *scaled*, signed short
    // data[10] "Radiometric_accuracy_pure_polarisation", *scaled*, signed short
    // data[11] "Radiometric_accuracy_cross_polarisation", *scaled*, signed short
    // data[12] "Snapshot_overall_quality", enum ...

    ArrayList<Observation> observations;
}
