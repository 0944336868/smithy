// This file defines test cases that serialize maps in XML payloads.

$version: "0.5.0"

namespace aws.protocols.tests.restxml

use aws.protocols.tests.shared#FooEnumMap
use aws.protocols.tests.shared#GreetingStruct
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests

/// The example tests basic map serialization.
@http(uri: "/XmlMaps", method: "POST")
operation XmlMaps {
    input: XmlMapsInputOutput,
    output: XmlMapsInputOutput
}

apply XmlMaps @httpRequestTests([
    {
        id: "XmlMaps",
        documentation: "Serializes XML maps",
        protocol: "aws.rest-xml",
        method: "POST",
        uri: "/XmlMaps",
        body: """
              <XmlMapsInputOutput>
                  <myMap>
                      <entry>
                          <key>foo</key>
                          <value>
                              <hi>there</hi>
                          </value>
                      </entry>
                      <entry>
                          <key>baz</key>
                          <value>
                              <hi>bye</hi>
                          </value>
                      </entry>
                  </myMap>
              </XmlMapsInputOutput>
              """,
        bodyMediaType: "application/xml",
        headers: {
            "Content-Type": "application/xml"
        },
        params: {
            myMap: {
                foo: {
                    hi: "there"
                },
                baz: {
                    hi: "bye"
                }
            }
        }
    }
])

apply XmlMaps @httpResponseTests([
    {
        id: "XmlMaps",
        documentation: "Serializes XML maps",
        protocol: "aws.rest-xml",
        code: 200,
        body: """
              <XmlMapsInputOutput>
                  <myMap>
                      <entry>
                          <key>foo</key>
                          <value>
                              <hi>there</hi>
                          </value>
                      </entry>
                      <entry>
                          <key>baz</key>
                          <value>
                              <hi>bye</hi>
                          </value>
                      </entry>
                  </myMap>
              </XmlMapsInputOutput>
              """,
        bodyMediaType: "application/xml",
        headers: {
            "Content-Type": "application/xml"
        },
        params: {
            myMap: {
                foo: {
                    hi: "there"
                },
                baz: {
                    hi: "bye"
                }
            }
        }
    }
])

structure XmlMapsInputOutput {
    myMap: XmlMapsInputOutputMap,
}

map XmlMapsInputOutputMap {
    key: String,
    value: GreetingStruct
}

// This example tests maps with @xmlName on members.
@http(uri: "/XmlMapsXmlName", method: "POST")
operation XmlMapsXmlName {
    input: XmlMapsXmlNameInputOutput,
    output: XmlMapsXmlNameInputOutput
}

apply XmlMapsXmlName @httpRequestTests([
    {
        id: "XmlMapsXmlName",
        documentation: "Serializes XML maps that have xmlName on members",
        protocol: "aws.rest-xml",
        method: "POST",
        uri: "/XmlMapsXmlName",
        body: """
              <XmlMapsXmlNameInputOutput>
                  <myMap>
                      <entry>
                          <Name>foo</Name>
                          <Setting>
                              <hi>there</hi>
                          </Setting>
                      </entry>
                      <entry>
                          <Name>baz</Name>
                          <Setting>
                              <hi>bye</hi>
                          </Setting>
                      </entry>
                  </myMap>
              </XmlMapsXmlNameInputOutput>
              """,
        bodyMediaType: "application/xml",
        headers: {
            "Content-Type": "application/xml"
        },
        params: {
            myMap: {
                foo: {
                    hi: "there"
                },
                baz: {
                    hi: "bye"
                }
            }
        }
    }
])

apply XmlMapsXmlName @httpResponseTests([
    {
        id: "XmlMapsXmlName",
        documentation: "Serializes XML lists",
        protocol: "aws.rest-xml",
        code: 200,
        body: """
              <XmlMapsXmlNameInputOutput>
                  <myMap>
                      <entry>
                          <Name>foo</Name>
                          <Setting>
                              <hi>there</hi>
                          </Setting>
                      </entry>
                      <entry>
                          <Name>baz</Name>
                          <Setting>
                              <hi>bye</hi>
                          </Setting>
                      </entry>
                  </myMap>
              </XmlMapsXmlNameInputOutput>
              """,
        bodyMediaType: "application/xml",
        headers: {
            "Content-Type": "application/xml"
        },
        params: {
            myMap: {
                foo: {
                    hi: "there"
                },
                baz: {
                    hi: "bye"
                }
            }
        }
    }
])

