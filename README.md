# gremlin-plus
<p align="center">
 <h2 align="center">Gremlin plus</h2>
</p>

<p align="center">
  <img alt="AUR license" src="https://img.shields.io/aur/license/intellij-idea-ce">
  <img alt="GitHub Repo stars" src="https://img.shields.io/github/stars/icoder0/gremlin-plus?style=social">
  <img alt="GitHub forks" src="https://img.shields.io/github/forks/icoder0/gremlin-plus?style=social">
</p>

解决常量类管理vertex/edge#label以及property的问题.
看了相关的ogm(ferma、spring-data-xx)的api和思路.

个人倾向mybatis-plus的api, jpa方面声明式的api对于图数据库的遍历不够灵活. 
> mybatis-plus的serializedLambda内部resolve方法不支持evaluate expression调试, 该问题在gremlin-plus已解决.

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
        // or u wanna convert bean directly.
        final User user = g.V().hasLabel(User.class).has(User::getName, "bofa1ex").toBean();
        // do sth...
    } catch (Exception ignored) {
    }
  }
}
```
更多samples参见example模块, 若有更多需求, 欢迎提issue/pr.

[![Anurag's github stats](https://github-readme-stats.vercel.app/api?username=bofa1ex&hide=contribs,prs&show_icons=true&theme=radical)](https://github.com/anuraghazra/github-readme-stats)
