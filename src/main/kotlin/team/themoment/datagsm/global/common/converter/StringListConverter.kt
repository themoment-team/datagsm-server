package team.themoment.datagsm.global.common.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringListConverter : AttributeConverter<List<String>, String> {
    private val delimiter = ","

    override fun convertToDatabaseColumn(attribute: List<String>): String = attribute.joinToString(delimiter)

    override fun convertToEntityAttribute(dbData: String): List<String> =
        if (dbData.isEmpty()) {
            emptyList()
        } else {
            dbData.split(delimiter)
        }
}
