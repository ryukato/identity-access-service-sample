package app.util;

import org.springframework.security.oauth2.provider.ClientDetails;

public interface ClientDetailsFactory<T> {
    ClientDetails createFrom(T t);
}
