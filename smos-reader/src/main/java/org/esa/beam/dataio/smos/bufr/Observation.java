package org.esa.beam.dataio.smos.bufr;


class Observation {

    Observation() {
        data = new int[12];
    }

    float lon;
    float lat;
    int[] data;

    // 0: bt_real
    // 1: bt_imag
    // 2: pixel_rad_acc;
    // 3: incidence_angle;
    // 4: azimuth_angle;
    // 5: faraday_rot_angle;
    // 6: geometric_rot_angle;
    // 7: footprint_axis_1;
    // 8: footprint_axis_2;
    // 9: water_fraction;
    // 10: smos_info_flag;
    // 11: polarisation;
}
