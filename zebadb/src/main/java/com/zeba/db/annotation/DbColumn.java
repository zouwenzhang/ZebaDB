package com.zeba.db.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface DbColumn
{
    String name();
    String length() default "";
    boolean isSave() default true;
    DbColumnType type() default DbColumnType.TEXT;
}
