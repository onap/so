<?xml version="1.0" encoding="UTF-8"?>
<!--
  ============LICENSE_START=======================================================
  ECOMP MSO
  ================================================================================
  Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
  ================================================================================
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  ============LICENSE_END=========================================================
  -->

<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exsl="http://exslt.org/common" version="2.0" extension-element-prefixes="exsl">
  <!--
   Select one namespace node for each unique URI (almost), excluding
   the implicit "xml" namespace. This does not filter out namespace
   nodes declared on the same element with the same URI (thanks to
   limitations of XPath 1.0); we take care of that later... Note that if
   we had a distinct() function, this would be much, much simpler,
   e.g. distinct(//namespace::*)
  -->
  <xsl:variable name="almost-unique-uri-namespace-nodes" select="//namespace::*[name()!='xml'][not(.=../preceding::*/namespace::* or .=ancestor::*[position()&gt;1]/namespace::*)]"/>
  
  <!-- EXSLT functions are not supported by Saxon HE.  Define the function we need here -->
  <xsl:function name="exsl:node-set" as="node()">
    <xsl:param name="n" as="node()"/>
    <xsl:sequence select="$n"/>
  </xsl:function>

  <!-- Create a table of URI-prefix bindings -->
  <xsl:variable name="almost-unique-uri-bindings-tree">
    <xsl:for-each select="$almost-unique-uri-namespace-nodes">
      <binding>
        <prefix>
          <xsl:choose>
            <!--
             If there are any unqualified element names or
             attributes in this namespace in our document,
             then force default namespaces to use an arbitrary
             prefix, because we want to guarantee that the
             only namespace declarations in our result will
             be attached to the root element.
            -->
            <xsl:when test="not(name()) and (//*[namespace-uri()=''] or //@*[namespace-uri()=current()])">
              <xsl:variable name="alternate-prefix-candidate" select="//namespace::*[count(.|current())!=1][.=current()][name()!=''][1]"/>
              <xsl:choose>
                <xsl:when test="$alternate-prefix-candidate">
                  <xsl:value-of select="name($alternate-prefix-candidate)"/>
                </xsl:when>
                <xsl:otherwise>
                  <!--
                   If no alternative candidates exist, then generate a
                   "random" one.
                  -->
                  <xsl:value-of select="generate-id()"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="name()"/>
            </xsl:otherwise>
          </xsl:choose>
        </prefix>
        <uri>
          <xsl:value-of select="."/>
        </uri>
      </binding>
    </xsl:for-each>
  </xsl:variable>

  <!-- Select the first binding from the table for each unique URI -->
  <xsl:variable name="unique-uri-bindings" select="exsl:node-set($almost-unique-uri-bindings-tree)/binding[not(uri=preceding::uri)]"/>

  <!--
   Since there is no <xsl:namespace/> instruction, the only way we
   can create the namespace nodes we want is to create elements in
   a certain namespace and with a certain (prefixed) name.
  -->
  <xsl:variable name="created-namespace-nodes-tree">
    <xsl:for-each select="$unique-uri-bindings">
      <xsl:variable name="prefix">
        <xsl:choose>
          <!-- Replace a duplicated prefix with a different prefix. -->
          <xsl:when test="prefix=preceding::prefix">
            <xsl:value-of select="generate-id()"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="prefix"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="maybe-colon">
        <xsl:if test="string($prefix)">:</xsl:if>
      </xsl:variable>
      <xsl:element name="{$prefix}{$maybe-colon}temporary" namespace="{uri}"/>
    </xsl:for-each>
  </xsl:variable>

  <!--
   Select all the namespace nodes from our temporary tree
   of namespace-decorated elements.
  -->
  <xsl:variable name="created-namespace-nodes" select="exsl:node-set($created-namespace-nodes-tree)//namespace::*"/>

  <!--
   Do for the root element the same thing we do for every element,
   except that we explicitly copy all of our namespace nodes onto
   the root element, eliminating the need for namespace declarations
   to appear anywhere else in the output.
  -->
  <xsl:template match="/*">
    <xsl:call-template name="copy">
      <xsl:with-param name="insert-namespace-declarations" select="true()"/>
    </xsl:call-template>
    <!-- <xsl:call-template name="do-xsl-message-diagnostics"/> -->
  </xsl:template>

  <!--
   For each element, create a new element with the same expanded name,
   but not necessarily the same QName. We create a new element instead
   of copying the original, because, besides potentially having a
   QName we don't want, a copy would include with it all of the
   namespace nodes attached to the original, and we don't necessarily
   want that.
  -->
  <xsl:template match="*" name="copy">
    <xsl:param name="insert-namespace-declarations"/>
    <xsl:variable name="prefix" select="name($created-namespace-nodes[.=namespace-uri(current())])"/>
    <xsl:variable name="maybe-colon">
      <xsl:if test="$prefix">:</xsl:if>
    </xsl:variable>
    <xsl:element name="{$prefix}{$maybe-colon}{local-name()}" namespace="{namespace-uri()}">
      <xsl:if test="$insert-namespace-declarations">
        <xsl:copy-of select="$created-namespace-nodes"/>
      </xsl:if>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>

  <!--
   For each attribute, create a new attribute with the same expanded
   name, but not necessarily the same QName.
  -->
  <xsl:template match="@*">
    <xsl:variable name="prefix" select="name($created-namespace-nodes[.=namespace-uri(current())])"/>
    <xsl:variable name="maybe-colon">
      <xsl:if test="$prefix">:</xsl:if>
    </xsl:variable>
    <xsl:attribute name="{$prefix}{$maybe-colon}{local-name()}" namespace="{namespace-uri()}">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>

  <!-- Do a simple copy of text, comments, and processing instructions -->
  <xsl:template match="text()|comment()|processing-instruction()">
    <xsl:copy/>
  </xsl:template>

  <!-- Print out some diagnostics to show what's going on beneath the covers. -->
  <xsl:template name="do-xsl-message-diagnostics">
    <xsl:message>
      <diagnostics xml:space="preserve">
        <diagnostic name="almost-unique-uri-bindings-tree">
          <xsl:copy-of select="$almost-unique-uri-bindings-tree"/>
        </diagnostic>
        <diagnostic name="unique-uri-bindings">
          <xsl:copy-of select="$unique-uri-bindings"/>
        </diagnostic>
        <diagnostic name="created-namespace-nodes-tree">
          <xsl:copy-of select="$created-namespace-nodes-tree"/>
        </diagnostic>
      </diagnostics>
    </xsl:message>
  </xsl:template>

</xsl:transform>
