// 文件路径: ...\src\main\java\org\campusmarket\exchange\config\KaptchaConfig.java
package org.campusmarket.exchange.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

// Kaptcha 验证码配置
@Configuration
public class KaptchaConfig {

    @Bean
    public Producer kaptchaProducer() {
        Properties properties = new Properties();
        // 图片边框，合法值：yes , no
        properties.setProperty("kaptcha.border", "yes");
        // 边框颜色，合法值： r,g,b (and optional alpha) 或者 white,black,blue.
        properties.setProperty("kaptcha.border.color", "105,179,90");
        // 验证码文本字符颜色，默认黑色
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        // 验证码图片宽度，默认 200
        properties.setProperty("kaptcha.image.width", "160");
        // 验证码图片高度，默认 50
        properties.setProperty("kaptcha.image.height", "60");
        // 验证码文本字符大小，默认为 40
        properties.setProperty("kaptcha.textproducer.font.size", "38");
        // Session Key (我们不用 session，但 Kaptcha 需要这个配置)
        properties.setProperty("kaptcha.session.key", "kaptchaCode");
        // 验证码文本字符长度，默认为 5
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 验证码文本字体样式，默认为 Arial, Courier
        properties.setProperty("kaptcha.textproducer.font.names", "Arial,Courier");
        // 图片样式：水纹 com.google.code.kaptcha.impl.WaterRipple, 鱼眼 com.google.code.kaptcha.impl.FishEyeGimpy, 阴影 com.google.code.kaptcha.impl.ShadowGimpy
        properties.setProperty("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.ShadowGimpy");
        // 字符间距
        properties.setProperty("kaptcha.textproducer.char.space", "3");
        // 使用哪些字符生成验证码
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");

        Config config = new Config(properties);
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}