/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.smithy.codegen.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import software.amazon.smithy.model.SmithyBuilder;
import software.amazon.smithy.model.ToSmithyBuilder;

/**
 * A "symbol" is created by a {@link SymbolProvider}, and represents the
 * qualified name of a type in a target programming language.
 *
 * <p>A symbol contains an optional namespace, optional namespace
 * delimiter, name, a map of additional properties, a declaration file
 * the determines where the symbol is declared, and a definition file that
 * determines where a symbol is defined.
 *
 * <p>A namespace can be used when the target language supports namespaces.
 * The provided namespace can be in whatever format is useful to the target
 * language. A namespace delimiter is injected between the namespace and the
 * name when creating the fully-qualified symbol name. The "name" is the
 * unqualified name of the symbol (e.g., "str", or "MyShape").
 *
 * <p>Additional properties can be included when it's useful to provide
 * more information about a symbol. For example, it might be useful to
 * identify the name of a dependency that needs to be pulled in through a
 * package manager when a symbol is used.
 *
 * <p>The following example shows how a Java type could be made into a symbol:
 *
 * <pre>
 * {@code
 * Class<Symbol> klass = Symbol.class;
 * Symbol symbol = Symbol.builder()
 *         .namespace(klass.getPackage().toString(), ".")
 *         .name(klass.getSimpleName())
 *         .build();
 * System.out.println(symbol);
 * // ^ outputs "software.amazon.smithy.codegen.Symbol"
 * System.out.println(symbol.relativize("software.amazon.smithy.codegen");
 * // ^ outputs "Symbol"
 * }
 * </pre>
 */
public final class Symbol implements ToSmithyBuilder<Symbol> {
    private final String namespace;
    private final String namespaceDelimiter;
    private final String name;
    private final String definitionFile;
    private final String declarationFile;
    private final Map<String, Object> properties;

    private Symbol(Builder builder) {
        this.namespace = builder.namespace;
        this.namespaceDelimiter = builder.namespaceDelimiter;
        this.name = builder.name;
        this.declarationFile = builder.declarationFile;
        this.definitionFile = !builder.definitionFile.isEmpty() ? builder.definitionFile : declarationFile;
        this.properties = Map.copyOf(builder.properties);
    }

    /**
     * Creates a new Symbol builder.
     *
     * @return Returns the created symbol builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Provides the namespace of the symbol or "" if empty.
     *
     * @return Returns the optional namespace.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Provides the namespace delimiter of the symbol or "" if empty.
     *
     * <p>This delimiter is injected between the namespace and name
     * when creating the full name.
     *
     * @return Returns the optional namespace.
     */
    public String getNamespaceDelimiter() {
        return namespaceDelimiter;
    }

    /**
     * Gets the unqualified name of the symbol, that is, a name with
     * namespace.
     *
     * @return Returns the name of the symbol.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the location/filename in which the symbol is declared.
     *
     * <p>Code generators should write the generated code for this
     * symbol's declaration in a file with the same name that is
     * returned from this method. Not all languages separate a symbol's
     * definition from its declaration. This method is useful for things
     * like like C and C++ header files.
     *
     * <p>This method returns an empty string if no value was provided
     * in the builder.
     *
     * @return The name of the file the symbol is declared.
     */
    public String getDeclarationFile() {
        return declarationFile;
    }

    /**
     * Gets the location/filename in which the symbol is defined.
     *
     * <p>Code generators should write the generated code for a
     * symbol in a file with the same name that is returned from this
     * method.
     *
     * <p>This method returns an empty string if no value was provided
     * for either the declaration file or the definition file.
     *
     * @return The name of the file the symbol is defined.
     */
    public String getDefinitionFile() {
        return definitionFile;
    }

    /**
     * Gets the additional properties of the symbol.
     *
     * @return Returns a map of additional property strings.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Gets a specific property if present.
     *
     * @param name Property to retrieve.
     * @return Returns the optionally found property.
     */
    public Optional<Object> getProperty(String name) {
        return Optional.ofNullable(properties.get(name));
    }

    /**
     * Gets an additional property of a specific type.
     *
     * @param name Name of the property to get.
     * @param type Type of value to expect.
     * @param <T> Type of value to expect.
     * @return Returns a map of additional property strings.
     * @throws IllegalArgumentException if the value is not of the given type.
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getProperty(String name, Class<T> type) {
        return getProperty(name)
                .map(value -> {
                    if (!type.isInstance(value)) {
                        throw new IllegalArgumentException(String.format(
                                "Symbol property `%s` of `%s` is not an instance of `%s`",
                                name, this, type.getName()));
                    }
                    return (T) value;
                });
    }

    /**
     * Gets a specific additional property or throws if missing.
     *
     * @param name Property to retrieve.
     * @return Returns the found property.
     * @throws IllegalArgumentException if the property is not present.
     */
    public Object expectProperty(String name) {
        return getProperty(name).orElseThrow(() -> new IllegalArgumentException(String.format(
                "Property `%s` is not part of Symbol, `%s`", name, this)));
    }

