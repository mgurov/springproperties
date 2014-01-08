package mgurov.spring;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

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
        assertEquals("template applied to sample", bean.get("template"));
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

    public static class KeyPrototypeFinder implements PropertyFileNamesSplitter.PrototypesNameFinder {

        private String prototypeKey = "prototype";

        @Override
        public String findPrototypeName(String input) {

            //In Spring do like Spring does.
            final ResourceEditor re = new ResourceEditor();
            re.setAsText(input);
            final Resource location = (Resource) re.getValue();
            final Properties properties = new Properties();

            try {
                final InputStream is = location.getInputStream();
                properties.load(is);
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return properties.getProperty(prototypeKey);
        }

        public void setPrototypeKey(String prototypeKey) {
            this.prototypeKey = prototypeKey;
        }
    }
}
