package mgurov.spring.impl;

import mgurov.spring.CircularReferenceException;

import java.util.*;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Emulates Spring's {@link org.springframework.core.env.PropertySourcesPropertyResolver} or something around that by simply
 * squashing all the maps into one big one and then processing each key and looking up properties from the squashed map.
 */
public class SimpleMapValuesResolver implements MapValuesResolver {

    private Map<String,String> originalMap;
    private final PropertyValueParser propertyValueParser;

    public SimpleMapValuesResolver(PropertyValueParser propertyValueParser) {
        this.propertyValueParser = propertyValueParser;
    }

    @Override
    public Map<String, String> merge(Map<String, String> input) {
        originalMap = input;

        final Map<String, String> result = newHashMap();
        for (Map.Entry<String, String> stringStringEntry : originalMap.entrySet()) {
            final String key = stringStringEntry.getKey();
            result.put(key, resolveValue(stringStringEntry.getValue(), newHashSet(key)));
        }

        return result;
    }

    private String resolveValue(String value, Set<String> visitedReferences) {
        return propertyValueParser.parse(value, new MyOnStringPartParsedEventListener(visitedReferences)).result.toString();
    }

    private class MyOnStringPartParsedEventListener implements PropertyValueParser.OnStringPartParsedEventListener {
        private final Set<String> visitedReferences;
        private StringBuilder result;

        public MyOnStringPartParsedEventListener(Set<String> visitedReferences) {
            this.visitedReferences = visitedReferences;
        }

        @Override
        public void onStart() {
            result = new StringBuilder();
        }

        @Override
        public void onResolvedStringPart(String part) {
            result.append(part);
        }

        @Override
        public void onPlaceholderPart(String keyRefererence, String placeholder) {
            final String value = originalMap.get(keyRefererence);
            if (null != value) {
                if (!visitedReferences.add(keyRefererence)) {
                    throw new CircularReferenceException(keyRefererence);
                }
                result.append(resolveValue(value, visitedReferences));
                visitedReferences.remove(keyRefererence);
            } else {
                result.append(placeholder);
            }
        }

        @Override
        public void onEnd() {
        }
    }
}
