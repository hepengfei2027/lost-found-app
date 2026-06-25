package cn.jxufe.iet.lostandfound;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.awt.*;
import java.net.URI;

@SpringBootApplication
@MapperScan("cn.jxufe.iet.lostandfound.mapper")
public class LostAndFoundApplication {

    public static void main(String[] args) {
        SpringApplication.run(LostAndFoundApplication.class, args);
    }

    @Bean
    public CommandLineRunner openBrowser() {
        return args -> {
            // 等待服务器启动完成后打开浏览器
            Thread.sleep(1500);
            String url = "http://localhost:8088";
            try {
                Desktop.getDesktop().browse(new URI(url));
                System.out.println("已自动打开浏览器访问: " + url);
            } catch (Exception e) {
                System.out.println("无法自动打开浏览器，请手动访问: " + url);
            }
        };
    }
}
