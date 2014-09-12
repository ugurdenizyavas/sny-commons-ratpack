package com.sony.ebs.octopus3.commons.ratpack.product.enhancer

public interface ProductEnhancer {

    public <T> rx.Observable<T> enhance(T obj) throws Exception

    public <T> rx.Observable<T> enhance(T obj, boolean encoded) throws Exception

}