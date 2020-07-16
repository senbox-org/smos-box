#New in SMOS-Box 5.8.0
Update to version 5.8.0 adds 
* support for L1C data V724 (schema version 401)
  * update RFI flag codings and visualisation
  * added variable "Radiometric Accuracy"
* support for L2OS data V691 (schema version 401)
  * adde variables "Equiv_ftprt_diam" and "Mean_acq_time"
* updated NetCDF exporter to handle these changes

#New in SMOS-Box 5.7.0
The update to version 5.7.0 does not add any new feature to the SMOS-BOX. Only released for compatibility reasons with SNAP 8.

#New in SMOS-Box 5.6.0

### Solved issues
####Bugs
    [SMOSTBX-17] In VTEC data the time within the tie-point grid description is incorrect
    
    
#New in SMOS-Box 5.5
The update to version 5.5 does not add any new feature to the SMOS-BOX. Only released for compatibility reasons with SNAP 6.


#New in SMOS-Box 5.4
The update to version 5.4 adds support for AUX_DFFSNO file type that contains the 
snow coverage for the northern hemisphere based on NOAA data.

### Solved issues
####Bugs
    [smos-box#2] Incomplete snapshot information NetCDF SCSF1C/SCSL1C
    [smos-box#3] Scaled_Volumetric_Soil_Water_L1 Variable missing in AUX_ECMWF


#New in SMOS-Box 5.3
SMOS-Box version 5.3 has been updated to fully support L1C, L2 and auxiliary files using the new schema version 7.3.0. 
This update implements support for all files following the SMOS L1 Format Specification v6.2 (SO-TN-IDR-GS-0005_v6.2_L1 Spec_2016-08-31) 
and the SMOS L2 Format Specification v8.4 (SO-TN-IDR-GS-0006_v8.4_L2 Spec_2016-08-30). 
All previous data formats are still supported, providing backwards compatibility. 
This update affects all SNAP components and the NetCDF stand-alone conversion utility.


### Solved issues
####Bugs
    [SMOSTBX-10] - SEVERE: Invalid Grid Point ID 0 at index 0 - SA Format Conversion Tool 2.0

####New Feature
    [SMOSTBX-15] - Update DDDB to support schema version 7.3.0 formatted files

####Other
    [SMOSTBX-13] - Update documentation wrt external DDDB installation
    [SMOSTBX-14] - Help pages mention S-3 Toolbox

#New in SMOS-Box 5.2

No changes have been done to SMOS-BOX since the last release. 

#New in SMOS-Box 5.1

###New Features and Important Changes

No major changes have been made to the SMOS-Box in this new release.
A comprehensive list of all issues resolved in this version of the SMOS-Box can be found in our 
[issue tracking system](https://senbox.atlassian.net/issues/?filter=11511)

#Release notes of former versions

* [Resolved issues in version 5.0](https://senbox.atlassian.net/issues/?filter=11510)

