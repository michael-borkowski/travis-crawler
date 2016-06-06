package at.borkowski.traviscrawler.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;

import static java.util.Collections.singletonList;

public class OptionalArrayDeserializer extends JsonDeserializer<List<String>> {
    @SuppressWarnings("unchecked")
    @Override
    public List<String> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.readValueAsTree();
        ObjectCodec objectCodec = jp.getCodec();

        if (node.isTextual()) return singletonList(node.asText());
        else if (node.isArray())
            return (List<String>) objectCodec.treeToValue(node, List.class);
        return null;
    }
}