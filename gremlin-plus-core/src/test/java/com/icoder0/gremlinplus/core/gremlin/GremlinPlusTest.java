package com.icoder0.gremlinplus.core.gremlin;

import com.icoder0.gremlinplus.annotation.GraphLabel;
import com.icoder0.gremlinplus.annotation.VertexId;
import com.icoder0.gremlinplus.annotation.VertexProperty;
import com.icoder0.gremlinplus.process.traversal.dsl.GraphPlusTraversalSource;
import com.icoder0.gremlinplus.process.traversal.function.SerializedFunction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Test;

import java.util.List;


/**
 * @author bofa1ex
 * @since 2020/12/5
 */
public class GremlinPlusTest {

    @Test
    public void addVManually() {
        final TinkerGraph graph = TinkerGraph.open();
        try (GraphPlusTraversalSource traversal = graph.traversal(GraphPlusTraversalSource.class)) {
             final User user = traversal.addV(User.class)
                    .property(User::getName, "bofa1ex")
                    .property(User::getAge, 21)
                    .property(User::getParkThread, Thread.currentThread())
                    .toBean();
            System.out.println(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void addVAutomatically() {
        final TinkerGraph graph = TinkerGraph.open();
        final User user = new User("bofa1ex", 21);
        try (GraphPlusTraversalSource traversal = graph.traversal(GraphPlusTraversalSource.class)) {
            final Vertex vertex = traversal.addV(user);
            System.out.println(user);
            vertex.values(SerializedFunction.method2Properties(User::getName)).forEachRemaining(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void addEManually() {
        final TinkerGraph graph = TinkerGraph.open();
        final User u1 = new User("bofa1ex", 21);
        final User u2 = new User("yl", 22);
        try (GraphPlusTraversalSource traversal = graph.traversal(GraphPlusTraversalSource.class)) {
            final Edge edge = traversal.addE(DefaultEdge.class)
                    .from(traversal.addV(u1))
                    .to(traversal.addV(u2))
                    .next();
            final Vertex inVertex = edge.inVertex();
            final Vertex outVertex = edge.outVertex();
            inVertex.values(SerializedFunction.method2Properties(User::getName, User::getAge)).forEachRemaining(System.out::println);
            System.out.println();
            outVertex.values(SerializedFunction.method2Properties(User::getName, User::getAge)).forEachRemaining(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void vProperty() {
        final TinkerGraph graph = TinkerGraph.open();
        try (GraphPlusTraversalSource traversal = graph.traversal(GraphPlusTraversalSource.class)) {
            traversal.addE(DefaultEdge.class)
                    .from(traversal.addV(new User("bofa1ex", 21)))
                    .to(traversal.addV(new User2("yl")))
                    .next();
            System.out.println("====================================");

            final User2 user = traversal.V().hasLabel(User.class)
                    .has(User::getName, "bofa1ex")
                    .out(DefaultEdge.class)
                    .hasLabel(User2.class)
                    .has(User2::getName, "yl")
                    .toBean();
            System.out.println(user);
        } catch (Exception ignored) {
        }
    }

    @GraphLabel("<DEFAULT>")
    public static class DefaultEdge {

    }

    @GraphLabel("<USER2>")
    public static class User2{
        @VertexId
        private Object id;
        @VertexProperty("[NAME]")
        private String name;

        public User2() {
        }

        public User2(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Object getId() {
            return id;
        }

        public void setId(Object id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "User2{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    @GraphLabel("<USER>")
    public static class User {
        @VertexId
        private Object id;
        @VertexProperty("[NAME]")
        private String name;
        @VertexProperty("[AGE]")
        private Integer age;
        @VertexProperty(value = "[PARK_THREAD]", serializable = false)
        private Thread parkThread;

        public User() {
        }

        // add, drop, 优先操作内存缓存. 这些操作存在队列里面, 去做持久化.
        // http api
        public User(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public Object getId() {
            return id;
        }

        public void setId(Object id) {
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

        public Thread getParkThread() {
            return parkThread;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", age=" + age +
                    ", parkThread=" + parkThread +
                    '}';
        }
    }
}
