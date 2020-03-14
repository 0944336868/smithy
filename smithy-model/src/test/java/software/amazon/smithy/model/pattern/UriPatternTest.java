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

package software.amazon.smithy.model.pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static software.amazon.smithy.model.pattern.SmithyPattern.Segment;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class UriPatternTest {
    @Test
    public void parsesUri() {
        String pattern = "/foo/baz/{bar}?bam=boo&heck";
        UriPattern uri = UriPattern.parse(pattern);
        Segment segment = new Segment("bar", Segment.Type.LABEL);

        assertThat(uri.toString(), equalTo(pattern));
        assertThat(uri.getSegments(), hasSize(3));
        assertThat(uri.getQueryLiterals().values(), hasSize(2));
        assertThat(uri.getQueryLiterals(), hasKey("bam"));
        assertThat(uri.getQueryLiterals(), hasKey("heck"));
        assertThat(uri.getQueryLiterals().get("bam"), equalTo("boo"));
        assertThat(uri.getQueryLiterals().get("heck"), equalTo(""));
        assertThat(uri.getGreedyLabel(), is(Optional.empty()));
        assertThat(uri.getLabels(), contains(segment));
        assertThat(uri.getLabel("bar"), is(Optional.of(segment)));
        assertThat(uri.getQueryLiteralValue("bam"), equalTo(Optional.of("boo")));
    }

    @Test
    public void computesHashAndEquals() {
        UriPattern uri1 = UriPattern.parse("/foo?baz");
        UriPattern uri2 = UriPattern.parse("/foo/{baz+}/?bam");

        assertThat(uri1, equalTo(uri1));
        assertThat(uri1, not(equalTo(uri2)));
        assertThat(uri1.hashCode(), is(uri1.hashCode()));
        assertThat(uri1.hashCode(), not(uri2.hashCode()));
        assertThat(uri2, equalTo(uri2));
        assertThat(uri2.hashCode(), is(uri2.hashCode()));
    }

    @Test
    public void labelsMustStartWithSlash() {
        Throwable thrown = Assertions.assertThrows(InvalidUriPatternException.class, () -> UriPattern.parse("foo"));

        assertThat(thrown.getMessage(), containsString("URI pattern must start with '/'"));
    }

    @Test
    public void labelsMustNotEndWithQuestionMark() {
        Throwable thrown = Assertions.assertThrows(InvalidUriPatternException.class, () -> UriPattern.parse("/foo?"));

        assertThat(thrown.getMessage(), containsString("URI patterns must not end with '?'"));
    }

    @Test
    public void patternsMustNotContainFragments() {
        Throwable thrown = Assertions.assertThrows(InvalidUriPatternException.class, () -> UriPattern.parse("/foo#bam"));

        assertThat(thrown.getMessage(), containsString("URI pattern must not contain a fragment"));
    }

    @Test
    public void labelsMustNotAppearInQueryString() {
        Throwable thrown = Assertions.assertThrows(InvalidUriPatternException.class, () -> {
            UriPattern.parse("/baz?bam={boozled}");
        });

        assertThat(thrown.getMessage(), containsString("URI labels must not appear in the query string"));
    }

    @Test
    public void detectsCaseSensitiveDuplicateQueryStringLiterals() {
        Throwable thrown = Assertions.assertThrows(InvalidUriPatternException.class, () -> {
            UriPattern.parse("/foo?baz=bar&baz=bam");
        });

        assertThat(thrown.getMessage(), containsString("Literal query parameters must not be repeated"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void patternConflictTest(String patternA, String patternB, boolean isConflicting) {
        UriPattern a = UriPattern.parse(patternA);
        UriPattern b = UriPattern.parse(patternB);
        if (a.conflictsWith(b) != isConflicting) {
            if (isConflicting) {
                Assertions.fail("Expected conflict between `" + a + "` and `" + b + "`");
            } else {
                Assertions.fail("Unexpected conflict between `" + a + "` and `" + b + "`");
            }
        }
    }

    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"/a/{x}", "/{y}/a", true},
                {"/{x}/a", "/a/{y}", true},
                {"/a/{x}", "/a/{y}", true},
                {"/a/{x}", "/b/{y}", false},
                {"/{x+}", "/a", true},
                {"/a", "/{x+}", true},
                {"/a/{x}/b/{y}", "/a/{x+}", true},
                {"/a/{x+}", "/a/{y}", true},
                {"/a/{x}", "/a/{y+}", true},
                {"/a/{x+}", "/b/{y+}", false},
                // Same length with conflicting labels.
                {"/a/{x}/b", "/a/{y}/b", true},
                // One is longer than the other.
                {"/a/{x}/b", "/a/{y}/b/{z}", false},
                // One is longer than the other.
                {"/a/{x}/b", "/a/{y}/b/{z}/c", false},
                {"/a/b/c", "/a/{b}/c", true},
                {"/a/b/c/d", "/a/{b}/c/{d}", true},
                {"/foo?a", "/foo?a", true},
                {"/foo?a=b", "/foo?a=b", true},
                {"/foo?a=b", "/foo?a=c", false},
                {"/foo?a", "/foo?a=", true},
                {"/a=b", "/a", false},
                {"/a=b", "/a=", false},
                {"/foo?a", "/foo?b", false},
        });
    }
}
