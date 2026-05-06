package ru.testit.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BaseJunit5ListenerParametersTest {

    static class SampleMethods {
        @SuppressWarnings("unused")
        static void twoParams(String a, Integer b) {
        }

        @SuppressWarnings("unused")
        static void arrayParam(int[] a) {
        }
    }

    private static Map<String, String> invokeGetParameters(BaseJunit5Listener listener, ReflectiveInvocationContext<Method> ctx)
            throws Exception {
        Method m = BaseJunit5Listener.class.getDeclaredMethod("getParameters", ReflectiveInvocationContext.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> res = (Map<String, String>) m.invoke(listener, ctx);
        return res;
    }

    private static ReflectiveInvocationContext<Method> invocationContext(Method method, List<Object> args) {
        return new ReflectiveInvocationContext<Method>() {
            @Override
            public Method getExecutable() {
                return method;
            }

            @Override
            public Class<?> getTargetClass() {
                return method.getDeclaringClass();
            }

            @Override
            public List<Object> getArguments() {
                return args;
            }

            @Override
            public Optional<Object> getTarget() {
                return Optional.empty();
            }
        };
    }

    static Stream<Arguments> nullSafeCases() throws Exception {
        Method twoParams = SampleMethods.class.getDeclaredMethod("twoParams", String.class, Integer.class);
        Method arrayParam = SampleMethods.class.getDeclaredMethod("arrayParam", int[].class);

        return Stream.of(
                Arguments.of(twoParams, Arrays.asList(null, 5), new LinkedHashMap<String, String>() {{
                    put("a", "null");
                    put("b", "5");
                }}),
                Arguments.of(twoParams, Arrays.asList("x", null), new LinkedHashMap<String, String>() {{
                    put("a", "x");
                    put("b", "null");
                }}),
                Arguments.of(arrayParam, Collections.singletonList(new int[]{1, 2}), new LinkedHashMap<String, String>() {{
                    put("a", "[1, 2]");
                }})
        );
    }

    @ParameterizedTest
    @MethodSource("nullSafeCases")
    void getParameters_isNullSafe_andStringifiesArrays(Method method, List<Object> args, Map<String, String> expected)
            throws Exception {
        BaseJunit5Listener listener = new BaseJunit5Listener();

        Map<String, String> actual = invokeGetParameters(listener, invocationContext(method, args));
        assertEquals(expected, actual);
    }

    @Test
    void getParameters_doesNotThrow_whenArgumentIsNull() throws Exception {
        Method m = SampleMethods.class.getDeclaredMethod("twoParams", String.class, Integer.class);
        BaseJunit5Listener listener = new BaseJunit5Listener();

        assertDoesNotThrow(() -> invokeGetParameters(listener, invocationContext(m, Arrays.asList(null, null))));
    }
}

