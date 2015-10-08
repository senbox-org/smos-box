package org.esa.smos.ee2netcdf.reader;

import org.junit.Before;
import org.junit.Test;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProductTypeSupportFactoryTest {

    private NetcdfFile netcdfFile;

    @Before
    public void setUp() {
        netcdfFile = mock(NetcdfFile.class);
    }

    @Test
    public void testL2TypeSupport() throws IOException {
        assertL2TypeReturned("MIR_SMUDP2");
        assertL2TypeReturned("MIR_OSUDP2");
    }

    @Test
    public void testBrowseTypeSupport() throws IOException {
        assertBrowseTypeReturned("MIR_BWLD1C");
        assertBrowseTypeReturned("MIR_BWLF1C");
        assertBrowseTypeReturned("MIR_BWND1C");
        assertBrowseTypeReturned("MIR_BWNF1C");
        assertBrowseTypeReturned("MIR_BWSD1C");
        assertBrowseTypeReturned("MIR_BWSF1C");
    }

    @Test
    public void testScienceTypeSupport() throws IOException {
        assertScienceTypeReturned("MIR_SCLD1C");
        assertScienceTypeReturned("MIR_SCLF1C");
        assertScienceTypeReturned("MIR_SCND1C");
        assertScienceTypeReturned("MIR_SCNF1C");
        assertScienceTypeReturned("MIR_SCSD1C");
        assertScienceTypeReturned("MIR_SCSF1C");
    }

    @Test
    public void testGet_invalidInput() throws IOException {
        try {
            ProductTypeSupportFactory.get("Schnickes", netcdfFile);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        try {
            ProductTypeSupportFactory.get("", netcdfFile);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        try {
            ProductTypeSupportFactory.get(null, netcdfFile);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    private void assertBrowseTypeReturned(String typeString) throws IOException {
        final ProductTypeSupport support = ProductTypeSupportFactory.get(typeString, netcdfFile);
        assertNotNull(support);
        assertTrue(support instanceof BrowseProductSupport);
    }

    private void assertL2TypeReturned(String typeString) throws IOException {
        final ProductTypeSupport support = ProductTypeSupportFactory.get(typeString, netcdfFile);
        assertNotNull(support);
        assertTrue(support instanceof L2ProductSupport);
    }

    private void assertScienceTypeReturned(String typeString) throws IOException {
        final ProductTypeSupport support = ProductTypeSupportFactory.get(typeString, netcdfFile);
        assertNotNull(support);
        assertTrue(support instanceof ScienceProductSupport);
    }
}
