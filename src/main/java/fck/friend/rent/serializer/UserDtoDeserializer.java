package fck.friend.rent.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import fck.friend.rent.dto.user.UserDto;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UserDtoDeserializer extends StdDeserializer<UserDto> {

    public UserDtoDeserializer() {
        this(null);
    }

    public UserDtoDeserializer(Class<UserDto> t) {
        super(t);
    }

    @Override
    public UserDto deserialize(JsonParser parser, DeserializationContext deserializer) throws IOException {
        ObjectCodec codec = parser.getCodec();
        JsonNode rootNode = codec.readTree(parser);
        JsonNode itemsNode = rootNode.findValue("items");

        List<String> friends = StreamSupport.stream(itemsNode.spliterator(), false)
                                            .map(JsonNode::intValue)
                                            .map(String::valueOf)
                                            .collect(Collectors.toList());
        return new UserDto(friends);
    }
}

