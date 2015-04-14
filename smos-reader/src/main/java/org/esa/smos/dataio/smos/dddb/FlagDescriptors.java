package org.esa.smos.dataio.smos.dddb;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FlagDescriptors implements Family<FlagDescriptor>{
    private final List<FlagDescriptor> descriptorList;
    private final Map<String, FlagDescriptor> descriptorMap;

    FlagDescriptors(List<String[]> recordList) {
        descriptorList = new ArrayList<>(recordList.size());
        descriptorMap = new HashMap<>(recordList.size());

        for (final String[] tokens : recordList) {
            final FlagDescriptorImpl record = new FlagDescriptorImpl(tokens);
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
