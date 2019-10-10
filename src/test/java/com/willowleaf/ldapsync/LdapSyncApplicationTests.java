package com.willowleaf.ldapsync;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class LdapSyncApplicationTests {

    @Test
    public void contextLoads() {
    }

    private static class TestAction extends RecursiveAction {

        private int start, end;
        private final List<String> result;
        private static int THRESHOLD = 10;

        public TestAction(int start, int end, List<String> result) {
            this.start = start;
            this.end = end;
            this.result = result;
        }

        @Override
        protected void compute() {
            if (end - start <= THRESHOLD) {
                for (int i = start; i < end; i++) {
                    result.add(i + "");
                }
            } else {
                int mid = (end - start) / 2;
                TestAction left = new TestAction(start, mid, result);
                TestAction right = new TestAction(mid, end, result);
                invokeAll(left, right);
            }
        }
    }

    private static class TestTask2 extends RecursiveTask<List<String>> {

        private int start, end;
        private static int THRESHOLD = 10;

        TestTask2(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected List<String> compute() {
            if (end - start <= THRESHOLD) {
                List<String> result = new ArrayList<>();
                for (int i = start; i < end; i++) {
                    result.add(i + "");
                }
                return result;
            } else {
                int mid = (end - start) / 2;
                TestTask left = new TestTask(start, mid);
                TestTask right = new TestTask(mid, end);
                invokeAll(left, right);
                List<String> result = left.join();
                result.addAll(right.join());
                return result;
            }
        }
    }

    public static void main(String[] args) {
        int count = 10000;
        for (int i = 1; i <= count; i++) {
//            TestTask testTask = new TestTask(0, i);
//            TestTask2 testTask = new TestTask2(0, i);
//            List<String> result = testTask.invoke();

            List<String> result = new ArrayList<>();
            TestAction testAction = new TestAction(0, i, result);
            testAction.invoke();
            System.out.println(result.size());
        }
    }

    private static class TestTask extends RecursiveTask<List<String>> {

        private int start, end;
        private static int THRESHOLD = 10;

        TestTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected List<String> compute() {
            List<String> result = new ArrayList<>();

            if ((end - start) < THRESHOLD) {
                for (int i = start; i < end; i++) {
                    result.add(i + "");
                }
            } else {
                int mid = (start + end) / 2;
                TestTask left = new TestTask(start, mid);
                TestTask right = new TestTask(mid, end);
                invokeAll(left, right);
                result.addAll(left.join());
                result.addAll(right.join());
            }

            return result;
        }
    }
}
