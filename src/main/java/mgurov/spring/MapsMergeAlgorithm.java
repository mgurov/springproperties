package mgurov.spring;

import mgurov.spring.impl.MapsMerger;
import mgurov.spring.impl.PropertyValueParser;
import mgurov.spring.impl.ResolutionTree;
import mgurov.spring.impl.SimpleMapsMerger;

public enum MapsMergeAlgorithm {
    SIMPLE_SQUASH {
        @Override
        MapsMerger newMerger(PropertyValueParser propertyValueParser) {
            return new SimpleMapsMerger(propertyValueParser);
        }
    } ,
    BUILD_TREE {
        @Override
        MapsMerger newMerger(PropertyValueParser propertyValueParser) {
            return new ResolutionTree(propertyValueParser);
        }
    };

    abstract MapsMerger newMerger(PropertyValueParser propertyValueParser);
}
