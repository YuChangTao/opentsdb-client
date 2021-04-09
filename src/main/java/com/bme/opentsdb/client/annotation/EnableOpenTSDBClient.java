package com.bme.opentsdb.client.annotation;

import com.bme.opentsdb.client.config.OpenTSDBClientMarkerConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(OpenTSDBClientMarkerConfiguration.class)
public @interface EnableOpenTSDBClient {

}
