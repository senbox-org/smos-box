package org.esa.smos.dataio.smos.dddb;

import java.util.*;

public class FlagDescriptorsCombined implements Family<FlagDescriptor>{
    private final List<FlagDescriptorCombined> descriptorList;
    private final Map<String, FlagDescriptorCombined> descriptorMap;

    FlagDescriptorsCombined(List<String[]> recordList) {
        descriptorList = new ArrayList<>(recordList.size());
        descriptorMap = new HashMap<>(recordList.size());

        for (final String[] tokens : recordList) {
            final FlagDescriptorCombined record = new FlagDescriptorCombined(tokens);
            descriptorList.add(record);
            descriptorMap.put(record.getFlagName(), record);
        }
    }

    @Override
    public final List<FlagDescriptor> asList() {
        return Collections.unmodifiableList(descriptorList);
    }

    @Override
    public final FlagDescriptor getMember(String flagName) {
        return descriptorMap.get(flagName);
    }
}
