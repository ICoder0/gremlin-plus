package com.icoder0.gremlinplus.core.gremlin;

import com.icoder0.gremlinplus.annotation.GraphLabel;
import com.icoder0.gremlinplus.annotation.VertexId;
import com.icoder0.gremlinplus.annotation.VertexProperty;
import com.icoder0.gremlinplus.process.traversal.dsl.GraphPlusTraversalSource;
import com.icoder0.gremlinplus.process.traversal.function.SerializedFunction;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Test;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/**
 * @author bofa1ex
 * @since 2020/12/5
 */
public class GremlinPlusTest {

    @Test
    public void addVManually() {
        final TinkerGraph graph = TinkerGraph.open();
        try (GraphPlusTraversalSource traversal = graph.traversal(GraphPlusTraversalSource.class)) {
            final Vertex vertex = traversal.addV(User.class)
                    .property(User::getName, "bofa1ex")
                    .property(User::getAge, 21)
                    .next();
            vertex.values(SerializedFunction.method2Properties(User::getName, User::getAge)).forEachRemaining(System.out::println);
        } catch (Exception ignored) {
        }
    }

    @Test
    public void addVAutomatically() {
        final TinkerGraph graph = TinkerGraph.open();
        final User user = new User("bofa1ex", 21);
        try (GraphPlusTraversalSource traversal = graph.traversal(GraphPlusTraversalSource.class)) {
            final Vertex vertex = traversal.addV(user);
            final Object name = vertex.value(SerializedFunction.method2Property(User::getName));
            System.out.println(name);
        } catch (Exception ignored) {
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
            traversal.addV(new User("bofa1ex", 21));
            traversal.addV(new User("yl", 22));
            final List<Vertex> vertexes = traversal.V().hasLabel(User.class)
                    .has(User::getName, P.within("bofa1ex", "yl"))
                    .toList();
            vertexes.stream().map(vertex -> vertex.values(SerializedFunction.method2Properties(User::getName, User::getAge)))
                    .flatMap(iterator -> StreamSupport.stream(Spliterators.spliterator(iterator, 0, Spliterator.ORDERED), true))
                    .forEach(System.out::println);
            System.out.println("====================================");
            final Integer age = traversal.V().hasLabel(User.class)
                    .has(User::getName, "bofa1ex")
                    .value(User::getAge)
                    .next();
            System.out.println(age);
        } catch (Exception ignored) {
        }
    }

    @GraphLabel("<DEFAULT>")
    public static class DefaultEdge {

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
//        private WebsocketSession session;

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
    }
}
