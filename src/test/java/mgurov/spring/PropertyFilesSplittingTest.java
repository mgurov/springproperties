package mgurov.spring;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PropertyFilesSplittingTest {

    private final PropertyFileNamesCommaSeparatedSplitter cut = new PropertyFileNamesCommaSeparatedSplitter();

    @Test
    public void byDefaultSimplySplitInput() {
        assertEquals(
                Arrays.asList("a", "b", "c"),
                cut.split("a,b,c")
        );
    }

    @Test
    public void prefixAndSuffixEachEntryWhenProvided() {

        cut.setPrefix("classpath:");
        cut.setSuffix(".properties");

        assertEquals(
                Arrays.asList("classpath:a.properties", "classpath:b.properties", "classpath:c.properties"),

                cut.split("a,b,c")
        );
    }

    @Test
    public void prototypingFunctionInjectsFoundPrototypesWrappedByPrefixAndSuffix() {
        cut.setPrefix("c:");
        cut.setSuffix(".p");
        cut.setPrototypeNamesFinder(new PropertyFileNamesCommaSeparatedSplitter.PrototypesNameFinder() {
            @Override
            public String findPrototypeName(String input) {
                if (input.equals("c:thenPrototyped.p")) {
                    return "proto1,proto2";
                }
                return null;
            }
        });

        assertEquals(
                Arrays.asList("c:firstNoProto.p", "c:proto1.p", "c:proto2.p", "c:thenPrototyped.p"),
                cut.split("firstNoProto,thenPrototyped")
        );
    }

    @Test
    public void IgnoreEmptyStringReturnedByPrototypingFunction() {
        cut.setPrototypeNamesFinder(new PropertyFileNamesCommaSeparatedSplitter.PrototypesNameFinder() {
            @Override
            public String findPrototypeName(String input) {
                return " ";
            }
        });

        assertEquals(
                Arrays.asList("noProto"),
                cut.split("noProto")
        );

    }

}
