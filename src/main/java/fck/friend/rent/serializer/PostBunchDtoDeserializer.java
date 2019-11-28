package fck.friend.rent.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import fck.friend.rent.dto.post.PostBunchDto;
import fck.friend.rent.dto.post.PostDto;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PostBunchDtoDeserializer extends StdDeserializer<PostBunchDto> {

    public PostBunchDtoDeserializer() {
        this(null);
    }

    public PostBunchDtoDeserializer(Class<PostDto> t) {
        super(t);
    }

    @Override
    public PostBunchDto deserialize(JsonParser parser, DeserializationContext deserializer) throws IOException {
        ObjectCodec codec = parser.getCodec();
        JsonNode rootNode = codec.readTree(parser);
        JsonNode itemsNode = rootNode.findValue("items");

        List<PostDto> posts = StreamSupport.stream(itemsNode.spliterator(), false)
                                           .map(this::createPostNode)
                                           .collect(Collectors.toList());

        return new PostBunchDto(posts);
    }

    private PostDto createPostNode(JsonNode postNode) {
        PostDto post = new PostDto();
        post.setId(postNode.get("id")
                           .asText());
        post.setOwnerId(postNode.get("owner_id")
                                .asText());
        post.setFromId(postNode.get("from_id")
                               .asText());
        post.setDate(postNode.get("date")
                             .asText());
        post.setText(postNode.get("text")
                             .asText());
        return post;

    }
}