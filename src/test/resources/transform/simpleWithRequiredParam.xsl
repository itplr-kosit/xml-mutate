<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xs "
                version="2.0">

    <xsl:output method="xml" indent="yes" />

    <xsl:param name="someParameter" required="yes" />

    <xsl:template match="/*">
        <test>
            <text>
                This is from Transformation.
                This is from original:
                <xsl:value-of select="text()" />
                param-Value:
                <xsl:value-of select="$someParameter" />
            </text>
            <orig>
                <xsl:copy-of select="current()" />
            </orig>
        </test>
    </xsl:template>
</xsl:stylesheet>