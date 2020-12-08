package com.icoder0.gremlinplus.process.traversal.definition;

/**
 * @author bofa1ex
 * @since 2020/12/5
 */
public class VertexPropertyDefinition {
    private String propertyName;
    private boolean serializable;
    private boolean primaryKey;

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public boolean isSerializable() {
        return serializable;
    }

    public static VertexPropertyDefinitionBuilder builder() {
        return new VertexPropertyDefinitionBuilder();
    }

    public static final class VertexPropertyDefinitionBuilder {
        private String propertyName;
        private boolean serializable;
        private boolean primaryKey;

        private VertexPropertyDefinitionBuilder() {
        }


        public VertexPropertyDefinitionBuilder withPropertyName(String propertyName) {
            this.propertyName = propertyName;
            return this;
        }

        public VertexPropertyDefinitionBuilder withPrimaryKey(boolean primaryKey) {
            this.primaryKey = primaryKey;
            return this;
        }

        public VertexPropertyDefinitionBuilder withSerializable(boolean serializable) {
            this.serializable = serializable;
            return this;
        }

        public VertexPropertyDefinition build() {
            VertexPropertyDefinition vertexPropertyDefinition = new VertexPropertyDefinition();
            vertexPropertyDefinition.serializable = this.serializable;
            vertexPropertyDefinition.primaryKey = this.primaryKey;
            vertexPropertyDefinition.propertyName = this.propertyName;
            return vertexPropertyDefinition;
        }
    }
}
