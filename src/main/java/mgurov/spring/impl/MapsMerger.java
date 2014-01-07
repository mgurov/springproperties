package mgurov.spring.impl;

import java.util.Map;

public interface MapsMerger {
    Map<String, String> merge(Iterable<Map<String, String>> inputs);
}
