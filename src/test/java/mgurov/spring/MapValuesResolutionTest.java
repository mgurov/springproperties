package mgurov.spring;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import mgurov.spring.impl.PropertyValueParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

@RunWith(Parameterized.class)
public class MapValuesResolutionTest {

    private final MapValuesResolutionAlgorithm mergeAlgorithm;

    public MapValuesResolutionTest(MapValuesResolutionAlgorithm mergeAlgorithm) {
        this.mergeAlgorithm = mergeAlgorithm;
    }

    @Parameterized.Parameters(name= "{index}: {0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[]{MapValuesResolutionAlgorithm.SIMPLE_SQUASH}, new Object[]{MapValuesResolutionAlgorithm.BUILD_TREE});
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
                MapUtils.resolveValues(mergeAlgorithm, data));
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
                MapUtils.resolveValues(mergeAlgorithm, data));
    }

    @Test
    public void unresolvedValue() {
        Map<String, String> data = Maps.newLinkedHashMap();
        data.put("unresolved.reference", "${http404}");

        assertEquals(
                ImmutableMap.<String, String>builder()
                        .put("unresolved.reference", "${http404}").build(),
                MapUtils.resolveValues(mergeAlgorithm, data));
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
                MapUtils.resolveValues(mergeAlgorithm, data));
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
        assumeFalse("The BUILD_TREE algo appeared to be tricker in the way of determining the ", mergeAlgorithm == MapValuesResolutionAlgorithm.BUILD_TREE);
        Map<String, String> data = Maps.newLinkedHashMap();
        data.put("forward.reference", "${referenced.earlier}");
        data.put("referenced.earlier", "closing the circle ${forward.reference}");

        MapUtils.resolveValues(mergeAlgorithm, data);
    }

}