    /**
     * Gets a specific additional property or throws if missing or if the
     * property is not an instance of the given type.
     *
     * @param name Property to retrieve.
     * @param type Type of value to expect.
     * @param <T> Type of value to expect.
     * @return Returns the found property.
     * @throws IllegalArgumentException if the property is not present.
     * @throws IllegalArgumentException if the value is not of the given type.
     */
    public <T> T expectProperty(String name, Class<T> type) {
        return getProperty(name, type).orElseThrow(() -> new IllegalArgumentException(String.format(
                "Property `%s` is not part of Symbol, `%s`", name, this)));
    }

    /**
     * Gets the full name of the symbol.
     *
     * <p>The full name is the concatenation of the namespace,
     * the namespace delimiter, and the name.
     *
     * @return Returns the fully qualified name of the symbol.
     */
    public String getFullName() {
        return toString();
    }

    /**
     * Creates a relativized Symbol for the given namespace.
     *
     * <p>If this symbol is in the same namespace as the provided namespace,
     * then only the symbol name is returned. Otherwise, the fully-qualified
     * symbol is returned.
     *
     * @param namespace Namespace to relativize against.
     * @return Returns the relativized symbol.
     */
    public String relativize(String namespace) {
        return this.namespace.equals(namespace) ? name : toString();
    }

    @Override
    public Builder toBuilder() {
        Builder builder = new Builder();
        return builder.namespace(namespace, namespaceDelimiter)
                .name(name)
                .properties(properties)
                .definitionFile(definitionFile)
                .declarationFile(declarationFile);
    }

    @Override
    public String toString() {
        return namespace.isEmpty() ? name : namespace + namespaceDelimiter + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Symbol)) {
            return false;
        }
        Symbol symbol = (Symbol) o;
        return Objects.equals(namespace, symbol.namespace)
               && Objects.equals(namespaceDelimiter, symbol.namespaceDelimiter)
               && Objects.equals(name, symbol.name)
               && Objects.equals(properties, symbol.properties)
               && Objects.equals(declarationFile, symbol.declarationFile)
               && Objects.equals(definitionFile, symbol.definitionFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, namespaceDelimiter, name);
    }

    /**
     * Builds a Symbol.
     */
    public static final class Builder implements SmithyBuilder<Symbol> {
        private String name;
        private String namespace = "";
        private String namespaceDelimiter = "";
        private String definitionFile = "";
        private String declarationFile = "";
        private Map<String, Object> properties = new HashMap<>();

        @Override
        public Symbol build() {
            return new Symbol(this);
        }

        /**
         * Sets the unqualified name of the symbol.
         *
         * @param name Name to set.
         * @return Returns the builder.
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the namespace and namespace delimiter of the symbol.
         *
         * @param namespace Namespace to set.
         * @param namespaceDelimiter Namespace delimiter to set.
         * @return Returns the builder.
         */
        public Builder namespace(String namespace, String namespaceDelimiter) {
            this.namespace = namespace == null ? "" : namespace;
            this.namespaceDelimiter = namespaceDelimiter == null ? "" : namespaceDelimiter;
            return this;
        }

        /**
         * Sets a specific custom property.
         *
         * @param key Key to set.
         * @param value Value to set.
         * @return Returns the builder.
         */
        public Builder putProperty(String key, Object value) {
            properties.put(key, value);
            return this;
        }

        /**
         * Removes a specific custom property.
         *
         * @param key Key to remove.
         * @return Returns the builder.
         */
        public Builder removeProperty(String key) {
            properties.remove(key);
            return this;
        }

        /**
         * Replaces all of the custom properties.
         *
         * @param properties Custom properties to replace with.
         * @return Returns the builder.
         */
        public Builder properties(Map<String, Object> properties) {
            this.properties.clear();
            this.properties.putAll(properties);
            return this;
        }

        /**
         * Sets the filename of where this symbol is defined.
         *
         * <p>This value defaults to the value provided for {@link #declarationFile}
         * if not present. One of a {@code definitionFile} or a {@code declarationFile}
         * must be provided for every Symbol.
         *
         * @param definitionFile Filename of where the symbol is defined.
         * @return Returns the builder.
         */
        public Builder definitionFile(String definitionFile) {
            this.definitionFile = definitionFile;
            return this;
        }

        /**
         * Sets the filename of where this symbol is declared.
         *
         * <p>This value defaults to the value provided for {@link #definitionFile}
         * if not present. One of a {@code definitionFile} or a {@code declarationFile}
         * must be provided for every Symbol.
         *
         * @param declarationFile Filename of where the symbol is declared.
         * @return Returns the builder.
         */
        public Builder declarationFile(String declarationFile) {
            this.declarationFile = declarationFile;
            return this;
        }
    }
}