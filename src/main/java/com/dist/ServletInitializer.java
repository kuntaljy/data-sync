package com.dist;

/**
 * @author lijy
 */
import com.dist.DataSyncApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {

        //打成jar使用这一句
        return builder;

        //打war包使用这一句
        //return builder.sources(DataSyncApplication.class);
    }

}