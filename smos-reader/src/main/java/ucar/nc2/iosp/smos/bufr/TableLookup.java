/*
 * Copyright 1998-2009 University Corporation for Atmospheric Research/Unidata
 *
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation.  Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package ucar.nc2.iosp.smos.bufr;

import net.jcip.annotations.Immutable;
import ucar.nc2.iosp.smos.bufr.tables.BufrTables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsolates lookup into the BUFR Tables.
 *
 * @author caron
 * @since Jul 14, 2008
 */
@Immutable
public class TableLookup {
  static private org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TableLookup.class);
  static private final boolean showErrors = true;

  /////////////////////////////////////////
  private final ucar.nc2.iosp.smos.bufr.tables.TableB localTableB;
  private final ucar.nc2.iosp.smos.bufr.tables.TableD localTableD;

  private final ucar.nc2.iosp.smos.bufr.tables.TableB wmoTableB;
  private final ucar.nc2.iosp.smos.bufr.tables.TableD wmoTableD;
  private final ucar.nc2.iosp.smos.bufr.tables.BufrTables.Mode mode;

  public TableLookup(ucar.nc2.iosp.smos.bufr.BufrIdentificationSection ids) throws IOException {
    this.wmoTableB = ucar.nc2.iosp.smos.bufr.tables.BufrTables.getWmoTableB(ids);
    this.wmoTableD = ucar.nc2.iosp.smos.bufr.tables.BufrTables.getWmoTableD(ids);

    ucar.nc2.iosp.smos.bufr.tables.BufrTables.Tables tables = ucar.nc2.iosp.smos.bufr.tables.BufrTables.getLocalTables(ids);
    if (tables != null) {
      this.localTableB = tables.b;
      this.localTableD = tables.d;
      this.mode = (tables.mode == null) ? ucar.nc2.iosp.smos.bufr.tables.BufrTables.Mode.localOverride : tables.mode;
    } else {
      this.localTableB = null;
      this.localTableD = null;
      this.mode = ucar.nc2.iosp.smos.bufr.tables.BufrTables.Mode.localOverride;
    }
  }

  public TableLookup(BufrIdentificationSection ids, ucar.nc2.iosp.smos.bufr.tables.TableB b, ucar.nc2.iosp.smos.bufr.tables.TableD d) throws IOException {
    this.wmoTableB = ucar.nc2.iosp.smos.bufr.tables.BufrTables.getWmoTableB(ids);
    this.wmoTableD = ucar.nc2.iosp.smos.bufr.tables.BufrTables.getWmoTableD(ids);
    this.localTableB = b;
    this.localTableD = d;
    this.mode = ucar.nc2.iosp.smos.bufr.tables.BufrTables.Mode.localOverride;
  }

  public String getWmoTableBName() {
    return wmoTableB.getName();
  }

  public String getLocalTableBName() {
    return localTableB == null ? "none" : localTableB.getName();
  }

  public String getLocalTableDName() {
    return localTableD == null ? "none" : localTableD.getName();
  }

  public String getWmoTableDName() {
    return wmoTableD.getName();
  }

  public ucar.nc2.iosp.smos.bufr.tables.BufrTables.Mode getMode() {
    return mode;
  }

  public ucar.nc2.iosp.smos.bufr.tables.TableB getLocalTableB() {
    return localTableB;
  }

  public ucar.nc2.iosp.smos.bufr.tables.TableD getLocalTableD() {
    return localTableD;
  }

  public ucar.nc2.iosp.smos.bufr.tables.TableB.Descriptor getDescriptorTableB(short fxy) {
    ucar.nc2.iosp.smos.bufr.tables.TableB.Descriptor b = null;
    boolean isWmoRange = Descriptor.isWmoRange(fxy);

    if (isWmoRange && (mode == ucar.nc2.iosp.smos.bufr.tables.BufrTables.Mode.wmoOnly)) {
      b = wmoTableB.getDescriptor(fxy);

    } else if (isWmoRange && (mode == ucar.nc2.iosp.smos.bufr.tables.BufrTables.Mode.wmoLocal)) {
      b = wmoTableB.getDescriptor(fxy);
      if ((b == null) && (localTableB != null))
        b = localTableB.getDescriptor(fxy);

    } else if (isWmoRange && (mode == ucar.nc2.iosp.smos.bufr.tables.BufrTables.Mode.localOverride)) {
      if (localTableB != null)
        b = localTableB.getDescriptor(fxy);
      if (b == null)
        b = wmoTableB.getDescriptor(fxy);
      else
        b.setLocalOverride(true);

    } else if (!isWmoRange) {
      if (localTableB != null)
        b = localTableB.getDescriptor(fxy);
    }

    if (b == null) {  // look forward in standard WMO table; often the version number of the message is wrong
      b = ucar.nc2.iosp.smos.bufr.tables.BufrTables.getWmoTableBlatest().getDescriptor(fxy);
    }

    if (b == null && showErrors)
      System.out.printf(" TableLookup cant find Table B descriptor = %s in tables %s, %s mode=%s%n", Descriptor.makeString(fxy),
                        getLocalTableBName(), getWmoTableBName(), mode);
    return b;
  }

  public List<Short> getDescriptorsTableD(short id) {
    ucar.nc2.iosp.smos.bufr.tables.TableD.Descriptor d = getDescriptorTableD(id);
    if (d != null)
      return d.getSequence();
    return null;
  }

  public List<String> getDescriptorsTableD(String fxy) {
    short id = Descriptor.getFxy(fxy);
    List<Short> seq = getDescriptorsTableD(id);
    if (seq == null) return null;
    List<String> result = new ArrayList<String>(seq.size());
    for (Short s : seq)
      result.add(Descriptor.makeString(s));
    return result;
  }

  public ucar.nc2.iosp.smos.bufr.tables.TableD.Descriptor getDescriptorTableD(short fxy) {
    ucar.nc2.iosp.smos.bufr.tables.TableD.Descriptor d = null;
    boolean isWmoRange = Descriptor.isWmoRange(fxy);

    if (isWmoRange && (mode == ucar.nc2.iosp.smos.bufr.tables.BufrTables.Mode.wmoOnly)) {
      d = wmoTableD.getDescriptor(fxy);

    } else if (isWmoRange && (mode == ucar.nc2.iosp.smos.bufr.tables.BufrTables.Mode.wmoLocal)) {
      d = wmoTableD.getDescriptor(fxy);
      if ((d == null) && (localTableD != null))
        d = localTableD.getDescriptor(fxy);

    } else if (isWmoRange && (mode == ucar.nc2.iosp.smos.bufr.tables.BufrTables.Mode.localOverride)) {
      if (localTableD != null)
        d = localTableD.getDescriptor(fxy);
      if (d == null)
        d = wmoTableD.getDescriptor(fxy);
      else
        d.setLocalOverride(true);

    } else {
      if (localTableD != null)
        d = localTableD.getDescriptor(fxy);
    }

    if (d == null) {  // look forward in standard WMO table; often the version number of the message is wrong
      d = BufrTables.getWmoTableDlatest().getDescriptor(fxy);
    }

    if (d == null && showErrors)
      System.out.printf(" TableLookup cant find Table D descriptor %s in tables %s,%s mode=%s%n", Descriptor.makeString(fxy),
                        getLocalTableDName(), getWmoTableDName(), mode);
    return d;
  }

}
