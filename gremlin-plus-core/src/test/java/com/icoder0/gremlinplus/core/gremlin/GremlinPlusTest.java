package com.icoder0.gremlinplus.core.gremlin;

import com.icoder0.gremlinplus.annotation.GraphLabel;
import com.icoder0.gremlinplus.annotation.VertexId;
import com.icoder0.gremlinplus.annotation.VertexProperty;
import com.icoder0.gremlinplus.process.traversal.dsl.GraphPlusTraversalSource;
import com.icoder0.gremlinplus.process.traversal.toolkit.SerializedFunctionSupport;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Test;

import java.util.List;
import java.util.Optional;


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
            vertex.values(SerializedFunctionSupport.method2Properties(User::getName)).forEachRemaining(System.out::println);
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
            final Optional<Edge> edgeOpt = traversal.addE(DefaultEdge.class)
                    .from(traversal.addV(u1))
                    .to(traversal.addV(u2))
                    .tryNext();
            edgeOpt.map(Edge::inVertex).map(vertex -> vertex.values(SerializedFunctionSupport.method2Properties(User::getName, User::getAge)))
                    .ifPresent(data -> data.forEachRemaining(System.out::println));
            edgeOpt.map(Edge::outVertex).map(vertex -> vertex.values(SerializedFunctionSupport.method2Properties(User::getName, User::getAge)))
                    .ifPresent(data -> data.forEachRemaining(System.out::println));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void vProperty() {
        final TinkerGraph graph = TinkerGraph.open();
        try (GraphPlusTraversalSource traversal = graph.traversal(GraphPlusTraversalSource.class)) {
            traversal.addE(DefaultEdge.class)
                    .from(traversal.addV(new User("bofa1ex", 21, Thread.currentThread())))
                    .to(traversal.addV(new User2("yl")))
                    .next();
            System.out.println("====================================");
            traversal.V().hasLabel(User.class)
                    .has(User::getName, "admin")
                    .toBean();
//
//            final User user = traversal.V().hasLabel(User.class)
//                    .has(User::getName, "bofa1ex")
//                    .toBean();
//
//            final Thread parkThread = traversal.V().hasLabel(User.class)
//                    .has(User::getName, "bofa1ex")
//                    .value(User::getParkThread)
//                    .tryNext().orElse(null);
//
//            final User2 user2 = traversal.V().hasLabel(User.class)
//                    .has(User::getName, "bofa1ex")
//                    .out(DefaultEdge.class)
//                    .hasLabel(User2.class)
//                    .has(User2::getName, "yl")
//                    .toBean();
//            System.out.println(user);
//            System.out.println(parkThread);
//            System.out.println(user2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void newTest() {
        final TinkerGraph graph = TinkerGraph.open();
        try (GraphPlusTraversalSource g = graph.traversal(GraphPlusTraversalSource.class)) {
            final List<Pair<User, Vertex>> pairs = g.addV(User.class).property(User::getParkThread, Thread.currentThread())
                    .property(User::getAge, 23)
                    .toPairList();
            System.out.println(pairs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @GraphLabel("<DEFAULT>")
    public static class DefaultEdge {

    }

    @GraphLabel("<USER2>")
    public static class User2 {
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
        @VertexProperty(value = "[NAME]", serializable = false)
        private String name;
        @VertexProperty(value = "[AGE]", serializable = false)
        private Integer age;
        @VertexProperty(value = "[PARK_THREAD]", serializable = false)
        private Thread parkThread;

        public User() {
        }

        public User(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public User(String name, Integer age, Thread parkThread) {
            this.name = name;
            this.age = age;
            this.parkThread = parkThread;
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

        public void setParkThread(Thread parkThread) {
            this.parkThread = parkThread;
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
