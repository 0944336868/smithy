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

package software.amazon.smithy.model.loader;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizes a .smithy formatted string into a list of tokens.
 *
 * <p>This lexer uses a regular expression to tokenize. Each token is defined
 * using a {@code TokenType} that contains partial regular expression used to
 * match the token. The order in which these tokens are defined is
 * significant. All of the tokens and their expressions are combined into a
 * single regular expressions that uses named captures. The name of each
 * capture is the name of the enum. Any unrecognized token is tokenized as
 * {@code UNKNOWN}.
 */
final class SmithyModelLexer implements Iterator<SmithyModelLexer.Token> {

    /** Represents a parsed token type, in order of precedence. */
    enum TokenType {
        NEWLINE("\\R"),
        WS("\\s+"),
        COMMENT("//[^\\n]*"),
        RETURN(Pattern.quote("->")),
        VERSION(Pattern.quote("$version")),
        UNQUOTED("[A-Za-z_][A-Za-z0-9_#$.-]*"),
        LPAREN(Pattern.quote("(")),
        RPAREN(Pattern.quote(")")),
        LBRACE(Pattern.quote("{")),
        RBRACE(Pattern.quote("}")),
        LBRACKET(Pattern.quote("[")),
        RBRACKET(Pattern.quote("]")),
        EQUAL("="),
        COLON(":"),
        COMMA(","),
        ANNOTATION("@[A-Za-z0-9.$#]+"),
        // DQUOTE and SQUOTE: supports espaced quotes and escaped escapes.
        DQUOTE("(?:\")([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)(?:\")"),
        SQUOTE("(?:')([^'\\\\]*(?:\\\\.[^'\\\\]*)*)(?:')"),
        NUMBER("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?"),
        ERROR(".");

        private final String pattern;

        TokenType(String pattern) {
            this.pattern = pattern;
        }

        @Override
        public String toString() {
            return super.toString() + "(" + pattern.replace("\\Q", "").replace("\\E", "") + ")";
        }
    }

    /** Represents a parsed token. */
    static final class Token {
        final TokenType type;
        final String lexeme;
        final int line;
        final int column;
        final int span;

        Token(TokenType type, String lexeme, int line, int column, int span) {
            this.type = type;
            this.lexeme = lexeme;
            this.line = line;
            this.column = column;
            this.span = span;
        }

        @Override
        public String toString() {
            return String.format("%s(%s, %d:%d)", type.name(), lexeme, line, column);
        }
    }

    private static final Pattern COMPILED;

    static {
        // Compile the lexer tokens into a regular expression with capture
        // groups matching the enum value name.
        StringBuilder builder = new StringBuilder();
        for (TokenType token : TokenType.values()) {
            builder.append(String.format("|(?<%s>%s)", token.name(), token.pattern));
        }
        COMPILED = Pattern.compile(builder.substring(1));
    }

    private Token peeked;
    private final Matcher matcher;
    private int line = 1;
    private int lastLineOffset;
    private boolean inPeek;

    SmithyModelLexer(String input) {
        matcher = COMPILED.matcher(input);
    }

    /**
     * Peeks at the next available token without consuming it.
     *
     * @return Returns the next token or null if at the end.
     */
    Token peek() {
        if (peeked == null) {
            inPeek = true;
            peeked = next();
            inPeek = false;
        }

        return peeked;
    }

    @Override
    public boolean hasNext() {
        return peek() != null;
    }

    @Override
    public Token next() {
        if (peeked != null) {
            Token value = peeked;
            peeked = null;
            return value;
        }

        next_token:
        while (matcher.find()) {
            for (TokenType token : TokenType.values()) {
                if (matcher.group(token.name()) != null) {
                    String lexeme = matcher.group(token.name());
                    switch (token) {
                        case NEWLINE:
                            line++;
                            lastLineOffset = matcher.end();
                            // fallthru
                        case WS: // fallthru
                        case COMMENT:
                            // Try to grab the next token before returning.
                            continue next_token;
                        case SQUOTE:
                            lexeme = fixString(lexeme, '\'');
                            break;
                        case DQUOTE:
                            lexeme = fixString(lexeme, '"');
                            break;
                        case ANNOTATION:
                            // Strip leading "@".
                            lexeme = lexeme.substring(1);
                            break;
                        default:
                            break;
                    }

                    int column = 1 + (matcher.start() - lastLineOffset);
                    int span = matcher.end() - matcher.start();
                    return new Token(token, lexeme, line, column, span);
                }
            }
        }

        if (inPeek) {
            return null;
        }

        // The expected behavior is to throw an exception if no more tokens
        // are available. However, we don't wan't to throw when peeking.
        throw new NoSuchElementException();
    }

    private static String fixString(String lexeme, char delimiter) {
        // This method does a single pass over the lexeme to remove wrapping
        // delimiters, remove escaped '\', and remove escaped delimiters.
        StringBuilder result = new StringBuilder(lexeme.length() - 2);
        boolean afterEscape = false;

        // Remove quotes by clipping offsets by 1.
        for (int i = 1; i < lexeme.length() - 1; i++) {
            char c = lexeme.charAt(i);
            if (c == '\\') {
                if (afterEscape) {
                    // Two "\\" is converted to "\".
                    result.append('\\');
                    afterEscape = false;
                } else {
                    afterEscape = true;
                }
            } else {
                // '\' followed by delimiter character becomes the delimiter.
                if (afterEscape && c == delimiter) {
                    result.append(delimiter);
                } else {
                    result.append(c);
                }
                afterEscape = false;
            }
        }

        return result.toString();
    }
}