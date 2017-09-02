package app.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class BeanCopyUtil {
    private BeanCopyUtil() {}

    public static <T> void copyNonNullProperties(T source, T target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    private static <T> String[] getNullPropertyNames(T source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = Arrays.stream(pds)
                .filter(pd -> src.getPropertyValue(pd.getName()) == null)
                .map(pd -> pd.getName())
                .collect(Collectors.toSet());
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}
