package com.icoder0.gremlinplus.core.cglib;

import net.sf.cglib.beans.BeanMap;
import org.junit.Test;

import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.StampedLock;

/**
 * @author bofa1ex
 * @since 2020/12/4
 */
public class BeanMapTest {

    @Test
    public void concurrentTest() throws InterruptedException {
        final Person person1 = new Person("bofa13x", 21);
        final Person person2 = new Person("yl", 22);
        final BeanMap beanMap = BeanMap.create(person1);
        final CountDownLatch order = new CountDownLatch(2);
        final Semaphore semaphore = new Semaphore(0);
        new Thread(() -> {
            try {
                order.countDown();
                order.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            final Object p1Name = beanMap.get(person1, "name");
            System.out.printf("time: %s\t %s%n", LocalTime.now(), p1Name);
            beanMap.put(person1, "name", "surprised!");
            semaphore.release();
        }).start();

        new Thread(() -> {
            try {
                order.countDown();
                order.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            final Object p2Name = beanMap.get(person2, "name");
            System.out.printf("time: %s\t %s%n", LocalTime.now(), p2Name);
            beanMap.put(person2, "name", "surprised!!!!");
            semaphore.release();
        }).start();

        semaphore.acquire(2);
        System.out.println(person1);
        System.out.println(person2);
    }

    @Test
    public void test() {
        final Person person = new Person("bofa1ex", 21);
        final Person person2 = new Person("ch", 25);
        final BeanMap beanMap = BeanMap.create(person);

        beanMap.put("name", "yl");
        beanMap.put("id", 23L);

        beanMap.setBean(person2);
        beanMap.put("name", "chex");
        System.out.println(person);
        System.out.println(person2);
    }


    public static class Person {

        public Person(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        private Long id;
        private String name;
        private Integer age;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
