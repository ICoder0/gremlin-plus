package com.icoder0.gremlinplus;

import com.icoder0.gremlinplus.process.traversal.dsl.GraphPlusTraversal;
import com.icoder0.gremlinplus.process.traversal.dsl.GraphPlusTraversalSource;
import com.orientechnologies.common.log.OLogManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;

import java.util.Objects;
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

    private final OrientGraphFactory localOrientGraphFactory;

    private final ThreadLocal<OrientGraph> localOrientGraphs = new ThreadLocal<>();

    {
        localOrientGraphFactory = new OrientGraphFactory("plocal:./data/orient");
        memoryOrient = new OrientGraphFactory("memory:orient").getTx();
        // 关闭orientdb日志打印.
        OLogManager.instance().setDebugEnabled(false);
        OLogManager.instance().setInfoEnabled(false);
        OLogManager.instance().setErrorEnabled(false);
    }

    public <D> D traversalCommit(Function<GraphPlusTraversalSource, D> func) {
        D resp = null;
        try (final GraphPlusTraversalSource traversal = _getOrientGraph().traversal(GraphPlusTraversalSource.class)) {
            resp = func.apply(traversal);
        } catch (Exception e) {
            log.error("memory orientdb 执行异常", e);
        }
        final OrientGraph localGraph = _getOrientGraph(false);
        for (int i = 0; i < 5; i++) {
            try (final GraphPlusTraversalSource traversal = localGraph.traversal(GraphPlusTraversalSource.class).supportSerializable()) {
                func.apply(traversal);
                localGraph.commit();
                break;
            } catch (Exception e) {
                log.error("local orientdb commit执行异常", e);
                if (i == 4){
                    log.warn("{} 持久落地失败, 请检查数据", resp);
                }
                localGraph.rollback();
                localGraph.begin();
            }
        }
        return resp;
    }

    public <D> D traversal(Function<GraphPlusTraversalSource, D> func) {
        D resp = null;
        try (final GraphPlusTraversalSource traversal = _getOrientGraph().traversal(GraphPlusTraversalSource.class)) {
            resp = func.apply(traversal);
        } catch (Exception e) {
            log.error("memory orientdb 执行异常", e);
        }
        return resp;
    }

    private OrientGraph _getOrientGraph() {
        return _getOrientGraph(true);
    }

    private OrientGraph _getOrientGraph(boolean memory) {
        if (memory) {
            return memoryOrient;
        }
        OrientGraph orientGraph = localOrientGraphs.get();
        if (Objects.isNull(orientGraph)) {
            orientGraph = localOrientGraphFactory.getTx();
            localOrientGraphs.set(orientGraph);
        }
        return orientGraph;
    }

    public void destroy(){
        memoryOrient.close();
        localOrientGraphFactory.close();
    }
}
