package team.themoment.datagsm.global.common.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringSetConverter : AttributeConverter<Set<String>, String> {
    private val objectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: Set<String>): String = objectMapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String?): Set<String> =
        if (dbData.isNullOrEmpty()) {
            emptySet()
        } else {
            objectMapper.readValue(dbData, object : TypeReference<Set<String>>() {})
        }
}
