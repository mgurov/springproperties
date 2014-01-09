package mgurov.spring;

import mgurov.spring.impl.PropertyValueParser;

import java.util.Map;


public class MapUtils {

    /**
     * Utility that substitutes placeholders in the values of the given map by its available keys.
     * <ul>
     *     <li>placeholder denoted by a pair of <em>${</em> and <em>}</em> (configurable)</li>
     *     <li>default values are not supported and the placeholder left intact in case referenced key is missing</li>
     *     <li>${@link CircularReferenceException} thrown upon such occasion detected (not suppored by the RESOLUTION_TREE algorithm)</li>
     * </ul>
     *
     * The utility was initially supposed to enhance Spring property loading but I quickly realized that I can achieve the goals
     * of that moment by rearranging the way I loaded properties with the help of Spring's PropertyPlaceholderConfigurer so
     * this remains just as a small exercise.
     *
     */
    public static Map<String, String> resolveValues(MapValuesResolutionAlgorithm algorithm, Map<String, String> input) {
        return merge(algorithm, new PropertyValueParser(), input);
    }

    public static Map<String, String> merge(MapValuesResolutionAlgorithm algorithm, PropertyValueParser propertyValueParser, Map<String, String> input) {
        return algorithm.newInstance(propertyValueParser).merge(input);
    }

}
