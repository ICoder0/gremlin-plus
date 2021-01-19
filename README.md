<p align="center">
 <h2 align="center">Gremlin plus</h2>
</p>

<p align="center">
  <img alt="AUR license" src="https://img.shields.io/aur/license/intellij-idea-ce">
  <img alt="GitHub Repo stars" src="https://img.shields.io/github/stars/icoder0/gremlin-plus?style=social">
  <img alt="GitHub forks" src="https://img.shields.io/github/forks/icoder0/gremlin-plus?style=social">
</p>

> 主要解决以下问题
> - 释放对Vertex/Edge/Property标签MAGIC_CODE管理.
> - 简化Vertex/Edge对应实体类的Convert/Mapping逻辑.
> - 提供Vertex/Edge对应非序列化字段的对象缓存机制, 减少客户端显式声明多个缓存管理的开发成本开销, 并提供KeyGenerator/UnSerializedPropertyCache扩展插件接口.

参见相关的ogm(ferma、mybatis-plus、spring-data-xx)源码.

个人倾向mybatis-plus的api, jpa方面声明式的api对于图数据库的遍历不够灵活. 

> mybatis-plus的serializedLambda内部resolve方法不支持evaluate expression调试, 该问题在gremlin-plus中已提供解决方案.

```java
@VertexLabel("<USER>")
public class User {
  @VertexProperty("[NAME]")
  private String name;
  @VertexProperty("[AGE]")
  private Long age;
}

public class Solution {

  public static void main(String[] args) {
    Graph graph = null;
    try (final GraphPlusTraversalSource g = graph.traversal(GraphPlusTraversalSource.class)) {
        g.addV(new User("bofa1ex", 23L));
        // or g.addV(User.class).property(User::getName, "bofa1ex").property(User::getAge, 23L).property(TheOther::Sth, "sth else");
        final Vertex vertex = g.V().hasLabel(User.class).has(User::getName, "bofa1ex").next();
        // or u wanna convert vertex/edge to bean directly.
        final User user = g.V().hasLabel(User.class).has(User::getName, "bofa1ex").toBean();
        // maybe need optional object which wrapper the destinition obj.
        final Optional<User> userOpt = g.V().hasLabel(User.class).has(User::getName, "bofa1ex").tryToBean();
        // maybe need beans collection/stream in normal.
        final List<User> users = g.V().hasLabel(User.class).has(User::getName, "bofa1ex").toBeanList();
        // maybe when u query the bean which is not exists, and u should try add it like that in below.
        final Optional<User> anonymousUserOpt = g.V().hasLabel(User.class).has(User::getName, "anonymous").tryToBean();
        if(!anonymousUserOpt.isPresent()){
            g.addV(User.builder().name("anonymous").build());
        }
        // but `gremlin-plus` supported `#getIfAbsent` api, u just need try it in the same situation in below.
        Pair<User,Vertex> userVertexPair = g.V().hasLabel(User.class).has(User::getName, "anonymous").getIfAbsent(
            User.builder().name("anonymous").build()
        );
        // u might be confused by this return value, why use tuple with bean and vertex, talk is cheap, see the code in below.
        g.addE(Follow.class).from(g.V().hasLabel(User.class).has(User::getName, "bofa1ex")).to(
            // when u wanna add edge from the vertex to the other vertex which maybe not exists.
            userVertexPair.getRight()
        ).tryNext();
        
    } catch (Exception ignored) {
    }
  }
}
```
更多samples参见example模块, 若有更多需求, 欢迎提issue/pr.

[![Anurag's github stats](https://github-readme-stats.vercel.app/api?username=bofa1ex&hide=stars,contribs,prs&show_icons=true&theme=radical)](https://github.com/anuraghazra/github-readme-stats)
