package br.ufrn.middleware.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.ufrn.middleware.enums.CreationStrategy;
import br.ufrn.middleware.enums.ResourceStrategy;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Lifecycle {
    CreationStrategy creation() default CreationStrategy.STATIC;
    ResourceStrategy resource() default ResourceStrategy.LAZY;
}