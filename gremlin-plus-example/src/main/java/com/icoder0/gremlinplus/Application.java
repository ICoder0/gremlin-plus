package com.icoder0.gremlinplus;

import com.icoder0.gremlinplus.entity.edge.DefaultEdge;
import com.icoder0.gremlinplus.entity.edge.LockEdge;
import com.icoder0.gremlinplus.entity.vertex.*;
import com.icoder0.gremlinplus.process.traversal.dsl.P;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author bofa1ex
 * @since 2020/12/10
 */
public class Application {
    public static void main(String[] args) {
        final GraphFacade graphFacade = GraphFacade.INSTANCE;
        final Object sessionPrototype = new Object();
        final Session session = Session.builder()
                .prototype(sessionPrototype)
                .build();
        graphFacade.traversalCommit(g -> {
            final Vertex userVertex = g.addV(User.builder()
                    .pwd("admin")
                    .name("admin")
                    .status((byte) 1)
                    .isDelete((byte) 1)
                    .updateTime(LocalDateTime.now())
                    .createTime(LocalDateTime.now())
                    .build()
            );
            final Vertex botVertex1 = g.addV(Contact.builder()
                    .name("小明")
                    .account(123L)
                    .pwd("123")
                    .avatarUrl("demo")
                    .prototype(new Object())
                    .parkThread(Thread.currentThread())
                    .permission((byte) 1)
                    .status((byte) 1)
                    .isDelete((byte) 1)
                    .lastOnlineTime(null)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build()
            );
            final Vertex botVertex2 = g.addV(Contact.builder()
                    .name("小强")
                    .account(123L)
                    .pwd("123")
                    .avatarUrl("demo")
                    .prototype(new Object())
                    .parkThread(Thread.currentThread())
                    .permission((byte) 1)
                    .status((byte) 1)
                    .isDelete((byte) 1)
                    .lastOnlineTime(null)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build()
            );
            final Vertex groupVertex = g.addV(Group.builder()
                    .name("房间1")
                    .groupSetting(GroupSetting.builder()
                            .entranceAnnouncement("demo")
                            .build()
                    )
                    .account(123L)
                    .avatarUrl("demo")
                    .isDelete((byte) 1)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build()
            );

            final Vertex sessionVertex = g.addV(session);
            final Vertex scriptVertex1 = g.addV(Script.builder()
                    .content("demo")
                    .env("demo env")
                    .keywords(Arrays.asList("keyword1", "keyword2"))
                    .type(0)
                    .isDelete((byte) 1)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build()
            );
            final Vertex scriptVertex2 = g.addV(Script.builder()
                    .content("demo 2")
                    .env("demo env 2")
                    .keywords(Arrays.asList("keyword1", "keyword3"))
                    .type(0)
                    .isDelete((byte) 1)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build()
            );
            g.addE(DefaultEdge.class)
                    .from(userVertex)
                    .to(botVertex1)
                    .tryNext();
            g.addE(DefaultEdge.class)
                    .from(userVertex)
                    .to(botVertex2)
                    .tryNext();
            g.addE(DefaultEdge.class)
                    .from(botVertex1)
                    .to(botVertex2)
                    .tryNext();
            g.addE(DefaultEdge.class)
                    .from(botVertex1)
                    .to(groupVertex)
                    .tryNext();
            g.addE(DefaultEdge.class)
                    .from(userVertex)
                    .to(sessionVertex)
                    .tryNext();
            g.addE(DefaultEdge.class)
                    .from(userVertex)
                    .to(scriptVertex1)
                    .tryNext();
            g.addE(DefaultEdge.class)
                    .from(userVertex)
                    .to(scriptVertex2)
                    .tryNext();
            g.addE(DefaultEdge.class)
                    .from(botVertex1)
                    .to(scriptVertex1)
                    .tryNext();
            g.addE(DefaultEdge.class)
                    .from(botVertex2)
                    .to(scriptVertex2)
                    .tryNext();
            g.addE(LockEdge.class)
                    .from(botVertex1)
                    .to(sessionVertex)
                    .tryNext();
            return null;
        });
        final User user = graphFacade.traversal(g -> g.V().hasLabel(User.class).has(User::getName, "admin").toBean());
        // 获取Proxy对象中的此字段的值
        final List<Contact> contacts = graphFacade.traversal(g ->
                g.V().hasLabel(Session.class)
                        .has(Session::getPrototype, sessionPrototype)
                        .in(DefaultEdge.class)
                        .hasLabel(User.class)
                        .out(DefaultEdge.class)
                        .hasLabel(Contact.class)
                        .toBeanList()
        );
        System.out.println(user);
        System.out.println(contacts);
        final List<Script> scripts = graphFacade.traversal(g ->
                g.V().hasLabel(User.class).has(User::getName, "123")
                        .out(DefaultEdge.class)
                        .hasLabel(Script.class)
                        .has(Script::getKeywords, P.flatWithin("keyword2", "keyword3"))
                        .toBeanList()
        );

        final Optional<Script> script = graphFacade.traversal(g ->
                g.V().hasLabel(User.class).has(User::getName, "123")
                        .out(DefaultEdge.class)
                        .hasLabel(Script.class)
                        .has(Script::getKeywords, P.flatWithin("keyword2", "keyword3"))
                        .tryToBean()
        );
        System.out.println(scripts);
        System.out.println(script);
        graphFacade.destroy();
    }
}
