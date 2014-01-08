package mgurov.spring;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

    public static class Bean {

    }
}
