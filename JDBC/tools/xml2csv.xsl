<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="text" />

	<xsl:template match="/">
		<xsl:apply-templates select="Delta/MAJ/Table" />
	</xsl:template>

	<xsl:template match="Table">
		<xsl:variable name="n"><xsl:value-of select="@name" />;</xsl:variable>
		<xsl:for-each select="T/A">
			<xsl:variable name="i"><xsl:value-of select="../@i" />;</xsl:variable>
			<xsl:variable name="a"><xsl:value-of select="../@a" />;</xsl:variable>
			<xsl:variable name="p"><xsl:value-of select="../@p" />;</xsl:variable>
			<xsl:variable name="s"><xsl:value-of select="../@s" />;</xsl:variable>
			
			<xsl:value-of select="$n" />
			<xsl:value-of select="$i" />
			<xsl:value-of select="$a" />
			<xsl:value-of select="$p" />
			<xsl:value-of select="$s" />
			<xsl:if test=".='[]'">[NULL]</xsl:if>
			<xsl:if test="not(.='[]')"><xsl:value-of select="." /></xsl:if>
			<xsl:text>
</xsl:text>
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>
