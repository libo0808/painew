package com.pansophicmind.server.aidog.common;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.pansophicmind.server.web.enums.converter.StringToEnumConverterFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        WebMvcConfigurer.super.addFormatters(registry);
        registry.addConverterFactory(new StringToEnumConverterFactory());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebMvcConfigurer.super.addInterceptors(registry);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 设置允许跨域的路径
                .allowCredentials(true) // 是否允许证书,不再默认开启
                .allowedOrigins("*") // 设置允许跨域请求的域名
                .allowedHeaders("*") // 设置请求头
                .allowedMethods("*"); // 设置允许的方法
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 调用父类的配置
        WebMvcConfigurer.super.configureMessageConverters(converters);
        // 创建FastJson的消息转换器
        FastJsonHttpMessageConverter convert = new FastJsonHttpMessageConverter();
        // 创建FastJson的配置对象
        FastJsonConfig config = new FastJsonConfig();
        // 对Json数据进行格式化
        config.setSerializerFeatures(
                SerializerFeature.WriteEnumUsingToString,
                SerializerFeature.PrettyFormat,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullNumberAsZero,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteNullBooleanAsFalse,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.DisableCircularReferenceDetect // 禁止循环引用
        );
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        config.setCharset(StandardCharsets.UTF_8);
        convert.setFastJsonConfig(config);
        convert.setSupportedMediaTypes(getSupportedMediaTypes());
        convert.setDefaultCharset(StandardCharsets.UTF_8);
        converters.add(convert);
    }

    public List<MediaType> getSupportedMediaTypes() {
        // 创建fastJson消息转换器
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_ATOM_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
        supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        supportedMediaTypes.add(MediaType.APPLICATION_PDF);
        supportedMediaTypes.add(MediaType.APPLICATION_RSS_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_XHTML_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_XML);
        supportedMediaTypes.add(MediaType.IMAGE_GIF);
        supportedMediaTypes.add(MediaType.IMAGE_JPEG);
        supportedMediaTypes.add(MediaType.IMAGE_PNG);
        supportedMediaTypes.add(MediaType.TEXT_EVENT_STREAM);
        supportedMediaTypes.add(MediaType.TEXT_HTML);
        supportedMediaTypes.add(MediaType.TEXT_MARKDOWN);
        supportedMediaTypes.add(MediaType.TEXT_PLAIN);
        supportedMediaTypes.add(MediaType.TEXT_XML);
        supportedMediaTypes.add(MediaType.ALL);
        return supportedMediaTypes;
    }

}
