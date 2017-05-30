<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE xsl:stylesheet [
	<!ENTITY eacute "&#233;">
	<!ENTITY Eacute "&#201;">
	<!ENTITY egrave "&#232;">
	<!ENTITY Eagrave "&#200;">
	<!ENTITY agrave "&#224;">
	<!ENTITY Agrave "&#192;">
	<!ENTITY ecirc "&#234;">
	<!ENTITY Ecirc "&#202;">
	<!ENTITY ccedil "&#231;">
	<!ENTITY Ccedil "&#199;">
	<!ENTITY euml "&#235;">
	<!ENTITY Euml "&#203;">
	<!ENTITY aelig "&#230;">
	<!ENTITY Aelig "&#198;">
	<!ENTITY ugrave "&#249;">
	<!ENTITY Ugrave "&#217;">
	<!ENTITY oelig "&#156;">
	<!ENTITY acute "&#180;">
]>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- This script produces valid XHTML file -->
	<xsl:output method="xml"
				doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
				doctype-public="-//W3C//DTD// XHTML 1.0 Transiotional//EN"
				indent="yes" />

	<!-- Main page template -->
	<xsl:template match="/">
<html>
	<style type="text/css">
body
{
	color:				#000000;
	background-color:	#FFFFFF;
	font-family:		"Lucida Console", monospace;
	font-size:			0.7em;
}
table
{
	border-collapse:	collapse;
}
th
{
	background-color:	#cccccc;
	font-weight:		bold;
}
th, td
{
	border:				1px solid black;
}
td.ERR
{
	background-color:	#ff0000;
	color:				#ffffff;
}
	</style>
	<body>
		<h1>Patient <xsl:value-of select="Delta/Patient/@ID" />, ACK = <xsl:value-of select="Delta/ACK" /></h1>
		<xsl:apply-templates select="Delta/MAJ/Table" />
	</body>
