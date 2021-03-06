package mgurov.spring;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * An utility to parse comma separated list (of property files) and return them as a list of strings. <br/>
 * Features: <br/>
 * * configurable prefix and suffix to wrap split strings <br/>
 * * configurable prototype provider to put prototypes in front of the split string <br/>
 * <br/>
 * Example
 * <pre class="code">{@code
 * <bean id="classUnderTest" class="mgurov.spring.PropertyFileNamesSplitter">
 *     <property name="prefix" value="classpath:mgurov/spring/"/>
 *     <property name="suffix" value=".properties"/>
 * </bean>
 *
 * <bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
 *     <property name="locations">
 *         <bean factory-bean="classUnderTest" factory-method="split">
 *             <constructor-arg name="commaSeparatedPropertyFileNames" value="test-prototype,test-sample"/>
 *         </bean>
 *     </property>
 *     <property name="ignoreUnresolvablePlaceholders" value="true"/>
 * </bean>
 * }
 * </pre>
 */
public class PropertyFileNamesSplitter {

    public static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();
    private Function<String, String> prefixFunction = Functions.identity();
    private Function<String, String> suffixFunction = Functions.identity();
    private PrototypesNameFinder prototypeNamesFinder = NO_PROTO;

    public List<String> split(String commaSeparatedPropertyFileNames) {
        final List<String> result = newArrayList();
        for (String s : COMMA_SPLITTER.split(commaSeparatedPropertyFileNames)) {
            final String wrapped = wrap(s);
            final String prototypes = prototypeNamesFinder.findPrototypeName(wrapped);
            if (prototypes != null) {
                result.addAll(split(prototypes));
            }
            result.add(wrapped);
        }
        return result;
    }

    private String wrap(String s) {
        return suffixFunction.apply(prefixFunction.apply(s));
    }

    public interface PrototypesNameFinder {
        /**
         * @param input property file name (wrapped prefix and suffix)
         * @return string containing comma separated list of unwrapped prototype names
         */
        String findPrototypeName(String input);
    }

    public void setPrototypeNamesFinder(PrototypesNameFinder prototypeNamesFinder) {
        this.prototypeNamesFinder = prototypeNamesFinder;
    }

    public static final PrototypesNameFinder NO_PROTO = new PrototypesNameFinder() {
        @Override
        public String findPrototypeName(String input) {
            return null;
        }
    };

    public void setSuffix(final String suffix) {
        if (Strings.isNullOrEmpty(suffix)) {
            suffixFunction = Functions.identity();
        } else {
            suffixFunction = new SuffixStringFunction(suffix);
        }
    }

    public void setPrefix(final String prefix) {
        if (Strings.isNullOrEmpty(prefix)) {
            prefixFunction = Functions.identity();
        } else {
            prefixFunction = new PreffixStringFunction(prefix);
        }
    }

    private static class PreffixStringFunction implements Function<String, String> {
        private final String prefix;

        public PreffixStringFunction(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String apply(String input) {
            return prefix + input;
        }
    }

    private static class SuffixStringFunction implements Function<String, String> {
        private final String suffix;

        public SuffixStringFunction(String suffix) {
            this.suffix = suffix;
        }

        @Override
        public String apply(String input) {
            return input + suffix;
        }
    }
}
