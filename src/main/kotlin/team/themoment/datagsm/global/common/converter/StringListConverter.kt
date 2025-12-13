package team.themoment.datagsm.global.common.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringListConverter : AttributeConverter<List<String>, String> {
    private val objectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<String>): String = objectMapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): List<String> =
        if (dbData.isEmpty()) {
            emptyList()
        } else {
            objectMapper.readValue(dbData, object : TypeReference<List<String>>() {})
        }
}
