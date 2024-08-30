package br.com.ifce.util;

import java.util.List;

public class ListUtil {
    public static <T> List<T> castList(List<?> list) {
        return list.stream().map(it -> (T) it).toList();
    }
}
