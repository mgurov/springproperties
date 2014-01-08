package mgurov.spring;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("test-properties.xml")
public class PropertyLoadingSpringTest {

    @Autowired
    private Bean bean;

    @Test
    public void firstCheckThatSpringHasKickedIn() {
        assertNotNull(bean);
    }

    @Test
    public void shouldHaveNamePropertyResolved() {
        assertEquals("sample", bean.get("name"));
    }

    @Test
    public void overridenPropertyShallBeTakenFromSampleFile() {
        assertEquals("overriden in sample", bean.get("overriden"));
    }

    @Test
    public void templatedPropertyShallGetNameSubstituted() {
        assertEquals("template applied to ${name}", bean.get("template"));
    }

    public static class Bean {
        private final Map<String,String> data;

        public Bean(Map<String, String> data) {
            this.data = data;
        }

        public String get(String key) {
            return data.get(key);
        }
    }
}
