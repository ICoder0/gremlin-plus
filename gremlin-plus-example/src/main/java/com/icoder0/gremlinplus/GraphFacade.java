package com.icoder0.gremlinplus;

import com.icoder0.gremlinplus.entity.vertex.Evict;
import com.icoder0.gremlinplus.process.traversal.dsl.GraphPlusTraversalSource;
import com.orientechnologies.common.log.OLogManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author bofa1ex
 * @since 2020/12/10
 */
@Slf4j
public class GraphFacade {

    public static final GraphFacade INSTANCE = new GraphFacade();

    @Getter(AccessLevel.PRIVATE)
    private final OrientGraph localOrient;

    private final OrientGraphFactory graphFactory;

    private final ThreadLocal<GraphPlusTraversalSource> traversalSources = new ThreadLocal<>();

    {
        graphFactory = new OrientGraphFactory("plocal:./data/orient");
        localOrient = graphFactory.getTx();
        // 关闭orientdb日志打印.
        OLogManager.instance().setDebugEnabled(false);
        OLogManager.instance().setInfoEnabled(false);
        OLogManager.instance().setErrorEnabled(false);
        commit(g -> {
            g.<Evict>V().has(Evict::isEvict).drop().iterate();
        });
    }

    public void commit(Consumer<GraphPlusTraversalSource> consumer) {
        for (int i = 0; i < 5; i++) {
            try {
                consumer.accept(_getTraversalSource());
                localOrient.commit();
                return;
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
        for (int i = 0; i < 5; i++) {
            try {
                final D resp = func.apply(_getTraversalSource());
                localOrient.commit();
                return resp;
            } catch (Exception e) {
                log.error("local orientdb commit执行异常", e);
                if (i == 4) {
                    log.warn("持久落地失败, 请检查数据");
                }
                localOrient.rollback();
                localOrient.begin();
            }
        }
        return null;
    }

    public GraphPlusTraversalSource traversal() {
        return _getTraversalSource();
    }

    private GraphPlusTraversalSource _getTraversalSource() {
        GraphPlusTraversalSource traversalSource = traversalSources.get();
        if (Objects.isNull(traversalSource)) {
            traversalSource = localOrient.traversal(GraphPlusTraversalSource.class);
            traversalSources.set(traversalSource);
        }
        return traversalSource;
    }

    public void destroy() {
        localOrient.close();
        graphFactory.close();
    }
}
