package mgurov.spring;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MapMergingTest {

    @Test
    public void prototypedLoadingWithLookAhead() {
        Map<String, String> prototype = Collections.singletonMap("inherited property expanded later", "http://id.${later.defined}/fooe");
        Map<String, String> inheritor = Collections.singletonMap("later.defined", "blah");

        assertEquals(
                ImmutableMap.<String, String>builder().put("inherited property expanded later", "http://id.blah/fooe").put("later.defined", "blah").build(),
                MapUtils.merge(inheritor, prototype));
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
                MapUtils.merge(data));
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
                MapUtils.merge(data));
    }

    @Test
    public void unresolvedValue() {
        Map<String, String> data = Maps.newLinkedHashMap();
        data.put("unresolved.reference", "${http404}");

        assertEquals(
                ImmutableMap.<String, String>builder()
                        .put("unresolved.reference", "${http404}").build(),
                MapUtils.merge(data));
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
                MapUtils.merge(data));
    }

    @Test
    @Ignore
    public void detectCircularDependency() {
        fail("Not implemented yet");
    }

}
