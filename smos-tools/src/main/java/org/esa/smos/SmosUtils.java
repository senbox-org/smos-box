package org.esa.smos;

import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.io.FileUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SmosUtils {

    private static final Logger logger = Logger.getLogger(SmosUtils.class.getName());

    /**
     * @param fileName The filename of the product, from which the start day will be read.
     * @return a Date with the year, month, and day field set from the filename, and the rest left default (today's time)
     */
    public static Date getSensingStartDayFromFilename(String fileName) {
        final String yearString = fileName.substring(19, 23);
        final String monthString = fileName.substring(23, 25);
        final String dayString = fileName.substring(25, 27);

        final int year = Integer.parseInt(yearString);
        final int month = Integer.parseInt(monthString);
        final int day = Integer.parseInt(dayString);

        final Calendar cal = DateTimeUtils.getUtcCalendar();
        //noinspection MagicConstant
        cal.set(year, month - 1, day);
        return cal.getTime();
    }


    public static Date getSensingStartTimeFromFilename(String fileName) {
        if (fileName.length() < 34) {
            return null;
        }
        return getSensingTime(fileName.substring(19, 34));
    }

    public static Date getSensingStopTimeFromFilename(String fileName) {
        if (fileName.length() < 50) {
            return null;
        }

        return getSensingTime(fileName.substring(35, 50));
    }

    /**
     * @param fileName the filename which may contain the path, and must contain the extension
     * @return the type string (which may not exactly equal a substring in the filename, depending on special conditions)
     */
    public static String getProductType(String fileName) {
        //format the name to remove path
        fileName = new File(fileName).getName();

        final String fileType;
        if (isAuxFileType(fileName)) {
            fileType = "AUX_DATA";
        } else if (isQualityControlType(fileName)) {
            // This value should never end up as a directory name. QualityFileHandler uses
            // getTargetForQCFiles(...) instead of getProductType(...) to find the directory name.
            fileType = "RQC_RQD";
        } else {
            fileType = getProductTypeFromFilename(fileName);
        }
        return fileType;
    }

    public static boolean isDblFileName(String fileName) {
        final String extension = FileUtils.getExtension(fileName);
        return ".dbl".equalsIgnoreCase(extension);
    }

    public static boolean isHdrFileName(String fileName) {
        final String extension = FileUtils.getExtension(fileName);
        return ".hdr".equalsIgnoreCase(extension);
    }

    public static boolean isL0Type(String fileName) {
        return fileName.matches("SM_.{4}_MIR_SC_[DF]0__.{45}") ||
                fileName.matches("SM_.{4}_MIR_TAR[DF]0__.{45}") ||
                fileName.matches("SM_.{4}_MIR_UNC[DNU]0__.{45}") ||
                fileName.matches("SM_.{4}_MIR_COR[DNU]0__.{45}") ||
                fileName.matches("SM_.{4}_MIR_TEST0__.{45}") ||
                fileName.matches("SM_.{4}_TLM_MIRA0__.{45}");
    }

    public static boolean isL1aType(String fileName) {
        return fileName.matches("SM_.{4}_MIR_SC_[DF]1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_TAR[DF]1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_AFW[DU]1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_ANIR1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_UNC[DNU]1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_CORN1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_CRS[DU]1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_UAV[DU]1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_NIR_1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_FWAS1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_TLM_MIRA1[Aa]_.{45}");
    }

    public static boolean isL1bType(String fileName) {
        return fileName.matches("SM_.{4}_MIR_SC_[DF]1[Bb]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_TAR[DF]1[Bb]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_FTT[DF]___.{45}") ||
                fileName.matches("SM_.{4}_MIR_[GJ]MAT[DU]__.{45}");
    }

    public static boolean isL1cType(String fileName) {
        return fileName.matches("SM_.{4}_MIR_SC[LS][DF]1[Cc]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_BW[LS][DF]1[Cc]_.{45}");
    }

    public static boolean isLightBufrType(String fileName) {
        return fileName.matches("W_ES-ESA-ESAC,SMOS,N256_C_LEMM_[0-9]{14}_[0-9]{14}_[0-9]{14}_bufr_v[0-9]{3}\\.bin(\\.bz2)?");
    }

    public static boolean isBufrType(String fileName) {
        return fileName.matches("miras_[0-9]{8}_[0-9]{6}_[0-9]{8}_[0-9]{6}_smos_[0-9]{5}_._[0-9]{8}_[0-9]{6}_l1c\\.bufr");
    }

    public static boolean isL2Type(String fileName) {
        return fileName.matches("SO_.{4}_MIR_TSM_2__.{45}") ||
                fileName.matches("SO_.{4}_MIR_TOS_2__.{45}") ||
                fileName.matches("SO_.{4}_MIR_SM__2__.{45}") ||
                fileName.matches("SM_.{4}_MIR_(SM|OS)UDP2_.{45}") ||
                fileName.matches("SM_.{4}_MIR_(SM|OS)DAP2_.{45}") ||
                fileName.matches("SO_.{4}_MIR_OS__2__.{45}");
    }

    public static boolean isAuxFileType(String fileName) {
        return fileName.matches("SM_.{4}_AUX_APOD01_.{44}") ||
                fileName.matches("SM_.{4}_AUX_AGDPT__.{45}") ||
                fileName.matches("SM_.{4}_AUX_APD[LS](00|__)_.{45}") ||
                fileName.matches("SM_.{4}_AUX_ATMOS__.{45}") ||
                fileName.matches("SM_.{4}_AUX_BFP____.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_BNDLST_.{45}") ||
                fileName.matches("SM_.{4}_AUX_BSCAT__.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_BWGHT__.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_CNF(L0P|L1P|SMD|SMF|OSD|OSF)_.{45}") ||
                fileName.matches("SM_.{4}_AUX_DFFFRA_.{45}") ||
                fileName.matches("SM_.{4}_AUX_D(FF|GG)XYZ_.{45}") ||
                fileName.matches("SM_.{4}_AUX_DFFL(AI|MX)_.{45}") ||
                fileName.matches("SM_.{4}_AUX_DGG____.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_DGGT(LV|FO)_.{45}") ||
                fileName.matches("SM_.{4}_AUX_DGGR(OU|FI)_.{45}") ||
                fileName.matches("SM_.{4}_AUX_DGGFLO_.{45}") ||
                fileName.matches("SM_.{4}_AUX_DISTAN_.{45}") ||
                fileName.matches("SM_.{4}_AUX_ECOLAI_.{45}") ||
                fileName.matches("SM_.{4}_AUX_FAIL___.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_FLATT__.{44}") ||
                fileName.matches("SM_.{4}_AUX_FLTSEA_.{45}") ||
                fileName.matches("SM_.{4}_AUX_FOAM___.{45}") ||
                fileName.matches("SM_.{4}_AUX_GALAXY_.{45}") ||
                fileName.matches("SM_.{4}_AUX_GALNIR_.{45}") ||
                fileName.matches("SM_.{4}_AUX_GAL(_|2)(SM|OS)_.{45}") ||
                fileName.matches("SM_.{4}_AUX_GLXY___.{45}") ||
                fileName.matches("SM_.{4}_AUX_IGRF___.{45}") ||
                fileName.matches("SM_.{4}_AUX_LCF____.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_MASK___.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_MISP___.{45}") ||
                fileName.matches("SM_.{4}_AUX_LANDCL_.{45}") ||
                fileName.matches("SM_.{4}_AUX_LSMASK_.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_MN_WEF_.{45}") ||
                fileName.matches("SM_.{4}_AUX_MOONT__.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_NIR____.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_ORBPRE_.{45}") ||
                fileName.matches("SM_.{4}_AUX_ORBRES_.{45}") ||
                fileName.matches("SM_.{4}_AUX_PATT[0-9_][0-9_]_.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_PLM____.{45,46}") ||
                fileName.matches("SM_.{4}_AUX_PMS____.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_RFI____.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_RGHNS[1-3]_.{45}") ||
                fileName.matches("SM_.{4}_AUX_SOIL_P_.{45}") ||
                fileName.matches("SM_.{4}_AUX_SPAR___.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_SSS____.{45}") ||
                fileName.matches("SM_.{4}_AUX_VTEC___.{45}") ||
                fileName.matches("SM_.{4}_AUX_SUNT___.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_SGLINT_.{45}") ||
                fileName.matches("SM_.{4}_AUX_TIME___.{45}") ||
                fileName.matches("SM_.{4}_AUX_VTEC_[CPR]_.{45}") ||
                fileName.matches("SM_.{4}_AUX_WEF____.{45}") ||
                fileName.matches("SM_.{4}_AUX_CNFFAR_.{45}") ||
                fileName.matches("SM_.{4}_AUX_BULL_B_.{45}") ||
                fileName.matches("SM_.{4}_AUX_OTT(1|2|3)(D|F)__.{45}") ||
                fileName.matches("SM_.{4}_AUX_ECMWF__.{45}");
    }

    public static boolean isQualityControlType(String fileName) {
        return fileName.matches("SM_.{4}_RQ(C|D)_.{49}(EEF|eef)");
    }

    public static boolean isMirasPlanType(String fileName) {
        return fileName.matches("SM_.{4}_MPL_ORBSCT_.{44,45}") ||
                fileName.matches("SM_.{4}_MPL_XBDOWN_.{44,45}") ||
                fileName.matches("SM_.{4}_MPL_APIDPL_.{44,45}") ||
                fileName.matches("SM_.{4}_MPL_HLPLAN_.{44,45}") ||
                fileName.matches("SM_.{4}_MPL_XBDPRE_.{44,45}") ||
                fileName.matches("SM_.{4}_MPL_PROTEV_.{44,45}");
    }

    public static boolean isAuxECMWFType(String fileName) {
        return fileName.indexOf("_AUX_ECMWF_") > 0;
    }

    public static boolean isOsAnalysisFormat(String formatName) {
        return formatName.contains("MIR_OSDAP2");
    }

    public static boolean isOsUserFormat(String formatName) {
        return formatName.contains("MIR_OSUDP2");
    }

    public static boolean isSmAnalysisFormat(String formatName) {
        return formatName.contains("MIR_SMDAP2");
    }

    public static boolean isSmUserFormat(String formatName) {
        return formatName.contains("MIR_SMUDP2");
    }

    public static boolean isDualPolBrowseFormat(String formatName) {
        return formatName.contains("MIR_BWLD1C")
                || formatName.contains("MIR_BWSD1C")
                || formatName.contains("MIR_BWND1C");
    }

    public static boolean isFullPolBrowseFormat(String formatName) {
        return formatName.contains("MIR_BWLF1C")
                || formatName.contains("MIR_BWSF1C")
                || formatName.contains("MIR_BWNF1C");
    }

    public static boolean isBrowseFormat(String formatName) {
        return isDualPolBrowseFormat(formatName) || isFullPolBrowseFormat(formatName);
    }

    public static boolean isDualPolScienceFormat(String formatName) {
        return formatName.contains("MIR_SCLD1C")
                || formatName.contains("MIR_SCSD1C")
                || formatName.contains("MIR_SCND1C");
    }

    public static boolean isFullPolScienceFormat(String formatName) {
        return formatName.contains("MIR_SCLF1C")
                || formatName.contains("MIR_SCSF1C")
                || formatName.contains("MIR_SCNF1C");
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public static boolean isCompressedFile(File file) {
        final String extension = FileUtils.getExtension(file);
        if (StringUtils.isNullOrEmpty(extension)) {
            return false;
        }

        return extension.contains("zip") || extension.contains("ZIP");
    }

    public static boolean isDggTlvFormat(String formatName) {
        return formatName.contains("AUX_DGGTLV");
    }

    public static boolean isDggTfoFormat(String formatName) {
        return formatName.contains("AUX_DGGTFO");
    }

    public static boolean isDggRouFormat(String formatName) {
        return formatName.contains("AUX_DGGROU");
    }

    public static boolean isDggRfiFormat(String formatName) {
        return formatName.contains("AUX_DGGRFI");
    }

    public static boolean isDggFloFormat(String formatName) {
        return formatName.contains("AUX_DGGFLO");
    }

    public static boolean isLsMaskFormat(String formatName) {
        return formatName.contains("AUX_LSMASK");
    }

    public static boolean isVTecFormat(String formatName) {
        return formatName.contains("AUX_VTEC_C")
                || formatName.contains("AUX_VTEC_P");
    }

    public static boolean isDffLaiFormat(String formatName) {
        return formatName.contains("AUX_DFFLAI");
    }
    // @todo 1 tb/tb merge these two functions to one DFF file checker 2017-02-28
    public static boolean isDffSnoFormat(String formatName) {
        return formatName.contains("AUX_DFFSNO");
    }

    public static boolean isL2Format(String formatName) {
        return SmosUtils.isSmUserFormat(formatName) ||
                SmosUtils.isSmAnalysisFormat(formatName) ||
                SmosUtils.isOsUserFormat(formatName) ||
                SmosUtils.isOsAnalysisFormat(formatName);
    }

    ////////////////////////////////////////////////////////////////////////////////
    /////// END OF PUBLIC
    ////////////////////////////////////////////////////////////////////////////////


    static String getProductTypeFromFilename(String fileName) {
        //in case fileName includes path, remove it
        fileName = new File(fileName).getName();

        final String typeString = fileName.substring(8, 18);
        return typeString.toUpperCase();
    }

    /**
     * @param sensingTime The sensing time from the filename, which looks like ISO-8601 timespec=second with colons and hyphens removed.
     * @return a Date representing the whole sensing time
     */
    private static Date getSensingTime(String sensingTime) {
        DateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return df.parse(sensingTime);
        } catch (ParseException e) {
            //unexpected; assuming file name filtering was done before
            logger.log(Level.WARNING, "Exception while parsing sensing time from string: \"" + sensingTime + "\"", e);
            return null;
        }
    }
}
