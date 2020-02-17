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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

description = "This module provides the core implementation of loading, validating, " +
        "traversing, mutating, and serializing a Smithy model."
extra["displayName"] = "Smithy :: Model"
extra["moduleName"] = "software.amazon.smithy.model"

apply(plugin = "com.github.johnrengelman.shadow")

dependencies {
    api(project(":smithy-utils"))
    implementation("com.fasterxml.jackson.core:jackson-core:2.9.8")
}

// Shade the Jackson Core dependency.
tasks.withType<ShadowJar> {
    dependencies {
        include(dependency("com.fasterxml.jackson.core:jackson-core:2.9.8"))
    }

    relocate("com.fasterxml.jackson.core", "software.amazon.smithy.private.com.fasterxml.jackson.core")
}
