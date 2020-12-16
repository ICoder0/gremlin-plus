package com.icoder0.gremlinplus;

import com.icoder0.gremlinplus.process.traversal.dsl.GraphPlusTraversal;
import com.icoder0.gremlinplus.process.traversal.dsl.GraphPlusTraversalSource;
import com.orientechnologies.common.log.OLogManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalSource;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author bofa1ex
 * @since 2020/12/10
 */
@Slf4j
public class GraphFacade {

    public static final GraphFacade INSTANCE = new GraphFacade();

    /**
     * 内存图数据库不支持事务, 因此建议db操作先执行
     */
    @Getter(AccessLevel.PRIVATE)
    private final OrientGraph memoryOrient;

    @Getter(AccessLevel.PRIVATE)
    private final OrientGraph localOrient;

    private final ThreadLocal<Pair<GraphPlusTraversalSource, GraphPlusTraversalSource>> traversalSources = new ThreadLocal<>();

    {
        localOrient = new OrientGraphFactory("plocal:./data/orient").getTx();
        memoryOrient = new OrientGraphFactory("memory:orient").getTx();
        // 关闭orientdb日志打印.
        OLogManager.instance().setDebugEnabled(false);
        OLogManager.instance().setInfoEnabled(false);
        OLogManager.instance().setErrorEnabled(false);
    }

    public void commit(Consumer<GraphPlusTraversalSource> consumer) {
        consumer.accept(_getTraversalSource());
        for (int i = 0; i < 5; i++) {
            try {
                consumer.accept(_getTraversalSource(false));
                localOrient.commit();
                break;
            } catch (Exception e) {
                log.error("local orientdb commit执行异常", e);
                if (i == 4) {
                    log.warn("持久落地失败, 请检查数据");
                }
                localOrient.rollback();
                localOrient.begin();
            }
        }
    }

    public <D> D commit(Function<GraphPlusTraversalSource, D> func) {
        D resp = null;
        resp = func.apply(_getTraversalSource());
        for (int i = 0; i < 5; i++) {
            try {
                func.apply(_getTraversalSource(false));
                localOrient.commit();
            } catch (Exception e) {
                log.error("local orientdb commit执行异常", e);
                if (i == 4) {
                    log.warn("{} 持久落地失败, 请检查数据", resp);
                }
                localOrient.rollback();
                localOrient.begin();
            }
        }
        return resp;
    }

    public GraphPlusTraversalSource traversal() {
        return _getTraversalSource();
    }

    private GraphPlusTraversalSource _getTraversalSource() {
        return _getTraversalSource(true);
    }

    private GraphPlusTraversalSource _getTraversalSource(boolean memory) {
        Pair<GraphPlusTraversalSource, GraphPlusTraversalSource> traversalSourcePair = traversalSources.get();
        if (Objects.isNull(traversalSourcePair)) {
            traversalSourcePair = Pair.of(
                    memoryOrient.traversal(GraphPlusTraversalSource.class),
                    localOrient.traversal(GraphPlusTraversalSource.class).supportSerializable()
            );
            traversalSources.set(traversalSourcePair);
        }
        return memory ? traversalSourcePair.getLeft() : traversalSourcePair.getRight();
    }

    public void destroy() {
        memoryOrient.close();
        localOrient.close();
    }
}
