package com.icoder0.gremlinplus.process.traversal.definition;

import net.sf.cglib.beans.BeanMap;

import java.util.Map;

/**
 * @author bofa1ex
 * @since 2020/12/5
 */
public class VertexDefinition {
    private String label;
    private BeanMap beanMap;
    // key: field_name, value:
    private Map<String, VertexPropertyDefinition> vertexPropertyDefinitionMap;

    public String getLabel() {
        return label;
    }

    public BeanMap getBeanMap() {
        return beanMap;
    }

    public Map<String, VertexPropertyDefinition> getVertexPropertyDefinitionMap() {
        return vertexPropertyDefinitionMap;
    }

    public static VertexDefinitionBuilder builder() {
        return new VertexDefinitionBuilder();
    }

    public static final class VertexDefinitionBuilder {
        private String label;
        // key: field_name, value:
        private Map<String, VertexPropertyDefinition> vertexPropertyDefinitionMap;
        private BeanMap beanMap;

        private VertexDefinitionBuilder() {
        }

        public VertexDefinitionBuilder withBeanMap(BeanMap beanMap) {
            this.beanMap = beanMap;
            return this;
        }

        public VertexDefinitionBuilder withLabel(String label) {
            this.label = label;
            return this;
        }

        public VertexDefinitionBuilder withVertexPropertyDefinitionMap(Map<String, VertexPropertyDefinition> vertexPropertyDefinitionMap) {
            this.vertexPropertyDefinitionMap = vertexPropertyDefinitionMap;
            return this;
        }

        public VertexDefinition build() {
            VertexDefinition vertexDefinition = new VertexDefinition();
            vertexDefinition.label = this.label;
            vertexDefinition.beanMap = this.beanMap;
            vertexDefinition.vertexPropertyDefinitionMap = this.vertexPropertyDefinitionMap;
            return vertexDefinition;
        }
    }
}
