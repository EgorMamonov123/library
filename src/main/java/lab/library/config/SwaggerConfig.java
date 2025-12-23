package lab.library.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library Management System API")
                        .version("1.0.0")
                        .description("""
                                Система управления библиотекой
                                
                                Тестовые учетные записи:
                                
                                **Читатель**: username=`reader`, password=`password`
                                
                                **Библиотекарь**: username=`librarian`, password=`librarian`
                                
                                **Администратор**: username=`admin`, password=`admin`
                                """))
                .tags(List.of(
                        new Tag().name("Books").description("Управление каталогом книг"),
                        new Tag().name("Readers").description("Управление читателями"),
                        new Tag().name("Loans").description("Управление выдачей книг")
                ))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .components(new Components()
                        .addSecuritySchemes("basicAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")));
    }
}