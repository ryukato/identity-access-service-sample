package app.util;

import app.domain.ApiKeyInformation;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public final class ApiKeyGenerator {
    private ApiKeyGenerator(){}

    public static ApiKeyInformation generate(int length, LocalDateTime expireDate) {
        RandomValueStringGenerator randomValueStringGenerator = new RandomValueStringGenerator();
        randomValueStringGenerator.setLength(length);
        LocalDateTime localDateTime = LocalDateTime.now().plus(1, ChronoUnit.MONTHS);

        return new ApiKeyInformation(
                randomValueStringGenerator.generate(),
                Optional.ofNullable(expireDate).orElse(localDateTime)
        );
    }

    public static ApiKeyInformation generate(int length) {
        return generate(length, null);
    }
}
