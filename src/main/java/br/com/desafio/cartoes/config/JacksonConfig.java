package br.com.desafio.cartoes.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Configuration
public class JacksonConfig {

    private static class BigDecimalSerializer extends tools.jackson.databind.ValueSerializer<BigDecimal> {
        @Override
        public void serialize(BigDecimal value, tools.jackson.core.JsonGenerator gen,
                              tools.jackson.databind.SerializationContext ctxt) throws tools.jackson.core.JacksonException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeNumber(value.setScale(2, RoundingMode.HALF_UP));
            }
        }
    }

    @Bean
    public JsonMapperBuilderCustomizer bigDecimalCustomizer() {
        return builder -> {
            tools.jackson.databind.module.SimpleModule module = new tools.jackson.databind.module.SimpleModule();
            module.addSerializer(BigDecimal.class, new BigDecimalSerializer());
            builder.addModule(module);
        };
    }
}