</html>
	</xsl:template>

	<!-- Selector to select a table with specified name -->
	<xsl:key name="FK" match="Delta/MAJ/Table" use="@name" />

	<!-- Table handler template -->
	<xsl:template match="Table">
		<xsl:variable name="n"><xsl:value-of select="@name" /></xsl:variable>
		<h2><xsl:value-of select="$n" /> (<xsl:value-of select="count(T)" /> rows)</h2>
		<!-- <table border="1" cellspacing="0"> -->
		<table>
			<tr>
				<th>IdGlobal</th>
				<th>Author</th>
				<th>TSSPT</th>
				<th>TSSanteos</th>
		<xsl:choose>
			<xsl:when test="$n='Formulaire'">
				<th>Nom</th>
				<th>Filtre</th>
			</xsl:when>
			<xsl:when test="$n='Episode'">
				<th>Nom</th>
			</xsl:when>
			<xsl:when test="$n='Event'">
				<th>IdForm</th>
				<th>IdUser</th>
				<th>IdEpisode</th>
				<th>DateEvent</th>
				<th>DateFin</th>
				<th>Filtre</th>
			</xsl:when>
			<xsl:when test="$n='Comment'">
				<th>ValComment</th>
			</xsl:when>
			<xsl:when test="$n='Info'">
				<th>IdEvent</th>
				<th>IdComment</th>
				<th>ValChar</th>
				<th>ValNum</th>
				<th>ValDate</th>
				<th>Position</th>
				<th>Filtre</th>
				<th>IdConcept</th>
			</xsl:when>
			<xsl:when test="$n='UserDMSP'">
				<th>Nom</th>
				<th>Type</th>
				<th>Responsable</th>
				<th>Identifiant</th>
				<th>Civilite</th>
				<th>Prenom</th>
				<th>Adresse</th>
				<th>Ville</th>
				<th>CodePost</th>
				<th>Tel</th>
				<th>Mobile</th>
				<th>Courriel</th>
				<th>InfoLegale</th>
				<th>Certificate</th>
				<th>IdRole</th>
			</xsl:when>
			<xsl:when test="$n='Role'">
				<th>Nom</th>
			</xsl:when>
			<xsl:when test="$n='Habilitation'">
				<th>IdRole</th>
				<th>IdUser</th>
			</xsl:when>
			<xsl:when test="$n='MatriceDMSP'">
				<th>IdForm</th>
				<th>IdRole</th>
				<th>Authorisations</th>
			</xsl:when>
			<xsl:when test="$n='Matrice_Patient'">
				<th>IdCategorie</th>
				<th>TypeCategorie</th>
				<th>IdActeur</th>
				<th>TypeActeur</th>
				<th>Authorisations</th>
			</xsl:when>
			<xsl:when test="$n='TupleDeleted'">
			</xsl:when>
			<xsl:when test="$n='LogDeleted'">
			</xsl:when>
			<xsl:when test="$n='UpdateLog'">
			</xsl:when>
			<xsl:otherwise></xsl:otherwise>
		</xsl:choose>
			</tr>

		<xsl:for-each select="T">
			<tr>
				<td><xsl:value-of select="@i" /></td>
				<td><xsl:value-of select="@a" /></td>
				<td><xsl:value-of select="@p" /></td>
				<td><xsl:value-of select="@s" /></td>
			<xsl:apply-templates select="A" />
			</tr>
		</xsl:for-each>

		</table>
	</xsl:template>

	<!-- Row values handler template, checks if value satisfies its foreign key -->
	<xsl:template match="A">
		<xsl:variable name="v">
			<xsl:if test=".='[]'">[NULL]</xsl:if>
			<xsl:if test="not(.='[]')"><xsl:value-of select="." /></xsl:if>
		</xsl:variable>
		<xsl:variable name="t" select="../../@name" />
		<xsl:variable name="f" select="position()" />
		<xsl:variable name="ok">
			<xsl:choose>
				<xsl:when test="$t='Info'">
					<xsl:choose>
						<xsl:when test="$f=1">
							<xsl:value-of select="count(key('FK','Event')/T[@i=$v])=1" />
						</xsl:when>
						<xsl:when test="$f=2">
							<xsl:value-of select="count(key('FK','Comment')/T[@i=$v])=1" />
						</xsl:when>
						<xsl:otherwise><xsl:value-of select="true()" /></xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="$t='Event'">
					<xsl:choose>
						<xsl:when test="$f=1">
							<xsl:value-of select="count(key('FK','Formulaire')/T[@i=$v])=1" />
						</xsl:when>
						<xsl:when test="$f=2">
							<xsl:value-of select="count(key('FK','UserDMSP')/T[@i=$v])=1" />
						</xsl:when>
						<xsl:when test="$f=3">
							<xsl:value-of select="count(key('FK','Episode')/T[@i=$v])=1" />
						</xsl:when>
						<xsl:otherwise><xsl:value-of select="true()" /></xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="$t='Habilitation'">
					<xsl:choose>
						<xsl:when test="$f=1">
							<xsl:value-of select="count(key('FK','Role')/T[@i=$v])=1" />
						</xsl:when>
						<xsl:when test="$f=2">
							<xsl:value-of select="count(key('FK','UserDMSP')/T[@i=$v])=1" />
						</xsl:when>
						<xsl:otherwise><xsl:value-of select="true()" /></xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="$t='MatriceDMSP'">
					<xsl:choose>
						<xsl:when test="$f=1">
							<xsl:value-of select="count(key('FK','Formulaire')/T[@i=$v])=1" />
						</xsl:when>
						<xsl:when test="$f=2">
							<xsl:value-of select="count(key('FK','Role')/T[@i=$v])=1" />
						</xsl:when>
						<xsl:otherwise><xsl:value-of select="true()" /></xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="$t='UserDMSP'">
					<xsl:choose>
						<xsl:when test="$f=15">
							<xsl:value-of select="count(key('FK','Role')/T[@i=$v])=1" />
						</xsl:when>
						<xsl:otherwise><xsl:value-of select="true()" /></xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise><xsl:value-of select="true()" /></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<td>
		<xsl:if test="$ok='false'">
			<xsl:attribute name="class">ERR</xsl:attribute>
		</xsl:if>
		<xsl:call-template name="escape">
			<xsl:with-param name="vv" select="$v" />
		</xsl:call-template>
		</td>
	</xsl:template>

	<!-- Recursive template to replace special characters in field's value -->
	<xsl:template name="escape">
		<xsl:param name="vv" />
		<xsl:variable name="apos">\'</xsl:variable>
		<xsl:variable name="value">
			<xsl:choose>
				<xsl:when test="contains($vv,'\u00E9')">
					<xsl:value-of select="substring-before($vv,'\u00E9')" />&eacute;<xsl:value-of select="substring-after($vv,'\u00E9')" />
				</xsl:when>
				<xsl:when test="contains($vv,'\u00C9')">
					<xsl:value-of select="substring-before($vv,'\u00C9')" />&Eacute;<xsl:value-of select="substring-after($vv,'\u00C9')" />
				</xsl:when>
				<xsl:when test="contains($vv,'\u00E8')">
					<xsl:value-of select="substring-before($vv,'\u00E8')" />&egrave;<xsl:value-of select="substring-after($vv,'\u00E8')" />
				</xsl:when>
				<xsl:when test="contains($vv,'\u00C8')">
					<xsl:value-of select="substring-before($vv,'\u00C8')" />&Eagrave;<xsl:value-of select="substring-after($vv,'\u00C8')" />
				</xsl:when>
				<xsl:when test="contains($vv,'\u00E0')">
					<xsl:value-of select="substring-before($vv,'\u00E0')" />&agrave;<xsl:value-of select="substring-after($vv,'\u00E0')" />
				</xsl:when>
				<xsl:when test="contains($vv,'\u00C0')">
					<xsl:value-of select="substring-before($vv,'\u00C0')" />&Agrave;<xsl:value-of select="substring-after($vv,'\u00C0')" />
				</xsl:when>
				<xsl:when test="contains($vv,'\u00EA')">
					<xsl:value-of select="substring-before($vv,'\u00EA')" />&ecirc;<xsl:value-of select="substring-after($vv,'\u00EA')" />
				</xsl:when>
				<xsl:when test="contains($vv,'\u00CA')">
					<xsl:value-of select="substring-before($vv,'\u00CA')" />&Ecirc;<xsl:value-of select="substring-after($vv,'\u00CA')" />
				</xsl:when>
				<xsl:when test="contains($vv,'\u00E7')">
					<xsl:value-of select="substring-before($vv,'\u00E7')" />&ccedil;<xsl:value-of select="substring-after($vv,'\u00E7')" />
				</xsl:when>
				<xsl:when test="contains($vv,'\u00C7')">
					<xsl:value-of select="substring-before($vv,'\u00C7')" />&Ccedil;<xsl:value-of select="substring-after($vv,'\u00C7')" />
				</xsl:when>
				<xsl:when test="contains($vv,'\u00EB')">
					<xsl:value-of select="substring-before($vv,'\u00EB')" />&euml;<xsl:value-of select="substring-after($vv,'\u00EB')" />
				</xsl:when>
				<xsl:when test="contains($vv,'\u00CB')">
					<xsl:value-of select="substring-before($vv,'\u00CB')" />&Euml;<xsl:value-of select="substring-after($vv,'\u00CB')" />
				</xsl:when>
				<xsl:when test="contains($vv,'\u00E6')">
					<xsl:value-of select="substring-before($vv,'\u00E6')" />&aelig;<xsl:value-of select="substring-after($vv,'\u00E6')" />
				</xsl:when>
				<xsl:when test="contains($vv,'\u00C6')">
					<xsl:value-of select="substring-before($vv,'\u00C6')" />&Aelig;<xsl:value-of select="substring-after($vv,'\u00C6')" />
				</xsl:when>
				<xsl:when test="contains($vv,'\u00F9')">
					<xsl:value-of select="substring-before($vv,'\u00F9')" />&ugrave;<xsl:value-of select="substring-after($vv,'\u00F9')" />
				</xsl:when>
				<xsl:when test="contains($vv,'\u00D9')">
					<xsl:value-of select="substring-before($vv,'\u00D9')" />&Ugrave;<xsl:value-of select="substring-after($vv,'\u00D9')" />
				</xsl:when>
				<xsl:when test="contains($vv,'\u009C')">
					<xsl:value-of select="substring-before($vv,'\u009C')"/>&oelig;<xsl:value-of select="substring-after($vv,'\u009C')" />
				</xsl:when>
				<xsl:when test="contains($vv,$apos)">
					<xsl:value-of select="substring-before($vv,$apos)"/>&acute;<xsl:value-of select="substring-after($vv,$apos)" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$vv" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="contains($value,'\u00') or contains($value,$apos)">
				<xsl:call-template name="escape">
					<xsl:with-param name="vv" select="$value" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$value" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
