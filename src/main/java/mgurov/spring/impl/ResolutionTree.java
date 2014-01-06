package mgurov.spring.impl;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Comparably more complicated than the {@link mgurov.spring.impl.SimpleMapsMerger} way of merging maps. Effectively builds
 * a composite containing resolved string and "futures" to be resolved. Could potentially be more efficient on large datasets
 * with deep nesting and high degree of key repetition. On the downside futures tend to hang even if overriding properties do not need them
 * which could probably be overidden careful use of {@link java.util.WeakHashMap}
 * TODO: measure
 */
public class ResolutionTree implements MapsMerger {
    private final Map<String, EntryPart> keyDefinitions = newHashMap();
    private final Map<String, FutureReference> futures = newHashMap();
    private PropertyValueParser propertyValueParser;

    @Override
    public Map<String, String> merge(Iterable<Map<String, String>> inputs, PropertyValueParser propertyValueParser) {
        //TODO: via constructor?
        this.propertyValueParser = propertyValueParser;
        for (Map<String, String> input : inputs) {
            add(input);
        }
        resolveFutures();
        return valuesToStrings();
    }


    private void add(Map<String, String> input) {
        for (Map.Entry<String, String> sourceMapEntry : input.entrySet()) {
            keyDefinitions.put(sourceMapEntry.getKey(), parseEntry(sourceMapEntry.getValue()));
        }
    }

    private EntryPart parseEntry(String value) {
        return propertyValueParser.parse(value, new MyOnStringPartParsedEventListener()).result;
    }

    private void resolveFutures() {
        for (FutureReference future : futures.values()) {
            future.resolve(keyDefinitions.get(future.name));
        }
    }

    public Map<String, String> valuesToStrings() {
        return Maps.transformValues(keyDefinitions, EntryPart.TO_S);
    }

    private static interface EntryPart {
        /**
         * even though it is tempting to reuse {@link Object#toString()} we will leave that to late debugging nights.
         */
        String toS();

        static Function<EntryPart, String> TO_S = new Function<EntryPart, String>() {
            @Override
            public String apply(EntryPart entryPart) {
                return entryPart.toS();
            }
        };
    }

    private static class CompositePart implements EntryPart {
        private final List<EntryPart> contents;

        private CompositePart(List<EntryPart> contents) {
            this.contents = contents;
        }

        @Override
        public String toS() {
            return Joiner.on("").join(Iterables.transform(contents, TO_S));
        }

        @Override
        public String toString() {
            return "CompositePart{" +
                    "contents=" + contents +
                    '}';
        }
    }

    private static class FutureReference implements EntryPart {
        private final String name;
        //TODO: mutability is evil. Replace LeafString ?
        private EntryPart resolvedValue;

        private FutureReference(String name) {
            this.name = name;
        }

        @Override
        public String toS() {
            if (null == resolvedValue) {
                return "${" + name + "}";
            }
            return resolvedValue.toS();
        }

        public void resolve(EntryPart entryPart) {
            this.resolvedValue = entryPart;
        }

        @Override
        public String toString() {
            return "FutureReference{" +
                    "name='" + name + '\'' +
                    ", resolvedValue=" + resolvedValue +
                    '}';
        }
    }

    private static class LeafString implements EntryPart {
        private final String value;

        private LeafString(String value) {
            this.value = value;
        }

        @Override
        public String toS() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private class MyOnStringPartParsedEventListener implements PropertyValueParser.OnStringPartParsedEventListener {

        public EntryPart result;

        private List<EntryPart> partsCollected;

        @Override
        public void onStart() {
            partsCollected = newArrayList();
        }

        @Override
        public void onResolvedStringPart(String part) {
            partsCollected.add(new LeafString(part));
        }

        @Override
        public void onPlaceholderPart(String placeholder) {
            final EntryPart alreadyResolved;
            if (null != (alreadyResolved = keyDefinitions.get(placeholder))) {
                partsCollected.add(alreadyResolved);
                return;
            }

            FutureReference future = futures.get(placeholder);
            if (null == future) {
                future = new FutureReference(placeholder);
                futures.put(placeholder, future);
            }
            partsCollected.add(future);
        }

        @Override
        public void onEnd() {
            if (partsCollected.size() == 1) {
                result = partsCollected.get(0);
            } else {
                result = new CompositePart(partsCollected);
            }
        }
    }
}
