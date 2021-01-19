package com.icoder0.gremlinplus;

import com.icoder0.gremlinplus.entity.edge.DefaultEdge;
import com.icoder0.gremlinplus.entity.edge.LockEdge;
import com.icoder0.gremlinplus.entity.vertex.*;
import com.icoder0.gremlinplus.process.extension.DefaultKeyGenerator;
import com.icoder0.gremlinplus.process.extension.DefaultUnSerializedPropertyCache;
import com.icoder0.gremlinplus.process.extension.DefaultVertexDefinitionCache;
import com.icoder0.gremlinplus.process.traversal.dsl.GraphPlusTraversalSource;
import com.icoder0.gremlinplus.process.traversal.toolkit.KeyGeneratorSupport;
import com.icoder0.gremlinplus.process.traversal.toolkit.UnSerializedPropertySupport;
import com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * @author bofa1ex
 * @since 2020/12/10
 */
public class Application {
    public static void main(String[] args) {
        final GraphFacade graphFacade = GraphFacade.INSTANCE;
//        KeyGeneratorSupport.init(DefaultKeyGenerator.getInstance());
//        KeyGeneratorSupport.init(DefaultKeyGenerator.class);
//        UnSerializedPropertySupport.init(DefaultUnSerializedPropertyCache.getInstance());
//        UnSerializedPropertySupport.init(DefaultUnSerializedPropertyCache.class);
//        VertexDefinitionSupport.init(DefaultVertexDefinitionCache.getInstance());
//        VertexDefinitionSupport.init(DefaultVertexDefinitionCache.class);
        final Object sessionPrototype = new Object();
        final Session session = Session.builder().build();
        final GraphPlusTraversalSource g = graphFacade.traversal();

        graphFacade.commit(_g -> {
            final Vertex userVertex = _g.addV(User.builder()
                    .pwd("admin")
                    .name("admin")
                    .status((byte) 1)
                    .isDelete((byte) 1)
                    .updateTime(LocalDateTime.now())
                    .createTime(LocalDateTime.now())
                    .build()
            );
            final Vertex botVertex0 = _g.addV(Contact.builder()
                    .name("小明")
                    .account(123L)
                    .pwd("123")
                    .avatarUrl("demo")
                    .prototype(null)
                    .parkThread(Thread.currentThread())
                    .permission((byte) 1)
                    .status((byte) 1)
                    .isDelete((byte) 1)
                    .lastOnlineTime(null)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build()
            );

            final Vertex botVertex1 = _g.addV(Contact.builder()
                    .name("小强")
                    .account(123L)
                    .pwd("123")
                    .avatarUrl("demo")
                    .prototype(null)
                    .parkThread(Thread.currentThread())
                    .permission((byte) 1)
                    .status((byte) 1)
                    .isDelete((byte) 1)
                    .lastOnlineTime(null)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build()
            );

            final Vertex groupVertex = _g.addV(Group.builder()
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

            final Vertex sessionVertex = _g.addV(session);
            final Vertex scriptVertex1 = _g.addV(Script.builder()
                    .content("demo")
                    .env("demo env")
                    .keywords(Arrays.asList("keyword1", "keyword2"))
                    .type(0)
                    .isDelete((byte) 1)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build()
            );
            final Vertex scriptVertex2 = _g.addV(Script.builder()
                    .content("demo 2")
                    .env("demo env 2")
                    .keywords(Arrays.asList("keyword1", "keyword3"))
                    .type(0)
                    .isDelete((byte) 1)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build()
            );
            _g.addE(DefaultEdge.class)
                    .from(userVertex)
                    .to(botVertex0)
                    .tryNext();
            _g.addE(DefaultEdge.class)
                    .from(userVertex)
                    .to(botVertex1)
                    .tryNext();
            _g.addE(DefaultEdge.class)
                    .from(botVertex0)
                    .to(groupVertex)
                    .tryNext();
            _g.addE(DefaultEdge.class)
                    .from(userVertex)
                    .to(sessionVertex)
                    .tryNext();
            _g.addE(DefaultEdge.class)
                    .from(userVertex)
                    .to(scriptVertex1)
                    .tryNext();
            _g.addE(DefaultEdge.class)
                    .from(userVertex)
                    .to(scriptVertex2)
                    .tryNext();
            _g.addE(DefaultEdge.class)
                    .from(botVertex1)
                    .to(scriptVertex1)
                    .tryNext();
            _g.addE(DefaultEdge.class)
                    .from(botVertex1)
                    .to(scriptVertex2)
                    .tryNext();
            _g.addE(LockEdge.class)
                    .from(botVertex1)
                    .to(sessionVertex)
                    .tryNext();
        });
        g.V().hasLabel(User.class)
                .has(User::getName, "admin")
                .out(DefaultEdge.class)
                .hasLabel(Contact.class)
                .has(Contact::getName, "小明")
                .toBeanList()
                .forEach(contact -> g.<Contact>V(contact.getId())
                        .property(Contact::getPrototype, new Bot(contact.getAccount(), contact.getPwd()))
                        .tryNext()
                );
        final List<Contact> contacts = g.V().hasLabel(User.class)
                .has(User::getName, "admin")
                .out(DefaultEdge.class)
                .hasLabel(Contact.class)
                .has(Contact::getName, "小明")
                .toBeanList();
        final Pair<User, Vertex> userPair = g.V().hasLabel(User.class).has(User::getName, "admin2").getIfAbsent(User.builder()
                .name("admin2")
                .build()
        );
        g.V().hasLabel(User.class)
                .has(User::getName, "admin")
                .out(DefaultEdge.class)
                .hasLabel(Session.class)
                .toBeanList()
                .forEach(_session -> g.<Session>V(_session.getId())
                        .property(Session::getPrototype, sessionPrototype)
                        .tryNext()
                );
        final List<Session> sessions = g.V().hasLabel(User.class)
                .has(User::getName, "admin")
                .out(DefaultEdge.class)
                .hasLabel(Session.class)
                .toBeanList();
        System.out.println("contacts = " + contacts);
        System.out.println("sessions = " + sessions);
        System.out.println("userPair.getKey() = " + userPair.getKey());
        graphFacade.destroy();
    }

    public static class Bot {
        private final Long account;
        private final String pwd;

        public Bot(Long account, String pwd) {
            this.account = account;
            this.pwd = pwd;
        }

        @Override
        public String toString() {
            return "Bot{" +
                    "account=" + account +
                    ", pwd='" + pwd + '\'' +
                    '}';
        }
    }
}
