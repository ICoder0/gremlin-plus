package com.icoder0.entity;

import com.icoder0.gremlinplus.annotation.GraphLabel;
import com.icoder0.gremlinplus.annotation.VertexId;
import com.icoder0.gremlinplus.annotation.VertexProperty;
import net.sf.cglib.beans.BeanMap;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


/**
 * @author bofa1ex
 * @since 2020/12/4
 * <p>
 *
 *   ex: traversal.V().hasLabel('<BOT>').has('[ACCOUNT]', 11111).in('<DEFAULT>').hasLabel('<USER>').toList();
 * * List<Vertex> userVertexes = vertexMapper.traversal(Vertex.<BotVertex>query()
 * *      .eq(BotVertex::getAccount, 11111L)
 * *      .<DefaultEdge, UserVertex>inV()
 * *      .toList()
 * * );
 * *
 * * ex: traversal.V().hasLabel('<BOT>').has('[ACCOUNT]', 11111).in('<DEFAULT>').hasLabel('<USER>').values('[ACCOUNT]','[PWD]').toList();
 * * List<BotVertex> userVertexes = vertexMapper.traversal(Vertex.<BotVertex>query()
 * *       .eq(BotVertex::getAccount, 11111L)
 * *       .<UserVertex>inV()
 * *       .select(BotVertex::getAccount, BotVertex::getPwd)
 * *       .toList()
 * * );
 * *
 * * ex: traversal.addV('<BOT>').property('[ACCOUNT]', 111111).property('[PWD]', "123123123").next();
 *
 * private VertexMapper vertexMapper;
 * private EdgeMapper edgeMapper;
 *
 * vertexMapper.traversal() 根据入参类型决定返参数类型(比如VertexQuery<T>, VertexPropertyQuery<T>)
 *
 * vertexMapper.insert(user);
 * vertexMapper.<XXVertex>update()
 *      .eq(xxVertex::getId, id)
 *      .set(xxVertex::getXx, xx)
 * );
 * vertexMapper.deleteById();
 *
 *
 *
 *   Vertex vertex = vertexMapper.insert(userVertex);
 *
 * * Vertex newVertex = vertexMapper.insert(Vertex.<BotVertex>builder()
 * *      .property(BotVertex::getAccount, 111111L)
 * *      .property(BotVertex::getPwd, "123123123")
 * *      ...
 * *      .build()
 * * );
 *
 * * ex: traversal.addE('<DEFAULT>').from(newVertex).to(__.V().hasLabel('<BOT>').has('[ACCOUNT]', 123123)).next()
 * * edgeMapper.insert(Edge.<DefaultEdge>builder()
 * *      .from(newVertex)
 * *      .to(__.<BotVertex>query().eq(BotVertex::getAccount, 123123L)
 * *      .build();
 */
@GraphLabel("<USER>")
public class User {
    public interface TraversalDefinition {

    }

    public static class VertexTraversalDefinition {
        public Vertex insert() {
            Graph graph = null;
            final GraphTraversalSource traversal = new GraphTraversalSource(graph);
            final User user = null;
            final Map<Class<?>, BeanMap> vertexBeanMap = new HashMap<>();

            final BeanMap userBeanMap = BeanMap.create(user);
            final GraphTraversal<Vertex, Vertex> peek = traversal.addV();
            for (Object key : userBeanMap.keySet()) {
                peek.property(key, userBeanMap.get(key));
            }
            final Vertex vertex = peek.next();
            userBeanMap.put("id", vertex.id());
            return vertex;
        }
    }

    public static class EdgeTraversalDefinition {

    }


    public static class VertexDefinition {
        private String label;
        /* key:field_name/getter_method_name key: propertyDefinition */
        private Map<String, User.VertexPropertyDefinition> propertyDefinitions;
    }

    public static class VertexPropertyDefinition {
        private String propertyName;
        private boolean primaryKey;
        private boolean serializable;
    }


    @VertexId
    private Object id;

    @VertexProperty("[NAME]")
    private String name;

    @VertexProperty("[PWD]")
    private String pwd;

    @VertexProperty("[PHONE_NUM]")
    private String phoneNum;

    @VertexProperty("[MAIL]")
    private String mail;

    @VertexProperty("[IS_DELETE]")
    private Byte isDelete;

    @VertexProperty("[STATUS]")
    private Byte status;

    @VertexProperty("[CREATE_TIME]")
    private LocalDateTime createTime;

    @VertexProperty("[UPDATE_TIME]")
    private LocalDateTime updateTime;
}
