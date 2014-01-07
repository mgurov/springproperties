package mgurov.spring;

import mgurov.spring.impl.PropertyValueParser;

import java.util.Arrays;
import java.util.Map;


public class MapUtils {

    /**
     * Utility that merges maps (or property files) according to the following rules:
     * <ul>
     *     <li>property defined in a later file overwrites property with the same key defined earlier</li>
     *     <li>property values may contain placeholders to be replaced by referenced key values.</li>
     *     <li>placeholder denoted by a pair of <em>${</em> and <em>}</em></li>
     *     <li>default values are not supported and the placeholder left intact in case referenced key is missing</li>
     * </ul>
     *
     * The utility was initially supposed to enhance Spring property loading but I quickly realized that I can achieve the goals
     * of that moment by rearrangign the way I loaded properties with the help of Spring's PropertyPlaceholderConfigurer so
     * this remains just as a small excercise.
     *
     */
    public static Map<String, String> merge(Map<String, String>... inputs) {
        return merge(MapsMergeAlgorithm.BUILD_TREE, inputs);
    }

    /**
     * calls {@link #merge(MapsMergeAlgorithm, mgurov.spring.impl.PropertyValueParser, java.util.Map[])} with default ${link PropertyValueParser}
     */
    public static Map<String, String> merge(MapsMergeAlgorithm algorithm, Map<String, String>... inputs) {
        return merge(algorithm, new PropertyValueParser(), inputs);
    }

    public static Map<String, String> merge(MapsMergeAlgorithm algorithm, PropertyValueParser propertyValueParser, Map<String, String>... inputs) {
        return algorithm.newMerger(propertyValueParser).merge(Arrays.asList(inputs));
    }

}