structure XmlMapsXmlNameInputOutput {
    myMap: XmlMapsXmlNameInputOutputMap,
}

map XmlMapsXmlNameInputOutputMap {
    @xmlName("Attribute")
    key: String,

    @xmlName("Setting")
    value: GreetingStruct
}

/// Flattened maps
@http(uri: "/FlattenedXmlMap", method: "POST")
operation FlattenedXmlMap {
    input: FlattenedXmlMapInputOutput,
    output: FlattenedXmlMapInputOutput
}

apply FlattenedXmlMap @httpRequestTests([
    {
        id: "FlattenedXmlMap",
        documentation: "Serializes flattened XML maps in requests",
        protocol: "aws.rest-xml",
        method: "POST",
        uri: "/FlattenedXmlMap",
        body: """
              <FlattenedXmlMapInputOutput>
                  <myMap>
                      <key>foo</key>
                      <value>Foo</value>
                  </myMap>
                  <myMap>
                      <key>baz</key>
                      <value>Baz</value>
                  </myMap>
              </FlattenedXmlMapInputOutput>""",
        bodyMediaType: "application/xml",
        headers: {
            "Content-Type": "application/xml"
        },
        params: {
            myMap: {
                foo: "Foo",
                baz: "Baz"
            }
        }
    }
])

apply FlattenedXmlMap @httpResponseTests([
    {
        id: "FlattenedXmlMap",
        documentation: "Serializes flattened XML maps in responses",
        protocol: "aws.rest-xml",
        code: 200,
        body: """
              <FlattenedXmlMapInputOutput>
                  <myMap>
                      <key>foo</key>
                      <value>Foo</value>
                  </myMap>
                  <myMap>
                      <key>baz</key>
                      <value>Baz</value>
                  </myMap>
              </FlattenedXmlMapInputOutput>""",
        bodyMediaType: "application/xml",
        headers: {
            "Content-Type": "application/xml"
        },
        params: {
            myMap: {
                foo: "Foo",
                baz: "Baz"
            }
        }
    }
])

structure FlattenedXmlMapInputOutput {
    @xmlFlattened
    myMap: FooEnumMap,
}

/// Flattened maps with @xmlName
@http(uri: "/FlattenedXmlMapWithXmlName", method: "POST")
operation FlattenedXmlMapWithXmlName {
    input: FlattenedXmlMapWithXmlNameInputOutput,
    output: FlattenedXmlMapWithXmlNameInputOutput
}

apply FlattenedXmlMapWithXmlName @httpRequestTests([
    {
        id: "FlattenedXmlMapWithXmlName",
        documentation: "Serializes flattened XML maps in requests that have xmlName on members",
        protocol: "aws.rest-xml",
        method: "POST",
        uri: "/FlattenedXmlMapWithXmlName",
        body: """
              <FlattenedXmlMapWithXmlNameInputOutput>
                  <KVP>
                      <K>a</K>
                      <V>A</V>
                  </KVP>
                  <KVP>
                      <K>b</K>
                      <V>B</V>
                  </KVP>
              </FlattenedXmlMapWithXmlNameInputOutput>""",
        bodyMediaType: "application/xml",
        headers: {
            "Content-Type": "application/xml"
        },
        params: {
            myMap: {
                a: "A",
                b: "B",
            }
        }
    }
])

apply FlattenedXmlMapWithXmlName @httpResponseTests([
    {
        id: "FlattenedXmlMapWithXmlName",
        documentation: "Serializes flattened XML maps in responses that have xmlName on members",
        protocol: "aws.rest-xml",
        code: 200,
        body: """
              <FlattenedXmlMapWithXmlNameInputOutput>
                  <KVP>
                      <K>a</K>
                      <V>A</V>
                  </KVP>
                  <KVP>
                      <K>b</K>
                      <V>B</V>
                  </KVP>
              </FlattenedXmlMapWithXmlNameInputOutput>""",
        bodyMediaType: "application/xml",
        headers: {
            "Content-Type": "application/xml"
        },
        params: {
            myMap: {
                a: "A",
                b: "B",
            }
        }
    }
])

structure FlattenedXmlMapWithXmlNameInputOutput {
    @xmlFlattened
    @xmlName("KVP")
    myMap: FlattenedXmlMapWithXmlNameInputOutputMap,
}

map FlattenedXmlMapWithXmlNameInputOutputMap {
    @xmlName("K")
    key: String,

    @xmlName("V")
    value: String,
}
