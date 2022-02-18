package io.picthor.config;

import ch.qos.logback.classic.helpers.MDCInsertingServletFilter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.TimeZone;

@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
public class MvcConfig implements WebMvcConfigurer {

    private final AppProperties properties;

    private final ThumbsResourceResolver thumbsResourceResolver;

    private final OriginalsResourceResolver originalsResourceResolver;

    @Value("${PICTHOR_UI_STATIC_FILES_DIR}")
    private String staticFileDir;

    public MvcConfig(AppProperties properties, ThumbsResourceResolver thumbsResourceResolver, OriginalsResourceResolver originalsResourceResolver) {
        this.properties = properties;
        this.thumbsResourceResolver = thumbsResourceResolver;
        this.originalsResourceResolver = originalsResourceResolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/thumbs/by-id/**")
                .addResourceLocations("file:" + properties.getCacheDir() + "/thumbs", "classpath:/public/")
                .setCachePeriod(360)
                .resourceChain(true)
                .addResolver(thumbsResourceResolver)
        ;
        registry
                .addResourceHandler("/originals/by-id/**")
                .setCachePeriod(360)
                .resourceChain(true)
                .addResolver(originalsResourceResolver)
        ;
        registry
                .addResourceHandler("/**")
                .addResourceLocations("file:" + staticFileDir + "/")
        ;
    }

    /**
     * Filter to forward all non /api and non static file requests to index.html of the UI dist
     */
    @Bean
    public FilterRegistrationBean<Filter> filterRegistrationBean() {
        FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter((request, response, chain) -> {
            HttpServletRequest request1 = (HttpServletRequest) request;
            if (!request1.getRequestURI().startsWith("/api/") && !request1.getRequestURI().contains(".")) {
                RequestDispatcher requestDispatcher = request.getRequestDispatcher("/index.html");
                requestDispatcher.forward(request, response);
                return;
            }
            chain.doFilter(request, response);
        });
        return filterFilterRegistrationBean;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setTimeZone(TimeZone.getDefault());
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return objectMapper;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }

    @Bean
    public Validator localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    @Bean
    public FilterRegistrationBean<MDCInsertingServletFilter> mdcFilterRegistrationBean() {
        FilterRegistrationBean<MDCInsertingServletFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new MDCInsertingServletFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Integer.MIN_VALUE);
        return registrationBean;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(properties.getAllowedCorsOrigins().toArray(String[]::new))
                .allowedMethods("POST", "PUT", "GET", "DELETE", "HEAD")
        ;
        log.info("Allowed CORS origins: {}", properties.getAllowedCorsOrigins());
    }
}
