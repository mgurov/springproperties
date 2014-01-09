package mgurov.spring;

import mgurov.spring.impl.*;
import mgurov.spring.impl.MapValuesResolver;
import mgurov.spring.impl.SimpleMapValuesResolver;

public enum MapValuesResolutionAlgorithm {
    SIMPLE_SQUASH {
        @Override
        MapValuesResolver newInstance(PropertyValueParser propertyValueParser) {
            return new SimpleMapValuesResolver(propertyValueParser);
        }
    } ,
    BUILD_TREE {
        @Override
        MapValuesResolver newInstance(PropertyValueParser propertyValueParser) {
            return new ResolutionTree(propertyValueParser);
        }
    };

    abstract MapValuesResolver newInstance(PropertyValueParser propertyValueParser);
}
