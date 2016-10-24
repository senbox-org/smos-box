Properties used in band descriptors:

    Name            Type            Explanation
    
    visible         boolean         should the band appear in the SNAP product tree?
    band            string          band name used in SNAP
    dataset         string          the name of the dataset in the product file
    pol             int             polarization
    sampleModel     int             0, 1, 2
    scalingOffset   double          scaling offset applied to the raw data
    scalingFactor   double          scaling factor applied to the raw data
    typicalMin      double          typical (scaled) minimum value
    typicalMax      double          typical (scaled) maximum value
    cyclic          boolean         is the value range cyclic?
    fillValue       double          value indicating no-data in SNAP
    validExpr       string          valid-pixel expression used in SNAP
    unit            string          physical unit
    description     string          description
    flagCoding      string          for flag bands only: name of the flag coding
    flags           string          for flag bands only: name of the flag descriptor resource
    gridPointData   boolean         does this band belong to a grid point structure
    dimensionNames  string          blank separated list of netCDF dimension names for the band

When you do not know the value of a property or do not care about the value use an asterisk '*'.
A hash mark '#' can be used to commence comments. Use UTF-8 character encoding.

