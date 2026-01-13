package team.themoment.datagsm.common.global.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

@Converter
class StringSetConverter : AttributeConverter<Set<String>, String> {
    companion object {
        private val objectMapper = ObjectMapper()
    }

    override fun convertToDatabaseColumn(attribute: Set<String>): String = objectMapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String?): Set<String> =
        if (dbData.isNullOrEmpty()) {
            emptySet()
        } else {
            objectMapper.readValue(dbData, object : TypeReference<Set<String>>() {})
        }
}
