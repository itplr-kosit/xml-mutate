<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xs "
                version="2.0">

    <xsl:output method="xml" indent="yes" />

    <xsl:param name="someParameter" required="no" />

    <xsl:template match="/*">
        <transformed>
            <text>This is transformed.</text>
            <orig>
                <xsl:value-of select="text()" />
            </orig>
            <param>
                <xsl:value-of select="$someParameter" />
            </param>
            <doc>
                <xsl:copy-of select="current()" />
            </doc>
        </transformed>
    </xsl:template>
</xsl:stylesheet>