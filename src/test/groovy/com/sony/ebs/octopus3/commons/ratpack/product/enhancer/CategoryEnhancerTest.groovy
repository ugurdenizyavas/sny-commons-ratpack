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
    void "test encoded KDL19S5700U"() {
        assert "TV 47 19 lcd" == categoryEnhancer.parseFeed("KDL19S5700U", true, getFeed())
    }

    @Test
    void "test encoded SGPCV5/B.AE"() {
        assert "Xperia Tablet Z Accessories" == categoryEnhancer.parseFeed("SGPCV5/B.AE", true, getFeed())
    }

    @Test
    void "test encoded SGPCV5/Z.AE"() {
        assert "Xperia Tablet Z Accessories" == categoryEnhancer.parseFeed("SGPCV5_2fZ.AE", true, getFeed())
    }

    @Test
    void "test encoded VPL-VW90ES"() {
        assert "HCS Home Cinema Projectors" == categoryEnhancer.parseFeed("VPL-VW90ES", true, getFeed())
    }

    @Test
    void "test for not found"() {
        assert null == categoryEnhancer.parseFeed("XXXXX", true, getFeed())
    }

    @Test
    void "test encoded MZB100//A+B .CE7"() {
        assert "Mini Disc" == categoryEnhancer.parseFeed("MZB100_2f_2fA_2bB+.CE7", true, getFeed())
    }

    @Test
    void "test MZB100//A+B .CE7"() {
        assert "Mini Disc" == categoryEnhancer.parseFeed("mzb100//a+b .CE7", false, getFeed())
    }

}
