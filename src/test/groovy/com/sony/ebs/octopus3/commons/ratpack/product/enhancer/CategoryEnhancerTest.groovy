package com.sony.ebs.octopus3.commons.ratpack.product.enhancer

import org.junit.Before
import org.junit.Test
import org.springframework.core.io.DefaultResourceLoader

class CategoryEnhancerTest {

    DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader()

    CategoryEnhancer categoryEnhancer

    @Before
    void before() {
        categoryEnhancer = new CategoryEnhancer()
    }

    def getFeed = {
        defaultResourceLoader.getResource("classpath:com/sony/ebs/octopus3/commons/ratpack/product/enhancer/category.xml")?.inputStream
    }

    @Test
    void "test KDL19S5700U"() {
        assert "TV 47 19 lcd" == categoryEnhancer.parseFeed("KDL19S5700U", getFeed())
    }

    @Test
    void "test VPL-VW90ES"() {
        assert "HCS Home Cinema Projectors" == categoryEnhancer.parseFeed("VPL-VW90ES", getFeed())
    }

    @Test
    void "test for not found"() {
        assert null == categoryEnhancer.parseFeed("XXXXX", getFeed())
    }

}
