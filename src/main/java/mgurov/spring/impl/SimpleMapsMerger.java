package mgurov.spring.impl;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Emulates Spring's {@link org.springframework.core.env.PropertySourcesPropertyResolver} or something around that by simply
 * squashing all the maps into one big one and then processing each key and looking up properties from the squashed map.
 */
public class SimpleMapsMerger implements MapsMerger {

    private final HashMap<String,String> squashedMap = newHashMap();
    private PropertyValueParser propertyValueParser;

    @Override
    public Map<String, String> merge(Iterable<Map<String, String>> inputs, PropertyValueParser propertyValueParser) {

        this.propertyValueParser = propertyValueParser;

        for (Map<String, String> input : inputs) {
            squashedMap.putAll(input);
        }


        final Map<String, String> result = newHashMap();
        for (Map.Entry<String, String> stringStringEntry : squashedMap.entrySet()) {
            result.put(stringStringEntry.getKey(), resolveValue(stringStringEntry.getValue()));
        }

        return result;
    }

    private String resolveValue(String value) {
        return propertyValueParser.parse(value, new MyOnStringPartParsedEventListener()).result.toString();
    }

    private class MyOnStringPartParsedEventListener implements PropertyValueParser.OnStringPartParsedEventListener {
        private StringBuilder result;

        @Override
        public void onStart() {
            result = new StringBuilder();
        }

        @Override
        public void onResolvedStringPart(String part) {
            result.append(part);
        }

        @Override
        public void onPlaceholderPart(String placeholder) {
            String value = squashedMap.get(placeholder);
            if (null == value) {
                //TODO: wrapping of the placeholder could be delegated back to the parser probably, although would be weird
                result.append("${").append(placeholder).append("}");
            } else {
                result.append(resolveValue(value));
            }
        }

        @Override
        public void onEnd() {
        }
    }
}
