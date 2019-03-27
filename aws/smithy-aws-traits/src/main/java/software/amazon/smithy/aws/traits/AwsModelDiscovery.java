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

package software.amazon.smithy.aws.traits;

import java.net.URL;
import java.util.List;
import software.amazon.smithy.model.loader.ModelAssembler;
import software.amazon.smithy.model.loader.ModelDiscovery;

/**
 * This is used to allow a {@link ModelAssembler} to discover the AWS
 * core model.
 */
public final class AwsModelDiscovery implements ModelDiscovery {
    @Override
    public List<URL> getModels() {
        return List.of(
                getClass().getResource("aws.api.json"),
                getClass().getResource("aws.iam.json"),
                getClass().getResource("aws.apigateway.json"));
    }
}