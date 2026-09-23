package io.github.rodrigoma.pagbank.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import kotlin.reflect.full.primaryConstructor

/**
 * `META-INF/spring-configuration-metadata.json` is written by hand: the Spring configuration processor
 * runs through kapt, which does not expose `src/main/resources` to it, so neither the generated file nor
 * an `additional-spring-configuration-metadata.json` ever described a property (the published RC6 jar
 * shipped `"properties": []`). This test is what keeps the hand-written file honest — add a property to
 * [PagBankProperties] without documenting it and the build fails here.
 */
class PagBankConfigurationMetadataTest {
    private val metadata =
        JsonMapper
            .builder()
            .build()
            .readTree(javaClass.getResourceAsStream("/META-INF/spring-configuration-metadata.json")!!)

    private fun documented(): Set<String> = properties().map { it.path("name").asString() }.toSet()

    private fun properties(): List<JsonNode> = metadata.path("properties").values().toList()

    private fun kebab(name: String) = name.replace(Regex("([a-z0-9])([A-Z])"), "$1-$2").lowercase()

    private fun declared(): Set<String> {
        val nested = setOf("webhook")
        val root =
            PagBankProperties::class
                .primaryConstructor!!
                .parameters
                .mapNotNull { it.name }
        return root.filterNot { it in nested }.map { "pagbank.${kebab(it)}" }.toSet() +
            PagBankProperties.Webhook::class
                .primaryConstructor!!
                .parameters
                .mapNotNull { it.name }
                .map { "pagbank.webhook.${kebab(it)}" }
    }

    @Test
    fun `every configuration property is documented for the IDE`() {
        assertThat(documented()).containsExactlyInAnyOrderElementsOf(declared())
    }

    @Test
    fun `every documented property has a type and a description`() {
        properties().forEach { property ->
            assertThat(property.path("type").asString()).isNotBlank()
            assertThat(property.path("description").asString()).isNotBlank()
            assertThat(property.path("sourceType").asString()).startsWith("io.github.rodrigoma.pagbank")
        }
    }

    @Test
    fun `documented defaults match the property defaults`() {
        val defaults = PagBankProperties(token = "T")

        fun defaultOf(name: String) =
            properties()
                .first { it.path("name").asString() == name }
                .path("defaultValue")
                .asString()

        assertThat(defaultOf("pagbank.environment")).isEqualTo(defaults.environment.name.lowercase())
        assertThat(defaultOf("pagbank.connect-timeout")).isEqualTo("${defaults.connectTimeout.toSeconds()}s")
        assertThat(defaultOf("pagbank.read-timeout")).isEqualTo("${defaults.readTimeout.toSeconds()}s")
    }
}
