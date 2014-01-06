package mgurov.spring;

import mgurov.spring.impl.MapsMerger;
import mgurov.spring.impl.ResolutionTree;
import mgurov.spring.impl.SimpleMapsMerger;

public enum MapsMergeAlgorithm {
    SIMPLE_SQUASH {
        @Override
        MapsMerger newMerger() {
            return new SimpleMapsMerger();
        }
    } ,
    BUILD_TREE {
        @Override
        MapsMerger newMerger() {
            return new ResolutionTree();
        }
    };

    abstract MapsMerger newMerger();
}
