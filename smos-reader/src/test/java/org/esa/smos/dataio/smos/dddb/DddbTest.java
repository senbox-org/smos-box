/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.smos.dataio.smos.dddb;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DddbTest {

    private static final String DBL_SM_XXXX_AUX_ECMWF__0200 = "DBL_SM_XXXX_AUX_ECMWF__0200";
    private static final String DBL_SM_XXXX_MIR_SMDAP2_0200 = "DBL_SM_XXXX_MIR_SMDAP2_0200";
    private static final String DBL_SM_XXXX_MIR_SMDAP2_0201 = "DBL_SM_XXXX_MIR_SMDAP2_0201";
    private static final String DBL_SM_XXXX_MIR_SMDAP2_0300 = "DBL_SM_XXXX_MIR_SMDAP2_0300";
    private static final String DBL_SM_XXXX_MIR_OSDAP2_0200 = "DBL_SM_XXXX_MIR_OSDAP2_0200";
    private static final String DBL_SM_XXXX_MIR_OSDAP2_0300 = "DBL_SM_XXXX_MIR_OSDAP2_0300";
    private static final String DBL_SM_XXXX_MIR_OSUDP2_0200 = "DBL_SM_XXXX_MIR_OSUDP2_0200";
    private static final String DBL_SM_XXXX_MIR_OSUDP2_0300 = "DBL_SM_XXXX_MIR_OSUDP2_0300";
    private static final String DBL_SM_XXXX_MIR_SMUDP2_0200 = "DBL_SM_XXXX_MIR_SMUDP2_0200";
    private static final String DBL_SM_XXXX_AUX_DGGROU_0400 = "DBL_SM_XXXX_AUX_DGGROU_0400";
    private static final String DBL_SM_XXXX_AUX_DGGTFO_0300 = "DBL_SM_XXXX_AUX_DGGTFO_0300";
    private static final String DBL_SM_XXXX_MIR_BWLF1C_0200 = "DBL_SM_XXXX_MIR_BWLF1C_0200";
    private static final String DBL_SM_XXXX_MIR_BWND1C_0200 = "DBL_SM_XXXX_MIR_BWND1C_0200";
    private static final String DBL_SM_XXXX_MIR_BWNF1C_0200 = "DBL_SM_XXXX_MIR_BWNF1C_0200";
    private static final String DBL_SM_XXXX_AUX_DGGTLV_0300 = "DBL_SM_XXXX_AUX_DGGTLV_0300";
    private static final String DBL_SM_XXXX_MIR_BWLD1C_0400 = "DBL_SM_XXXX_MIR_BWLD1C_0400";
    private static final String DBL_SM_XXXX_MIR_BWSD1C_0200 = "DBL_SM_XXXX_MIR_BWND1C_0200";
    private Dddb dddb;

    @Before
    public void setUp() {
        dddb = Dddb.getInstance();
    }

    @Test
    public void testGetBandDescriptors_ECMWF() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_AUX_ECMWF__0200);
        assertEquals(57, descriptors.asList().size());

        final BandDescriptor descriptor = descriptors.getMember("RR");
        assertNotNull(descriptor);

        assertEquals("RR", descriptor.getBandName());
        assertEquals("Rain_Rate", descriptor.getMemberName());
        assertTrue(descriptor.hasTypicalMin());
        assertTrue(descriptor.hasTypicalMax());
        assertFalse(descriptor.isCyclic());
        assertTrue(descriptor.hasFillValue());
        assertFalse(descriptor.getValidPixelExpression().isEmpty());
        assertEquals("RR.raw != -99999.0 && RR.raw != -99998.0", descriptor.getValidPixelExpression());
        assertFalse(descriptor.getUnit().isEmpty());
        assertFalse(descriptor.getDescription().isEmpty());
        assertEquals(0.0, descriptor.getTypicalMin(), 0.0);
        assertEquals("m 3h-1", descriptor.getUnit());
    }

    @Test
    public void testGetBandDescriptors_BUFR() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors("BUFR");
        assertEquals(27, descriptors.asList().size());

        final BandDescriptor flagsBandDescriptor = descriptors.getMember("Flags");
        assertNotNull(flagsBandDescriptor);
        assertEquals("Flags", flagsBandDescriptor.getFlagCodingName());
        assertNotNull("Flags", flagsBandDescriptor.getFlagDescriptors());
    }

    @Test
    public void testGetFlagDescriptors_BUFR() {
        final Family<FlagDescriptor> descriptors = dddb.getFlagDescriptors("BUFR_flags");
        assertEquals(14, descriptors.asList().size());
    }

    @Test
    public void testGetFlagDescriptors() {
        final Family<FlagDescriptor> descriptors = dddb.getFlagDescriptors(DBL_SM_XXXX_AUX_ECMWF__0200 + "_flags1");
        assertEquals(21, descriptors.asList().size());

        FlagDescriptor descriptor;
        descriptor = descriptors.getMember("RR_FLAG");
        assertNotNull(descriptor);

        assertEquals("RR_FLAG", descriptor.getFlagName());
        assertEquals(0x00000080, descriptor.getMask());
        assertNull(descriptor.getColor());
        assertEquals("", descriptor.getCombinedDescriptor());
        assertEquals("Quality flag: rain rate", descriptor.getDescription());
    }

    @Test
    public void testGetFlagDescriptorsFromBandDescriptor() {
        final Family<BandDescriptor> bandDescriptor = dddb.getBandDescriptors(DBL_SM_XXXX_AUX_ECMWF__0200);
        final Family<FlagDescriptor> flagDescriptors = bandDescriptor.getMember("F1").getFlagDescriptors();
        assertNotNull(flagDescriptors);

        assertNotNull(dddb.getFlagDescriptors(DBL_SM_XXXX_AUX_ECMWF__0200 + "_flags1"));
        assertSame(flagDescriptors, dddb.getFlagDescriptors(DBL_SM_XXXX_AUX_ECMWF__0200 + "_flags1"));
    }

    @Test
    public void testFindBandDescriptorForMember() {
        final BandDescriptor descriptor = dddb.findBandDescriptorForMember(DBL_SM_XXXX_MIR_OSUDP2_0300, "Sigma_Tb_42.5Y");
        assertNotNull(descriptor);
        assertEquals("Sigma_TBY", descriptor.getBandName());
    }

    @Test
    public void testFindBandDescriptorForMember_unknownFormatName() {
        assertNull(dddb.findBandDescriptorForMember("schnick-schnack-for-mat", "Sigma_Tb_42.5Y"));
    }

    @Test
    public void testFindBandDescriptorForMember_unknownBandName() {
        assertNull(dddb.findBandDescriptorForMember(DBL_SM_XXXX_MIR_OSUDP2_0300, "rubber-band"));
    }

    @Test
    public void testGetSMDAP2_v0200Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_SMDAP2_0200);
        assertEquals(70, descriptors.asList().size());

        final BandDescriptor x_swath = descriptors.getMember("X_Swath");
        assertNotNull(x_swath);
    }

    @Test
    public void testGetSMDAP2_v0201Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_SMDAP2_0201);
        assertEquals(70, descriptors.asList().size());

        final BandDescriptor tSurf_init_std = descriptors.getMember("TSurf_Init_Std");
        assertNotNull(tSurf_init_std);
    }

    @Test
    public void testGetSMDAP2_v0300Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_SMDAP2_0300);
        assertEquals(70, descriptors.asList().size());

        final BandDescriptor hr_in_dqx = descriptors.getMember("HR_IN_DQX");
        assertNotNull(hr_in_dqx);
    }

    @Test
    public void testGetOSDAP2_v0200Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_OSDAP2_0200);
        assertEquals(133, descriptors.asList().size());

        final BandDescriptor param2_sigma_m3 = descriptors.getMember("Param2_sigma_M3");
        assertNotNull(param2_sigma_m3);

        final BandDescriptor param6_sigma_m2 = descriptors.getMember("Param6_sigma_M2");
        assertNotNull(param6_sigma_m2);
    }

    @Test
    public void testGetOSDAP2_v0300Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_OSDAP2_0300);
        assertEquals(133, descriptors.asList().size());

        final BandDescriptor out_of_lut_flags_4 = descriptors.getMember("Out_of_LUT_flags_4");
        assertNotNull(out_of_lut_flags_4);

        final BandDescriptor diff_tb_1 = descriptors.getMember("Diff_TB_1");
        assertNotNull(diff_tb_1);
    }

    @Test
    public void testGetOSUDP2_v0200Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_OSUDP2_0200);
        assertEquals(64, descriptors.asList().size());

        final BandDescriptor dg_num_meas_l1c = descriptors.getMember("Dg_num_meas_L1c");
        assertNotNull(dg_num_meas_l1c);

        final BandDescriptor a_card = descriptors.getMember("Acard");
        assertNotNull(a_card);
    }

    @Test
    public void testGetSMUPD2_v0200Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_SMUDP2_0200);
        assertEquals(66, descriptors.asList().size());

        final BandDescriptor n_instrument_error = descriptors.getMember("N_Instrument_Error");
        assertNotNull(n_instrument_error);
    }

    @Test
    public void testGetMemberDescriptors_BWLF1C() throws IOException {
        final File hdrFile = TestHelper.getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.HDR");

        final Family<MemberDescriptor> memberDescriptors = dddb.getMemberDescriptors(hdrFile);
        assertNotNull(memberDescriptors);
        assertEquals(12, memberDescriptors.asList().size());

        final MemberDescriptor flagsDescriptor = memberDescriptors.getMember("Flags");
        assertNotNull(flagsDescriptor);
        assertTrue(flagsDescriptor.isGridPointData());
        assertEquals("ushort", flagsDescriptor.getDataTypeName());
        assertEquals("n_grid_points n_bt_data", flagsDescriptor.getDimensionNames());
        assertEquals(0, flagsDescriptor.getMemberIndex());
        final short[] flagMasks = flagsDescriptor.getFlagMasks();
        assertNotNull(flagMasks);
        assertEquals(16, flagMasks.length);
        final short[] flagValues = flagsDescriptor.getFlagValues();
        assertNotNull(flagValues);
        assertEquals(16, flagValues.length);
        assertEquals("POL_FLAG_1 POL_FLAG_2 SUN_FOV SUN_GLINT_FOV MOON_GLINT_FOV SINGLE_SNAPSHOT FTT SUN_POINT SUN_GLINT_AREA MOON_POINT AF_FOV EAF_FOV BORDER_FOV SUN_TAILS RFI_1 RFI_2", flagsDescriptor.getFlagMeanings());
        assertEquals("", flagsDescriptor.getUnit());
        assertEquals(0.0f, flagsDescriptor.getFillValue(), 1e-8);
        assertEquals(1.0f, flagsDescriptor.getScalingFactor(), 1e-8);
        assertEquals(0.0f, flagsDescriptor.getScalingOffset(), 1e-8);

        final MemberDescriptor btValueDescriptor = memberDescriptors.getMember("BT_Value");
        assertNotNull(btValueDescriptor);
        assertTrue(btValueDescriptor.isGridPointData());
        assertEquals("float", btValueDescriptor.getDataTypeName());
        assertEquals("n_grid_points n_bt_data", btValueDescriptor.getDimensionNames());
        assertEquals(1, btValueDescriptor.getMemberIndex());
        assertNull(btValueDescriptor.getFlagMasks());
        assertEquals("K", btValueDescriptor.getUnit());
        assertEquals(-999.0f, btValueDescriptor.getFillValue(), 1e-8);
        assertEquals(1.0f, btValueDescriptor.getScalingFactor(), 1e-8);
        assertEquals(0.0f, btValueDescriptor.getScalingOffset(), 1e-8);

        final MemberDescriptor radiometricAccuracyOfPixelDescriptor = memberDescriptors.getMember("Radiometric_Accuracy_of_Pixel");
        assertNotNull(radiometricAccuracyOfPixelDescriptor);
        assertTrue(radiometricAccuracyOfPixelDescriptor.isGridPointData());
        assertEquals("ushort", radiometricAccuracyOfPixelDescriptor.getDataTypeName());
        assertEquals("n_grid_points n_bt_data", radiometricAccuracyOfPixelDescriptor.getDimensionNames());
        assertEquals(2, radiometricAccuracyOfPixelDescriptor.getMemberIndex());
        assertNull(radiometricAccuracyOfPixelDescriptor.getFlagValues());
        assertEquals("K", radiometricAccuracyOfPixelDescriptor.getUnit());
        assertEquals(0.0f, radiometricAccuracyOfPixelDescriptor.getFillValue(), 1e-8);
        assertEquals(1.52587890625E-5f, radiometricAccuracyOfPixelDescriptor.getScalingFactor(), 1e-8);
        assertEquals(0.0f, radiometricAccuracyOfPixelDescriptor.getScalingOffset(), 1e-8);

        final MemberDescriptor azimuthAngleDescriptor = memberDescriptors.getMember("Azimuth_Angle");
        assertNotNull(azimuthAngleDescriptor);
        assertTrue(azimuthAngleDescriptor.isGridPointData());
        assertEquals("ushort", azimuthAngleDescriptor.getDataTypeName());
        assertEquals("n_grid_points n_bt_data", azimuthAngleDescriptor.getDimensionNames());
        assertEquals(3, azimuthAngleDescriptor.getMemberIndex());
        assertEquals("deg", azimuthAngleDescriptor.getUnit());
        assertEquals(0.0f, azimuthAngleDescriptor.getFillValue(), 1e-8);
        assertEquals(0.0054931640625f, azimuthAngleDescriptor.getScalingFactor(), 1e-8);
        assertEquals(0.0f, azimuthAngleDescriptor.getScalingOffset(), 1e-8);

        final MemberDescriptor footprintAxis1Descriptor = memberDescriptors.getMember("Footprint_Axis1");
        assertNotNull(footprintAxis1Descriptor);
        assertTrue(footprintAxis1Descriptor.isGridPointData());
        assertEquals("ushort", footprintAxis1Descriptor.getDataTypeName());
        assertEquals("n_grid_points n_bt_data", footprintAxis1Descriptor.getDimensionNames());
        assertEquals(4, footprintAxis1Descriptor.getMemberIndex());
        assertEquals("km", footprintAxis1Descriptor.getUnit());
        assertEquals(0.0f, footprintAxis1Descriptor.getFillValue(), 1e-8);
        assertEquals(1.52587890625E-5f, footprintAxis1Descriptor.getScalingFactor(), 1e-8);
        assertEquals(0.0f, footprintAxis1Descriptor.getScalingOffset(), 1e-8);

        final MemberDescriptor footprintAxis2Descriptor = memberDescriptors.getMember("Footprint_Axis2");
        assertNotNull(footprintAxis2Descriptor);
        assertTrue(footprintAxis2Descriptor.isGridPointData());
        assertEquals("ushort", footprintAxis2Descriptor.getDataTypeName());
        assertEquals("n_grid_points n_bt_data", footprintAxis2Descriptor.getDimensionNames());
        assertEquals(5, footprintAxis2Descriptor.getMemberIndex());
        assertEquals("km", footprintAxis2Descriptor.getUnit());
        assertEquals(0.0f, footprintAxis2Descriptor.getFillValue(), 1e-8);
        assertEquals(1.52587890625E-5f, footprintAxis2Descriptor.getScalingFactor(), 1e-8);
        assertEquals(0.0f, footprintAxis2Descriptor.getScalingOffset(), 1e-8);

        final MemberDescriptor gridPointIdDescriptor = memberDescriptors.getMember("Grid_Point_ID");
        assertNotNull(gridPointIdDescriptor);
        assertTrue(gridPointIdDescriptor.isGridPointData());
        assertEquals("uint", gridPointIdDescriptor.getDataTypeName());
        assertEquals("n_grid_points", gridPointIdDescriptor.getDimensionNames());
        assertEquals(0, gridPointIdDescriptor.getMemberIndex());
        assertEquals("", gridPointIdDescriptor.getUnit());
        assertEquals(Float.NaN, gridPointIdDescriptor.getFillValue(), 1e-8);
        assertEquals(1.0f, gridPointIdDescriptor.getScalingFactor(), 1e-8);
        assertEquals(0.0f, gridPointIdDescriptor.getScalingOffset(), 1e-8);


        final MemberDescriptor latitudeDescriptor = memberDescriptors.getMember("Grid_Point_Latitude");
        assertNotNull(latitudeDescriptor);
        assertEquals("Latitude", latitudeDescriptor.getBinXName());
        assertTrue(latitudeDescriptor.isGridPointData());
        assertEquals("float", latitudeDescriptor.getDataTypeName());
        assertEquals("n_grid_points", latitudeDescriptor.getDimensionNames());
        assertEquals(1, latitudeDescriptor.getMemberIndex());
        assertEquals("deg", latitudeDescriptor.getUnit());
        assertEquals(Float.NaN, latitudeDescriptor.getFillValue(), 1e-8);
        assertEquals(1.0f, latitudeDescriptor.getScalingFactor(), 1e-8);
        assertEquals(0.0f, latitudeDescriptor.getScalingOffset(), 1e-8);

        final MemberDescriptor longitudeDescriptor = memberDescriptors.getMember("Grid_Point_Longitude");
        assertNotNull(longitudeDescriptor);
        assertEquals("Longitude", longitudeDescriptor.getBinXName());
        assertTrue(longitudeDescriptor.isGridPointData());
        assertEquals("float", longitudeDescriptor.getDataTypeName());
        assertEquals("n_grid_points", longitudeDescriptor.getDimensionNames());
        assertEquals(2, longitudeDescriptor.getMemberIndex());
        assertEquals("deg", longitudeDescriptor.getUnit());
        assertEquals(Float.NaN, longitudeDescriptor.getFillValue(), 1e-8);
        assertEquals(1.0f, longitudeDescriptor.getScalingFactor(), 1e-8);
        assertEquals(0.0f, longitudeDescriptor.getScalingOffset(), 1e-8);

        final MemberDescriptor altitudeDescriptor = memberDescriptors.getMember("Grid_Point_Altitude");
        assertNotNull(altitudeDescriptor);
        assertEquals("Altitude", altitudeDescriptor.getBinXName());
        assertTrue(altitudeDescriptor.isGridPointData());
        assertEquals("float", altitudeDescriptor.getDataTypeName());
        assertEquals("n_grid_points", altitudeDescriptor.getDimensionNames());
        assertEquals(3, altitudeDescriptor.getMemberIndex());
        assertEquals("m", altitudeDescriptor.getUnit());
        assertEquals(Float.NaN, altitudeDescriptor.getFillValue(), 1e-8);
        assertEquals(1.0f, altitudeDescriptor.getScalingFactor(), 1e-8);
        assertEquals(0.0f, altitudeDescriptor.getScalingOffset(), 1e-8);

        final MemberDescriptor maskDescriptor = memberDescriptors.getMember("Grid_Point_Mask");
        assertNotNull(maskDescriptor);
        assertTrue(maskDescriptor.isGridPointData());
        assertEquals("ubyte", maskDescriptor.getDataTypeName());
        assertEquals("n_grid_points", maskDescriptor.getDimensionNames());
        assertEquals(4, maskDescriptor.getMemberIndex());
        assertEquals("", maskDescriptor.getUnit());
        assertEquals(Float.NaN, maskDescriptor.getFillValue(), 1e-8);
        assertEquals(1.0f, maskDescriptor.getScalingFactor(), 1e-8);
        assertEquals(0.0f, maskDescriptor.getScalingOffset(), 1e-8);

        final MemberDescriptor btCountDescriptor = memberDescriptors.getMember("BT_Data_Counter");
        assertNotNull(btCountDescriptor);
        assertTrue(btCountDescriptor.isGridPointData());
        assertEquals("ubyte", btCountDescriptor.getDataTypeName());
        assertEquals("n_grid_points", btCountDescriptor.getDimensionNames());
        assertEquals(5, btCountDescriptor.getMemberIndex());
        assertEquals("", btCountDescriptor.getUnit());
        assertEquals(Float.NaN, btCountDescriptor.getFillValue(), 1e-8);
        assertEquals(1.0f, btCountDescriptor.getScalingFactor(), 1e-8);
        assertEquals(0.0f, btCountDescriptor.getScalingOffset(), 1e-8);
    }

    @Test
    public void testGetDGGROU_v0400Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_AUX_DGGROU_0400);
        assertEquals(11, descriptors.asList().size());

        final BandDescriptor dt_branch_hr_asc = descriptors.getMember("DT_Branch_HR_Asc");
        assertNotNull(dt_branch_hr_asc);
        assertEquals("DT_Branch_HR_Asc", dt_branch_hr_asc.getMemberName());

        final BandDescriptor hr_asc = descriptors.getMember("HR_Asc");
        assertNotNull(hr_asc);
        assertEquals("HR_DQX_Asc", hr_asc.getAncilliaryBandName());
        assertEquals(-1, hr_asc.getPolarization());
    }

    @Test
    public void testGetDGGTFO_v0300Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_AUX_DGGTFO_0300);
        assertEquals(7, descriptors.asList().size());

        final BandDescriptor date_stamp_fo = descriptors.getMember("Date_Stamp_FO");
        assertNotNull(date_stamp_fo);
        assertEquals(0, date_stamp_fo.getSampleModel());

        final BandDescriptor tau_nad_fo = descriptors.getMember("Tau_Nad_FO");
        assertNotNull(tau_nad_fo);
        assertEquals("Tau_Nad_FO_DQX", tau_nad_fo.getAncilliaryBandName());
        assertEquals(0.0, tau_nad_fo.getScalingOffset(), 1e-8);
    }

    @Test
    public void testGetBWLF1C_v0200Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_BWLF1C_0200);
        assertEquals(25, descriptors.asList().size());

        final BandDescriptor footprint_axis2_xy = descriptors.getMember("Footprint_Axis2_XY");
        assertNotNull(footprint_axis2_xy);

        final BandDescriptor bt_value_xy_real = descriptors.getMember("BT_Value_XY_Real");
        assertNotNull(bt_value_xy_real);
        assertEquals("Pixel_Radiometric_Accuracy_XY", bt_value_xy_real.getAncilliaryBandName());
    }

    @Test
    public void testGetBWND1C_v0200Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_BWND1C_0200);
        assertEquals(18, descriptors.asList().size());

        final BandDescriptor flags_x = descriptors.getMember("Flags_X");
        assertNotNull(flags_x);
        assertEquals(1.0, flags_x.getScalingFactor(), 1e-8);

        final BandDescriptor bt_value_y = descriptors.getMember("BT_Value_Y");
        assertNotNull(bt_value_y);
        assertEquals("Pixel_Radiometric_Accuracy_Y", bt_value_y.getAncilliaryBandName());
        assertEquals(0.0, bt_value_y.getTypicalMin(), 1e-8);
    }

    @Test
    public void testGetBWNF1C_v0200Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_BWNF1C_0200);
        assertEquals(25, descriptors.asList().size());

        final BandDescriptor flags_xy = descriptors.getMember("Flags_XY");
        assertNotNull(flags_xy);

        final BandDescriptor bt_value_xy_real = descriptors.getMember("BT_Value_XY_Real");
        assertNotNull(bt_value_xy_real);
        assertEquals("Pixel_Radiometric_Accuracy_XY", bt_value_xy_real.getAncilliaryBandName());
    }

    @Test
    public void testGetDGGTLV_v0300Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_AUX_DGGTLV_0300);
        assertEquals(7, descriptors.asList().size());

        final BandDescriptor dt_branch_lv = descriptors.getMember("DT_Branch_LV");
        assertNotNull(dt_branch_lv);

        final BandDescriptor tau_nad_lv = descriptors.getMember("Tau_Nad_LV");
        assertNotNull(tau_nad_lv);
        assertEquals("Tau_Nad_LV_DQX", tau_nad_lv.getAncilliaryBandName());
    }

    @Test
    public void testGetBWLD1C_v0400Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_BWLD1C_0400);
        assertEquals(18, descriptors.asList().size());

        final BandDescriptor footprint_axis2_y = descriptors.getMember("Footprint_Axis2_Y");
        assertNotNull(footprint_axis2_y);

        final BandDescriptor bt_value_y = descriptors.getMember("BT_Value_Y");
        assertNotNull(bt_value_y);
        assertEquals("Pixel_Radiometric_Accuracy_Y", bt_value_y.getAncilliaryBandName());
    }

    @Test
    public void testGetBWSD1C_v0200Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_BWSD1C_0200);
        assertEquals(18, descriptors.asList().size());

        final BandDescriptor flags_x = descriptors.getMember("Flags_X");
        assertNotNull(flags_x);

        final BandDescriptor bt_value_x = descriptors.getMember("BT_Value_X");
        assertNotNull(bt_value_x);
        assertEquals("Pixel_Radiometric_Accuracy_X", bt_value_x.getAncilliaryBandName());
    }

    @Test
    public void testGetOriginalName() {
        final Properties properties = new Properties();
        properties.setProperty("a_key", "a_value");
        properties.setProperty("another_key", "another_value");

        final String key = Dddb.findOriginalName(properties, "another_value");
        assertEquals("another_key", key);
    }

    @Test
    public void testGetOriginalName_nameNotInProperties() {
        final Properties properties = new Properties();
        properties.setProperty("a_key", "a_value");
        properties.setProperty("another_key", "another_value");

        final String key = Dddb.findOriginalName(properties, "the Werner Value");
        assertNull(key);
    }

    @Test
    public void testExtractUniqueMemberNames() {
        final ArrayList<BandDescriptor> descriptors = new ArrayList<>();
        descriptors.add(new TestBandDescriptor("name"));
        descriptors.add(new TestBandDescriptor("name_2"));
        descriptors.add(new TestBandDescriptor("name_3"));
        descriptors.add(new TestBandDescriptor("name_4"));
        descriptors.add(new TestBandDescriptor("name_2"));
        descriptors.add(new TestBandDescriptor("name"));

        final Map<String, BandDescriptor> descriptorMap = Dddb.extractUniqueMembers(descriptors);
        assertEquals(4, descriptorMap.size());
        assertTrue(descriptorMap.containsKey("name"));
        assertTrue(descriptorMap.containsKey("name_2"));
        assertTrue(descriptorMap.containsKey("name_3"));
        assertTrue(descriptorMap.containsKey("name_4"));
    }

    @Test
    public void testGetEEVariableName() throws IOException {
        assertEquals("Footprint_Axis2", dddb.getEEVariableName("Footprint_Axis2", DBL_SM_XXXX_MIR_BWSD1C_0200));
        assertEquals("Radiometric_Accuracy_of_Pixel", dddb.getEEVariableName("Pixel_Radiometric_Accuracy", DBL_SM_XXXX_MIR_BWSD1C_0200));

        assertEquals("schneckBand", dddb.getEEVariableName("schneckBand", "invalid_schema"));
    }

    @Test
    public void testGetComplexFlagDescriptors() {
        final Family<FlagDescriptor> descriptorFamily = dddb.getCombinedFlagDescriptors("DBL_SM_XXXX_MIR_XXXF1C_0401_RFI");
        final List<FlagDescriptor> descriptors = descriptorFamily.asList();
        assertEquals(4, descriptors.size());

        FlagDescriptor descriptor = descriptors.get(1);
        assertEquals("RFI_LOW", descriptor.getFlagName());
        assertEquals(0X0000C000, descriptor.getMask());

        descriptor = descriptors.get(3);
        assertEquals("RFI_HIGH", descriptor.getFlagName());
        assertEquals(0X0000C000, descriptor.getMask());
    }

    private class TestBandDescriptor implements BandDescriptor {

        private final String memberName;

        private TestBandDescriptor(String memberName) {
            this.memberName = memberName;
        }

        @Override
        public String getBandName() {
            return null;
        }

        @Override
        public String getMemberName() {
            return memberName;
        }

        @Override
        public int getPolarization() {
            return 0;
        }

        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public int getSampleModel() {
            return 0;
        }

        @Override
        public double getScalingOffset() {
            return 0;
        }

        @Override
        public double getScalingFactor() {
            return 0;
        }

        @Override
        public boolean hasTypicalMin() {
            return false;
        }

        @Override
        public boolean hasTypicalMax() {
            return false;
        }

        @Override
        public boolean hasFillValue() {
            return false;
        }

        @Override
        public double getTypicalMin() {
            return 0;
        }

        @Override
        public double getTypicalMax() {
            return 0;
        }

        @Override
        public double getFillValue() {
            return 0;
        }

        @Override
        public String getValidPixelExpression() {
            return null;
        }

        @Override
        public String getUnit() {
            return null;
        }

        @Override
        public boolean isCyclic() {
            return false;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String getFlagCodingName() {
            return null;
        }

        @Override
        public Family<FlagDescriptor> getFlagDescriptors() {
            return null;
        }

        @Override
        public String getAncilliaryBandName() {
            return null;
        }

        @Override
        public boolean isGridPointData() {
            return false;
        }

        @Override
        public String getDimensionNames() {
            return null;
        }
    }
}
