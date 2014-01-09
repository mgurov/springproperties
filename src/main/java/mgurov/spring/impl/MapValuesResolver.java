package mgurov.spring.impl;

import java.util.Map;

public interface MapValuesResolver {
    Map<String, String> merge(Map<String, String> inputs);
}
