package mgurov.spring;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import mgurov.spring.impl.CircularReferenceException;
import mgurov.spring.impl.PropertyValueParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class MapMergingTest {

    private final MapsMergeAlgorithm mergeAlgorithm;

    public MapMergingTest(MapsMergeAlgorithm mergeAlgorithm) {
        this.mergeAlgorithm = mergeAlgorithm;
    }

    @Parameterized.Parameters(name= "{index}: {0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[]{MapsMergeAlgorithm.SIMPLE_SQUASH}, new Object[]{MapsMergeAlgorithm.BUILD_TREE});
    }

    @Test
    public void prototypedLoadingWithLookAhead() {
        Map<String, String> prototype = Collections.singletonMap("inherited property expanded later", "http://id.${later.defined}/fooe");
        Map<String, String> inheritor = Collections.singletonMap("later.defined", "blah");

        assertEquals(
                ImmutableMap.<String, String>builder().put("inherited property expanded later", "http://id.blah/fooe").put("later.defined", "blah").build(),
                MapUtils.merge(mergeAlgorithm, inheritor, prototype));
    }


    @Test
    public void resolvingLaterEntry() {
        Map<String, String> data = Maps.newLinkedHashMap();
        data.put("forward.reference", "${referenced.earlier}");
        data.put("referenced.earlier", "value");

        assertEquals(
                ImmutableMap.<String, String>builder()
                        .put("forward.reference", "value")
                        .put("referenced.earlier", "value").build(),
                MapUtils.merge(mergeAlgorithm, data));
    }

    @Test
    public void resolvingEarlier() {
        Map<String, String> data = Maps.newLinkedHashMap();
        data.put("referenced.later", "value");
        data.put("backward.reference", "${referenced.later}");

        assertEquals(
                ImmutableMap.<String, String>builder()
                        .put("backward.reference", "value")
                        .put("referenced.later", "value").build(),
                MapUtils.merge(mergeAlgorithm, data));
    }

    @Test
    public void unresolvedValue() {
        Map<String, String> data = Maps.newLinkedHashMap();
        data.put("unresolved.reference", "${http404}");

        assertEquals(
                ImmutableMap.<String, String>builder()
                        .put("unresolved.reference", "${http404}").build(),
                MapUtils.merge(mergeAlgorithm, data));
    }

    @Test
    public void sameFutureTwice() {
        Map<String, String> data = Maps.newLinkedHashMap();
        data.put("forward.reference", "${referenced.earlier} and again ${referenced.earlier}");
        data.put("referenced.earlier", "value");

        assertEquals(
                ImmutableMap.<String, String>builder()
                        .put("forward.reference", "value and again value")
                        .put("referenced.earlier", "value").build(),
                MapUtils.merge(mergeAlgorithm, data));
    }

    @Test
    public void modifiedPlaceholderSuffixAndPrefix() {
        Map<String, String> data = Maps.newLinkedHashMap();
        data.put("forward.reference", "#(referenced.earlier) and #(unresolved)");
        data.put("referenced.earlier", "value");

        assertEquals(
                ImmutableMap.<String, String>builder()
                        .put("forward.reference", "value and #(unresolved)")
                        .put("referenced.earlier", "value").build(),
                MapUtils.merge(mergeAlgorithm, new PropertyValueParser("#(", ")"), data));
    }

    @Test(expected = CircularReferenceException.class)
    public void detectCircularDependency() {
        Map<String, String> data = Maps.newLinkedHashMap();
        data.put("forward.reference", "${referenced.earlier}");
        data.put("referenced.earlier", "closing the circle ${forward.reference}");

        assertEquals("", MapUtils.merge(mergeAlgorithm, data));
    }

}
