package com.willowleaf.ldapsync;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class ZKLockTests {


    public static void main(String[] args) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                "localhost:2181",
                new RetryNTimes(0, 0)
        );
        client.start();

        String lockPath = "/lock/test";
        TA ta = new TA(client, lockPath);
        TB tb = new TB(client, lockPath);

        ta.start();
        tb.start();

        ta.join();
        tb.join();
    }

    private static class TA extends Thread {

        private CuratorFramework client;
        private String lockPath;

        public TA(CuratorFramework client, String lockPath) {
            this.client = client;
            this.lockPath = lockPath;
        }

        @Override
        public void run() {
            System.err.println("TA start: " + LocalDateTime.now());
            InterProcessLock lock = new InterProcessMutex(client, lockPath);
            try {
                lock.acquire();
                System.out.println("业务执行10秒钟");
                TimeUnit.SECONDS.sleep(10);

                System.err.println("TA end: " + LocalDateTime.now());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    lock.release();
                    System.err.println("TA release");
//                    client.delete().forPath(lockPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class TB extends Thread {

        private CuratorFramework client;
        private String lockPath;

        public TB(CuratorFramework client, String lockPath) {
            this.client = client;
            this.lockPath = lockPath;
        }

        @Override
        public void run() {
            System.err.println("TB start: " + LocalDateTime.now());
            InterProcessLock lock = new InterProcessMutex(client, lockPath);
            try {
                lock.acquire(5, TimeUnit.SECONDS);
                System.out.println("业务执行5秒钟");
                TimeUnit.SECONDS.sleep(5);

                System.err.println("TB end: " + LocalDateTime.now());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    lock.release();
                    System.err.println("TB release");
//                    client.delete().forPath(lockPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
