package mgurov.spring;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

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
        return merge(MergeAlgorithm.TREE, inputs);
    }

    public static Map<String, String> merge(MergeAlgorithm algorithm, Map<String, String>... inputs) {
        return algorithm.newMerger().merge(Arrays.asList(inputs));
    }

    public enum MergeAlgorithm {
        SIMPLE{
            @Override
            protected Merger newMerger() {
                return new SimpleMapsMerger();
            }
        } ,
        TREE {
            @Override
            protected Merger newMerger() {
                return new ResolutionTree();
            }
        };

        protected abstract Merger newMerger();

    }



    private interface Merger {
        Map<String, String> merge(Iterable<Map<String, String>> inputs);
    }

    private static class SimpleMapsMerger implements Merger {

        @Override
        public Map<String, String> merge(Iterable<Map<String, String>> inputs) {
            final HashMap<String,String> result = newHashMap();
            for (Map<String, String> input : inputs) {
                result.putAll(input);
            }
            return result;
        }
    }

    private static class ResolutionTree implements Merger {
        final Map<String, EntryPart> keyDefinitions = newHashMap();
        final Map<String, FutureReference> futures = newHashMap();

        @Override
        public Map<String, String> merge(Iterable<Map<String, String>> inputs) {
            for (Map<String, String> input : inputs) {
                add(input);
            }
            resolveFutures();
            return valuesToStrings();
        }


        private void add(Map<String, String> input) {
            for (Map.Entry<String, String> sourceMapEntry : input.entrySet()) {
                //TODO: what if we override? Here we might want to distinguish between the prototypes and the rest
                keyDefinitions.put(sourceMapEntry.getKey(), parseEntry(sourceMapEntry.getValue()));
            }
        }

        private EntryPart parseEntry(String value) {
            //TODO: configurable placeholder markers
            Pattern p = Pattern.compile("\\$\\{(.*?)\\}");
            Matcher m = p.matcher(value);
            if (!m.find()) {
                return new LeafString(value);
            }

            final List<EntryPart> entries = newArrayList();
            int unclaimedPosition = 0;

            do {
                if (m.start() > unclaimedPosition) {
                    //TODO: char sequences?
                    entries.add(new LeafString(value.substring(unclaimedPosition, m.start())));
                }
                unclaimedPosition = m.end();

                final String refName = m.group(1);
                final EntryPart alreadyResolved;
                if (null == (alreadyResolved = keyDefinitions.get(refName))) {

                    FutureReference future = futures.get(refName);
                    if (null == future) {
                        future = new FutureReference(refName);
                        futures.put(refName, future);
                    }
                    entries.add(future);
                } else {
                    entries.add(alreadyResolved);
                }

            } while (m.find());

            if (unclaimedPosition < value.length() - 1) {
                entries.add(new LeafString(value.substring(unclaimedPosition)));
            }

            if (entries.size() == 1) {
                return entries.get(0);
            } else {
                return new CompositePart(entries);
            }
        }

        private void resolveFutures() {
            for (FutureReference future : futures.values()) {
                future.resolve(keyDefinitions.get(future.name));
            }
        }

        public Map<String, String> valuesToStrings() {
            return Maps.transformValues(keyDefinitions, EntryPart.TO_S);
        }
    }

    private static interface EntryPart {
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
    }

    private static class FutureReference implements EntryPart {
        private final String name;
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
    }

}
