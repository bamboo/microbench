package microbench;

import clojure.lang.AFn;
import clojure.lang.IFn;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import static clojure.java.api.Clojure.read;
import static clojure.java.api.Clojure.var;
import static java.util.Collections.emptyMap;

public class Main {

    private static final int ELEMENT_COUNT = 1000000;

    private static final int ITERATIONS = 100;

    // a (foolish?) attempt to fool hotspot into not optimizing away operations
    static volatile int value;

    static void post(int v) {
        value = v;
    }

    public static void main(String[] args) throws InterruptedException {

        // clojure.core.println to flush report output
        IFn println = var("clojure.core", "println");
        println.invoke(
            String.format("%d iterations on collection containing %d elements.", ITERATIONS, ELEMENT_COUNT));

        benchmark("ArrayList", Main::arrayList);
        println.invoke(value);

        benchmark("LinkedHashSet", Main::linkedHashSet);
        println.invoke(value);
    }

    private static void arrayList() {
        ArrayList<Integer> s = new ArrayList<>();
        for (int i = 0; i < ELEMENT_COUNT; ++i) {
            s.add(i);
        }
        for (int i = 0; i < ITERATIONS; ++i) {
            int sum = 0;
            for (int e : s) {
                sum += e;
            }
            post(sum);
        }
    }

    private static void linkedHashSet() {
        Set<Integer> s = new LinkedHashSet<>();
        for (int i = 0; i < ELEMENT_COUNT; ++i) {
            s.add(i);
        }
        for (int i = 0; i < ITERATIONS; ++i) {
            int sum = 0;
            for (int e : s) {
                sum += e;
            }
            post(sum);
        }
    }

    private static void benchmark(String label, Runnable r) {
        System.out.println(label);
        benchmark(r);
    }

    private static void benchmark(final Runnable r) {
        require("criterium.core");

        IFn bench = var("criterium.core", "benchmark*");
        IFn reportResult = var("criterium.core", "report-result");
        reportResult.invoke(
            bench.invoke(asClojureFunction(r), emptyMap()));
    }

    private static AFn asClojureFunction(final Runnable r) {
        return new AFn() {
            @Override
            public Object invoke() {
                r.run();
                return null;
            }
        };
    }

    private static void require(String ns) {
        IFn require = var("clojure.core", "require");
        require.invoke(read(ns));
    }
}
